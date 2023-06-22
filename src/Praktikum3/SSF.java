package Praktikum3;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

/**
 * Die Klasse SSF ermöglicht das Verschlüsseln von Dateien mit einem öffentlichen RSA-Schlüssel.
 * Sie erwartet den privaten Schlüssel, den öffentlichen Schlüssel, die zu importierende Datei und die
 * Ausgabedatei als Eingabeparameter.
 */

public class SSF {

    /**
     * Die Hauptmethode der Klasse SSF.
     *
     * @param args Die Eingabeparameter: PrivateKeyFile, PublicKeyFile, ImportFile, ExportFile.
     * @throws IOException              Wenn ein Fehler beim Lesen oder Schreiben der Dateien auftritt.
     * @throws NoSuchAlgorithmException Wenn der angeforderte Algorithmus nicht verfügbar ist.
     * @throws InvalidKeySpecException  Wenn die Schlüsselspezifikation ungültig ist.
     * @throws SignatureException       Wenn ein Fehler beim Signieren des Secret Keys auftritt.
     * @throws InvalidKeyException      Wenn der angegebene Schlüssel ungültig ist.
     */
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        if (args.length != 4) {
            System.err.println("Invalid Usage: SSF PrivateKeyFile PublicKeyFile ImportFile ExportFile(SSF)");
        } else {
            // a)
            byte[] privKeyByte = readKeyFromFile(args[0]);
            if (privKeyByte == null) {
                System.err.println("Privater Schluessel konnte nicht eingelesen werden");
                return;
            }
            // b)
            byte[] pubKeyByte = readKeyFromFile(args[1]);
            if (pubKeyByte == null) {
                System.err.println("Oeffentlicher Schluessel konnte nicht eingelesen werden");
                return;
            }

            //private & public key formatieren (Aus bytes einen schluessel erstellen)
            KeyFactory keyFactoryPrivate = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactoryPrivate.generatePrivate(new PKCS8EncodedKeySpec(privKeyByte));

            KeyFactory keyFactoryPublic = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactoryPublic.generatePublic(new X509EncodedKeySpec(pubKeyByte));

            // c) Generierung des Geheim Schluessels
            byte[] secretKeyBytes = genSecretKeyBytes(256);
            if (secretKeyBytes == null) {
                System.err.println("Secret Key konnte nicht generiert werden");
                return;
            }
            // d) Signieren des SecretKeys
            byte[] signatureBytes = signKey(privateKey, secretKeyBytes);
            if (signatureBytes == null) {
                System.err.println("Secret Key konnte nicht signiert werden");
                return;
            }
            // e) SecretKey verschluesseln
            byte[] secretKeyBytesEnc = encrypt(publicKey, secretKeyBytes);
            if (secretKeyBytesEnc == null) {
                System.err.println("Secret Key konnte nicht verschluesselt werden");
                return;
            }

            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(args[3]))) {
                // f) Einlesen einer Dokumentendatei, Verschlüsseln der Dateidaten mit dem symmetrischen
                //AES-Algorithmus (geheimer Schlüssel aus c) im Counter-Mode („CTR“) und Erzeugen einer Ausgabedatei.
                SecretKeySpec skspec = new SecretKeySpec(secretKeyBytes, "AES"); //sksspec sind die algorithmische parameter fuer init vector

                Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding"); // weil es bitweise ist und nicht blockweise, deswegen no Padding
                cipher.init(Cipher.ENCRYPT_MODE, skspec);
                byte[] algorithmParams = cipher.getParameters().getEncoded();

                //In Datei schreiben
                // 1. & 2. : SecretKey Laenge und der SecretKey
                out.writeInt(secretKeyBytesEnc.length);
                out.write(secretKeyBytesEnc);

                // 3. & 4. : Signatur Laenge und Signatur
                out.writeInt(signatureBytes.length);
                out.write(signatureBytes);

                // 5. & 6. : Algirhtmische Parameter Laenge  und Algorithmische Parameter
                out.writeInt(algorithmParams.length);
                out.write(algorithmParams);

                // 7. Daten verschluesseln und verschluesselte Daten in ausgabedatei schreiben
                out.write(cipher.update(readFile(args[2])));
                out.write(cipher.doFinal());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Liest die Daten aus einer Datei.
     *
     * @param messageFilePath Der Pfad zur Datei.
     * @return Ein Byte-Array mit den gelesenen Daten.
     */
    //Datei wird gelesen, um diese Datei zu verschluesseln
    public static byte[] readFile(String messageFilePath)  {

        try(DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(messageFilePath)))){
            return inputStream.readAllBytes(); // Liest alle Bytes aus dem Dateieingabestrom und gibt sie als Byte-Array zurück

        } catch (IOException e) {
            e.printStackTrace(); // Gibt die Fehlermeldung und den Stacktrace aus, falls ein Fehler beim Lesen oder Öffnen der Datei auftritt
        }
        return null; // Gibt null zurück, wenn ein Fehler auftritt
    }


    /**
     * Liest den Schlüssel aus einer Datei.
     *
     * @param keyFilePath Der Pfad zur Schlüsseldatei.
     * @return Ein Byte-Array mit den Schlüsseldaten.
     * @throws IOException Wenn ein Fehler beim Lesen der Datei auftritt.
     */
    //Methode um die Schluessel aus den jeweiligen Files zu lesen
    public static byte[] readKeyFromFile(String keyFilePath) throws IOException {

        byte[] keyBytes; // Deklariert ein Byte-Array zur Speicherung der Schlüsseldaten

        try(DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(keyFilePath)))){
            inputStream.skipBytes(inputStream.readInt()); // Überspringt die Anzahl der zu überspringenden Bytes, die in der Datei angegeben ist

            keyBytes = inputStream.readNBytes(inputStream.readInt()); // Liest die angegebene Anzahl von Bytes aus dem Dateieingabestrom und speichert sie im Byte-Array
        }catch (IOException e){
            throw new RuntimeException(); // Wirft eine RuntimeException, wenn ein Fehler beim Lesen der Datei auftritt
        }
        return keyBytes; // Gibt das Byte-Array mit den Schlüsseldaten zurück
    }


    /**
     * Erzeugt eine Signatur für den geheimen Schlüssel mit dem privaten Schlüssel.
     *
     * @param privateKey Der private Schlüssel.
     * @param secretKey  Der geheime Schlüssel.
     * @return Ein Byte-Array mit der erzeugten Signatur.
     * @throws NoSuchAlgorithmException Wenn der angeforderte Algorithmus nicht verfügbar ist.
     * @throws InvalidKeyException      Wenn der angegebene Schlüssel ungültig ist.
     * @throws SignatureException       Wenn ein Fehler beim Signieren des Secret Keys auftritt.
     */
    //methode um eine Signatur für den secretKey zu erzeugen
    public static byte[] signKey(PrivateKey privateKey, byte[] secretKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        //Signatur erzeugen
        Signature rsaSignature = null;
        rsaSignature = Signature.getInstance("SHA512withRSA");
        // zum Signieren benoetigen wir den privaten Schluessel (hier: RSA)
        rsaSignature.initSign(privateKey);
        // Daten fuer die kryptographische Hashfunktion (hier: SHA-512) liefern
        rsaSignature.update(secretKey);
        // Signaturbytes durch Verschluesselung des Hashwerts (mit privatem RSA-Schluessel) erzeugen
        return rsaSignature.sign();

    }

    /**
     * Generiert den geheimen Schlüssel mit der angegebenen Schlüsselgröße.
     *
     * @param bits Die Schlüsselgröße in Bits.
     * @return Ein Byte-Array mit dem generierten geheimen Schlüssel.
     */
    //methode um den secretKey zu generieren
    public static byte[] genSecretKeyBytes(int bits) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES"); // Erzeugt eine Instanz des KeyGenerator-Objekts für den AES-Algorithmus
            keyGenerator.init(bits); // Initialisiert den KeyGenerator mit der angegebenen Schlüsselgröße in Bits
            return keyGenerator.generateKey().getEncoded(); // Generiert den geheimen Schlüssel und gibt ihn als Byte-Array zurück
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); // Gibt eine Fehlermeldung aus, wenn der angeforderte Algorithmus nicht verfügbar ist
        }

        return null; // Gibt null zurück, wenn ein Fehler aufgetreten ist
    }


    /**
     * Verschlüsselt den geheimen Schlüssel mit dem öffentlichen Schlüssel.
     *
     * @param pubKey          Der öffentliche Schlüssel.
     * @param secretKeyBytes  Die geheimen Schlüsselbytes.
     * @return Das verschlüsselte Byte-Array des geheimen Schlüssels.
     */
    //encrypt methode um den secret key zu verschluesseln
    public static byte[] encrypt(PublicKey pubKey, byte[] secretKeyBytes) {
        try {
            Cipher cipher = Cipher.getInstance("RSA"); // Erzeugt eine Instanz des Cipher-Objekts für den RSA-Algorithmus
            cipher.init(Cipher.ENCRYPT_MODE, pubKey); // Initialisiert den Cipher im Verschlüsselungsmodus mit dem öffentlichen Schlüssel
            return cipher.doFinal(secretKeyBytes); // Verschlüsselt die geheimen Schlüsselbytes und gibt das verschlüsselte Byte-Array zurück
        } catch (Exception e) {
            e.printStackTrace(); // Gibt eine Fehlermeldung aus, wenn ein Fehler beim Verschlüsseln auftritt
        }
        return null; // Gibt null zurück, wenn ein Fehler aufgetreten ist
    }
}