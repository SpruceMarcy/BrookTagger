import javax.xml.transform.Source;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BrookTagger {
    private static Map<String,List<String>> getPOSsFromThesaurus(List<String>[] sources) {
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
    private static Map<String,ProbabilitySet> getProbabilitySets(){
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
    public static String[] tag(String sentence){
        String[] words=sentence.split(" ");
        String[] tags=new String[words.length];
        Map<String,ProbabilitySet> wordProbabilities = getProbabilitySets();
        for(int i=0;i<words.length;i++){
            ProbabilitySet wordProbability=wordProbabilities.containsKey(words[i])?wordProbabilities.get(words[i]):ProbabilitySet.unit();
            tags[i]=wordProbability.getHighest();
        }
        return tags;
    }
    public static void main(String[] args){
        String sentence="John likes the blue house at the end of the street";
        for(String tagstr:tag(sentence)) {
            System.out.println(tagstr);
        }
    }
    public static class ProbabilitySet{
        public double noun;
        public double verb;
        public double adje;
        public double adve;
        public double prep;
        public double pron;
        public double conj;
        public double dete;
        public ProbabilitySet(double noun,double verb, double adje, double adve,double prep, double pron, double conj,double dete){
            this.noun=noun;
            this.verb=verb;
            this.adje=adje;
            this.adve=adve;
            this.prep=prep;
            this.pron=pron;
            this.conj=conj;
            this.dete=dete;
        }
        public String getHighest(){
            List<Double> data=new ArrayList<Double>(Arrays.asList(noun,verb,adje,adve,prep,pron,conj,dete));
            return new String[]{"noun","verb","adjective","adverb","preposition","pronoun","conjunction","determiner"}[data.indexOf(Collections.max(data))];
        }
        public void normalize(){
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
        public static ProbabilitySet unit(){
            return new ProbabilitySet(1d/8d,1d/8d,1d/8d,1d/8d,1d/8d,1d/8d,1d/8d,1d/8d);
        }
    }
}
