<!DOCTYPE html>
<html>
<head>
  <title>Konzert - Kartenbestellung</title>
  <link href="karten.css" type="text/css" rel="stylesheet">
  <meta http-equiv="content-type" content="text/html" charset="utf-8">  
  <meta name="viewport" content="width=device-width, initial-scale=1">    
</head>

<body>
<?php
require_once ("functions.inc.php");

try {
	$db_name = "karten.db";
	
	// Formulardaten ermitteln
	$Kartenanzahl = $_POST['Kartenanzahl'];
	$Name = $_POST['Name'];
	$Passwort = $_POST['Passwort'];
	$Mail = $_POST['Mail'];
	
	// Neue Bestellung in Datenbank speichern
   $db = new SQLite3 ( $db_name );
   // Datensatz einfügen
   $sql = sprintf ( 
         "INSERT INTO bestellung (anzahl, name, passwort, mail) 
                        VALUES ('%s', '%s', '%s', '%s')\n",
         $Kartenanzahl, $Name, $Passwort, $Mail );
   // echo $sql;
   if (!$db->exec ($sql)) {
      throw new Exception($db->lastErrorMsg());
   }
	
	echo "<p>Die folgende Bestellung wurde verbucht, vielen Dank!</p>\n";
   showData($Kartenanzahl, $Name, $Passwort, $Mail);
   echo "<p><a href=\"karten.html\">Neue Bestellung</a></p>";   
               
   $db->close();
} catch (Exception $ex) {
    echo "Datenbank-Fehler! " . $ex->getMessage();
}               
?>
</body>
</html>
