package WebfingerTest;

import Stream.TCPStream;
import Util.MyFileWriter;
import packets.PCAPPacket;
import packets.TCP;
import sun.net.util.IPAddressUtil;

import java.util.*;

/**
 * Created by jasonzhuo on 2016/8/4.
 */
public class WFanalyzer {


    WFfileLoader loader;
    private boolean isMainpage = false;
    AmnioAlphabet amnioAlphabet = new AmnioAlphabet();
    private ArrayList<String> Geneseq = new ArrayList<String>();
    private ArrayList<String> GoogleSizeArray = new ArrayList<String>();  // for google sites size analysis
    private double culmulateLength = 0.0; // for google sites size analysis
    private ArrayList<TCPStream> FilteredTcpStreams;  // remove retransmission packets and ack with no payload
    private ArrayList<TCPStream> SortedTcpStreams;  // sorted tcpstream
    String NDSS2016path = "NDSS2016";
    String Kfp = "Kfingerprint";
    String packetLenstat = "paketLenstat.txt";

    private Map<TCPStream, ArrayList<PCAPPacket>> incommingArrayMap = new HashMap<TCPStream, ArrayList<PCAPPacket>>();
    private Map<TCPStream, ArrayList<PCAPPacket>> outgoingArrayMap = new HashMap<TCPStream, ArrayList<PCAPPacket>>();
    private Map<TCPStream, ArrayList<Integer>> CompleteMap = new HashMap<TCPStream, ArrayList<Integer>>();
    private ArrayList<PCAPPacket> allpackets;
    private ArrayList<PCAPPacket> Filtered_allpackets = new ArrayList<PCAPPacket>();  // remove retransmission packets and ack with no payload

    //for packet statistics
    public static ArrayList<Integer> packetsizearry_ssh = new ArrayList<Integer>();
    public static ArrayList<Integer> packetsizearry_SS = new ArrayList<Integer>();
    private int numberofOutgoingpackets;
    private int nmberofOutgoingbytes;

    private int numberofincommingpackets;
    private int numberofincomebyte;

    // for most occurred packets limitation
    private int NumberofMTUpackets;
    private int MTU_MAX = WFstaticConfig.GeneLength / 100;
    private int MTU_count = 0;

    private int MAX_MTU_PERSESSION = 1;
    private int MTU_PERSESSION_COUNT = 0;
    // for shadowsocks
    private int MM_MAX = WFstaticConfig.GeneLength / 100;
    private int MM_count = 0;
    //for ssh
    private int SSH_ACK = WFstaticConfig.GeneLength / 100;
    private int SSH_ACK_count = 0;
    private int SSH_2 = WFstaticConfig.GeneLength / 100;
    private int SSH_2_count = 0;
    MyFileWriter sshLenStatfileWriter = new MyFileWriter(packetLenstat);

    public WFfileLoader getLoader() {
        return loader;
    }

    public int getNumberofOutgoingpackets() {
        return numberofOutgoingpackets;
    }

    public int getNmberofOutgoingbytes() {
        return nmberofOutgoingbytes;
    }

    public int getNumberofincommingpackets() {
        return numberofincommingpackets;
    }

    public int getNumberofincomebyte() {
        return numberofincomebyte;
    }
    public int getInstanceNum() {
        String currentfilename = loader.getFileloader().GetFileInfo();
        int class_id=0;
        if (WFstaticConfig.isSS){

            int dotindex = currentfilename.indexOf("-");
            class_id = Integer.parseInt(currentfilename.substring(0, dotindex));
            return class_id;

        }else if (WFstaticConfig.isSSH){
            int dotindex = currentfilename.indexOf(".");
            class_id = Integer.parseInt(currentfilename.substring(0, dotindex));

        }
        return class_id;
    }
    public ArrayList<String> getGoogleseq() {
        return GoogleSizeArray;
    }

    public ArrayList<String> getGeneseq() {
        return Geneseq;
    }


    public int getSubdomaininstanceNumer() {
        return subdomaininstanceNumer;
    }

    public int getSitenumber() {
        return sitenumber;
    }

    int subdomaininstanceNumer, sitenumber;

    public boolean isMainpage() {
        return isMainpage;
    }

