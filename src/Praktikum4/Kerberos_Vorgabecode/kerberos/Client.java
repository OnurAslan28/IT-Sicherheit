package Praktikum4.Kerberos_Vorgabecode.kerberos;

/* Simulation einer Kerberos-Session mit Zugriff auf einen Fileserver
 /* Client-Klasse
 */

import java.util.*;

public class Client extends Object {

	private KDC myKDC; // Konstruktor-Parameter

	private String currentUser; // Speicherung bei Login nötig
	private Ticket tgsTicket = null; // Speicherung bei Login nötig
	private long tgsSessionKey; // K(C,TGS) // Speicherung bei Login nötig

	// Konstruktor
	public Client(KDC kdc) {
		myKDC = kdc;
	}

	public boolean login(String userName, char[] password) {
		/* ToDo */
		//TGS-Ticket für den übergebenen Benutzer vom KDC (AS) holen (TGS-Servername: myTGS) und zusammen mit dem TGS-Sessionkey und dem UserNamen speichern.

		this.currentUser = userName;

		//TGSticket beim myKDC mit userName, myKDC name und einen Nonce wert anfordern.
		TicketResponse ticketResponse = myKDC.requestTGSTicket(userName,myKDC.getName(),generateNonce());
		//prüfen ob passwort stimmt
		if (!ticketResponse.decrypt(this.generateSimpleKeyFromPassword(password))){
			ticketResponse.printError("cannot decrypted ticketresponse with password");
			return false;
		}
		ticketResponse.print();
		this.tgsSessionKey = ticketResponse.getSessionKey();
		this.tgsTicket = ticketResponse.getResponseTicket();

		return true;

	}

	public boolean showFile(Server fileServer, String filePath) throws Exception {

		/* ToDo */
		//Aufgabe: Serverticket vom KDC (TGS) holen und „showFile“-Service beim übergebenen Fileserver anfordern.


		Auth authForServerTicket = new Auth(currentUser,(new Date().getTime()));
		authForServerTicket.encrypt(tgsSessionKey);

		//Server ticket beim KDC mit tgsTicket, authServerTicket, namen vom fileserver und wieder einem nonce wert anfordern
		TicketResponse ticketResponse = myKDC.requestServerTicket(tgsTicket,authForServerTicket,fileServer.getName(),generateNonce());
		if (!ticketResponse.decrypt(tgsSessionKey)){
			//System.out.println("------Client----- cannot decrypt ticketResponse with Sessionkey!");
			ticketResponse.printError("cannot decrypt ticketResponse with Sessionkey!");
			return false;
		}
		ticketResponse.print();

		//auth für service erstellen und encrypteten session key der ticketResponse vom Server als key setzen
		Auth authForService = new Auth(currentUser,(new Date().getTime()));
		authForService.encrypt(ticketResponse.getSessionKey());
		//authForService.print();

		//request an den fileServer schicken mit dem responseTicket dem AUTH für Service dem "showFile" command sowie den path vom zum lesenden file
		return fileServer.requestService(ticketResponse.getResponseTicket(),authForService,"showFile",filePath);

	}

	/* *********** Hilfsmethoden **************************** */

	private long generateSimpleKeyFromPassword(char[] passwd) {
		// Liefert einen eindeutig aus dem Passwort abgeleiteten Schlüssel
		// zurück, hier simuliert als long-Wert
		long pwKey = 0;
		if (passwd != null) {
			for (int i = 0; i < passwd.length; i++) {
				pwKey = pwKey + passwd[i];
			}
		}
		return pwKey;
	}

	private long generateNonce() {
		// Liefert einen neuen Zufallswert
		long rand = (long) (100000000 * Math.random());
		return rand;
	}
}
