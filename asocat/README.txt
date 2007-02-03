ASOCAT... Another Set Of Custom Ant Tasks
=========================================

While developing for the eXist Native XML database (http://www.exist-db.org) 
I needed some additonal Ant Tasks. Since I could not find them on the internet, 
I decided to write them myself.....

What can you expect:

- unsign jar files
- normalize jar files (so they can be signed and packed using Pack200)
- Pack200 tasks

and

- SVN tasks, e.g. to determine revision.

You'll need Java5+ to compile it, the tasks are developed for Apache 
Ant 1.6.5. The Tasks will be used for Java1.4 too, so additional availability 
tests of Classes need to be performed.

As expected, an Ant build.xml file is provided. The code has been developped 
with Netbeans 5.0 (nbproject folder is provided too).


How-To's
========

Compile project:
ant jar

Clean project:
ant clean

Execute test scripts:
ant -f data\build.xml