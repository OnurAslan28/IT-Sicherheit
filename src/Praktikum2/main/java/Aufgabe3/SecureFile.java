package Praktikum2.main.java.Aufgabe3;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class SecureFile {
    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.out.println("4 Argumente werden benoetigt um dieses Programm auszufuehren");
            System.out.println("Erstes Argument: Dateiname einer zu verschluesselnden/entschluesselnden Datei");
            System.out.println("Zweites Argument: Dateiname einer Schluessel-Datei mit folgendem Inhalt:\n" +
                    "Byte 1-24: 24 Schluesselbytes (3.DES-Schluessel mit je 8 Byte)\n" +
                    "Byte 25-32: 8 Bytes für den Initialisierungsvektor zum Betrieb im\n" +
                    "CFB - Modus");
            System.out.println("Drittes Argument: Dateiname der Ausgabedatei");
            System.out.println("Viertes Argument: Status-String zur Angabe der gewuenschten Operation:\n" +
                    "encrypt: Verschluesselung der Datei\n" +
                    "decrypt: Entschluesselung der Datei");
        } else {
            if (!(args[3].equals("encrypt") || args[3].equals("decrypt"))) {
                System.out.println("Vierte Argument muss entweder \"encrypt\" oder \"decrypt\" lauten");
            }
        }

        //File deEnFile = new File("C:/Users/onur_/IdeaProjects/ITS_PRAKTIKUM/src/Praktikum2/main/java/Vorgaben-3DES");
        //Path keyPath = Paths.get("C:/Users/onur_/IdeaProjects/ITS_PRAKTIKUM/src/Praktikum2/main/java/Vorgaben-3DES");
        //File outputFile = new File("C:/Users/onur_/IdeaProjects/ITS_PRAKTIKUM/src/Praktikum2/main/java/Vorgaben-3DES");
        File deEnFile = new File(args[0]);
        Path keyPath = Paths.get(args[1]);
        File outputFile = new File(args[2]);

        FileInputStream in = new FileInputStream(deEnFile);
        FileOutputStream out = new FileOutputStream(outputFile);

        byte[] keyAry = Files.readAllBytes(keyPath);
        byte[] key1 = Arrays.copyOfRange(keyAry, 0, 8);
        byte[] key2 = Arrays.copyOfRange(keyAry, 8, 16);
        byte[] key3 = Arrays.copyOfRange(keyAry, 16, 24);
        byte[] initVector = Arrays.copyOfRange(keyAry, 24, 32);

        TripleDES tripleDES = new TripleDES(key1, key2, key3);
        byte[] blockchiffre = tripleDES.encryptBytes(initVector);


        byte[] buffer = new byte[8];
        byte[] chiffreText = new byte[8];
        byte[] encryptedBytes = new byte[8];
        int len;
        // aktueller Chiffretext = aktueller Klartextblock XOR Blockchiffre beim Verschluesseln
        //                       = aktueller Chiffretextblock (vom Input) beim Entschluesseln
        while ((len = in.read(buffer)) > 0) {
            for (int i = 0; i < 8; i++) {
                encryptedBytes[i] = (byte) (buffer[i] ^ blockchiffre[i]);
                if (args[3].equals("encrypt")) {
                    chiffreText[i] = (byte) (buffer[i] ^ blockchiffre[i]);
                } else {
                    chiffreText[i] = buffer[i];
                }
            }
            out.write(encryptedBytes, 0, len);
            // aktuellen Chiffretext als Argument fuer Blockchiffre TripleDES
            blockchiffre = tripleDES.encryptBytes(chiffreText);
        }
    }
}

