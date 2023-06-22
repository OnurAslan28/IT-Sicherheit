package Praktikum2.main.java.Aufgabe12;

import java.util.HashSet;

public class LCG {
    //referenz = SUN-UNIX drand48
    private long seed;
    private final long a = 25214903917L;
    private final long c = 11L;
    private final long m = (long) Math.pow(2, 48);

    public LCG(long seed) {
        this.seed = seed;
    }

    public int nextInt() {
        seed = (a * seed + c) % m;
        return (int) seed;
    }

    public static void main(String[] args) {
        LCG lcg = new LCG(20); // Startwert beliebig gleicher startwert = gleiche zufallszahlen
        HashSet<Integer> set = new HashSet<Integer>();

        for (int i = 0; i < 256; i++) {
            int rand = lcg.nextInt() & 0x000000FF; // niederwertigstes Byte
            set.add(rand);
        }

        System.out.println("Anzahl der verschiedenen Zahlen: " + set.size());
    }
}
