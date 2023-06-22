package Praktikum3;

import java.security.*;
import java.io.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
/**
 * Die Klasse RSAKeyCreation generiert einen RSA-Schlüsselpaar und erstellt die entsprechenden
 * Schlüsseldateien für den öffentlichen und privaten Schlüssel.
 */

public class RSAKeyCreation extends Object {

    private KeyPair keyPair = null;
    private String inhaber;

    /**
     * Erzeugt eine neue Instanz von RSAKeyCreation mit dem angegebenen Inhaber.
     *
     * @param inhaber Der Name des Schlüsselinhabers.
     */
    public RSAKeyCreation(String inhaber) {
        this.inhaber = inhaber;
    }

    public static void main(String args[]) throws NoSuchAlgorithmException, InvalidKeySpecException {

        if (args.length != 1) {
            System.out.println("Bitte Namen des Inhabers als Argument uebergeben.");
        } else {
            String inhaber = args[0]; // Name des Inhabers aus dem Kommandozeilenargument lesen
            RSAKeyCreation sm = new RSAKeyCreation(inhaber); // Eine Instanz von RSAKeyCreation mit dem Inhabernamen erstellen

            sm.generateKeyPair(); // Schlüsselpaar generieren

            sm.createKeyFiles(); // Schlüsseldateien erstellen
        }
    }

    /**
     * Generiert ein RSA-Schlüsselpaar mit einer Schlüssellänge von 4096 Bits.
     */
    public void generateKeyPair() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA"); // Erzeugt eine Instanz des KeyPairGenerator mit dem RSA-Algorithmus

            gen.initialize(4096); // Initialisiert den Generator mit einer Schlüssellänge von 4096 Bits
            keyPair = gen.generateKeyPair(); // Generiert das Schlüsselpaar
        } catch (NoSuchAlgorithmException ex) {
            showErrorAndExit("Es existiert kein KeyPairGenerator fuer RSA", ex); // Zeigt eine Fehlermeldung an, wenn der Algorithmus nicht verfügbar ist
        }
    }
    /**
     * Erstellt die Schlüsseldateien für den öffentlichen und privaten Schlüssel des Inhabers.
     *
     * @throws NoSuchAlgorithmException Wenn der RSA-Algorithmus nicht verfügbar ist.
     * @throws InvalidKeySpecException  Wenn die Schlüsselspezifikation ungültig ist.
     */
    public void createKeyFiles() throws NoSuchAlgorithmException, InvalidKeySpecException {

        //fuer <inhaber>.pub:
        byte[] inhaberAsBytes = inhaber.getBytes(); // Konvertiert den Schlüsselinhaber-Namen in Bytes
        int inhaberLength = inhaberAsBytes.length; // Länge des Schlüsselinhaber-Namens in Bytes
        PublicKey publicKey = keyPair.getPublic(); // Öffentlicher Schlüssel aus dem Schlüsselpaar
        byte[] publicKeyAsBytes = publicKey.getEncoded(); // Konvertiert den öffentlichen Schlüssel in Bytes
        int publicKeyLength = publicKeyAsBytes.length; // Länge des öffentlichen Schlüssels in Bytes

        //fuer <inhaber>.prv
        PrivateKey privateKey = keyPair.getPrivate(); // Privater Schlüssel aus dem Schlüsselpaar
        byte[] privateKeyAsBytes = privateKey.getEncoded(); // Konvertiert den privaten Schlüssel in Bytes
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Erzeugt eine Instanz von KeyFactory mit dem RSA-Algorithmus
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyAsBytes); // Erzeugt eine Schlüsselspezifikation für den privaten Schlüssel im PKCS8-Format
        PrivateKey privateKeyPKCS8 = keyFactory.generatePrivate(privateKeySpec); // Erzeugt einen privaten Schlüssel aus der Schlüsselspezifikation
        byte[] privateKeyPKCS8AsBytes = privateKeyPKCS8.getEncoded(); // Konvertiert den privaten Schlüssel im PKCS8-Format in Bytes
        int privateKeyLength = privateKeyPKCS8AsBytes.length; // Länge des privaten Schlüssels in Bytes

        try {
            DataOutputStream os = new DataOutputStream(new FileOutputStream(
                    inhaber + ".pub")); // Datenstrom für die öffentliche Schlüsseldatei erstellen
            os.writeInt(inhaberLength); // Länge des Schlüsselinhaber-Namens schreiben
            os.write(inhaberAsBytes); // Schlüsselinhaber-Namen schreiben
            os.writeInt(publicKeyLength); // Länge des öffentlichen Schlüssels schreiben
            os.write(publicKeyAsBytes); // Öffentlichen Schlüssel schreiben
            os.close();

            DataOutputStream os1 = new DataOutputStream(new FileOutputStream(
                    inhaber + ".prv")); // Datenstrom für die private Schlüsseldatei erstellen
            os1.writeInt(inhaberLength); // Länge des Schlüsselinhaber-Namens schreiben
            os1.write(inhaberAsBytes); // Schlüsselinhaber-Namen schreiben
            os1.writeInt(privateKeyLength); // Länge des privaten Schlüssels schreiben
            os1.write(privateKeyPKCS8AsBytes); // Privaten Schlüssel im PKCS8-Format schreiben
            os1.close();
        } catch (IOException ex) {
            showErrorAndExit("Fehler beim Schreiben der Dateien.", ex); // Fehlermeldung anzeigen, wenn ein Fehler beim Schreiben der Dateien auftritt
        }
        System.out.println("Der Public Key wird in folgendem Format gespeichert: " + publicKey.getFormat()); // Anzeige des Speicherformats des öffentlichen Schlüssels
    }


    /**
     * Zeigt eine Fehlermeldung an und beendet das Programm.
     *
     * @param msg Die Fehlermeldung.
     * @param ex  Die Ausnahme, die den Fehler verursacht hat.
     */
    private void showErrorAndExit(String msg, Exception ex) {
        System.out.println(msg); // Gibt die Fehlermeldung aus
        System.out.println(ex.getMessage()); // Gibt die Nachricht der Ausnahme aus
        System.exit(0); // Beendet das Programm
    }
}
