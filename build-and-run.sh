#!/bin/bash		
mvn package
pushd ~/java/spigot
java -Xmx1000m -jar Spigot/Spigot-Server/target/spigot-1.8-R0.1-SNAPSHOT.jar
popd
