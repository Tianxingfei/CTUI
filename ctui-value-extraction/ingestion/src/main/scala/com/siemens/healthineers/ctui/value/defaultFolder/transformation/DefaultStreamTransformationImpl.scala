package com.siemens.healthineers.ctui.value.defaultFolder.transformation

import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import com.siemens.healthineers.mdf.transformation.Transformation
import org.apache.spark.sql.functions._

/**
 * DefaultStreamTransformationImpl Case class, a default transformations implementation
 * @param spark: Spark session
 * @param ds: Dataset to transform
 */
case class DefaultStreamTransformationImpl(spark:SparkSession, ds:Dataset[Row]) extends Transformation {

  private val logger =  Logger.getLogger(this.getClass.getName)

  /**
   * Adds input data to the class
   * @param df: Input data
   * @return DefaultStreamTransformationImpl
   */

  def withDF(df: DataFrame): DefaultStreamTransformationImpl = {
    logger.trace("Input dataframe is added")
    copy(ds = df)
  }

  /**
   * Creates a dataframe containing path of the input file
   *
   * @return DefaultStreamTransformationImpl with transformed dataset
   */
  def other: DefaultStreamTransformationImpl = {
    logger.warn("executing other transformation")

    //Read the distinct path
    val PathDF = df.select(input_file_name().as("Path"))


    copy(ds = PathDF)
  }

  /**
   *  Calls the transformation steps in a specified order
   * @return
   */
  def chain: DataFrame = {
    logger.info("default transformation started")
    val res = withDF(df).other.df
    logger.info("default transformation finished")
    res
  }

  /**
   *
   * @return
   */
  override def df: DataFrame = ds.toDF()
}