package Praktikum4.Kerberos_Vorgabecode.kerberos;

/* Simulation einer Kerberos-Session mit Zugriff auf einen Fileserver
 /* KDC-Klasse
 */

import java.util.*;

public class KDC extends Object {

	private final long tenHoursInMillis = 36000000; // 10 Stunden in
	// Millisekunden

	private final long fiveMinutesInMillis = 300000; // 5 Minuten in
	// Millisekunden

	/* *********** Datenbank-Simulation **************************** */

	private String tgsName;  // TGS

	private long tgsKey; // K(TGS)

	private String user; // C

	private long userPasswordKey; // K(C)

	private String serverName; // S

	private long serverKey; // K(S)

	// Konstruktor
	public KDC(String name) {
		tgsName = name;
		// Eigenen Key für TGS erzeugen (streng geheim!!!)
		tgsKey = generateSimpleKey();
	}

	public String getName() {
		return tgsName;
	}

	/* *********** Initialisierungs-Methoden **************************** */

	public long serverRegistration(String sName) {
		/*
		 * Server in der Datenbank registrieren. Rückgabe: ein neuer geheimer
		 * Schlüssel für den Server
		 */
		serverName = sName;
		// Eigenen Key für Server erzeugen (streng geheim!!!)
		serverKey = generateSimpleKey();
		return serverKey;
	}

	public void userRegistration(String userName, char[] password) {
		/* User registrieren --> Eintrag des Usernamens in die Benutzerdatenbank */
		user = userName;
		userPasswordKey = generateSimpleKeyForPassword(password);

		System.out.println("Principal: " + user);
		System.out.println("Password-Key: " + userPasswordKey);
	}

	/* *********** AS-Modul: TGS - Ticketanfrage **************************** */

	public TicketResponse requestTGSTicket(String userName, String tgsServerName, long nonce) {
		/* Anforderung eines TGS-Tickets bearbeiten. Rückgabe: TicketResponse für die Anfrage */
		long tgsSessionKey; // K(C,TGS)

		TicketResponse tgsTicketResp = null;
		Ticket tgsTicket = null;
		long currentTime = 0;

		// TGS-Antwort zusammenbauen
		if (userName.equals(user) && // Usernamen und Userpasswort in der
		// Datenbank suchen!
		tgsServerName.equals(tgsName)) {
			// OK, neuen Session Key für Client und TGS generieren
			tgsSessionKey = generateSimpleKey();
			currentTime = (new Date()).getTime(); // Anzahl mSek. seit
			// 1.1.1970

			// Zuerst TGS-Ticket basteln ...
			tgsTicket = new Ticket(user, tgsName, currentTime, currentTime + tenHoursInMillis, tgsSessionKey);

			// ... dann verschlüsseln ...
			tgsTicket.encrypt(tgsKey);

			// ... dann Antwort erzeugen
			tgsTicketResp = new TicketResponse(tgsSessionKey, nonce, tgsTicket);

			// ... und verschlüsseln
			tgsTicketResp.encrypt(userPasswordKey);
		}
		return tgsTicketResp;
	}

	/*
	 * *********** TGS-Modul: Server - Ticketanfrage
	 * ****************************
	 */

	public TicketResponse requestServerTicket(Ticket tgsTicket, Auth tgsAuth, String serverName, long nonce) throws Exception {
		/* ToDo */
		//Aufgabe: Anforderung eines Server-Tickets bearbeiten.

		//sessionKey generieren
		long srvSessionKey = generateSimpleKey();
		TicketResponse srvTicketResponse = null;
		Ticket srvTicket;
		if(tgsTicket.decrypt(tgsKey)){
			if (timeValid(tgsTicket.getStartTime(), tgsTicket.getEndTime())) {
				if (tgsAuth.decrypt(tgsTicket.getSessionKey())&&timeFresh(tgsAuth.getCurrentTime())&&tgsTicket.getClientName().equals(tgsAuth.getClientName())) {
					tgsAuth.print();
					//neues Ticket generieren mit Clientname vom tgsTicket, servername, start und end time des Tickets sowie den vorher generierten SessionKey
					srvTicket = new Ticket(tgsTicket.getClientName(),serverName,tgsTicket.getStartTime(),tgsTicket.getEndTime(),srvSessionKey);
					srvTicket.encrypt(getServerKey(serverName));

					//ticket response mit sessionKey, nonce sowie den vorher generierten srvTicket generieren
					srvTicketResponse = new TicketResponse(srvSessionKey,nonce,srvTicket);
					srvTicketResponse.encrypt(tgsTicket.getSessionKey());
				} else {
					tgsAuth.printError("TGSAuth cannot be decrypted with tgsTicket Session key!");
				}
			}else {
				throw new Exception("------ KDC - tgsTicket Time is expired!");
			}
		}else{
			tgsTicket.printError("tgsTicket cannot be decrypted with tgsKey!");
		}

		return srvTicketResponse;
	}

	/* *********** Hilfsmethoden **************************** */

	private long getServerKey(String sName) {
		// Liefert den zugehörigen Serverkey für den Servernamen zurück
		// Wenn der Servername nicht bekannt, wird -1 zurückgegeben
		if (sName.equalsIgnoreCase(serverName)) {
			System.out.println("Serverkey ok");
			return serverKey;
		} else {
			System.out.println("Serverkey unbekannt!!!!");
			return -1;
		}
	}

	private long generateSimpleKeyForPassword(char[] pw) {
		// Liefert einen Schlüssel für ein Passwort zurück, hier simuliert als
		// long-Wert
		long pwKey = 0;
		for (int i = 0; i < pw.length; i++) {
			pwKey = pwKey + pw[i];
		}
		return pwKey;
	}

	private long generateSimpleKey() {
		// Liefert einen neuen geheimen Schlüssel, hier nur simuliert als
		// long-Wert
		long sKey = (long) (100000000 * Math.random());
		return sKey;
	}

	boolean timeValid(long lowerBound, long upperBound) {
		long currentTime = (new Date()).getTime(); // Anzahl mSek. seit
		// 1.1.1970
		if (currentTime >= lowerBound && currentTime <= upperBound) {
			return true;
		} else {
			System.out.println(
					"-------- Time not valid: " + currentTime + " not in (" + lowerBound + "," + upperBound + ")!");
			return false;
		}
	}

	boolean timeFresh(long testTime) {
		// Wenn die übergebene Zeit nicht mehr als 5 Minuten von der aktuellen
		// Zeit abweicht,
		// wird true zurückgegeben
		long currentTime = (new Date()).getTime(); // Anzahl mSek. seit
		// 1.1.1970
		if (Math.abs(currentTime - testTime) < fiveMinutesInMillis) {
			return true;
		} else {
			System.out.println("-------- Time not fresh: " + currentTime + " is current, " + testTime + " is old!");
			return false;
		}
	}
}
