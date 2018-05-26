@echo off
@javac hearts/client/*.java
@javac hearts/*.java
start "" javaw hearts/client/CClient