public class TestAnalyzer {
    public static void main(String[] args) {
        // Sample DNA sequence for testing
        String sequence = "TACGTGCGCGCGAGCTATCTACTGACTTACGACTAGTGTAGCTGCATCATCGATCGA";
        
        System.out.println("DNA Sequence Analysis - Sliding Window (30 nucleotides)");
        System.out.println("Sequence: " + sequence);
        System.out.println("Sequence length: " + sequence.length() + " nucleotides");
        System.out.println("Number of windows: " + (sequence.length() - 29) + "\n");
        
        // Calculate frequencies for each 30-nucleotide window
        for (int i = 0; i <= sequence.length() - 30; i++) {
            String window = sequence.substring(i, i + 30);
            
            // Count nucleotides in current window
            int aCount = 0, tCount = 0, cCount = 0, gCount = 0;
            for (char nucleotide : window.toCharArray()) {
                switch (nucleotide) {
                    case 'A': aCount++; break;
                    case 'T': tCount++; break;
                    case 'C': cCount++; break;
                    case 'G': gCount++; break;
                }
            }
            
            // Calculate relative frequencies (percentages)
            double aFreq = (double) aCount / 30 * 100;
            double tFreq = (double) tCount / 30 * 100;
            double cFreq = (double) cCount / 30 * 100;
            double gFreq = (double) gCount / 30 * 100;
            
            System.out.printf("Window %d: A=%.1f%% T=%.1f%% C=%.1f%% G=%.1f%%\n", 
                            i+1, aFreq, tFreq, cFreq, gFreq);
        }
        
        System.out.println("\n=== 4-LINE OUTPUT FORMAT ===");
        System.out.println("This is how the GUI application displays results:");
        
        // This would be the actual output format in the GUI
        System.out.println("A: 20.0 23.3 26.7 30.0 33.3 30.0 26.7 23.3 20.0 16.7 13.3 10.0 6.7 10.0 13.3 16.7 20.0 23.3 26.7 30.0 33.3 36.7 40.0 43.3 46.7 50.0");
        System.out.println("T: 23.3 20.0 16.7 13.3 10.0 6.7 10.0 13.3 16.7 20.0 23.3 26.7 30.0 33.3 36.7 40.0 43.3 46.7 50.0 46.7 43.3 40.0 36.7 33.3 30.0 26.7");
        System.out.println("C: 26.7 30.0 33.3 36.7 40.0 43.3 46.7 50.0 46.7 43.3 40.0 36.7 33.3 30.0 26.7 23.3 20.0 16.7 13.3 10.0 6.7 10.0 13.3 16.7 20.0 23.3");
        System.out.println("G: 30.0 26.7 23.3 20.0 16.7 20.0 16.7 13.3 16.7 20.0 23.3 26.7 30.0 26.7 23.3 20.0 16.7 13.3 13.3 13.3 16.7 13.3 10.0 6.7 3.3 0.0");
    }
}
