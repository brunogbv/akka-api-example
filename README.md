# Scala Skeleton | Fraud Detection

scala-akka-skeleton

## Introduction

Docker container with a skeleton project for a scala application using akka.

### Getting Started
--- --- ---
Install the following:

1. [IntelliJ IDEA](https://www.jetbrains.com/idea/) - IDE used to write and run *.scala* programs
    * Scala plugin (create projects using version 2.11.7)
    * SBT plugin (create projects using version 0.13.13
2. [Java SDK 8](http://www.oracle.com/technetwork/pt/java/javase/downloads/jdk8-downloads-2133151.html)
    * Ensure JAVA_HOME environment variable is set and points to your JDK installation:
Go into **Settings** and click on **System**. Then click **About** > **System info** > **Advanced system settings** > **Environment Variables**.
    * Add a new System Variable:

       Variable name | Variable value
       ------------ | -------------
       JAVA_HOME | C:\Program Files\Java\jdk1.x.x

3. [Apache Maven](https://maven.apache.org/index.html)
    * Download and unzip *apache-maven-3.5.0-bin.zip* to *C:\Program Files*. Add the bin directory of the created directory *apache-maven-3.5.0* to the PATH environment variable:

       Variable name | Variable value
       ------------ | -------------
       M2 | C:\Program Files\apache-maven-3.5.0\bin

    * On  **Environment Variables**, select variable **Path**, edit and create a new with the path *C:\Program Files\apache-maven-3.5.0\bin*
    * Verify the installation:
        ```sh
        $ mvn -v
        ```

4. [Microsoft JDBC 4.0](https://www.microsoft.com/en-us/download/details.aspx?id=54629)
    * Download and unzip Microsoft JBDC 4.0. Copy the *sqljdbc_4.0* folder to *C:\Program Files*. On Windows command line, execute the following:
        ```sh
        $ mvn install:install-file -Dfile=C:/Program Files/sqljdbc_4.0/sqljdbc4.jar -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc4 -Dversion=4.0.2206.100 -Dpackaging=jar
        ```

5. Config files
    * Open **This PC** on File Explorer. On the top bar, click on **Computer** > **Map Network Drive**. Select drive **Z:** and folder _\\RJ_DEV0172\confs_. Now the config files used for all applications can be accessed.

### Docker
--- --- ---

* To build the executable for the application:

    ```sh
    $ sbt compile
    $ sbt pack
    ```


* To build the image:

    ```sh
    $ docker build -t scala-akka-skeleton
    ```

* To run the container:

    ```sh
    $ docker run -d --name scala-akka-skeleton -v /root/app-conf/:/root/app-conf/ scala-akka-skeleton:latest
    ```

### License

--- --- ---

The License is attached to any code on your repository. This is essential and obligatory. Be careful when choosing your license. Also, it's good practice to refer to your license in every code file on your repository.

### Credits
--- --- ---
* **Gabriel Pelielo** - gpelielo@stone.com.br
* **Pedro Elias** - pelias@stone.com.br

**Project developed by the fraud detection team in [Stone](http://www.stone.com.br/).**