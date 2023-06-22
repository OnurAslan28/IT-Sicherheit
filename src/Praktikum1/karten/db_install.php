<?php
$db_name = "karten.db";
try {
	$db = new SQLite3 ( $db_name );
	
	// Person
	$sql = "CREATE TABLE bestellung (
               id INTEGER PRIMARY KEY, 
               anzahl,               
               name, 
               passwort, 
               mail )";
	if ($db->exec ( $sql )) {
		echo "Tabelle bestellung angelegt<br>";
	} else {
		echo "Fehler bei Erzeugung von Tabelle bestellung";
	}
	$db->close ();
} catch ( Exception $ex ) {
	echo "Fehler ". $ex->getMessage ();
}
?>

