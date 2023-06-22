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
	
	
	// Formulardaten ermitteln
	$Kartenanzahl = $_POST['Kartenanzahl'];
	$Name = $_POST['Name'];
	$Passwort = $_POST['Passwort'];
	$Mail = $_POST['Mail'];
	
	
	//connect to PDO
	$db_name = "karten.db";
	$db = new PDO('sqlite:' . $db_name);
	
	
   // Datensatz einfügen
   
   $sql = sprintf ( 
         "INSERT INTO bestellung (anzahl, name, passwort, mail) 
                        VALUES (?, ?, ?, ?)\n");
   //prepared statement damit user und sql query seperat sind und der SQL command so sicher ausgeführt werden kann
   // User daten sind so nicht direkt im SQL statement enthalten da dieser erst später hinzugefügt wird
   $stmt = $db->prepare($sql);
   
   
   //hier werden User Daten zur ausgeführten SQL query hinzugefügt
   $stmt->execute([$Kartenanzahl, $Name, $Passwort, $Mail]);

   
   // echo $sql;
   if (!$db->exec ($sql)) {
      throw new Exception($db->lastErrorMsg());
   }
	
	echo "<p>Die folgende Bestellung wurde verbucht, vielen Dank!</p>\n";
   showData($Kartenanzahl, $Name, $Passwort, $Mail);
   echo "<p><a href=\"karten.html\">Neue Bestellung</a></p>";   
               
   return $db;
} catch (Exception $ex) {
    echo "Datenbank-Fehler! " . $ex->getMessage();
}               
?>
</body>
</html>
