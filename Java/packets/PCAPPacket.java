package packets;

public class PCAPPacket {

    private long frameNumber;
    private int frameLength;
    private String protocol = "FRAME";
    private long arrivalMillis = 0;
    private long nanos = 0;
    private long micros = 0;//11111

    public long getMicros() {//1111
        return micros;
    }

    public void setMicros(long micros) {//11111
        this.micros = micros;
    }
    public long getNanos() {
        return nanos;
    }

    public void setNanos(long nanos) {
        this.nanos = nanos;
    }

    public double getSeconds() {
        long seconds = Long.parseLong(arrivalMillis / 1000 + "");
        double seconds2 = (double) nanos / 1000000000;
        double s = seconds + seconds2;
        return s;
    }

    public long getArrivalMillis() {
        return arrivalMillis;
    }

    public void setArrivalMillis(long arrivalMillis) {
        this.arrivalMillis = arrivalMillis;
    }

    public int getFrameLength() {
        return frameLength;
    }

    public void setFrameLength(int frameLength) {
        this.frameLength = frameLength;
    }

    public long getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(long frameNumber) {
        this.frameNumber = frameNumber;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSummary() {
        return ("FRAME");
    }
}
