Realtime Decision Engine
-

Command line parameters:
-
example: -Dprofile=test -Drule=rules.drl

Maven build commands
-
mvn clean package -Denv=event-stream
mvn clean package -Denv=history

Not run test cases
-
-Dmaven.test.skip=true
