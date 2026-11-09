@echo off
if exist out rmdir /s /q out
mkdir out
javac -d out src\rms\*.java
echo Main-Class: rms.MainGUI> manifest.txt
jar cfm RouteManagementSystem.jar manifest.txt -C out rms
echo Build done.
echo GUI:     java -jar RouteManagementSystem.jar
echo Console: java -cp RouteManagementSystem.jar rms.Main
