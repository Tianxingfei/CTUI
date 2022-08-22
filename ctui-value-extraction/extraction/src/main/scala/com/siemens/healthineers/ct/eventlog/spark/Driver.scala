package com.siemens.healthineers.ctui.value.spark

import com.siemens.healthineers.ctui.value.defaultFolder.pipeline.streaming.DefaultPipelineFacade
import org.apache.log4j.Logger
import com.siemens.healthineers.mdf.spark.SparkFactory

/**
 * Entry point of the application
 * Args:IDE data output checkpoint
 */
object Driver extends App {

  val logger =  Logger.getLogger(this.getClass.getName)
  val applicationMode = args(0)

  logger.info(s"Application started in $applicationMode")
  val spark = SparkFactory(applicationMode)
    .getSparkSession("ctui-value-extraction") //Use CLI to run from commandline and IDE to run from IntelliJ
  logger.trace(s"Spark session object fetched from factory.")

  DefaultPipelineFacade(args, spark)

  //If DataOps is required, use following pipeline facade & remove the DefaultPipelineFacade() and associated files
  //DefaultPipelineFacadeWithDataOps(args, spark)

  logger.info("Application finished!")
}
