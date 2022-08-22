package com.siemens.healthineers.ctui.value.defaultFolder.pipeline.streaming

import com.siemens.healthineers.ctui.value.defaultFolder.transformation.DefaultTransformationImpl
import com.siemens.healthineers.mdf.ingestion.pipeline.IngestionPipeline
import com.siemens.healthineers.mdf.ingestion.spark.{ConfigKey, ReadConfig, WriteConfig}
import com.siemens.healthineers.mdf.transformation.TransformationFactory
import com.siemens.healthineers.mdf.util.Utility
import org.apache.log4j.Logger
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{SaveMode, SparkSession}

/**
 * DefaultPipelineFacade Object, Pipeline Facade
 * */
object DefaultPipelineFacade {

  private val logger =  Logger.getLogger(this.getClass.getName)
  var ReadConfigObjectGetter: Map[ConfigKey.Value, Any] = _
  var WriteConfigObjectGetter: Map[ConfigKey.Value, Any] = _

  /**
   * Initiates the pipeline
   *
   * @param appArgs : Array(rawZonePath, goldZonePath, checkPointPath)
   * @param spark   : spark session from driver
   */
  def apply(appArgs: Array[String], spark: SparkSession): Unit = {

    logger.info("Default tube pipeline facade execution started")

    //Capturing the command line arguments
    val inputFolder: String = appArgs(1)
    val outputFolder: String = appArgs(2)
    val checkpointFolder: String = appArgs(3)

    logger.trace(
      s"""Arguments used:
         |inputFolder: $inputFolder
         |outputFolder: $outputFolder
         |checkpointFolder: $checkpointFolder
         |""".stripMargin)

    //Transformation Module name
    val trnsfName: String = "DEFAULT"

    //Get the default readConfig Map and update or add extra key if needed
    val readConfigOptionsMap: Map[String, String] = (ReadConfig()
      .ReadConfigMap
      + ("path" -> inputFolder)
      + ("sep" -> "|")
      + ("header" -> "true")
      + ("dateformat" -> "MM/dd/yyyy HH:mm:ss")
      //We must define the date format manually, because the input is not standard
      )

    //Get the default OtherReadConfig Map and update or add extra key if needed
    val otherReadConfigOptionsMap: Map[ConfigKey.Value, Any] = (ReadConfig()
      .OtherReadConfigMap
      + (ConfigKey.schema -> Utility.getSchema(spark, inputFolder, readConfigOptionsMap))
      + (ConfigKey.sourceType -> "STREAM_TEXT")
      + (ConfigKey.format -> "csv")
      )

    //Constructing the sparkReadConfig map
    val sparkReadConfigObject:Map[ConfigKey.Value, Any] = Map(
      ConfigKey.readConfigOptions -> readConfigOptionsMap.withDefaultValue("NA"),
      ConfigKey.otherReadConfigOptions -> otherReadConfigOptionsMap.withDefaultValue("NA")
    )

    ReadConfigObjectGetter = sparkReadConfigObject

    logger.trace(s"Read Config Object = $sparkReadConfigObject")

    /* Below configuration is for batch or stream write */

    //Get the default writeConfig Map and update or add extra key if needed
    val writeConfigOptionsMap: Map[String, String] = (WriteConfig()
      .WriteConfigMap
      + ( "checkpointLocation" -> checkpointFolder)
      )
    logger.trace(s"Write Config Object = $writeConfigOptionsMap")

    //Configurations for MicroBatch Parquet Target
    val microBatchConfigTargetParquet:Map[ConfigKey.Value, Any] = Map(
      ConfigKey.batchMode -> SaveMode.Append,
      ConfigKey.format -> "parquet",
      ConfigKey.target -> outputFolder,
      ConfigKey.partitionColumnSeq -> Seq("system_mat_no"),
      ConfigKey.rePartitionColumnSeq -> Seq(col("system_mat_no")),
      ConfigKey.targetType -> "BATCH_TEXT",
      ConfigKey.storageType -> "lake") //lake or external serving layer storage e.g. jdbc
    logger.trace(s"microBatchConfigTargetParquet = $microBatchConfigTargetParquet")


    //Get the default OtherWriteConfig Map and update or add extra key if needed
    val otherWriteConfigOptionsMap: Map[ConfigKey.Value, Any] = (WriteConfig()
      .OtherWriteConfigMap
      + (ConfigKey.targetType -> "STREAM_MICRO_BATCH")
      + (ConfigKey.arrMicroBatchConfig -> Array(microBatchConfigTargetParquet))
      )
    logger.trace(s"otherWriteConfigOptionsMap = $otherWriteConfigOptionsMap")

    //Constructing the sparkWriteConfig map
    val sparkWriteConfigObject:Map[ConfigKey.Value, Any] = Map(
      ConfigKey.writeConfigOptions -> writeConfigOptionsMap.withDefaultValue("NA"),
      ConfigKey.otherWriteConfigOptions -> otherWriteConfigOptionsMap.withDefaultValue("NA")
    )

    WriteConfigObjectGetter = sparkWriteConfigObject

    logger.trace(s"sparkWriteConfigObject = $sparkWriteConfigObject")

    //Set the Transformation Object

    TransformationFactory.registerTransformation(trnsfName,
      DefaultTransformationImpl(spark, spark.emptyDataFrame))
    logger.trace("Transformation object registered in the factory")

    logger.info("Default tube pipeline triggered")

    //Trigger the pipeline
    IngestionPipeline(spark, sparkReadConfigObject, trnsfName,
      sparkWriteConfigObject, spark.emptyDataFrame)
      .load
      .transform
      .saveTo

    logger.info("Default tube pipeline execution finished")
    logger.info("Facade execution finished")
  }
}

