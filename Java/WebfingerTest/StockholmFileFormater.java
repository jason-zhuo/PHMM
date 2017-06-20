package WebfingerTest;

import Util.MyFileWriter;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * format the out put to Stockholm file format
 * Created by jasonzhuo on 2016/11/3.
 */
public class StockholmFileFormater {
    String Stockholmfolder = "Stockholm";
    String filesufix = ".sto";
    String testfilesufix = ".query";
    String header = "# STOCKHOLM 1.0";
    String footer ="//";
    GeneSeqManager manager;
    WFanalyzer ana;
    String filename;


    public StockholmFileFormater(WFanalyzer ana,GeneSeqManager manager){
        this.ana =ana;
        this.manager=manager;
        StringBuilder result = new StringBuilder();
        String currentfilename = ana.getLoader().getFileloader().GetFileInfo();
        if (WFstaticConfig.isTor){
            filename = currentfilename.split("_")[1];
        }else if (WFstaticConfig.isSSH){
            filename = currentfilename;
        }
        else if (WFstaticConfig.isSS){
            int dotindex = currentfilename.indexOf("-");
            filename = currentfilename.substring(0, dotindex);
        }
    }

//    public void writeFiles(){
//        Trainwriter.WriteResultToFile(header+"\n");
//        for (String a :Trainall){
//            Trainwriter.WriteResultToFile(filename+" "+ a+"\n");
//        }
//        Trainwriter.WriteResultToFile(footer);
//
//
//        Testwriter.WriteResultToFile(header+"\n");
//        for (String a :Testall){
//            Testwriter.WriteResultToFile(">"+filename+"\n");
//            Testwriter.WriteResultToFile(a+"\n");
//
//        }
//        Testwriter.WriteResultToFile(footer);
//    }


    public void Multi_tab_tofile(){
        MyFileWriter Trainwriter ;
        //first write main page
        for (int class_id :manager.getSeqDatabase().keySet()){
            Trainwriter= new MyFileWriter(Stockholmfolder + "/" + class_id + filesufix);
            Trainwriter.WriteResultToFile(header+"\n");
            int train_count =0;
            for (String seq : manager.getSeqDatabase().get(class_id)){
                Trainwriter.WriteResultToFile(class_id+"/"+train_count +" "+seq+"\n");
                train_count++;
            }
            Trainwriter.WriteResultToFile("\n");
        }

        //then write subdomain
        for (int class_id : manager.getSubdomainSeqDatabase().keySet()){
            Trainwriter= new MyFileWriter(Stockholmfolder + "/" + class_id + filesufix);
            TreeMap<Integer,ArrayList<String>> thissubdomain = manager.getSubdomainSeqDatabase().get(class_id);
            for (int subdomainID : thissubdomain.keySet()){
                int train_count =0;
                for (String seq : thissubdomain.get(subdomainID)){
                    Trainwriter.WriteResultToFile(class_id+"/" +train_count+ " "+seq+"\n");
                    train_count++;
                }
                Trainwriter.WriteResultToFile("\n");
            }

            Trainwriter.WriteResultToFile(footer);
        }

        manager.getSubdomainSeqDatabase().clear();
        manager.getSeqDatabase().clear();

    }


    public void Test_train_split(){
        MyFileWriter Trainwriter;
        MyFileWriter Testwriter ;
        for (int class_id :manager.getSeqDatabase().keySet()){
            int train_count =0;
            int test_count=0;
            Trainwriter= new MyFileWriter(Stockholmfolder + "/" + class_id + filesufix);
            Testwriter= new MyFileWriter(Stockholmfolder + "/" + class_id + testfilesufix);
            if (WFstaticConfig.Train>0)
            Trainwriter.WriteResultToFile(header+"\n");
            for (String seq : manager.getSeqDatabase().get(class_id)){
                if (train_count<WFstaticConfig.Train && WFstaticConfig.Train>0){
                    Trainwriter.WriteResultToFile(class_id+"/"+train_count +" "+seq+"\n");
                    train_count++;
                }else{
                    Testwriter.WriteResultToFile(">"+class_id+"/"+test_count+"\n");
                    test_count++;
                    Testwriter.WriteResultToFile(seq + "\n");
                }
            }
            //Testwriter.WriteResultToFile(footer);
            if (WFstaticConfig.Train>0)
            Trainwriter.WriteResultToFile(footer);
        }
//        Trainwriter.WriteResultToFile(footer+"\n");
//        Testwriter.WriteResultToFile(footer+"\n");
    }


}
