@echo off
@javac hearts/*.java
@javac hearts/server/*.java
@javac hearts/server/swing/*.java
start "" javaw hearts/server/Server