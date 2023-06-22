package Praktikum2.main.java.Aufgabe3;


public class TripleDES {
    private byte[] keyPart1;
    private byte[] keyPart2;
    private byte[] keyPart3;
    DES one;
    DES two;
    DES three;

    /* Constructor */
    public TripleDES (byte[] keyPart1, byte[] keyPart2, byte[] keyPart3) {
        //todo
        /* key-length: 24 Byte, each keyPart must be of length 8 Byte */
        if (keyPart1.length != 8 || keyPart2.length != 8 || keyPart3.length != 8) {
            throw new IllegalArgumentException("Each keyPart must be of length 8 Byte");
        }

        this.keyPart1 = keyPart1;
        this.keyPart2 = keyPart2;
        this.keyPart3 = keyPart3;
        one = new DES(keyPart1);
        two = new DES(keyPart2);
        three = new DES(keyPart3);

    }

    /* encrypt plaintext block  */
    public byte[] encryptBytes (byte[] plaintextBytes){
        //todo
        if (plaintextBytes.length < 8) {
            throw new IllegalArgumentException("Plaintext block must be at least 8 bytes long");
        }



        byte[] byteAry = new byte[8];


        one.encrypt(plaintextBytes,0,byteAry,0);
        //System.out.println(byteArraytoHexString(first));
        two.decrypt(byteAry,0,byteAry,0);
        //System.out.println(byteArraytoHexString(second));
        three.encrypt(byteAry,0,byteAry,0);
        //System.out.println(byteArraytoHexString(third));

        return byteAry;
    }

    /* decrypt plaintext block  */
    public byte[] decryptBytes (byte[] chiffreBytes){
        //todo
        if (chiffreBytes.length < 8) {
            throw new IllegalArgumentException("chiffreBytes block must be at least 8 bytes long");
        }

        byte[] byteAry = new byte[8];

        three.decrypt(chiffreBytes,0,byteAry,0);
        //System.out.println("First Aufgabe3.DES: " + byteArraytoHexString(first));
        two.encrypt(byteAry,0,byteAry,0);
        //System.out.println("Second Aufgabe3.DES: " + byteArraytoHexString(second));
        one.decrypt(byteAry,0,byteAry,0);
        //System.out.println("Third Aufgabe3.DES: " + byteArraytoHexString(third));

        return byteAry;
    }

    private String byteArraytoHexString(byte[] byteArray) {
        String ret = "";
        for (int i = 0; i < byteArray.length; ++i) {
            ret = ret + String.format("%02x", byteArray[i]) + " ";
        }
        return ret;
    }

    public static void main(String[] args) {
        /* Testcode */
        TripleDES cipher = new TripleDES("qwertzui".getBytes(), "asdfghjk".getBytes(), "yxcvbnm,".getBytes());

        byte[] plain = "12345678".getBytes();
        byte[] chiffre = cipher.encryptBytes(plain);
        System.out.println(" Encrypted: " +  cipher.byteArraytoHexString(plain) + " to: " + cipher.byteArraytoHexString(chiffre));

        byte[] plainNew = cipher.decryptBytes(chiffre);
        System.out.println(" Decrypted: " + cipher.byteArraytoHexString(plainNew) );

        if (java.util.Arrays.equals(plain, plainNew)) {
            System.out.println(" ---> Erfolg!");
        } else {
            System.out.println(" ---> Hat leider noch nicht funktioniert ...!");
        }
    }
}
