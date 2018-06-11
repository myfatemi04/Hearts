@echo off
@javac hearts/client/*.java
@javac hearts/*.java
@javac hearts/client/swing/*.java
start "" javaw hearts/client/CClient