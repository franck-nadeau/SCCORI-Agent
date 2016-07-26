# SCCORI-Agent

SCCORI-Agent is a web-controlled intelligent agents which interact with the Supply Chain Collaboration On-line Research Institute (http://www.sccori.com/sim/) supply chain simulation.

This project was originally released in 2007, and has since been moved from sourceforge to here. 

https://sourceforge.net/projects/sccori-agent/

# Compiling and installing the software

## Download JBoss 4.X (which is what we used to make the original software, and we have not upgraded it since).
 http://sourceforge.net/projects/jboss/files/JBoss/JBoss-4.0.3SP1/jboss-4.0.3SP1.zip/download

## unzip jboss-4.0.3SP1.zip

## Download SCCORI agent's code and unzip it, and compile.
       $ cd sccoriAgent/05_Server/
       $ ant jar
       $ cd ../04_Client
       $ ant deploy

   You'll need Java and ant. Not sure how comfortable you are with these libraries, but there is lots of info on both on the internet.

## Copy the war file to your JBoss folder (default/deploy)
       $ cp sccoriAgentClient.war ~/Downloads/jboss-4.0.3SP1/server/default/deploy/

## Start JBoss
       $ cd jboss-4.0.3SP1/bin
       $ ./run.sh (./run.bat in window$)

## Point your browser to http://localhost:8080/sccoriAgentClient/Client
