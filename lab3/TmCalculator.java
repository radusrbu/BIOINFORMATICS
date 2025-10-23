import java.text.DecimalFormat;

public class TmCalculator {
    public static void main(String[] args) {
        String dna = "ATGCGTACGTTAGGGGGCCCCCC"; 
        double sodiumMilliMolar = 50.0; 
        dna = dna.trim().toUpperCase();

    

        int length = dna.length();
        int countA = countChar(dna, 'A');
        int countC = countChar(dna, 'C');
        int countG = countChar(dna, 'G');
        int countT = countChar(dna, 'T');

        
        int tmWallace = 4 * (countG + countC) + 2 * (countA + countT);

    
        double gcPercent = ((countG + countC) * 100.0) / Math.max(1, length);
        double sodiumMolar = sodiumMilliMolar / 1000.0; 
        double tm2 = 81.5 + 16.6 * log10(sodiumMolar) + 0.41 * gcPercent - (600.0 / length);

        DecimalFormat df = new DecimalFormat("0.00");
        System.out.println();
        System.out.println("Sequence: " + dna);
        System.out.println("Na+ (mM): " + df.format(sodiumMilliMolar));
        System.out.println("Sequence length: " + length);
        System.out.println("%GC: " + df.format(gcPercent));
        System.out.println("Wallace Tm (°C): " + tmWallace);
        System.out.println("Tm with the 2nd formula (°C): " + df.format(tm2));
    }

    

    private static int countChar(String s, char target) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == target) count++;
        }
        return count;
    }

    private static double log10(double x) {
        return Math.log(x) / Math.log(10.0);
    }
}


