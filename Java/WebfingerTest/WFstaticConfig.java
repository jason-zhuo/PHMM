package WebfingerTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Main configuration file for PHMM sequence generation
 * Created by jason on 2016/11/3.
 */
public class WFstaticConfig {

    public static Map<Integer,ArrayList<Integer>> FilteredSizeDistribution =new HashMap<Integer,ArrayList<Integer>>();
    //define
    //public static boolean isTor= false;
    public static boolean isSSH =false;
    public static boolean isSS =true;

    private static ArrayList<String> MyIP = new ArrayList<String>();

    //define the my source IP to determine the packe direction
    static {
        //public static String myIP = "10.0.2.15";
          String myIP = "146.57.249.110";    // mini 02
          String myIP2 = "146.57.249.109";  ///mini 01
          String myIP3 = "146.57.249.111";  ///mini 03
          String myIP4 = "10.211.55.3";
          String myIP5 = "10.10.10.11";  ///ssh
        MyIP.add(myIP);
       // MyIP.add(myIP2);
        MyIP.add(myIP2);
       // MyIP.add(myIP4);
       // MyIP.add(myIP5);

    }
    public static boolean ismyIPinteral(String testIP){
        for (String i : MyIP){
            if (testIP.equals(i))
                return true;
        }
        return false;
    }

    public static int MTU = 1500;
    public static int CharaterNumber = 2;
    public static int aplabetlen = AmnioAlphabet.alphabet.length();
    public static int StatLimitLength=1500;
    public static int GAP = 10;
    //public static int GAP =(int) Math.floor(MTU * 2.0 / aplabetlen * (1.0));
    public static int outgoing = -1;
    public static int incomming = 1;

    public static int SeqL=300;   // the gene sequence length L
    public static int KFPlength=SeqL;
    public static int GeneLength = SeqL*CharaterNumber; // two character represent one packet

    //chose how to get the sequence, can only use one true below
    public static boolean getSequenceByTimeOrder = true;
    public static boolean getSequenceByFlowOrder = false;

    //use how many instance to train HMM
    public static int Train = 17;
    public static int Test = 8;


    public static int NumberofpacketsPerStream =20;
    public static ArrayList<Integer> FirstMTUindex = new ArrayList<Integer>();
//    public static void main(String[] args) {
//        int directionSize =-46;
//        int halfidx = WFstaticConfig.aplabetlen/2 -1;
//        int myidx  = (directionSize)/WFstaticConfig.GAP;
//
//        char tmp = WFstaticConfig.TransList[myidx+halfidx];
//        System.out.println(tmp);
//    }

    public static void showAvgSize(){
        for (Map.Entry<Integer, ArrayList<Integer>> entry : FilteredSizeDistribution.entrySet()) {
            double sum=0.0;
            for (Integer a :entry.getValue()){
                sum+=a;
            }
            System.out.println("Site = " + entry.getKey()+ " "+ sum/entry.getValue().size());

        }
    }

}
