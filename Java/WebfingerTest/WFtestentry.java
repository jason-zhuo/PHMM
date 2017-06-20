package WebfingerTest;

import java.io.File;
import java.util.*;

/**
 * Program entry function
 * Created by jasonzhuo on 2016/8/4.
 */

public class WFtestentry {

    GeneSeqManager SeqManaer = new GeneSeqManager();
    StockholmFileFormater formater;

    public List<File> getPcapList() {
        return pcapList;
    }

    private List<File> pcapList = new ArrayList<>();


    /**
     * save multitab sequence file to correct format and filename
     * @param ana WFanalyzer
     */
    private void saveResultM_Multitab(WFanalyzer ana){
        if (ana.isMainpage()){
            saveResult(ana);
            return;
        }else {
            Map<Integer,TreeMap<Integer,ArrayList<String>>> subdomainSeqDatabase = SeqManaer.getSubdomainSeqDatabase();
            int class_id = ana.getSitenumber();
            int subdomain_id = ana.getSubdomaininstanceNumer();
            if(subdomainSeqDatabase.get(class_id)!=null ){
                TreeMap<Integer,ArrayList<String>> thissubdomain = subdomainSeqDatabase.get(class_id);
                if (thissubdomain.get(subdomain_id)!=null){
                    ArrayList<String> subdomainGene = thissubdomain.get(subdomain_id);
                    for (String g: ana.getGeneseq()){
                        subdomainGene.add(g);
                    }
                }else{

                    thissubdomain.put(subdomain_id,ana.getGeneseq());
                   // subdomainSeqDatabase.put(class_id,newsubdomain);
                }
               // thissubdomain.put()
            }else {
                TreeMap<Integer,ArrayList<String>> newsubdomain = new TreeMap<Integer,ArrayList<String>>();
                newsubdomain.put(subdomain_id,ana.getGeneseq());
                subdomainSeqDatabase.put(class_id,newsubdomain);
            }

        }

    }

    private void saveResult(WFanalyzer ana){
        Map<Integer,ArrayList<String>> SeqDatabase = SeqManaer.getSeqDatabase();
        int class_id = ana.getInstanceNum();
        if (SeqDatabase.get(class_id)!=null){
            ArrayList<String> GenSeqences = SeqDatabase.get(class_id);
            for (String g: ana.getGeneseq()){
                GenSeqences.add(g);
            }
        }else{
            SeqDatabase.put(class_id,ana.getGeneseq());
        }
    }
    /*
        load pcap file form one-level folder
     */
    private void loadfromDIR(File path) {
        File[] files;
        if (path.isDirectory()) {
            files = path.listFiles();

            for (File f : files) {
                if (f.isDirectory()) {
                    continue;
                }
                String fileName = f.getName();
                String fileSuffix = fileName.substring(fileName
                        .lastIndexOf("."));
                if (fileSuffix.equalsIgnoreCase(".pcap")) {
                    WFfileLoader loader = new WFfileLoader(f);
                    //System.out.println(fileName + " " + loader.getTCPstreamCount());
                    WFanalyzer ana = new WFanalyzer(loader);
                    ana.doAnalyze();
                    saveResult(ana);
                    //ana.showgoogleSeq();
                    formater = new StockholmFileFormater(ana,SeqManaer);
                }
            }
            SeqManaer.showDataBase();
            formater.Test_train_split();
        }
    }

    // PHMM multitab
    private void loadfromDIR_Multi(File path, boolean fortest) {
        File[] files;
        if (path.isDirectory()& path.listFiles().length >0) {
            files = path.listFiles();

            for (File f : files) {
                boolean isMainpage=false;
                int siteNumber=0, subdomainInstanceNumber=0;
                if (f.isDirectory()|| f.getName().equalsIgnoreCase(".DS_Store")) {
                    continue;
                }
                String fileName = f.getName();
                String fileSuffix = fileName.substring(fileName
                        .lastIndexOf("."));

                //check if it is main site
                String[] tmp= fileName.split("-");
                int n = tmp.length;
                if (n==3) {  // sub pages
                    siteNumber =Integer.parseInt(tmp[0]);
                    subdomainInstanceNumber = Integer.parseInt(tmp[1]);
                }else if (n==2){  // main page
                    isMainpage=true;
                    siteNumber =Integer.parseInt(tmp[0]);
                }
                if (fileSuffix.equalsIgnoreCase(".pcap")) {
                    WFfileLoader loader = new WFfileLoader(f);
                    //System.out.println(fileName + " " + loader.getTCPstreamCount());
                    WFanalyzer ana = new WFanalyzer(loader,siteNumber,subdomainInstanceNumber,isMainpage);
                    ana.doAnalyze();
                    saveResultM_Multitab(ana);
                    //ana.showgoogleSeq();
                    formater = new StockholmFileFormater(ana,SeqManaer);
                }
            }
            //SeqManaer.showDataBase_multi();
            if (!fortest){
                formater.Multi_tab_tofile();
            }else {
                WFstaticConfig.Train=0;
                formater.Test_train_split();
            }

        }
    }
//    public static List<File> getFileList(String strPath) {
//        File dir = new File(strPath);
//        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
//        if (files != null) {
//            for (int i = 0; i < files.length; i++) {
//                String fileName = files[i].getName();
//                if (files[i].isDirectory()) { // 判断是文件还是文件夹
//                    getFileList(files[i].getAbsolutePath()); // 获取文件绝对路径
//                } else if (fileName.endsWith("avi")) { // 判断文件名是否以.avi结尾
//                    String strFileName = files[i].getAbsolutePath();
//                    System.out.println("---" + strFileName);
//                    filelist.add(files[i]);
//                } else {
//                    continue;
//                }
//            }
//
//        }
//        return filelist;
//    }

