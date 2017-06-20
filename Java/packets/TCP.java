package packets;

import Stream.TCPStream;

import java.util.ArrayList;


public class TCP extends IP {

    private int sourcePort;
    private int destinationPort;
    private long sequenceNumber;
    private long acknowledgementNumber;
    private int headerLength;
    private boolean reserved;
    private boolean nonce;
    private boolean CWR;
    private boolean ECNEcho;
    private boolean URG;
    private boolean ACK;
    private boolean PSH;
    private boolean RST;
    private boolean SYN;
    private boolean FIN;
    private int windowSize;
    private int checksum;
    private byte[] data;
    private TCPStream parentStream;
    private boolean retransmission = false;
    private boolean duplicateACK = false;
    private boolean windowUpdate = false;
    private boolean reassembledPDU = false;
    private boolean ACKUnseenSegment = false;
    private boolean PreviousSegmentLost = false;

    public TCP(PCAPPacket packet) {
        super(packet);
        IP ip = (IP) packet;
        super.setTotalLength(ip.getTotalLength());
        super.setIPV4(ip.getIPV4());
        // super.setIPV6(ip.getIPV6());
        super.setSource(ip.getSource());
        super.setDestination(ip.getDestination());
    }

    public boolean isPreviousSegmentLost() {
        return PreviousSegmentLost;
    }

    public void setPreviousSegmentLost(boolean PreviousSegmentLost) {
        this.PreviousSegmentLost = PreviousSegmentLost;
    }

    public boolean isACKUnseenSegment() {
        return ACKUnseenSegment;
    }

    public void setACKUnseenSegment(boolean ACKUnseenSegment) {
        this.ACKUnseenSegment = ACKUnseenSegment;
    }

    public boolean isReassembledPDU() {
        return reassembledPDU;
    }

    public void setReassembledPDU(boolean reassembledPDU) {
        this.reassembledPDU = reassembledPDU;
    }

    public boolean isRetransmission() {
        return retransmission;
    }

    public void setRetransmission(boolean retransmission) {
        this.retransmission = retransmission;
    }

    public boolean isDuplicateACK() {
        return duplicateACK;
    }

    public void setDuplicateACK(boolean duplicateACK) {
        this.duplicateACK = duplicateACK;
    }

    public boolean isWindowUpdate() {
        return windowUpdate;
    }

    public void setWindowUpdate(boolean windowUpdate) {
        this.windowUpdate = windowUpdate;
    }

    public TCPStream getParentStream() {
        return parentStream;
    }

    public void setParentStream(TCPStream parentStream) {
        this.parentStream = parentStream;
    }

    public boolean isACK() {
        return ACK;
    }

    public void setACK(boolean ACK) {
        this.ACK = ACK;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isCWR() {
        return CWR;
    }

    public void setCWR(boolean CWR) {
        this.CWR = CWR;
    }

    public boolean isECNEcho() {
        return ECNEcho;
    }

    public void setECNEcho(boolean ECNEcho) {
        this.ECNEcho = ECNEcho;
    }

    public long getAcknowledgementNumber() {
        return acknowledgementNumber;
    }

    public void setAcknowledgementNumber(long acknowledgementNumber) {
        this.acknowledgementNumber = acknowledgementNumber;
    }

    public boolean isAcknowledgment() {
        return ACK;
    }

    public void setAcknowledgment(boolean acknowledgment) {
        this.ACK = acknowledgment;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public boolean isFIN() {
        return FIN;
    }

    public void setFIN(boolean fin) {
        this.FIN = fin;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    public boolean isNonce() {
        return nonce;
    }

    public void setNonce(boolean nonce) {
        this.nonce = nonce;
    }

    public boolean isPSH() {
        return PSH;
    }

    public void setPSH(boolean push) {
        this.PSH = push;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public boolean isRST() {
        return RST;
    }

    public void setRST(boolean reset) {
        this.RST = reset;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public boolean isSYN() {
        return SYN;
    }

    public void setSYN(boolean syn) {
        this.SYN = syn;
    }

    public boolean isURG() {
        return URG;
    }

    public void setURG(boolean urgent) {
        this.URG = urgent;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getLength() {
        if (data == null) {
            return (0);
        } else {
            return (Math.max(0, data.length));
        }
    }

    public String getAdditionalInformation() {
        String summary = "";
        if (retransmission) {
            summary = "[TCP Retransmission] ";
        }
        if (windowUpdate) {
            summary = "[TCP Window Update] ";
        }
        if (duplicateACK) {
            summary = "[TCP Duplicate ACK] ";
        }
        if (reassembledPDU) {
            summary = "[TCP Segment Of A Reassembled PDU] ";
        }
        if (ACKUnseenSegment) {
            summary = "[TCP ACKed Unseen Segment] ";
        }
        if (PreviousSegmentLost) {
            summary = "[TCP Previous Segment Not Captured] ";
        }
        return (summary);
    }

    @Override
    public String getSummary() {
        String summary = getAdditionalInformation() + sourcePort + " > " + destinationPort;
        ArrayList<String> flags = new ArrayList<String>();
        if (PSH) {
            flags.add("PSH");
        }
        if (SYN) {
            flags.add("SYN");
        }
        if (FIN) {
            flags.add("FIN");
        }
        if (URG) {
            flags.add("URG");
        }
        if (RST) {
            flags.add("RST");
        }
        if (ACK) {
            flags.add("ACK");
        }
        if (flags.size() > 0) {
            summary += " [";
            for (int i = 0; i < flags.size(); i++) {
                if (i == flags.size() - 1) {
                    summary += flags.get(i);
                } else {
                    summary += flags.get(i) + ", ";
                }
            }
            summary += "]";
        }
        return (summary);
    }
}
