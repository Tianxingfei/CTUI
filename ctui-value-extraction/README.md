# Introduction 
TODO: Give a short introduction of your project. Let this section explain the objectives or the motivation behind this project. 


### The Latest release number of project: 1.0.0

# Business Specification
TODO

# Technical Specification
## Tools and Dependencies

IntelliJ version: 2021.1 (Community Edition)  
IntelliJ Plugins: Scala(2021.1.18), Save Actions(2.2.0)  

## Build and Test

- Build:

  Maven command to build the project:

  ``mvn package -D scoverage.skip=true``


- Test:

  Maven command to run tests:

  ``mvn clean test``



- Run BDD:

  Run `runBDD.sh` file  
  or alternatively follow these steps:
    - Run maven command to build the project
    - Copy ``/path/to/your/app/fat/jar`` to ``/path/to/your/BDD/module/``
    - Run `main` function of `BDDRunner.scala` (Hint: don't forget to set working directory in run
      configuration)


- Command to run the Spark App using spark-submit(on HDI Cluster):

  ```shell
  spark-submit --class path.to.your.sparkDriver \
  --master yarn \
  --deploy-mode cluster \
  --conf spark.driver.extraJavaOptions=-Dlog4j.configuration=log4j-driver.properties \
  --conf spark.executor.extraJavaOptions=-Dlog4j.configuration=log4j-executor.properties \
  --files $abfsPath/log4/log4j-driver.properties,$abfsPath/log4/log4j-executor.properties  \
  $abfsPath/jars/* CLI $abfsPath/rawzone $abfsPath/goldzone $abfsPath/checkpoint
  ```


## DevOps

[CI Pipeline]()

[Build Status]()

[CD Pipeline]()

[Build Status]()

## Development Standards

### Checks

- scalastyle: TBD
- sonarlint: TBD

# Conclusion