    /**
     * Tranverse the DIR to find Pcap for analyze, multi-level folder
     * @param strPath
     * @return void
     */
    private void tranversPcapFinder(String strPath){
        File path = new File(strPath);
        File[] currentFolderFILES = path.listFiles();
        if (currentFolderFILES!=null){
           for (int j =0; j< currentFolderFILES.length;j++){
               String filename  = currentFolderFILES[j].getName();
               if (currentFolderFILES[j].isDirectory()){
                   tranversPcapFinder(currentFolderFILES[j].getAbsolutePath());
               }else if (filename.endsWith("pcap")){
                   String targetfiles = currentFolderFILES[j].getAbsolutePath();
                   pcapList.add(new File(targetfiles));
               }else
                   continue;
           }
        }
    }


    /**
     * analyze dir by dir
     * @param strPath
     */
    private void startAnalyzeDirbyDir(String strPath,boolean fortest){
        File path = new File(strPath);
        File[] currentFolderFILES = path.listFiles();
        if (currentFolderFILES!=null){
            for (int j =0; j< currentFolderFILES.length;j++){
                if (currentFolderFILES[j].isDirectory()){
                    tranversPcapFinder(currentFolderFILES[j].getAbsolutePath());
                    //System.out.println("building model for " + currentFolderFILES[j]);
                    loadfromDIR_Multi(currentFolderFILES[j],fortest);
                }
            }
        }
    }

    private void startAnalyze(String type){
        double totoalpcaps = pcapList.size();
        double current=0;
        WFanalyzer ana =null;
        for (File pcap :pcapList){
            current++;
            if (current%1000==0){
                System.out.println("progress: "+ current/totoalpcaps*100+"%");
            }
            WFfileLoader loader = new WFfileLoader(pcap);
            String fileName = loader.getFileloader().GetFileInfo();
//            fileName = fileName.substring(0,fileName
//                    .lastIndexOf("."));


            if (loader.isFileisEmpty()==true){
                System.err.println(loader.getFileloader().GetFileInfo()+ " contains 0 packets! skipped");
                continue;
            }
            //System.out.println("analyzing " + loader.getFileloader().GetFilefullPath());
            //System.out.println("analyzing " + pcap.getName() + " " + loader.getTCPstreamCount());
            if (type.equalsIgnoreCase("tor")){
                String[] tmp= fileName.split("_");
                ana = new WFanalyzer(loader,Integer.parseInt(tmp[1]));
            }else if (type.equalsIgnoreCase("ssh")){
                ana = new WFanalyzer(loader);
            }
            ana.doAnalyze(type);
            saveResult(ana);
            formater = new StockholmFileFormater(ana,SeqManaer);
        }
        SeqManaer.showDataBase();
        formater.Test_train_split();
    }


    public static void main(String[] args) {
        WFtestentry test1 = new WFtestentry();
        //for single webpage
        //File pcapfolderpath = new File("Y:\\Desktop\\MydevFolderWin7\\2-Alexa74-25");
        //test1.loadfromDIR(pcapfolderpath);
       // test1.tranversPcapFinder("Y:\\Desktop\\MydevFolderWin7\\1-SSH66\\");
       // test1.startAnalyze("ssh");


        // below is for multi_tab
        //for training
        //WFstaticConfig.GeneLength=300*2;
        test1.startAnalyzeDirbyDir("Y:\\Desktop\\MydevFolderWin7\\", false);
       // test1.startAnalyzeDirbyDir("Y:\\Desktop\\MydevFolderWin7\\",false);

        //for testing

       // WFstaticConfig.GeneLength=300*2;
         test1.startAnalyzeDirbyDir("Y:\\Desktop\\MydevFolderWin7\\",true);
       // test1.startAnalyzeDirbyDir("Y:\\Desktop\\MydevFolderWin7\\selectedTrainHomepage",true);
       // WFstaticConfig.showAvgSize();
//        for (File a :test1.getPcapList()){
//            System.out.println(a.getName());
//        }

    }

}
