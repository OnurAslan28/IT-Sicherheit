package Praktikum3;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
/**
 * Die Klasse RSF ermöglicht das Entschlüsseln von Dateien mit einem privaten RSA-Schlüssel.
 * Sie erwartet den privaten Schlüssel, den öffentlichen Schlüssel, die zu importierende Datei und die
 * Ausgabedatei als Eingabeparameter.
 */
public class RSF {
    /**
     * Die Hauptmethode der Klasse RSF.
     *
     * @param args Die Eingabeparameter: PrivateKeyFile, PublicKeyFile, ImportFile(SSF), ExportFile.
     * @throws NoSuchAlgorithmException Wenn der angeforderte Algorithmus nicht verfügbar ist.
     * @throws IOException              Wenn ein Fehler beim Lesen oder Schreiben der Dateien auftritt.
     * @throws InvalidKeySpecException  Wenn die Schlüsselspezifikation ungültig ist.
     */
    public static <Key> void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        // Überprüfen, ob die Anzahl der Eingabeparameter korrekt ist
        if (args.length != 4) {
            System.err.println("Invalid Usage: RSF PrivateKeyFile PublicKeyFile ImportFile(SSF) ExportFile");
        } else {
            // a) Private Key aus Datei einlesen
            byte[] privKeyByte = readKeyFromFile(args[0]);
            if (privKeyByte == null) {
                System.err.println("Privater Schluessel konnte nicht eingelesen werden");
                return;
            }
            // b) Public Key aus Datei einlesen
            byte[] pubKeyByte = readKeyFromFile(args[1]);
            if (pubKeyByte == null) {
                System.err.println("Oeffentlicher Schluessel konnte nicht eingelesen werden");
                return;
            }

            // Private und Public Key aus den Byte-Arrays erstellen
            KeyFactory keyFactoryPrivate = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactoryPrivate.generatePrivate(new PKCS8EncodedKeySpec(privKeyByte));

            KeyFactory keyFactoryPublic = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactoryPublic.generatePublic(new X509EncodedKeySpec(pubKeyByte));

            try (DataInputStream inputStream = new DataInputStream(new FileInputStream(args[2]))) {
                // c) Verschlüsselten geheimen Schlüssel, Signatur und algorithmische Parameter einlesen
                byte[] secretKeyBytesEnc = new byte[inputStream.readInt()];
                inputStream.read(secretKeyBytesEnc);
                byte[] signature = new byte[inputStream.readInt()];
                inputStream.read(signature);
                byte[] encodedAP = new byte[inputStream.readInt()];
                inputStream.read(encodedAP);

                try (FileOutputStream fileOutputStream = new FileOutputStream(args[3])) {
                    // Algorithmische Parameter für den AES-Algorithmus setzen
                    AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance("AES");
                    algorithmParameters.init(encodedAP);

                    // Geheimen Schlüssel entschlüsseln
                    byte[] secretKeyBytesDec = decrypt(privateKey, secretKeyBytesEnc);

                    // AES Cipher initialisieren
                    Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
                    SecretKeySpec skspec = new SecretKeySpec(secretKeyBytesDec, "AES");
                    cipher.init(Cipher.DECRYPT_MODE, skspec, algorithmParameters);

                    // Daten entschlüsseln und in die Ausgabedatei schreiben
                    fileOutputStream.write(cipher.update(readFile(args[2])));
                    fileOutputStream.write(cipher.doFinal());

                    // d) Signatur verifizieren
                    if (verfiySig(publicKey, secretKeyBytesDec, signature)) {
                        System.out.println("Signatur erfolgreich verifiziert");
                    } else {
                        System.err.println("Signatur nicht erfolgreich verifiziert");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Liest die Daten aus einer Datei.
     *
     * @param filePath Der Pfad zur Datei.
     * @return Ein Byte-Array mit den gelesenen Daten.
     */
    public static byte[] readFile(String filePath)  {

        try(DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)))) {
            // Die anderen Informationen wie Signature etc. überspringen
            inputStream.skipBytes(inputStream.readInt()); // Anzahl der Bytes für den Secret Key überspringen
            inputStream.skipBytes(inputStream.readInt()); // Anzahl der Bytes für die Signatur überspringen
            inputStream.skipBytes(inputStream.readInt()); // Anzahl der Bytes für die algorithmischen Parameter überspringen

            return inputStream.readAllBytes(); // Restliche Bytes als gelesene Daten zurückgeben

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Falls ein Fehler auftritt, wird null zurückgegeben
    }

    /**
     * Liest den Schlüssel aus einer Datei.
     *
     * @param keyFilePath Der Pfad zur Schlüsseldatei.
     * @return Ein Byte-Array mit den Schlüsseldaten.
     * @throws IOException Wenn ein Fehler beim Lesen der Datei auftritt.
     */
    public static byte[] readKeyFromFile(String keyFilePath) {

        byte[] keyBytes;

        try(DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(keyFilePath)))) {
            inputStream.skipBytes(inputStream.readInt()); // Anzahl der Bytes für den Schlüsseltyp überspringen

            keyBytes = inputStream.readNBytes(inputStream.readInt()); // Die angegebene Anzahl von Bytes als Schlüsseldaten lesen
        } catch (IOException e) {
            throw new RuntimeException(); // Bei einem Fehler wird eine Laufzeitfehler-Ausnahme ausgelöst
        }
        return keyBytes; // Die gelesenen Schlüsseldaten werden zurückgegeben
    }

    /**
     * Entschlüsselt den geheimen Schlüssel mit dem privaten Schlüssel.
     *
     * @param privKey         Der private Schlüssel.
     * @param secretKeyBytes Die verschlüsselten geheimen Schlüsselbytes.
     * @return Das entschlüsselte Byte-Array des geheimen Schlüssels.
     */
    public static byte[] decrypt(PrivateKey privKey, byte[] secretKeyBytes) {
        try {
            Cipher rsa = Cipher.getInstance("RSA"); // Erzeugen einer RSA-Cipher-Instanz
            rsa.init(Cipher.DECRYPT_MODE, privKey); // Initialisieren der Cipher-Instanz mit dem privaten Schlüssel im Entschlüsselungsmodus
            return rsa.doFinal(secretKeyBytes); // Entschlüsseln der geheimen Schlüsselbytes
        } catch (Exception e) {
            e.printStackTrace(); // Bei einer Ausnahme wird die Ausnahme verfolgt und gedruckt
        }
        return null; // Bei einem Fehler wird `null` zurückgegeben
    }

    /**
     * Verifiziert die Signatur mit dem öffentlichen Schlüssel.
     *
     * @param pubKey Der öffentliche Schlüssel.
     * @param buf    Die zu überprüfenden Daten.
     * @param sig    Die Signatur der Daten.
     * @return {@code true}, wenn die Signatur gültig ist, andernfalls {@code false}.
     */
    public static boolean verfiySig(PublicKey pubKey, byte[] buf, byte[] sig) {
        try {
            Signature rsa = Signature.getInstance("SHA512withRSA"); // Erzeugen einer RSA-Signatur-Instanz mit SHA-512
            rsa.initVerify(pubKey); // Initialisieren der Signatur-Instanz mit dem öffentlichen Schlüssel im Überprüfungsmodus
            rsa.update(buf); // Aktualisieren der zu überprüfenden Daten
            return rsa.verify(sig); // Überprüfen der Signatur der Daten
        } catch (Exception e) {
            e.printStackTrace(); // Bei einer Ausnahme wird die Ausnahme verfolgt und gedruckt
        }
        return false; // Bei einem Fehler wird `false` zurückgegeben
    }
}
