<!DOCTYPE html>
<html>
<head>
  <title>Konzert - Kartenbestellung</title>
  <link href="karten.css" type="text/css" rel="stylesheet">
  <meta name="viewport" content="width=device-width, initial-scale=1">    
</head>

<body>
<?php
require_once ("functions.inc.php");

try {
	$db_name = "karten.db";
   $db = new SQLite3 ( $db_name );
   
	// Alle Bestellungen ausgeben
   echo "<h2>Alle Kartenbestellungen in der Datenbank:</h2>";
   $sql = "SELECT * from bestellung";
   
   $Bestellungen = $db->query ( $sql );
   if (!$Bestellungen) {
      printf("<h1>Die Tabelle \"bestellung\" existiert nicht!</h1>\n");
   } else {
      $i = 0;
      while ( $bestellung = $Bestellungen->fetchArray () ) {
         $i++;
         showData($bestellung['anzahl'], $bestellung['name'], $bestellung['passwort'], $bestellung['mail']);
      }
      printf("<br /><p>%d Bestellungen wurden insgesamt in der Datenbank gefunden!</p>\n", $i);
   }
   $db->close();
} catch (Exception $ex) {
    echo "Datenbank-Fehler! " . $ex->getMessage();
}               
?>
</body>
</html>