    // main logic
    public void doAnalyze() {
        FilterPackets();
        //getHomePageFilteredAveragePackets();
        FilterTCPstream();
        // KFbuildtimeSeries();

        doPacketsStats();  // for stat information
        //for packet distribution stat, not used
        // sshLenStatfileWriter.WriteArrayToFile(packetsizearry_SS);
        //packetsizearry_SS.clear();
        // BuldCumulState();
        // sortStream();
        //showStream2();
        BuildGENseq();
    }

    public void getHomePageFilteredAveragePackets() {
        int classID = getSitenumber();
        if (WFstaticConfig.FilteredSizeDistribution.containsKey(classID)) {
            ArrayList<Integer> sizearry = WFstaticConfig.FilteredSizeDistribution.get(classID);
            sizearry.add(Filtered_allpackets.size());
        } else {
            ArrayList<Integer> sizearry = new ArrayList<Integer>();
            sizearry.add(Filtered_allpackets.size());
            WFstaticConfig.FilteredSizeDistribution.put(classID, sizearry);
        }

    }

    public void doAnalyze(String type) {
        //for ssh
        if (type.equalsIgnoreCase("ssh")) {
            FilterPackets("ssh");
            FilterTCPstream("ssh");
            doPacketsStats("ssh");
            //sshLenStatfileWriter.WriteArrayToFile(packetsizearry_ssh);
            //packetsizearry_ssh.clear();
            //KFbuildtimeSeries("ssh");
            //sortStream();
            //showStream2();
            BuildGENseq("ssh");
        }

    }


