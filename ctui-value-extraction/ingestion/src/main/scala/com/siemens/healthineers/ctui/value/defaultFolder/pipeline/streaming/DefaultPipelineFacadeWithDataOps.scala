package com.siemens.healthineers.ctui.value.defaultFolder.pipeline.streaming

import com.siemens.healthineers.ctui.value.defaultFolder.transformation.{DefaultStreamTransformationImpl, DefaultTransformationImpl}
import com.siemens.healthineers.ctui.value.defaultFolder.util.DataOpsSchemaProvider
import com.siemens.healthineers.mdf.ingestion.pipeline.IngestionPipeline
import com.siemens.healthineers.mdf.ingestion.spark.{ConfigKey, ReadConfig, WriteConfig}
import com.siemens.healthineers.mdf.spark.{DefaultQueryListener, StreamingQueryFactory}
import com.siemens.healthineers.mdf.transformation.{MicroBatchTransformationWithDQImpl, TransformationFactory}
import com.siemens.healthineers.mdf.verification.DQRulesFactory
import org.apache.log4j.Logger
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

/**
 * DefaultPipelineFacadeWithDataOps Object, Pipeline Facade for Default
 * */
object DefaultPipelineFacadeWithDataOps {

    private val logger = Logger.getLogger(this.getClass.getName)
    var ReadConfigObjectGetter: Map[ConfigKey.Value, Any] = _
    var WriteConfigObjectGetter: Map[ConfigKey.Value, Any] = _

    /**
     * Initiates the pipeline
     *
     * @param appArgs : Array(rawZonePath, goldZonePath, checkPointPath)
     * @param spark   : spark session from driver
     */
    def apply(appArgs: Array[String], spark: SparkSession): Unit = {

        logger.info("Default  ingestion pipeline facade execution started")

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

        //Configure target for bad and good data
        val stagingBad = s"""$outputFolder/bad"""
        val out = s"""$outputFolder/out"""

        //Transformation Module name
        val batchTrnsfName: String = "Default_MICRO_BATCH_TRANSFORMATION"
        val streamTrnsfName: String = "Default_STREAM_TRANSFORMATION"
        //DataOps transformation name
        val DQTrnsfName: String = "Default_MICRO_BATCH_DQ_TRANSFORMATION"
        val queryName: String = "Default"

        //Get the default readConfig Map and update or add extra key if needed
        val readConfigOptionsMap: Map[String, String] = (ReadConfig()
            .ReadConfigMap
            + ("path" -> inputFolder)
            )

        //Get the default OtherReadConfig Map and update or add extra key if needed
        val otherReadConfigOptionsMap: Map[ConfigKey.Value, Any] = (ReadConfig()
            .OtherReadConfigMap
            + (ConfigKey.sourceType -> "STREAM_TEXT")
            )

        //Constructing the sparkReadConfig map
        val sparkReadConfigObject: Map[ConfigKey.Value, Any] = Map(
            ConfigKey.readConfigOptions -> readConfigOptionsMap.withDefaultValue("NA"),
            ConfigKey.otherReadConfigOptions -> otherReadConfigOptionsMap.withDefaultValue("NA")
        )

        ReadConfigObjectGetter = sparkReadConfigObject

        logger.trace(s"Read Config Object = $sparkReadConfigObject")


        //Get the default writeConfig Map and update or add extra key if needed
        val writeConfigOptionsMap: Map[String, String] = (WriteConfig()
            .WriteConfigMap
            + ("checkpointLocation" -> checkpointFolder)
            )
        logger.trace(s"Write Config Object = $writeConfigOptionsMap")

        //Configurations for MicroBatch Parquet Target
        val microBatchConfigTargetParquet: Map[ConfigKey.Value, Any] = Map(
            ConfigKey.batchMode -> SaveMode.Append,
            ConfigKey.format -> "parquet",
            ConfigKey.microBatchTrnsfName -> DQTrnsfName,
            ConfigKey.target -> out,
            ConfigKey.targetType -> "BATCH_TEXT_NO_PARTITION",
            ConfigKey.storageType -> "lake") //lake or external serving layer storage e.g. jdbc
        logger.trace(s"microBatchConfigTargetParquet = $microBatchConfigTargetParquet")


        //Get the default OtherWriteConfig Map and update or add extra key if needed
        val otherWriteConfigOptionsMap: Map[ConfigKey.Value, Any] = (WriteConfig()
            .OtherWriteConfigMap
            + (ConfigKey.targetType -> "STREAM_MICRO_BATCH")
            + (ConfigKey.triggerType -> Trigger.Once())
            + (ConfigKey.arrMicroBatchConfig -> Array(microBatchConfigTargetParquet))
            + (ConfigKey.queryListener -> DefaultQueryListener)
            + (ConfigKey.queryName -> queryName)
            )
        logger.trace(s"otherWriteConfigOptionsMap = $otherWriteConfigOptionsMap")

        //Constructing the sparkWriteConfig map
        val sparkWriteConfigObject: Map[ConfigKey.Value, Any] = Map(
            ConfigKey.writeConfigOptions -> writeConfigOptionsMap.withDefaultValue("NA"),
            ConfigKey.otherWriteConfigOptions -> otherWriteConfigOptionsMap.withDefaultValue("NA")
        )

        WriteConfigObjectGetter = sparkWriteConfigObject

        logger.trace(s"sparkWriteConfigObject = $sparkWriteConfigObject")

        val schema = DataOpsSchemaProvider.expectedSchema

        //Register the DQ rules in the factory
        DQRulesFactory.registerVerification("schema", schema)

        //Set the Transformation Objects

        TransformationFactory.registerTransformation(streamTrnsfName,
            DefaultStreamTransformationImpl(spark, spark.emptyDataFrame))

        TransformationFactory.registerTransformation(DQTrnsfName,
            MicroBatchTransformationWithDQImpl(spark, spark.emptyDataFrame, -99L, stagingBad, transformData))

        TransformationFactory.registerTransformation(batchTrnsfName,
            DefaultTransformationImpl(spark, spark.emptyDataFrame))

        logger.trace("Transformation objects registered in the factory")

        logger.info("Default  ingestion pipeline triggered")

        //Trigger the pipeline
        IngestionPipeline(spark, sparkReadConfigObject, streamTrnsfName,
            sparkWriteConfigObject, spark.emptyDataFrame)
            .load
            .transform
            .saveTo

        //Fetch the streaming query object from factory for further monitoring
        val query = StreamingQueryFactory.getStreamingQuery(queryName)

        //Log the number of files processed for SRE activities
        logger.info("Number of records processed: " + query.lastProgress.numInputRows)

        logger.info("Default  ingestion pipeline execution finished")
        logger.info("Default  ingestion Facade execution finished")
    }

    /**
     * The transformation business logic goes here
     *
     * @param input : The input DataFrame
     * @return DataFrame
     */
    def transformData(input: DataFrame): DataFrame = {
        TransformationFactory.
            getTransformation("Default_MICRO_BATCH_TRANSFORMATION")
            .withDF(input)
            .chain
    }
}

