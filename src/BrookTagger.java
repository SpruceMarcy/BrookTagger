import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BrookTagger {
    public static void main(String[] args) throws IOException {
        BrookTagger brookTagger = new BrookTagger();

        if(args.length>0){
            String sentence=args[0];
            System.out.println("Running "+sentence);
            System.out.print(String.join(" ",brookTagger.tag(sentence)));
        }else {
            Scanner in = new Scanner(System.in);
            while (true) {
                System.out.println(String.join(" ", brookTagger.tag(in.nextLine())));
            }
        }
    }
    private Map<String,ProbabilitySet> wordProbabilities;
    public BrookTagger(){
        wordProbabilities = getProbabilitySets();
    }
    public String[] tag(String sentence) throws IOException {
        String[] words=sentence.split(" ");
        ProbabilitySet[] probabilitySets = new ProbabilitySet[words.length];
        for(int i=0;i<words.length;i++){
            String word=validate(words[i]);//,wordProbabilities.keySet());
            probabilitySets[i]=wordProbabilities.containsKey(word)?wordProbabilities.get(word):ProbabilitySet.unit();
        }
        probabilitySets=applyModels(probabilitySets,Files.readAllLines(Paths.get("resources/models.txt")));
        String[] tags=new String[words.length];
        for(int i=0;i<words.length;i++) {
            System.out.println((probabilitySets[i]));
            tags[i] = probabilitySets[i].getHighest()+"\\"+words[i];
        }
        return tags;
    }
    private Map<String,List<String>> getPOSsFromThesaurus(List<String>[] sources) {
        Map<String, List<String>> thesaurus = new HashMap<String, List<String>>(){};
        String word = "";
        for (List<String> source : sources){
            for (String line : source) {
                if (line.matches(".*\\|.*")) {
                    if (line.charAt(0) == '(') {
                        List<String> newList = (thesaurus.containsKey(word)) ? thesaurus.get(word) : new ArrayList<>();
                        newList.add(line.replaceFirst("\\((.*)\\).*", "$1"));
                        thesaurus.put(word, newList);
                    } else {
                        word = line.replaceFirst("(.*)\\|.*", "$1");
                    }
                }
            }
        }
        return thesaurus;
    }
    private String validate(String valStr){
        valStr=valStr.toLowerCase().replaceAll("[^a-z0-9]","");
        //System.out.println(valStr);
        return valStr;
    }
    private Map<String,ProbabilitySet> getProbabilitySets(){
        Map<String, List<String>> pOSsFromThesaurus=new HashMap<String, List<String>>(){};
        try {
            List<String>[] sources=new List[]{Files.readAllLines(Paths.get("resources/thesaurus.dat")), Files.readAllLines(Paths.get("resources/thesaurus_ext.txt"))};
            pOSsFromThesaurus = getPOSsFromThesaurus((List[])sources);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Map<String,ProbabilitySet> results=new HashMap<>();
        for (String key:pOSsFromThesaurus.keySet()) {
            results.put(key,new ProbabilitySet(0,0,0,0,0,0,0,0));
            for(String pOS:pOSsFromThesaurus.get(key)) {
                switch (pOS) {
                    case "noun":
                        results.get(key).noun += 1;
                        break;
                    case "verb":
                        results.get(key).verb += 1;
                        break;
                    case "adj":
                        results.get(key).adje += 1;
                        break;
                    case "adv":
                        results.get(key).adve += 1;
                        break;
                    case "pre":
                        results.get(key).prep += 1;
                        break;
                    case "pro":
                        results.get(key).pron += 1;
                        break;
                    case "con":
                        results.get(key).conj += 1;
                        break;
                    case "det":
                        results.get(key).dete += 1;
                    default:
                        break;
                }
            }
            results.get(key).normalize();
            double mOEFactor=(1.0d/12.0d);
            results.get(key).noun+=mOEFactor;
            results.get(key).verb+=mOEFactor;
            results.get(key).adve+=mOEFactor;
            results.get(key).adje+=mOEFactor;
            results.get(key).pron+=mOEFactor;
            results.get(key).prep+=mOEFactor;
            results.get(key).conj+=mOEFactor;
            results.get(key).dete+=mOEFactor;
            results.get(key).normalize();
        }
        return results;
    }
    private ProbabilitySet[] applyModels(ProbabilitySet[] primarySet,List<String> models){
        ProbabilitySet[] secondarySet=primarySet.clone();
        System.out.println("Testing Sentence: "+primarySet.toString());
        for(int sentenceIndex=0;sentenceIndex<primarySet.length;sentenceIndex++){
            System.out.println(" For set: "+sentenceIndex+" - "+primarySet[sentenceIndex]);
            for(String modelString:models){
                String[] model=modelString.split(" ");
                for(int modelWordStartIndex=Math.max(0,sentenceIndex-model.length+1);modelWordStartIndex<Math.min(sentenceIndex+1,primarySet.length-model.length+1);modelWordStartIndex++){
                    double modelProbability=1;
                    for(int modelWordIndex=0;modelWordIndex<model.length;modelWordIndex++){
                        try {
                            modelProbability*=primarySet[modelWordStartIndex+modelWordIndex].get(model[modelWordIndex]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println("  Comparing: model["+modelWordIndex+"] with word "+(modelWordStartIndex+modelWordIndex));
                    }
                    modelProbability=Math.pow(modelProbability,1/model.length);
                    try {
                        secondarySet[sentenceIndex].add(model[sentenceIndex-modelWordStartIndex],modelProbability);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            secondarySet[sentenceIndex].normalize();
        }
        return secondarySet;
    }


    static class ProbabilitySet{
        double noun;
        double verb;
        double adje;
        double adve;
        double prep;
        double pron;
        double conj;
        double dete;
        ProbabilitySet(double noun,double verb, double adje, double adve,double prep, double pron, double conj,double dete){
            this.noun=noun;
            this.verb=verb;
            this.adje=adje;
            this.adve=adve;
            this.prep=prep;
            this.pron=pron;
            this.conj=conj;
            this.dete=dete;
        }
        String getHighest(){
            List<Double> data=new ArrayList<Double>(Arrays.asList(noun,verb,adje,adve,prep,pron,conj,dete));
            return new String[]{"Noun","Verb","Adjective","Adverb","Preposition","Pronoun","Conjunction","Determiner"}[data.indexOf(Collections.max(data))];
        }
        void add(String property,double value) throws Exception {
            property=property.substring(0,4).toLowerCase();
            switch (property){
                case "noun":noun+=value;break;
                case "verb":verb+=value;break;
                case "adje":adje+=value;break;
                case "adve":adve+=value;break;
                case "prep":prep+=value;break;
                case "pron":pron+=value;break;
                case "conj":conj+=value;break;
                case "dete":dete+=value;break;
                default:
                    throw new Exception("invalid property"+property);
            }
        }
        double get(String property) throws Exception {
            property=property.substring(0,4).toLowerCase();
            switch (property){
                case "noun":return noun;
                case "verb":return verb;
                case "adje":return adje;
                case "adve":return adve;
                case "prep":return prep;
                case "pron":return pron;
                case "conj":return conj;
                case "dete":return dete;
                default:
                    throw new Exception("invalid property"+property);
            }
        }
        void normalize(){
            double total=noun+verb+adve+adje+prep+pron+conj+dete;
            noun/=total;
            verb/=total;
            adje/=total;
            adve/=total;
            prep/=total;
            pron/=total;
            conj/=total;
            dete/=total;
        }
        public String toString(){
            String string="";
            string+="noun:"+noun;
            string+=" verb:"+verb;
            string+=" adje:"+adje;
            string+=" adve:"+adve;
            string+=" prep:"+prep;
            string+=" pron:"+pron;
            string+=" conj:"+conj;
            string+=" dete:"+dete;
            return string;
        }
        static ProbabilitySet unit(){
            return new ProbabilitySet(1d/8d,1d/8d,1d/8d,1d/8d,1d/8d,1d/8d,1d/8d,1d/8d);
        }
    }
}
