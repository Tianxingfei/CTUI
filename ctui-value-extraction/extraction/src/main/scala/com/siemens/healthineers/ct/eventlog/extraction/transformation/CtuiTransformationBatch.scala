package com.siemens.healthineers.ctui.value.defaultFolder.transformation

import com.siemens.healthineers.mdf.transformation.Transformation
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

/**
 * DefaultTransformationImpl Case class, a default transformations implementation
 * @param spark: Spark session
 * @param ds: Dataset to transform
 */
case class DefaultTransformationImpl(spark:SparkSession, ds:Dataset[Row]) extends Transformation {

  private val logger =  Logger.getLogger(this.getClass.getName)

  /**
   * Adds input data to the class
   * @param df: Input data
   * @return DefaultTransformationImpl
   */

  def withDF(df: DataFrame): DefaultTransformationImpl = {
    logger.trace("Input dataframe is added")
    copy(ds = df)
  }

  /**
   *  Transforms the data as per business logic
   * @return DefaultTransformationImpl with transformed dataset
   */
  def other: DefaultTransformationImpl = {
    logger.info("executing other transformation")
    copy(ds = ds)
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