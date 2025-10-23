import java.util.*;

public class Main {
    public static void main(String[] args) {
        
        System.out.println ("Dinucleotides");
        
        String string = "TACGTGCGCGCGAGCTATCTACTGACTTACGACTAGTGTAGCTGCATCATCGATCGA";

        for (int i = 0; i < string.length()-1; i++){
            System.out.print("" + string.charAt(i) + string.charAt(i + 1) + " ");
        }
        System.out.println();
        System.out.println ("Trinucleotides");
        
        for (int i = 0; i < string.length()-2; i++){
            System.out.print("" + string.charAt(i) + string.charAt(i + 1) + string.charAt(i + 2) + " ");
        }
        System.out.println();
        
        System.out.println("Dinucleotide Percentages:");
        calculateDinucleotidePercentages(string);
        
        System.out.println("\nTrinucleotide Percentages:");
        calculateTrinucleotidePercentages(string);
    }
    
    public static void calculateDinucleotidePercentages(String sequence) {
        Map<String, Integer> dinucleotideCount = new HashMap<>();
        int totalDinucleotides = sequence.length() - 1;
        
        
        for (int i = 0; i < sequence.length() - 1; i++) {
            String dinucleotide = "" + sequence.charAt(i) + sequence.charAt(i + 1);
            dinucleotideCount.put(dinucleotide, dinucleotideCount.getOrDefault(dinucleotide, 0) + 1);
        }
        
        for (Map.Entry<String, Integer> entry : dinucleotideCount.entrySet()) {
            double percentage = (double) entry.getValue() / totalDinucleotides * 100;
            System.out.printf("%s: %.2f%% (%d/%d)%n", entry.getKey(), percentage, entry.getValue(), totalDinucleotides);
        }
    }
    
    public static void calculateTrinucleotidePercentages(String sequence) {
        Map<String, Integer> trinucleotideCount = new HashMap<>();
        int totalTrinucleotides = sequence.length() - 2;
        
        for (int i = 0; i < sequence.length() - 2; i++) {
            String trinucleotide = "" + sequence.charAt(i) + sequence.charAt(i + 1) + sequence.charAt(i + 2);
            trinucleotideCount.put(trinucleotide, trinucleotideCount.getOrDefault(trinucleotide, 0) + 1);
        }
        
        for (Map.Entry<String, Integer> entry : trinucleotideCount.entrySet()) {
            double percentage = (double) entry.getValue() / totalTrinucleotides * 100;
            System.out.printf("%s: %.2f%% (%d/%d)%n", entry.getKey(), percentage, entry.getValue(), totalTrinucleotides);
        }
    }
}
