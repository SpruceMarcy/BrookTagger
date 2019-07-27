import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BrookTagger {
    public static void main(String[] args) throws IOException {
        BrookTagger brookTagger = new BrookTagger();

        if(args.length>0) {
            String sentence = args[0];
            brookTagger.modelCoefficient=Double.parseDouble(args[0]);
        }
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println(String.join(" ", brookTagger.tag(in.nextLine())));
        }

    }
    private Map<String,ProbabilitySet> wordProbabilities;
    private Double modelCoefficient=0.0035;
    public BrookTagger(){
        wordProbabilities = getProbabilitySets();
    }
    public String[] tag(String sentence) throws IOException {
        String[] words=sentence.split(" ");
        ProbabilitySet[] probabilitySets = new ProbabilitySet[words.length];
        for(int i=0;i<words.length;i++){
            String word=validate(words[i]);//,wordProbabilities.keySet());
            probabilitySets[i]=approximate(wordProbabilities.containsKey(word)?wordProbabilities.get(word):ProbabilitySet.unit(),word);
        }
        probabilitySets=applyModels(probabilitySets,getResource("models.txt"));
        String[] tags=new String[words.length];
        for(int i=0;i<words.length;i++) {
            //System.out.println(words[i]+(probabilitySets[i]));
            tags[i] = probabilitySets[i].getHighest()+"\\"+words[i];
        }
        return tags;
    }

    private ProbabilitySet approximate(ProbabilitySet base, String word){
        String suffix;
        switch (word.length()){
            default:;
            case 6:suffix=word.substring(word.length()-4);
                if(suffix.equals("ance")){base.add("noun",0.2);}
                if(suffix.equals("ence")){base.add("noun",0.2);}
                if(suffix.equals("ment")){base.add("noun",0.2);}
                if(suffix.equals("ness")){base.add("noun",0.2);}
                if(suffix.equals("ship")){base.add("noun",0.2);}
                if(suffix.equals("able")){base.add("adjective",0.2);}
                if(suffix.equals("ible")){base.add("adjective",0.2);}
                if(suffix.equals("less")){base.add("adjective",0.2);}
            case 5:suffix=word.substring(word.length()-3);
                if(suffix.equals("ion")){base.add("noun",0.15);}
                if(suffix.equals("ity")){base.add("noun",0.12);}
                if(suffix.equals("ate")){base.add("verb",0.15);}
                if(suffix.equals("ify")){base.add("verb",0.15);}
                if(suffix.equals("ise")){base.add("verb",0.15);}
                if(suffix.equals("ize")){base.add("verb",0.15);}
                if(suffix.equals("ing")){base.add("verb",0.5);
                                         base.add("adjective",0.1);}
                if(suffix.equals("ive")){base.add("adjective",0.15);}
                if(suffix.equals("ant")){base.add("adjective",0.15);}
                if(suffix.equals("ent")){base.add("adjective",0.15);}
                if(suffix.equals("ful")){base.add("adjective",0.15);}
                if(suffix.equals("ous")){base.add("adjective",0.15);}
            case 4:suffix=word.substring(word.length()-2);
                if(suffix.equals("er")){base.add("noun",0.1);
                                        base.add("adjective",0.1);}
                //if(suffix.equals("or")){base.add("noun",0.1);}
                if(suffix.equals("en")){base.add("verb",0.1);
                                        base.add("noun",0.1);}
                if(suffix.equals("al")){base.add("adjective",0.15);}
                if(suffix.equals("ed")){base.add("verb",0.4);
                                        base.add("adjective",0.1);}
                if(suffix.equals("ic")){base.add("adjective",0.2);}
                if(suffix.equals("ly")){base.add("adverb",0.3);}
            case 3: break;
            case 2: break;
            case 1: break;
            case 0: break;
        }
        base.normalize();
        return base;

    }

    private Map<String,List<String>> getPOSsFromThesaurus(String[][] sources) {
        Map<String, List<String>> thesaurus = new HashMap<String, List<String>>(){};
        String word = "";
        for (String[] source : sources){
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
    private String[] getResource(String resourceName) throws IOException{
        Object[] rawThesaurusdat=new BufferedReader(new InputStreamReader(this.getClass().getResource(resourceName).openStream(), StandardCharsets.UTF_8)).lines().toArray();
        return Arrays.copyOf(rawThesaurusdat, rawThesaurusdat.length, String[].class);
    }
    private Map<String,ProbabilitySet> getProbabilitySets(){
        Map<String, List<String>> pOSsFromThesaurus=new HashMap<String, List<String>>(){};
        try {
            String[] thesaurusdat = getResource("thesaurus.dat");
            String[] thesaurusext = getResource("thesaurus_ext.txt");
            String[][] sources=new String[][]{thesaurusdat, thesaurusext};
            pOSsFromThesaurus = getPOSsFromThesaurus(sources);
        } catch (Exception e) {
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
            double mOEFactor=(1.0d/3.0d);
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
    private ProbabilitySet[] applyModels(ProbabilitySet[] primarySet,String[] models){
        ProbabilitySet[] secondarySet=primarySet.clone();
        //System.out.println("Testing Sentence: "+primarySet.toString());
        for(int sentenceIndex=0;sentenceIndex<primarySet.length;sentenceIndex++){
            //System.out.println(" For set: "+sentenceIndex+" - "+primarySet[sentenceIndex]);
            for(String modelString:models){
                List<String> model=new ArrayList<String>(Arrays.asList(modelString.split(" ")));
                double modelFactor=Double.parseDouble(model.remove(0));
                for(int modelWordStartIndex=Math.max(0,sentenceIndex-model.size()+1);modelWordStartIndex<Math.min(sentenceIndex+1,primarySet.length-model.size()+1);modelWordStartIndex++){
                    double modelProbability=1;
                    for(int modelWordIndex=0;modelWordIndex<model.size();modelWordIndex++){
                        modelProbability*=primarySet[modelWordStartIndex+modelWordIndex].get(model.get(modelWordIndex));
                        //System.out.println("  Comparing: model["+modelWordIndex+"] with word "+(modelWordStartIndex+modelWordIndex));
                    }
                    modelProbability=modelProbability*modelCoefficient/models.length;
                    secondarySet[sentenceIndex].add(model.get(sentenceIndex-modelWordStartIndex),modelProbability);
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
        void add(String property,double value){
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
            }
        }
        double get(String property) {
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
                    return 0;
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
