package Stream;

import WebfingerTest.WFstaticConfig;
import packets.PCAPPacket;
import packets.TCP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class TCPStream {

    private ArrayList<PCAPPacket> tcpPackets = new ArrayList<PCAPPacket>();
    private ArrayList<PCAPPacket> reassembledPackets = new ArrayList<PCAPPacket>();
    private String type = "TCP";
    private int num;

    public TCPStream(int num) {
        this.num = num;
    }


    public static ArrayList<PCAPPacket> reassebleTCPStream(ArrayList<PCAPPacket> packets) {
        HashSet<DuplicateTCPPacket> currentPackets = new HashSet<DuplicateTCPPacket>();
        HashMap<DuplicateACK, Integer> currentACKWindowSizes = new HashMap<DuplicateACK, Integer>();

        ArrayList<PCAPPacket> newPackets = new ArrayList<PCAPPacket>();
        String source = "";
        long soseq = 0;
        long snseq = 0;
        long slack = 0;
        long slseq = 0;
        long dnseq = 0;
        long dlack = 0;
        long dlseq = 0;
        long slwindow = 0;
        long dlwindow = 0;
        boolean lastSourceSYN = false;
        boolean lastDestSYN = false;

        if (packets.size() >= 3) {
            TCP first = (TCP) packets.get(0);
            source = first.getSource();

            if (first.isFIN()) {
                newPackets.addAll(packets);
                packets.clear();
            } else {
                boolean foundACK = false;
                boolean foundSYNACK = false;
                boolean foundSYN = false;
                //SYN
                if (first.isSYN() && !first.isACK() && !first.isFIN() && !first.isRST() && !first.isPSH() && !first.isURG()) {
                    soseq = first.getSequenceNumber();
                    newPackets.add(first);
                    packets.remove(0);
                    foundSYN = true;
                }
                //SYN-ACK
                if (foundSYN) {
                    for (PCAPPacket packet : packets) {
                        TCP tcp = (TCP) packet;

                        if (!tcp.getSource().equals(source)) {
                            if (tcp.isSYN() && tcp.isACK() && !tcp.isFIN() && !tcp.isRST() && !tcp.isPSH() && !tcp.isURG()) {
                                foundSYNACK = true;
                                newPackets.add(tcp);
                                dlwindow = tcp.getWindowSize();
                                break;
                            }
                        } else {
                            newPackets.add(tcp);
                        }
                    }
                    if (foundSYNACK) {
                        packets.removeAll(newPackets);
                        //ACK
                        for (PCAPPacket packet : packets) {
                            TCP tcp = (TCP) packet;
                            if (tcp.getSource().equals(source)) {
                                if (!tcp.isSYN() && tcp.isACK() && !tcp.isFIN() && !tcp.isRST() && !tcp.isPSH() && !tcp.isURG()) {
                                    foundACK = true;
                                    newPackets.add(tcp);
                                    snseq = tcp.getSequenceNumber();
                                    slack = tcp.getAcknowledgementNumber();
                                    slseq = tcp.getSequenceNumber();
                                    dnseq = tcp.getAcknowledgementNumber();
                                    dlack = tcp.getSequenceNumber();
                                    dlseq = tcp.getAcknowledgementNumber();
                                    slwindow = tcp.getWindowSize();
                                    break;
                                }
                            }
                        }
                    }
                }

                if (!foundACK) {
                    if (packets.size() > 0) {
                        TCP tcp = (TCP) packets.remove(0);
                        snseq = tcp.getSequenceNumber() + tcp.getLength();
                        slack = tcp.getAcknowledgementNumber();
                        slseq = tcp.getSequenceNumber();
                        dnseq = tcp.getAcknowledgementNumber();
                        dlack = tcp.getSequenceNumber();
                        dlseq = tcp.getAcknowledgementNumber();
                        slwindow = tcp.getWindowSize();
                        newPackets.add(tcp);
                    }
                } else {
                    packets.removeAll(newPackets);
                }


                //OTHER
                PCAPPacket removePacket = new PCAPPacket();
                boolean sourceFIN = false;
                boolean destFIN = false;
                boolean seenEnd = false;
                int startPacket = 0;
                while (!seenEnd && packets.size() > 0) {
                    removePacket = null;
                    for (int a = startPacket; a < packets.size(); a++) {
                        PCAPPacket packet = packets.get(a);
                        if (a == packets.size() - 1) {
                            seenEnd = true;
                        }
                        TCP tcp = (TCP) packet;
                        DuplicateACK dupACK = new DuplicateACK(tcp.getSequenceNumber(), tcp.getAcknowledgementNumber());
                        if (!tcp.isSYN() && tcp.isACK() && !tcp.isFIN() && !tcp.isRST() && !tcp.isPSH() && !tcp.isURG() && tcp.getLength() == 0) {
                            if (currentACKWindowSizes.containsKey(dupACK)) {
                                if (currentACKWindowSizes.get(dupACK) != tcp.getWindowSize()) {
                                    tcp.setWindowUpdate(true);
                                }
                                continue;
                            }
                        }
                        if (tcp.getSource().equals(source)) {
                            if (tcp.isFIN()) {
                                sourceFIN = true;
                                removePacket = tcp;
                                if (tcp.isSYN()) {
                                    lastSourceSYN = true;
                                } else {
                                    lastSourceSYN = false;
                                }
                                break;
                            }
                            //MORE SYNS - ALLOW THEM IF CORRECT SOSEQ
                            if (tcp.isSYN() && !tcp.isACK() && !tcp.isFIN() && !tcp.isRST() && !tcp.isPSH() && !tcp.isURG()) {
                                if (tcp.getSequenceNumber() == soseq && tcp.getAcknowledgementNumber() == 0) {
                                    removePacket = tcp;
                                    if (tcp.isSYN()) {
                                        lastSourceSYN = true;
                                    } else {
                                        lastSourceSYN = false;
                                    }
                                    break;
                                }
                            }
                            if (sourceFIN && tcp.getLength() > 0) {
                                continue;
                            }
                            if (!tcp.isRST() && tcp.getSequenceNumber() == slseq && tcp.getAcknowledgementNumber() == slack && tcp.getLength() == 0 && tcp.getWindowSize() != slwindow) {
                                tcp.setWindowUpdate(true);
                                removePacket = tcp;
                                if (tcp.isSYN()) {
                                    lastSourceSYN = true;
                                } else {
                                    lastSourceSYN = false;
                                }
                                continue;
                            }
                            if (!tcp.isSYN() && !tcp.isRST() && tcp.getSequenceNumber() == slseq && tcp.getAcknowledgementNumber() == slack && tcp.getLength() == 0 && tcp.getWindowSize() == slwindow) {
                            } else {
                                if (tcp.isRST() || ((tcp.getSequenceNumber() == snseq || (destFIN && tcp.getSequenceNumber() == snseq + 1) || (lastSourceSYN && tcp.getSequenceNumber() == dnseq + 1))
                                        && ((tcp.getAcknowledgementNumber() == dnseq)
                                        || (tcp.getAcknowledgementNumber() >= slack && tcp.getAcknowledgementNumber() <= dnseq)
                                        || (destFIN && tcp.getAcknowledgementNumber() == dnseq + 1)))) {
                                    removePacket = tcp;
                                    if (tcp.isSYN()) {
                                        lastSourceSYN = true;
                                    } else {
                                        lastSourceSYN = false;
                                    }
                                    break;
                                }
                            }
                            if (tcp.isRST() || ((tcp.getSequenceNumber() == dnseq || (sourceFIN && tcp.getSequenceNumber() == dnseq + 1) || (lastDestSYN && tcp.getSequenceNumber() == dnseq + 1)))
                                    && tcp.getAcknowledgementNumber() > dnseq) {
                                tcp.setACKUnseenSegment(true);
                                dnseq = tcp.getAcknowledgementNumber();
                                continue;
                            }
//                            if (tcp.getSequenceNumber() > snseq) {
//                                tcp.setPreviousSegmentLost(true);
//                                removePacket = tcp;
//                                break;
//                            }
                        } else {
                            if (tcp.isFIN()) {
                                destFIN = true;
                                removePacket = tcp;
                                if (tcp.isSYN()) {
                                    lastDestSYN = true;
                                } else {
                                    lastDestSYN = false;
                                }
                                break;
                            }
                            if (foundACK && tcp.getAcknowledgementNumber() == soseq + 1) { //LATE SYN-ACK TO SYN
                                removePacket = tcp;
                                if (tcp.isSYN()) {
                                    lastDestSYN = true;
                                } else {
                                    lastDestSYN = false;
                                }
                                break;
                            }
                            if (destFIN && tcp.getLength() > 0) {
                                continue;
                            }
                            if (!tcp.isSYN() && !tcp.isRST() && tcp.getSequenceNumber() == dlseq && tcp.getAcknowledgementNumber() == dlack && tcp.getLength() == 0 && tcp.getWindowSize() != dlwindow) {
                                tcp.setWindowUpdate(true);
                                removePacket = tcp;
                                if (tcp.isSYN()) {
                                    lastDestSYN = true;
                                } else {
                                    lastDestSYN = false;
                                }
                                continue;
                            }
                            if (!tcp.isRST() && tcp.getSequenceNumber() == dlseq && tcp.getAcknowledgementNumber() == dlack && tcp.getLength() == 0 && tcp.getWindowSize() == dlwindow) {
                            } else {
                                if (tcp.isRST() || ((tcp.getSequenceNumber() == dnseq || (sourceFIN && tcp.getSequenceNumber() == dnseq + 1) || (lastDestSYN && tcp.getSequenceNumber() == dnseq + 1))
                                        && ((tcp.getAcknowledgementNumber() == snseq)
                                        || (tcp.getAcknowledgementNumber() >= dlack && tcp.getAcknowledgementNumber() <= snseq)
                                        || (sourceFIN && tcp.getAcknowledgementNumber() == snseq + 1)))) {
                                    removePacket = tcp;
                                    if (tcp.isSYN()) {
                                        lastDestSYN = true;
                                    } else {
                                        lastDestSYN = false;
                                    }
                                    break;
                                }
                            }
                            if (tcp.isRST() || ((tcp.getSequenceNumber() == dnseq || (sourceFIN && tcp.getSequenceNumber() == dnseq + 1) || (lastDestSYN && tcp.getSequenceNumber() == dnseq + 1)))
                                    && tcp.getAcknowledgementNumber() > snseq) {
                                tcp.setACKUnseenSegment(true);
                                snseq = tcp.getAcknowledgementNumber();
                                continue;
                            }
//                            if (tcp.getSequenceNumber() > dnseq) {
//                                tcp.setPreviousSegmentLost(true);
//                                removePacket = tcp;
//                                break;
//                            }
                        }
                    }

                    TCP tcp = (TCP) removePacket;
                    if (removePacket != null && tcp.getLength() > 0) {
                        if (currentPackets.contains(new DuplicateTCPPacket(tcp.getSequenceNumber(), tcp.getAcknowledgementNumber(), tcp.getData()))) {
                            removePacket = null;
                        } else {
                            if (!tcp.isRST() && !tcp.getSource().equals(source)) {
                                dnseq = tcp.getSequenceNumber() + (long) tcp.getLength();
                                dlack = tcp.getAcknowledgementNumber();
                                dlseq = tcp.getSequenceNumber();
                                dlwindow = tcp.getWindowSize();
                            } else if (!tcp.isRST() && tcp.getSource().equals(source)) {
                                snseq = tcp.getSequenceNumber() + (long) tcp.getLength();
                                slack = tcp.getAcknowledgementNumber();
                                slseq = tcp.getSequenceNumber();
                                slwindow = tcp.getWindowSize();
                            }
                        }
                    }

                    if (removePacket != null) {
//                        if (!tcp.isSYN() && tcp.isACK() && !tcp.isFIN() && !tcp.isRST() && !tcp.isPSH() && !tcp.isURG() && tcp.getLength() == 0) {
//                            currentACKWindowSizes.put(new DuplicateACK(tcp.getSequenceNumber(), tcp.getAcknowledgementNumber()), tcp.getWindowSize());
//                        }
                        if (tcp.isACK()) {
                            currentACKWindowSizes.put(new DuplicateACK(tcp.getSequenceNumber(), tcp.getAcknowledgementNumber()), tcp.getWindowSize());
                        }
                        currentPackets.add(new DuplicateTCPPacket(tcp.getSequenceNumber(), tcp.getAcknowledgementNumber(), tcp.getData()));
                        newPackets.add(removePacket);
                        packets.remove(removePacket);
                    } else {
                        startPacket++;
                    }
                }
            }
        } else {
            newPackets.addAll(packets);
            packets.clear();
        }
        for (PCAPPacket packet : packets) {
            TCP tcp = (TCP) packet;
            if (!tcp.isWindowUpdate() && !tcp.isACKUnseenSegment()) {
                if (!tcp.isPSH() && WFstaticConfig.isSSH?tcp.getFrameLength()-66==0:tcp.getLength() == 0) {
                    tcp.setDuplicateACK(true);
                } else {
                   // tcp.setRetransmission(true);
                }
            }
        }
        return (newPackets);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addTCPPacket(TCP packet) {
        tcpPackets.add(packet);
    }

    public ArrayList<PCAPPacket> getTCPPackets() {
        return tcpPackets;
    }
    public int getTCPPacketsNumber() {
        return tcpPackets.size();
    }

    public ArrayList<PCAPPacket> getReassembledPackets() {
        return reassembledPackets;
    }

    public void setReassembledPackets(ArrayList<PCAPPacket> reassembledPackets) {
        this.reassembledPackets = reassembledPackets;
    }
    //zzl
    public String getStreamDestIP(){
        TCP t = (TCP) tcpPackets.get(0);
        return t.getDestination();
    }
    public String getStreamSourceIP(){
        TCP t = (TCP) tcpPackets.get(0);
        return t.getSource();
    }
    public int getDestPort(){
        TCP t = (TCP) tcpPackets.get(0);
        return t.getDestinationPort();
    }
    public int getStreamSourcePort(){
        TCP t = (TCP) tcpPackets.get(0);
        return t.getSourcePort();
    }
    public String GetStreamSummary(){
        String temp =getStreamSourceIP()+":"+ getStreamSourcePort()+" -> "+getStreamDestIP()+":" +getDestPort() +"\n";
        for (PCAPPacket a : tcpPackets){
            TCP t = (TCP) a;
            temp += t.getSequenceNumber() + " ";
        }

        return temp;


    }


    public boolean isInStream(TCP packet) {
        TCP t = (TCP) tcpPackets.get(0);
        if ((t.getSource().equals(packet.getSource())
                && t.getDestination().equals(packet.getDestination())
                && t.getSourcePort() == packet.getSourcePort()
                && t.getDestinationPort() == packet.getDestinationPort())
                || (t.getSource().equals(packet.getDestination())
                && t.getDestination().equals(packet.getSource())
                && t.getSourcePort() == packet.getDestinationPort()
                && t.getDestinationPort() == packet.getSourcePort())) {
            int amount = Math.max(0, tcpPackets.size() - 11);
            for (int i = tcpPackets.size() - 1; i >= amount; i--) {
                t = (TCP) tcpPackets.get(i);
                if (t.getSequenceNumber() == packet.getAcknowledgementNumber()) {
                    return (true);
                }
                if (t.getAcknowledgementNumber() == packet.getSequenceNumber()) {
                    return (true);
                }
                if (t.getSequenceNumber() == packet.getAcknowledgementNumber() - 1) {
                    return (true);
                }
                if (t.getAcknowledgementNumber() == packet.getSequenceNumber() - 1) {
                    return (true);
                }
                if (t.getSequenceNumber() == packet.getAcknowledgementNumber() - t.getLength()) {
                    return (true);
                }
                if (t.getSequenceNumber() == packet.getSequenceNumber() - t.getLength()) {
                    return (true);
                }
                if (t.getAcknowledgementNumber() == packet.getAcknowledgementNumber() - t.getLength()) {
                    return (true);
                }
                if (t.getAcknowledgementNumber() == packet.getSequenceNumber() - t.getLength()) {
                    return (true);
                }
                if (t.getSequenceNumber() == packet.getSequenceNumber()) {
                    return (true);
                }
                if (t.getAcknowledgementNumber() == packet.getAcknowledgementNumber()) {
                    return (true);
                }
                if (t.getSequenceNumber() + t.getLength() == packet.getAcknowledgementNumber()) {
                    return (true);
                }
            }
            return (false);
        } else {
            return (false);
        }
    }

    public int getTotalLength() {
        int total = 0;
        for (PCAPPacket t : tcpPackets) {
            total += t.getFrameLength();
        }
        return (total);
    }

    public int getNum() {
        return num;
    }

    @Override
    public String toString() {
        return ("TCP Stream #" + num + " Number Of Packets: " + tcpPackets.size() + " Total Length: " + getTotalLength());
    }

    private static class DuplicateACK {

        long seq;
        long ack;

        public DuplicateACK(long seq, long ack) {
            this.seq = seq;
            this.ack = ack;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + (int) (this.seq ^ (this.seq >>> 32));
            hash = 47 * hash + (int) (this.ack ^ (this.ack >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DuplicateACK other = (DuplicateACK) obj;
            if (this.seq != other.seq) {
                return false;
            }
            if (this.ack != other.ack) {
                return false;
            }
            return true;
        }
    }

    private static class DuplicateTCPPacket {

        long sequenceNumber;
        long acknowledgementNumber;
        byte[] data;

        public DuplicateTCPPacket(long sequenceNumber, long acknowledgementNumber, byte[] data) {
            this.sequenceNumber = sequenceNumber;
            this.acknowledgementNumber = acknowledgementNumber;
            this.data = data;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 73 * hash + (int) (this.sequenceNumber ^ (this.sequenceNumber >>> 32));
            hash = 73 * hash + Arrays.hashCode(this.data);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DuplicateTCPPacket other = (DuplicateTCPPacket) obj;
            if (this.sequenceNumber != other.sequenceNumber) {
                return false;
            }
            if (!Arrays.equals(this.data, other.data)) {
                return false;
            }
            return true;
        }
    }
}
