# ASOCAT... Another Set Of Custom Ant Tasks

While developing for the [eXist Native XML database](http://www.exist-db.org) 
I needed some additonal Ant Tasks. Since I could not find them on the internet, 
I decided to write them myself.....

What can you expect:

- unsign jar files
- normalize jar files (so they can be signed and packed using Pack200)
- Pack200 tasks

and

- SVN tasks, e.g. to determine revision.

You'll need Java 5+ to compile it, the tasks are developed for Apache 
Ant 1.6.5. The Tasks will be used for Java 1.4 too, so additional availability 
tests of Classes need to be performed.

The code can be built using Apache Maven.

## How-To's
Compile project:
```
mvn package
```

Clean project:
```
mvn clean
```

Execute test scripts:
```
mvn test
```
