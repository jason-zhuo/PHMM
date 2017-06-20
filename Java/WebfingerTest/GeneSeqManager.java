package WebfingerTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by jasonzhuo on 2016/11/3.
 */
public class GeneSeqManager {


    public Map<Integer, ArrayList<String>> getSeqDatabase() {
        return SeqDatabase;
    }
    public Map<Integer, TreeMap<Integer,ArrayList<String>>> getSubdomainSeqDatabase() {
        return subdomainSeqDatabase;
    }



    private Map<Integer,ArrayList<String>> SeqDatabase = new HashMap<Integer,ArrayList<String>>();
    // first int is site number, second integer is subdomain number
    private Map<Integer,TreeMap<Integer,ArrayList<String>>> subdomainSeqDatabase = new HashMap<Integer,TreeMap<Integer,ArrayList<String>>>();


    public void showDataBase_multi(){
        showDataBase();
        for (Map.Entry<Integer, TreeMap<Integer,ArrayList<String>>> Subdomainentry : subdomainSeqDatabase.entrySet()) {
            System.out.println("Subdomains of  " + Subdomainentry.getKey());
            for (Map.Entry<Integer,ArrayList<String>> instanceentry : Subdomainentry.getValue().entrySet()) {
                System.out.println("Subdomain = " + instanceentry.getKey());
                for (String a :instanceentry.getValue()){
                    System.out.println(a);
                }
            }

        }
    }
    public void showDataBase(){
        for (Map.Entry<Integer, ArrayList<String>> entry : SeqDatabase.entrySet()) {
            System.out.println("Site = " + entry.getKey() );
            for (String a :entry.getValue()){
                System.out.println(a);
            }
        }
    }
}
