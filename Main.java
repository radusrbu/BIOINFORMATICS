import java.io.*;
import java.util.*;


public class Main {

    public static void main(String[] args){
        String str = "RRADU";
        String reversed = uniqueLettersInOrder(str);
        System.out.println(reversed);
        relativefq(str);
    }

    public static String uniqueLettersInOrder(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        char previous = s.charAt(0);
        out.append(previous);
        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != previous) {
                out.append(c);
                previous = c;
            }
        }
        return out.toString();
    }

 
    public static void relativefq(String s) {
        if (s == null || s.isEmpty()) {
            return;
        }
        Map<Character, Integer> counts = new LinkedHashMap<>();
        for (char c : s.toCharArray()) {
            counts.put(c, counts.getOrDefault(c, 0) + 1);
        }
        int n = s.length();
        for (Map.Entry<Character, Integer> e : counts.entrySet()) {
            char symbol = e.getKey();
            int count = e.getValue();
            int percent = (int)Math.round((count * 100.0) / n);
            System.out.println(symbol + " " + percent);
        }
    }

 
}