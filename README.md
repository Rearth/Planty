# Planty
Planty ist eine Java-Spring Webanwendung welche es vereinfacht Pflanzen regelmäßig zu gießen.

## Anwendung Starten
Gestartet wird die Anwendung indem die Datei 'planty-0.0.1-SNAPSHOT.jar' gestartet wird, welche sich im 'target' Ordner befindet.
Zum starten der .jar datei einfach den Befehl 'java -jar planty-0.0.1-SNAPSHOT.jar' benutzen.
Als Build-System wird Maven verwendet, gebaut wird die Anwendung mit einem simplen 'mvn clean compile package'

## Tests
Tests werden auch von maven gestartet, sie können mit dem Befehl 'mvn test' ausgeführt werden.
Die Test-Coverage kann dem beiliegenden Bild 'test-coverage.png' entnommen werden.
Alle Tests wurden als Integrationstests realisiert, welche verschiedene Schichten der Anwendung abhängig vom Test simulieren,

## Datenbank und Webserver
In der 'planty-0.0.1-SNAPSHOT.jar' Datei sind sowohl die H2O In-Memory Datenbank sowie der Webserver enthalten.
Die Anwendung selbst kann dann unter der URL 'http://localhost:8081/' gefunden werden.

## Sensor-Simulator
Die Anwendung wertet Daten von echten Sensoren aus, welche sich im selben Netzwerk befinden. Für diese Sensoren wurde auch ein simulator in go geschrieben. Dieser befindet sich im 'sensor-sim' Ordner
Gestartet wird dieses mit dem Befehl und den Paramtern 'go run sensorDataMocker.go WasserVerlustRate StartWasserLevel SensorName PlanzenID ServerURL'
Beispielparamter sind 'go run sensorDataMocker.go 0.025 0.9 gomocker 1753 http://localhost:8081'
Die PflanzenID kann über die Webanwendung gefunden werden, indem ein Sensor dort ausgewählt wird, und dann der Knopf im Kontextmenu Get Configuration gedrückt wird
Falls kein Go vorhanden ist kann stattdessen auch die bereits komplilierte Datei datamocker verwendet werden.
Der Befehl lautet dann wie folgt:
'./datamocker WasserVerlustRate StartWasserLevel SensorName PlanzenID ServerURL'

## Nutzen der Anwendung
Zu Beginn der Nutzung sollte zuerst eine Pflanze erstellt werden, dies kann über den Knopf 'Add new Plant' getan werden. Zu dieser Pflanze sollte dann noch ein Sensor erstellt werden, und für diesen Sensor im Anschluss noch ein Simulator gestartet werden.
Damit sollten auf der Weboberfläche die empfangenen Daten angezeigt werden, und sobald mehr als vierzig Datensätze vorhanden sind werden diese Analysiert.

## Code-Übersicht
Tests befinden sich im order 'src/test', während der Anwendungscode im Verzeichnis 'src/main/' zu findet ist.
Im 'src/main/resources/' Ordner befinden sich Web-Datein, statische Dateien wie CSS und JS sind dabei im static Ordner, und Thymeleaf templates befinden sich im templates Ordner.

### controller
Die Anwendung enthält drei Controller, welche HTTP-Anfragen annehmen. Der APIController wird aktuell nur genutzt um Sensordaten entgegenzunehmen und diese einzuspeichern,
Der ImageController ist dafür da, die für Pflanzen gespeicherten Bilder bereitzustellen. Diese werden im Ordner upload-dir gespeichert,
Der PlantController ist der Teil mit dem der nutzer selbst über eine Webseite interagiert. Über die Webseite werden alle Daten angezeigt, und jegliche Konfiguration der Anwendung wird dort vorgenommen.

### entities
Alle im entities Ordner befindlichen Dateien stellen eine Entität bzw. Tabelle in der Datenbank dar.

### repositories
Ein Repository in Spring wird genutzt um eine Verbindung zur Datenbank darzustellen. Es werden keine manuellen SQL-Statements benötigt, spring kümmert sich um das speichern und abfragen aller Daten in der Datenbank

### storage
Alle Klassen im storage Ordner dienen der Speicherung von Bildern der Pflanzen. Aktuell werden die Bilder auf dem Dateisystem gespeichert.

## Sonstiges
Anders als ursprünglich im Anforderungsdokument beschrieben wird nun doch ein Webserver statt JavaFX genutzt. Da das Projekt während eines Auslandssemesters durchgeführt wurde waren leider nur Ubuntu-Computer mit beschränktem Zugriff verfügbar, auf denen JavaFX verfügbar war. Deshalb wurde stattdessen ein Webserver mithilfe von Spring genutzt. Mehr dazu im Designdokument.
Krankheitsbedingt sind die Dokumentation und Tests etwas kürzer geraten als geplant, da ich in der letzten Woche kaum in der Lage war einen Computer zu benutzen.
Dennoch sind Tests vorhanden und der Code ist kurz dokumentiert. Allerdings ist die Logik auch immer relativ simpel gehalten und es wurden immer sprechende Namen genutzt, weshalb ein verstehen des Codes nicht allzu schwer sein sollte.
