#!/bin/sh
rm -rf out && mkdir out
javac -d out src/rms/*.java || exit 1
printf 'Main-Class: rms.MainGUI\n' > manifest.txt
jar cfm RouteManagementSystem.jar manifest.txt -C out rms
echo "Build done."
echo "GUI:     java -jar RouteManagementSystem.jar"
echo "Console: java -cp RouteManagementSystem.jar rms.Main"
