package Praktikum2.main.java.Aufgabe12;

import java.io.*;


public class HC1 {
    //txt und dec mit FC in cmd vergleichen
    private static boolean encrypt;

    public static void main(String[] args) {
        encrypt = false;
        String inputFile;
        String outputFile;
        if (encrypt) {
            inputFile = "C:/Users/onur_/Desktop";
            String[] tempOutput = inputFile.split("\\.(?=[^\\.]+$)");
            outputFile = tempOutput[0] + ".enc";
        } else {
            inputFile = "C:/Users/onur_/Desktop";
            String[] tempOutput = inputFile.split("\\.(?=[^\\.]+$)");
            outputFile = tempOutput[0] + ".dec";
        }
        long key = 5034534L;

        LCG lcg = new LCG(key);

        try (InputStream in = new FileInputStream(inputFile);
             OutputStream out = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[8];
            byte[] encryptedBytes = new byte[8];
            int len;
            while ((len = in.read(buffer)) > 0) {
                //buffer inhalt wird verschlüsselt und in die Ziel Datei geschrieben
                for (int i = 0; i < len; i++) {
                    int keyByte = lcg.nextInt() & 0xFF; // LCG-Schlüsselbyte
                    encryptedBytes[i] = (byte) (buffer[i] ^ keyByte); // XOR  // 1011 ^ 1110 = 0101 ^ 0110 = 0011
                }
                out.write(encryptedBytes,0,len);
            }
            if(encrypt) {
                System.out.println("File encrypted successfully to " + outputFile);
            }else {

                System.out.println("File decrypted successfully to " + outputFile);

            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}