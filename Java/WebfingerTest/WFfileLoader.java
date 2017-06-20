package WebfingerTest;

import PcapAnalyzer.LoadPCAP;
import Stream.TCPStream;
import packets.ARP;
import packets.IP;
import packets.PCAPPacket;
import packets.TCP;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by jasonzhuo on 2016/8/4.
 */
public class WFfileLoader {
    private boolean fileisEmpty =false;
    private ArrayList<TCPStream> tcpStreams;
    private LoadPCAP fileloader;
    private ArrayList<PCAPPacket> allPCAPPackets;
    private double TimeRange = 0.0;


    public boolean isFileisEmpty(){
        return fileisEmpty;
    }
    public int getTCPstreamCount() {
        return tcpStreams.size();
    }

    public ArrayList<TCPStream> getTcpStreams() {
        return tcpStreams;
    }

    public LoadPCAP getFileloader() {
        return fileloader;
    }

    public ArrayList<PCAPPacket> getAllPCAPPackets() {
        return allPCAPPackets;
    }

    public WFfileLoader(File f) {
        tcpStreams = new ArrayList<TCPStream>();
        fileloader = new LoadPCAP(f);
        fileloader.Load();
        initaliseAnalyzer(fileloader);
    }


    public void initaliseAnalyzer(LoadPCAP loadedPcap) {
        allPCAPPackets = loadedPcap.getAllpacket();
        if (allPCAPPackets.size()==0){
            fileisEmpty=true;
            return;
        }
        PCAPPacket last = allPCAPPackets.get(allPCAPPackets.size() - 1);
        PCAPPacket first = allPCAPPackets.get(0);
        TimeRange = last.getSeconds() - first.getSeconds();
        createStreams();

    }

    public void udpateStreams(ArrayList<PCAPPacket> newPCAPPackets){
        tcpStreams.clear();
        for (PCAPPacket p : newPCAPPackets) {
            if (p instanceof TCP && !isIpv6(p) && !isBoardCast(p)) {
                TCP tcp = (TCP) p;
                boolean added = false;
                for (int i = tcpStreams.size() - 1; i >= 0; i--) {
                    TCPStream tcpStream = tcpStreams.get(i);
                    if (tcpStream.isInStream(tcp)) {
                        tcpStream.addTCPPacket(tcp);
                        tcp.setParentStream(tcpStream);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    TCPStream tcpStream = new TCPStream(tcpStreams.size() + 1);
                    tcpStream.addTCPPacket(tcp);
                    tcp.setParentStream(tcpStream);
                    tcpStreams.add(tcpStream);
                }
            }
        }

        for (TCPStream tcpStream : tcpStreams) {
            ArrayList<PCAPPacket> packets = new ArrayList<PCAPPacket>();
            packets.addAll(tcpStream.getTCPPackets());
            tcpStream.setReassembledPackets(TCPStream
                    .reassebleTCPStream(packets));
        }

    }
    public void createStreams() {
        tcpStreams.clear();
        for (PCAPPacket p : allPCAPPackets) {
            if (p instanceof TCP && !isIpv6(p) && !isBoardCast(p)) {
                TCP tcp = (TCP) p;
                boolean added = false;
                for (int i = tcpStreams.size() - 1; i >= 0; i--) {
                    TCPStream tcpStream = tcpStreams.get(i);
                    if (tcpStream.isInStream(tcp)) {
                        tcpStream.addTCPPacket(tcp);
                        tcp.setParentStream(tcpStream);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    TCPStream tcpStream = new TCPStream(tcpStreams.size() + 1);
                    tcpStream.addTCPPacket(tcp);
                    tcp.setParentStream(tcpStream);
                    tcpStreams.add(tcpStream);
                }
            }
        }

        for (TCPStream tcpStream : tcpStreams) {
            ArrayList<PCAPPacket> packets = new ArrayList<PCAPPacket>();
            packets.addAll(tcpStream.getTCPPackets());
            tcpStream.setReassembledPackets(TCPStream
                    .reassebleTCPStream(packets));
        }

    }

    public boolean isIpv6(PCAPPacket p) {
        TCP u = (TCP) p;
        if (u.getDestination().contains(":") || u.getSource().contains(":")) {
            return true;
        } else
            return false;
    }

    public boolean isBoardCast(PCAPPacket p) {
        TCP u = (TCP) p;
        if (u.getDestination().equals("255.255.255.255") || u.getDestination().equals("192.168.1.255")) {
            return true;
        } else
            return false;
    }
}
