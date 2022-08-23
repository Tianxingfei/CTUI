package com.siemens.healthineers.ct.eventlog.valueExtraction.util

import org.apache.log4j.Logger
import org.apache.spark.sql.SparkSession

trait
SparkFactory {
  /**
   * Creates a new spark session based on the parameter passed with the constructor
   * @return New spark session
   */

  def getSparkSession(appName: String):SparkSession

  protected val logger: Logger = Logger.getLogger(this.getClass.getName)
  protected val appName = "mdf-spark-core"
}
case object SparkFactory{

  private val sparkMaster:String = "spark.master"

  private class IDESetup extends SparkFactory {

    override def getSparkSession(appName: String):SparkSession = {
      logger.info("SparkSession initialization started in IDE mode")
      SparkSession
        .builder()
        .appName(appName)
        .config(sparkMaster,"local[*]")
        .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
        .getOrCreate()
    }
  }

  private class CLISetup extends SparkFactory {

    override def getSparkSession(appName: String):SparkSession = {
      logger.info("SparkSession initialization started in CLI mode")
      SparkSession
        .builder()
        .appName(appName)
        .getOrCreate()
    }
  }

  private class SmallTestSetup extends SparkFactory {
    override def getSparkSession(appName: String):SparkSession = {
      logger.info("SparkSession initialization started in TEST mode")
      SparkSession
        .builder()
        .appName(appName)
        .config(sparkMaster,"local[1]")
        .config("spark.sql.shuffle.partitions", "1")
        .getOrCreate()
    }
  }

  /**
   * Method for creating a spark factory
   * @param mode: "IDE" - For local environment, "CLI" - For cluster, "TEST" - For unit tests
   * @return SparkFactory predefined for the environment.
   */
  def apply(mode: String):SparkFactory = {
    mode match {
      case "IDE" => new IDESetup
      case "CLI" => new CLISetup
      case "TEST" => new SmallTestSetup
      case _ => new CLISetup
    }
  }

  def apply:SparkFactory = {
    new CLISetup
  }
}

