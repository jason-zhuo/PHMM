package WebfingerTest;


/**
 * Symbolization Step
 * Compatible with HMMER3
 *
 * Transfer pkt size with direction to Gene symbol
 */
public class AmnioAlphabet {
    public  alphabet AmnioAlpha = new alphabet();
    public static String alphabet = "ACDEFGHIKLMNPQRSTVWY";
    public final String [] Map=new String[alphabet.length()*alphabet.length()*WFstaticConfig.GAP];


    public AmnioAlphabet(){
        initMap();
    }
    private void initMap(){
        int currentMapIdx=0;
        for (int i =0 ;i<Map.length;i++)
            Map[i]="XX";
        for (int i =0 ;i<alphabet.length()*alphabet.length();i++){
            String currentAlpha = AmnioAlpha.toString();
            for(int k =0 ; k < WFstaticConfig.GAP;k++){
                Map[currentMapIdx]=currentAlpha;
                currentMapIdx++;
            }
            AmnioAlpha.addone();
        }

    }

    public int Getsize(String alpha){
        int size=-9999;  // error alphabet
        for (int i=0;i<Map.length;i++){
            if (Map[i].equalsIgnoreCase(alpha)){
                return i-WFstaticConfig.MTU;
            }
        }
        return size;
    }
    public String GetSymbol(int payloadsize){
        return Map[payloadsize+WFstaticConfig.MTU];
    }


    public static void main(String[] args) {
        AmnioAlphabet test1 = new AmnioAlphabet();
        int p = 2492;
        int a = -1448;



        System.out.println(test1.GetSymbol(p));
        System.out.println(test1.GetSymbol(1448));
        System.out.println(test1.GetSymbol(-2));
        System.out.println(test1.Getsize("MM"));
        System.out.println(test1.Getsize("RR"));
    }
    class alphabet{
        char[] alphabet = {'A','C','D','E','F','G','H','I','K','L','M','N','P','Q','R','S','T','V','W','Y'};

        int firstCharidx;
        int secondCharidx;
        public alphabet(){
            firstCharidx=0;
            secondCharidx=0;
        }
        public void addone(){
            if (secondCharidx==alphabet.length-1){
                if (firstCharidx<alphabet.length-1){
                    firstCharidx++;
                    secondCharidx=0;
                }
//                else {
//                    System.err.println("first char out of bound");
//                }
            }else {
                if (secondCharidx<alphabet.length-1){
                    secondCharidx++;
                }
            }
        }

        public String toString(){
            return alphabet[firstCharidx]+""+alphabet[secondCharidx];
        }

    }

}
