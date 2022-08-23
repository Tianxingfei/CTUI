package com.siemens.healthineers.ct.eventlog.valueExtraction.pipeline.streaming

import org.apache.log4j.Logger
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{SaveMode, SparkSession}
import com.siemens.healthineers.ct.eventlog.valueExtraction.util.CtuiUtils.{extractUnit,splitString}

/**
 * DefaultPipelineFacade Object, Pipeline Facade
 * */
object CtuiPipelineFacade {

  private val logger =  Logger.getLogger(this.getClass.getName)


  /**
   * Initiates the pipeline
   *
   * @param appArgs : Array(rawZonePath, goldZonePath, checkPointPath)
   * @param spark   : spark session from driver
   */
  def apply(appArgs: Array[String], spark: SparkSession): Unit = {

    logger.info("Default tube pipeline facade execution started")


    //Capturing the command line arguments
    // relevant paths to the input and output locations
    val patternMatchingTablePath = "C:\\Users\\z004jfad\\ctui_test_data\\input\\patternMatchData_august_1_day\\" // path to the input pattern match table
    val tbScanningMultiTableBasePath = "C:\\Users\\z004jfad\\ctui_test_data\\output\\ctui_result_august_1_day_local\\" // base output folder
    val patternTableSchemesPath = "C:\\Users\\z004jfad\\pipeline_config\\config\\output_table_schemes\\" // folder that contains the schemes for the output pattern tables
    val fieldColumnMappingPath = "C:\\Users\\z004jfad\\pipeline_config\\config\\field_column_mappings\\tbScanning_mapping.xml" // path to the xml file that maps the fields in the message to a corresponding column in a resulting table. One xml file should contain the mappings for one table



    logger.info("Default tube pipeline execution finished")
    logger.info("Facade execution finished")
  }

}

