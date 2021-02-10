# Retro
A Java program with which "retroify" Java bytecode to an older Java version than the compilation version

### Launch project (Command line version)
To launch the project easily :
```Java -jar command line <version>```
- To retroify a file use the "-help" command.
- The result of retro-ified files is displayed in the "RetroTarget" directory created at the root of the project.

### Launch project (Web version)
To launch the project easily :
```mvn clean compile quarkus:dev```
- Please note that the "force" button is intentionally disabled as the rewrite feature is not yet implemented.

#### How to use website 
- To launch a retro-ification, please check the cases that match with the features that you want to detect in your bytecode.
- The result of the "infos" option or the "help" button is displayed under the "Result" tag and the resulting files are placed in the "RetroTarget" folder which is placed at the root of the file tree produced by extracting the given archive.

### Developed with
- [JDK 13](https://www.oracle.com/java/technologies/javase-downloads.html)
- [Quarkus](https://quarkus.io/)
- [Maven](https://maven.apache.org/)


*Developed by Pierre-Jean Besnard & Louis Billaut*