    private void PacketDistribution(String ssh) {
        for (TCPStream s : FilteredTcpStreams) {
            ArrayList<PCAPPacket> packets = s.getTCPPackets();
            for (PCAPPacket p : packets) {
                TCP tcp = (TCP) p;
                int payloadLen = tcp.getFrameLength() - 66;

                if (payloadLen == 1448 || payloadLen == 1460) {
                    NumberofMTUpackets++;
                }
                if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
                    numberofOutgoingpackets++;
                    nmberofOutgoingbytes += payloadLen;
                } else {
                    numberofincommingpackets++;
                    numberofincomebyte += payloadLen;
                }
            }
        }

    }

    /**
     * for Multitab analyze
     *
     * @param loader
     * @param sitenumber
     * @param subdomaininstanceNumer
     * @param isMainpage
     */
    public WFanalyzer(WFfileLoader loader, int sitenumber, int subdomaininstanceNumer, boolean isMainpage) {
        this.loader = loader;
        allpackets = loader.getAllPCAPPackets();
        this.sitenumber = sitenumber;
        this.subdomaininstanceNumer = subdomaininstanceNumer;
        this.isMainpage = isMainpage;
        System.out.println(loader.getFileloader().GetFileInfo());
    }

    public WFanalyzer(WFfileLoader loader, int sitenumber) {
        this.loader = loader;
        allpackets = loader.getAllPCAPPackets();
        this.sitenumber = sitenumber;
        System.out.println(loader.getFileloader().GetFileInfo());
    }

    public WFanalyzer(WFfileLoader loader) {
        this.loader = loader;
        allpackets = loader.getAllPCAPPackets();
        System.out.println(loader.getFileloader().GetFileInfo());
    }


    public static boolean internalIp(String ip) {
        byte[] addr = IPAddressUtil.textToNumericFormatV4(ip);
        return internalIp(addr);
    }


    public static boolean internalIp(byte[] addr) {
        final byte b0 = addr[0];
        final byte b1 = addr[1];
        //10.x.x.x/8
        final byte SECTION_1 = 0x0A;
        //172.16.x.x/12
        final byte SECTION_2 = (byte) 0xAC;
        final byte SECTION_3 = (byte) 0x10;
        final byte SECTION_4 = (byte) 0x1F;
        //192.168.x.x/16
        final byte SECTION_5 = (byte) 0xC0;
        final byte SECTION_6 = (byte) 0xA8;
        switch (b0) {
            case SECTION_1:
                return true;
            case SECTION_2:
                if (b1 >= SECTION_3 && b1 <= SECTION_4) {
                    return true;
                }
            case SECTION_5:
                switch (b1) {
                    case SECTION_6:
                        return true;
                }
            default:
                return false;

        }
    }

    /**
     * build the time series fingerprint for K-fingerpting ssh version, incomming p> 0; outgoing p< 0</>
     */
    public void KFbuildtimeSeries(String type) {
        StringBuilder result = new StringBuilder();
        String url, intstance_num;
        String currentfilename = loader.getFileloader().GetFileInfo();
        int dotindex = currentfilename.indexOf("-");
        int dotindex2 = currentfilename.indexOf(".");
        url = currentfilename.substring(0, dotindex);
        intstance_num = currentfilename.substring(dotindex + 1, dotindex2);
        int url_int = Integer.parseInt(url);
        MyFileWriter writer1 = new MyFileWriter(Kfp + "/" + url_int + "_" + intstance_num);

        for (PCAPPacket p : Filtered_allpackets) {
            TCP tcp = (TCP) p;
            int payloadLen = tcp.getFrameLength() - 66;
            if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
                result.append(String.format("%.10f", tcp.getSeconds()));
                result.append(" ");
                result.append(payloadLen * WFstaticConfig.outgoing);
                //result.append(-1);
                result.append("\n");

            } else {
                result.append(String.format("%.10f", tcp.getSeconds()));
                result.append(" ");
                result.append(payloadLen * WFstaticConfig.incomming);
                //result.append(1);
                result.append("\n");
            }

        }
        //System.out.println(result.toString());
        writer1.WriteResultToFile(result.toString());

    }

    /**
     * build the time series fingerprint for K-fingerpting, incomming p> 0; outgoing p< 0</>
     */
    public void KFbuildtimeSeries() {
        StringBuilder result = new StringBuilder();
        String url, intstance_num;
        String currentfilename = loader.getFileloader().GetFileInfo();
        int dotindex = currentfilename.indexOf("-");
        int dotindex2 = currentfilename.indexOf(".");
        url = currentfilename.substring(0, dotindex);
        intstance_num = currentfilename.substring(dotindex + 1, dotindex2);
        int url_int = Integer.parseInt(url);
        MyFileWriter writer1 = new MyFileWriter(Kfp + "/" + url_int + "_" + intstance_num);
        int count = 0;
        for (PCAPPacket p : Filtered_allpackets) {
            if (count == WFstaticConfig.KFPlength) {
                break;
            }
            TCP tcp = (TCP) p;
            if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
                result.append(String.format("%.10f", tcp.getSeconds()));
                result.append(" ");
                //result.append(tcp.getLength()*WFstaticConfig.outgoing);
                result.append(-1);
                result.append("\n");

            } else {
                result.append(String.format("%.10f", tcp.getSeconds()));
                result.append(" ");
                //result.append(tcp.getLength()*WFstaticConfig.incomming);
                result.append(1);
                result.append("\n");
            }
            count++;
        }
        //System.out.println(result.toString());
        writer1.WriteResultToFile(result.toString());

    }


    public void FilterPackets(String ssh) {
        //  System.out.println(allpackets.size());
        for (PCAPPacket p : allpackets) {
            if (p instanceof TCP) {
                TCP tcp = (TCP) p;
                if (tcp.isSYN() || (tcp.isACK() && tcp.getFrameLength() == 66)) {
                    //Filtered_allpackets.add(tcp);
                    continue;
                } else {
                    Filtered_allpackets.add(tcp);
                }
            }
        }
        //   System.out.println(Filtered_allpackets.size());
    }

    public void FilterPackets() {
        for (PCAPPacket p : allpackets) {
            if (p instanceof TCP) {
                TCP tcp = (TCP) p;
                //for packet size stat
//                    if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
//                        packetsizearry_SS.add(tcp.getLength()*WFstaticConfig.outgoing);
//                    } else {
//                        packetsizearry_SS.add(tcp.getLength()*WFstaticConfig.incomming);
//                    }

                if (tcp.isSYN() || tcp.isRetransmission() || (tcp.isACK() && tcp.getLength() == 0)) {
                    //Filtered_allpackets.add(tcp);
                    continue;
                } else {
                    Filtered_allpackets.add(tcp);
                }
            }
        }

    }

    public void FilterTCPstream(String ssh) {
        loader.udpateStreams(Filtered_allpackets);
        ArrayList<TCPStream> streams = loader.getTcpStreams();
        FilteredTcpStreams = new ArrayList<TCPStream>();
        for (TCPStream s : streams) {
            if (s.getTCPPacketsNumber() == 1 || s.getTCPPacketsNumber() == 0 || s.getTotalLength() == 0) {
                continue;
            }
            ArrayList<PCAPPacket> packets = s.getTCPPackets();
            TCPStream newstream = new TCPStream(s.getNum());
            for (PCAPPacket p : packets) {
                TCP tcp = (TCP) p;
                if ((tcp.isACK() && tcp.getFrameLength() == 66)) {
                    //newstream.addTCPPacket(tcp);  for packet stat
                    continue;
                } else {
                    newstream.addTCPPacket(tcp);
                }
            }
            if (newstream.getTCPPacketsNumber() == 1 || newstream.getTCPPacketsNumber() == 0 || newstream.getTotalLength() == 0) {
                continue;
            }
            FilteredTcpStreams.add(newstream);
        }


    }

    public void FilterTCPstream() {

        loader.udpateStreams(Filtered_allpackets);
        ArrayList<TCPStream> streams = loader.getTcpStreams();
        FilteredTcpStreams = new ArrayList<TCPStream>();

        for (TCPStream s : streams) {
            if (s.getTCPPacketsNumber() == 1 || s.getTCPPacketsNumber() == 0 || s.getTotalLength() == 0) {
                continue;
            }
            ArrayList<PCAPPacket> packets = s.getTCPPackets();
            TCPStream newstream = new TCPStream(s.getNum());
            for (PCAPPacket p : packets) {
                TCP tcp = (TCP) p;
                if (tcp.isRetransmission() || (tcp.isACK() && tcp.getLength() == 0)) {
                    continue;
                    //newstream.addTCPPacket(tcp);
                } else {
                    newstream.addTCPPacket(tcp);
                }
            }
            if (newstream.getTCPPacketsNumber() < WFstaticConfig.NumberofpacketsPerStream || newstream.getTotalLength() == 0) {
                continue;
            }
            FilteredTcpStreams.add(newstream);
        }

    }


    // sort the tcpstream
    public void sortStream() {
        Collections.sort(FilteredTcpStreams, new Comparator<TCPStream>() {
            @Override
            public int compare(TCPStream o1, TCPStream o2) {
                return o1.getTotalLength() - o2.getTotalLength();   //asending
                //return o1.getTCPPackets().size() - o2.getTCPPackets().size();
//                double timediff = o1.getTCPPackets().get(0).getSeconds() - o2.getTCPPackets().get(0).getSeconds();
//                if (timediff>0.0){
//                    return -1;
//                }else {
//                    return 1;
//                }
            }
        });
    }


    /**
     *
     * charater transcoding and symbolization
     */
    public String Transcoder(int payloadsize, int direction) {
        int lowerbound = WFstaticConfig.MTU * (-1);
        int upperbound = WFstaticConfig.MTU;
        if ((payloadsize <= lowerbound)) {
            //System.err.println("payloadsize underflow:" + payloadsize);
            return "AA"; // AA == -1500
        } else if (payloadsize >= upperbound) {
            // System.err.println("payloadsize upflow:" + payloadsize);
            return "SA"; //SA == 1500
        }
        // packet time sequence
        if (payloadsize == 1448 || payloadsize == 1460) {
            if (MTU_count < MTU_MAX) {
            } else {
                return "";
            }
            MTU_count++;
        }

//        //for ssh Limits
//        if (payloadsize==56 ){
//            if (SSH_ACK_count< SSH_ACK){
//            }else {
//                return "";
//            }
//            SSH_ACK_count++;
//        }
//
//        //for ssh
//        if (payloadsize==40 || payloadsize==48 || payloadsize==80){
//            if (SSH_2_count< SSH_2){
//            }else {
//                return "";
//            }
//            SSH_2_count++;
//        }
        //for ssh end

        int directionSize = payloadsize * direction;
        String tmp = amnioAlphabet.GetSymbol(directionSize);
        //for shadowsocks Limit
//        if (tmp.equalsIgnoreCase("MM")) {  // size 600
//            //    System.out.println("MM"+directionSize);
//            if (MM_count < MM_MAX) {
//            } else {
//                return "";
//            }
//            MM_count++;
//        }
        // System.out.println(directionSize+ "->" + tmp);
        return tmp;
    }

    /**
     * used for transfer total number of packets to Gene seq
     * minus MTU is to fully use the 4000 symbol in the amnioMap
     *
     * @param payloadsize
     * @return
     */
    public String TranscoderPacket(int payloadsize) {
        payloadsize = payloadsize - WFstaticConfig.MTU;
        int directionSize = payloadsize;
        int lowerbound = WFstaticConfig.MTU * (-1);
        int upperbound = WFstaticConfig.MTU + 1000;    // -1500 ~ 2500 in total 4000 symbols

        if ((payloadsize <= lowerbound)) {
            //System.err.println("payloadsize underflow:" + payloadsize);
            return "AA"; // AA == -1500
        } else if (payloadsize >= upperbound) {
            // System.err.println("payloadsize upflow:" + payloadsize);
            return "YY"; //YY == 2490~2500
        }

        String tmp = amnioAlphabet.GetSymbol(directionSize);
        //System.out.println(directionSize+ "->" + tmp);
        return tmp;
    }

    /**
     * two charater transcoding  for flow stats
     */
    public String Transcoder(int payloadsize) {

        int lowerbound = WFstaticConfig.MTU * (-1);
        int upperbound = WFstaticConfig.MTU;

        if ((payloadsize <= lowerbound)) {
            //System.err.println("payloadsize underflow:" + payloadsize);
            return "AA"; // AA == -1500
        } else if (payloadsize >= upperbound) {
            // System.err.println("payloadsize upflow:" + payloadsize);
            return "SA"; //SA == 1500
        }
        int directionSize = payloadsize;
        String tmp = amnioAlphabet.GetSymbol(directionSize);
        //System.out.println(directionSize+ "->" + tmp);
        return tmp;
    }


    private void doPacketsStats(String ssh) {
        for (TCPStream s : FilteredTcpStreams) {
            ArrayList<PCAPPacket> packets = s.getTCPPackets();
            for (PCAPPacket p : packets) {
                TCP tcp = (TCP) p;
                int payloadLen = tcp.getFrameLength() - 66;
//                    if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
//                        payloadLen= payloadLen*WFstaticConfig.outgoing;
//                    } else {
//                        payloadLen= payloadLen*WFstaticConfig.incomming;
//                    }
//                    packetsizearry_ssh.add(payloadLen);
                if (payloadLen == 1448 || payloadLen == 1460) {
                    NumberofMTUpackets++;
                }
                if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
                    numberofOutgoingpackets++;
                    nmberofOutgoingbytes += payloadLen;
                } else {
                    numberofincommingpackets++;
                    numberofincomebyte += payloadLen;
                }
            }
        }

    }

    private void doPacketsStats() {
        if (WFstaticConfig.isSS ) {
            for (TCPStream s : FilteredTcpStreams) {
                ArrayList<PCAPPacket> packets = s.getTCPPackets();
                for (PCAPPacket p : packets) {
                    TCP tcp = (TCP) p;
                    int payloadLen = tcp.getLength();
                    if (payloadLen == 1448 || payloadLen == 1460) {
                        NumberofMTUpackets++;
                    }
                    if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
                        numberofOutgoingpackets++;
                        nmberofOutgoingbytes += payloadLen;
                    } else {
                        numberofincommingpackets++;
                        numberofincomebyte += payloadLen;
                    }
                }
            }
        } else {
            for (TCPStream s : FilteredTcpStreams) {
                ArrayList<PCAPPacket> packets = s.getTCPPackets();
                for (PCAPPacket p : packets) {
                    TCP tcp = (TCP) p;
                    int payloadLen = tcp.getFrameLength() - 66;
                    if (payloadLen == 1448 || payloadLen == 1460) {
                        NumberofMTUpackets++;
                    }
                    if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
                        numberofOutgoingpackets++;
                        nmberofOutgoingbytes += payloadLen;
                    } else {
                        numberofincommingpackets++;
                        numberofincomebyte += payloadLen;
                    }
                }
            }
        }
    }

    /**
     * for ssh analyze
     *
     * @param type
     */
    private void BuildGENseq(String type) {

        StringBuilder bulder = new StringBuilder();
        int total = numberofincommingpackets + numberofOutgoingpackets;
        int totalKbytes = numberofincomebyte / 1024 + nmberofOutgoingbytes / 1024;
        //System.out.println(bulder.toString() + numberofincommingpackets + " " + numberofincomebyte/1024 + " " + numberofOutgoingpackets + " " + nmberofOutgoingbytes/1024+" " +total+" "+ totalKbytes+" "+NumberofMTUpackets);
        //Geneseq.add(bulder.toString() + numberofincommingpackets + " " + numberofincomebyte/1024 + " " + numberofOutgoingpackets + " " + nmberofOutgoingbytes/1024+" " +total+" "+ totalKbytes+" "+NumberofMTUpackets);
        bulder.append(TranscoderPacket(numberofincommingpackets) + TranscoderPacket(numberofincomebyte / 1024) + TranscoderPacket(numberofOutgoingpackets) + TranscoderPacket(nmberofOutgoingbytes / 1024) + TranscoderPacket(total) + TranscoderPacket(totalKbytes));

        if (WFstaticConfig.getSequenceByFlowOrder) {
            for (TCPStream s : FilteredTcpStreams) {
                MTU_PERSESSION_COUNT = 0;
                ArrayList<PCAPPacket> packets = s.getTCPPackets();
                if (packets.size() > WFstaticConfig.NumberofpacketsPerStream) {
                    for (int k = 0; k < WFstaticConfig.NumberofpacketsPerStream; k++) {
                        TCP tcp = (TCP) packets.get(k);
                        int payloadLen = tcp.getFrameLength() - 66;  // 14 + 20 + 20 + 12(options)
                        //char transcodedGene; // Single charater
                        if (payloadLen == 1448 || payloadLen == 1460) {
                            continue;
                        }
                        String transcodedGene; // Two or more charater
                        if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
                            transcodedGene = Transcoder(payloadLen, WFstaticConfig.outgoing);
                        } else {
                            transcodedGene = Transcoder(payloadLen, WFstaticConfig.incomming);
                        }
                        if (bulder.length() >= WFstaticConfig.GeneLength) {
                            Geneseq.add(bulder.toString());
                            return;
                        } else {
                            bulder.append(transcodedGene);
                            //return Geneseq;
                        }
                    }
                    // bulder.append("|");
                } else {
                    for (PCAPPacket p : packets) {
                        TCP tcp = (TCP) p;
                        int payloadLen = tcp.getFrameLength() - 66;
                        //char transcodedGene; // Single charater

                        String transcodedGene; // Two or more charater
                        if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
                            transcodedGene = Transcoder(payloadLen, WFstaticConfig.outgoing);
                        } else {
                            transcodedGene = Transcoder(payloadLen, WFstaticConfig.incomming);
                        }
                        if (bulder.length() >= WFstaticConfig.GeneLength) {
                            Geneseq.add(bulder.toString());
                            return;
                        } else {
                            bulder.append(transcodedGene);
                            //return Geneseq;
                        }

                    }
                    // bulder.append("|");
                }
            }

        } else if (WFstaticConfig.getSequenceByTimeOrder) {
            for (PCAPPacket p : Filtered_allpackets) {
                TCP tcp = (TCP) p;
                int payloadLen = tcp.getFrameLength() - 66;
                //char transcodedGene; // Single charater
                String transcodedGene; // Two or more charater
                if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
                    transcodedGene = Transcoder(payloadLen, WFstaticConfig.outgoing);
                } else {
                    transcodedGene = Transcoder(payloadLen, WFstaticConfig.incomming);
                }
                if (bulder.length() >= WFstaticConfig.GeneLength) {
                    Geneseq.add(bulder.toString());
                    return;
                } else {
                    //return Geneseq;
                    bulder.append(transcodedGene);
                }
            }

        }

        if (bulder.length() < WFstaticConfig.GeneLength) {
            for (int i = bulder.length(); i < WFstaticConfig.GeneLength; i++)
                bulder.append("-");
        }

        Geneseq.add(bulder.toString());
        //System.out.println(bulder.length());
    }


    //for none ssh
    //main process function
    private void BuildGENseq() {

        StringBuilder bulder = new StringBuilder();
        //added statistic information at the begining of the sequence
//        int total =numberofincommingpackets+numberofOutgoingpackets;
//        int totalKbytes = numberofincomebyte/1024+nmberofOutgoingbytes/1024;
//        //System.out.println(bulder.toString() + numberofincommingpackets + " " + numberofincomebyte/1024 + " " + numberofOutgoingpackets + " " + nmberofOutgoingbytes/1024+" " +total+" "+ totalKbytes+" "+NumberofMTUpackets);
//        //Geneseq.add(bulder.toString() + numberofincommingpackets + " " + numberofincomebyte/1024 + " " + numberofOutgoingpackets + " " + nmberofOutgoingbytes/1024+" " +total+" "+ totalKbytes+" "+NumberofMTUpackets);
//        bulder.append(TranscoderPacket(numberofincommingpackets) + TranscoderPacket(numberofincomebyte / 1024) + TranscoderPacket(numberofOutgoingpackets) + TranscoderPacket(nmberofOutgoingbytes) + TranscoderPacket(total) + TranscoderPacket(totalKbytes));
        // System.out.println(bulder.toString());
        if (WFstaticConfig.getSequenceByFlowOrder) {
            for (TCPStream s : FilteredTcpStreams) {
                MTU_PERSESSION_COUNT = 0;
                ArrayList<PCAPPacket> packets = s.getTCPPackets();
                if (packets.size() > WFstaticConfig.NumberofpacketsPerStream) {
                    for (int k = 0; k < WFstaticConfig.NumberofpacketsPerStream; k++) {
                        TCP tcp = (TCP) packets.get(k);
                        int payloadLen = tcp.getLength();

                        //char transcodedGene; // Single charater
                        String transcodedGene; // Two or more charater
                        if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
                            transcodedGene = Transcoder(payloadLen, WFstaticConfig.outgoing);
                        } else {
                            transcodedGene = Transcoder(payloadLen, WFstaticConfig.incomming);
                        }
                        if (bulder.length() >= WFstaticConfig.GeneLength) {
                            Geneseq.add(bulder.toString());
                            return;

                        } else {
                            bulder.append(transcodedGene);
                            //return Geneseq;
                        }
                    }
                    bulder.append("|");
                } else {
                    for (PCAPPacket p : packets) {
                        TCP tcp = (TCP) p;
                        int payloadLen = tcp.getLength();
                        //char transcodedGene; // Single charater
                        String transcodedGene; // Two or more charater
                        if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
                            transcodedGene = Transcoder(payloadLen, WFstaticConfig.outgoing);
                        } else {
                            transcodedGene = Transcoder(payloadLen, WFstaticConfig.incomming);
                        }
                        if (bulder.length() >= WFstaticConfig.GeneLength) {  // 14 is the packet size stats
                            Geneseq.add(bulder.toString());
                            return;

                        } else {
                            bulder.append(transcodedGene);
                            //return Geneseq;
                        }

                    }
                    // bulder.append("|");  // for evaluation and test
                }
            }


        } else if (WFstaticConfig.getSequenceByTimeOrder) {
            for (PCAPPacket p : Filtered_allpackets) {
                TCP tcp = (TCP) p;
                int payloadLen = tcp.getLength();

                //char transcodedGene; // Single charater
                String transcodedGene; // Two or more charater
                if (WFstaticConfig.ismyIPinteral(tcp.getSource())) {
                    transcodedGene = Transcoder(payloadLen, WFstaticConfig.outgoing);
                } else {
                    transcodedGene = Transcoder(payloadLen, WFstaticConfig.incomming);
                }
                if (bulder.length() >= WFstaticConfig.GeneLength) {
                    break;
                } else {
                    //return Geneseq;
                    bulder.append(transcodedGene);
                }

            }

        }


        if (bulder.length() < WFstaticConfig.GeneLength) {
            for (int i = bulder.length(); i < WFstaticConfig.GeneLength; i++)
                bulder.append("-");
        }
        Geneseq.add(bulder.toString());

        //System.out.println(bulder.length());
    }


}
