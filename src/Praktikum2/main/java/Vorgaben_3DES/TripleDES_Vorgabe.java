package Praktikum2.main.java.Vorgaben_3DES;
import java.io.*;

public class TripleDES {
/* ToDo */

   /* Constructor */
   public TripleDES (byte[] keyPart1, byte[] keyPart2, byte[] keyPart3) {
      /* key-length: 24 Byte, each keyPart must be of length 8 Byte */

   /* ToDo */
   }
   
   /* encrypt plaintext block  */
   public byte[] encryptBytes (byte[] plaintextBytes){

   /* ToDo */
   
   }
   
  /* decrypt plaintext block  */
   public byte[] decryptBytes (byte[] chiffreBytes){

   /* ToDo */
   
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
