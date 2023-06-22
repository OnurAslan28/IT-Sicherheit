<?php
/* Eingebundene Hilfsfunktionen */

function showData ($Kartenanzahl, $Name, $Passwort, $Mail) {
   /* Übergebene Daten anzeigen */
   printf ("<p>&lt;%d Karten für %s (%s) - Mail: %s&gt;</p>\n", 
            htmlentities($Kartenanzahl), htmlentities($Name), htmlentities($Passwort), htmlentities($Mail));
}
?>
