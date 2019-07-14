import javax.xml.transform.Source;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BrookTagger {
    private static Map<String,List<String>> getPOSsFromThesaurus(List<String> source){
        Map<String,List<String>> thesaurus=new HashMap<String, List<String>>(){};
        String word="";
        for (String line: source) {
            if(line.matches(".*\\|.*")){
                if(line.charAt(0)=='('){
                    List<String> newList=(thesaurus.containsKey(word))?thesaurus.get(word):new ArrayList<>();

                    newList.add(line.replaceFirst("\\((.*)\\).*","$1"));
                    thesaurus.put(word,newList);
                }
                else{
                    word=line.replaceFirst("(.*)\\|.*","$1");
                }
            }
        }
        return thesaurus;
    }
    public static void main(String[] args){
        Map<String, List<String>> pOSsFromThesaurus=new HashMap<String, List<String>>(){};
        try {
            pOSsFromThesaurus = getPOSsFromThesaurus(Files.readAllLines(Paths.get("resources/thesaurus.dat")));

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Map<String,Double> results=new HashMap<>();
        for (String key:pOSsFromThesaurus.keySet()) {
            for(String pOS:pOSsFromThesaurus.get(key)){
                results.put(pOS,(results.containsKey(pOS)?results.get(pOS):0)+1);
            }
        }
        System.out.println(results.toString());
    }
}
