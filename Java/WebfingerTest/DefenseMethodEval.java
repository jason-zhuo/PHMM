package WebfingerTest;

import Util.MyFileWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Defense method evaluation class
 *
 * We evaluate Dummy packet and MTU probablistic padding
 */
public class DefenseMethodEval {


    //base input path
    String basepath="Y:\\Desktop\\MydevFolderWin7\\testpadding\\L300\\";
    //out put path
    String MTUprobpad_output="Y:\\Desktop\\MydevFolderWin7\\testpadding\\MTUpad\\";
    String Dummy_output="Y:\\Desktop\\MydevFolderWin7\\testpadding\\Dummypacket\\";

    File outfile1;
    File outfile2;
    //probability to insert dummy packet and padding
    double prob=1;
    AmnioAlphabet alpha_transcode = new AmnioAlphabet();
    public DefenseMethodEval(){
        outfile1 =new File(MTUprobpad_output);
        outfile2 =new File(Dummy_output);
        if(!outfile1.exists()  && !outfile1.isDirectory())
        {
            outfile1.mkdir();
        }
        if(!outfile2.exists()  && !outfile2.isDirectory())
        {
            outfile2.mkdir();
        }
    }

    /**
     *
     * @param geneseq String, original gene sequence
     * @return MTU padded gene sequence
     */
    private String MTUprobPad(String geneseq){
        StringBuilder stringBuilder = new StringBuilder();
        int leng = geneseq.length();
        // out going <0  MTU == AG
        // in coming >0 MTU == RR
        for(int i=0; i<leng; i=i+2){
            char a = geneseq.charAt(i);
            char b = geneseq.charAt(i+1);
            String pkt = ""+a+b;
            int pktsize= alpha_transcode.Getsize(pkt);
            if (pktsize >0 && Math.random() < prob){
                stringBuilder.append("RR");
            }else if (pktsize <0 && Math.random() < prob){
                stringBuilder.append("AG");
            }else
                stringBuilder.append(pkt);
        }
        //System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    /**
     *
     * @param geneseq, original geneseq
     * @return Dummy packet inserted gene sequence
     */
    private String DummyPkt(String geneseq){
        StringBuilder stringBuilder = new StringBuilder();
        int leng = geneseq.length();
        for(int i=0; i<leng; i=i+2){
            char a = geneseq.charAt(i);
            char b = geneseq.charAt(i + 1);
            String pkt = ""+a+b;
            if (Math.random() < prob){
                //insert random
                int randompktsize = ThreadLocalRandom.current().nextInt(-1448, 1448 + 1);
                String randomPkt= alpha_transcode.GetSymbol(randompktsize);
                stringBuilder.append(randomPkt);
            }
                stringBuilder.append(pkt);
        }
        //System.out.println(stringBuilder.toString());
        return stringBuilder.toString();

    }

    /**
     * process the query file to simulate defense method
     * @param filename oringal gene sequence file, *.query
     * @param evalmethod defense method string
     */
    private void preprocess(String filename,String evalmethod){
        File file = new File(basepath+filename);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            MyFileWriter fileWriter=null;
            if (evalmethod=="MTUprobpad"){
                fileWriter = new MyFileWriter(MTUprobpad_output+filename);
            }
            if (evalmethod=="DummyPkt"){
                fileWriter = new MyFileWriter(Dummy_output+filename);
            }
            while ((tempString = reader.readLine()) != null) {
                //System.out.println(tempString);
                if (tempString.startsWith(">")){
                    fileWriter.WriteResultToFile(tempString+"\n");
                }else{
                    String modified="";
                    if (evalmethod=="MTUprobpad"){
                        modified= MTUprobPad(tempString);
                    }
                    if (evalmethod=="DummyPkt"){
                        modified=DummyPkt(tempString);
                    }
                    fileWriter.WriteResultToFile(modified+"\n");
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    /**
     * Traverse dir path to find *.query file and process each file
     * @param path *.query file path
     * @param evalmethod defense method string
     */
    private void loadfromDIR(File path,String evalmethod) {
        File[] files;
        if (path.isDirectory()) {
            files = path.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    continue;
                }
                String fileName = f.getName();
                String fileSuffix;
                if (fileName.endsWith(".query")){
                    fileSuffix= fileName.substring(fileName
                            .lastIndexOf("."));
                }else
                    continue;
                if (fileSuffix.equalsIgnoreCase(".query")) {
                    System.out.println(fileName);
                        preprocess(fileName,evalmethod);
                }
            }

        }
    }

    public static void main(String[] args) {
        DefenseMethodEval eva1=new DefenseMethodEval();
        eva1.loadfromDIR(new File(eva1.basepath),"DummyPkt");
        eva1.loadfromDIR(new File(eva1.basepath),"MTUprobpad");
    }
}
