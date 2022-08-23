package com.siemens.healthineers.ct.eventlog.valueExtraction.util

import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types.{DataType, StructType}

import java.text.SimpleDateFormat
import java.util.Calendar
import scala.util.matching.Regex

object CtuiUtils {

  /** Util function for reading StructType from json.
   *
   * @param jsonFilePath path to the json config
   * @return the StructType
   */
  def utilReadSchemaFromJson(jsonFilePath: String): org.apache.spark.sql.types.StructType = {
    val jsonSchema = scala.io.Source.fromFile(jsonFilePath).mkString
    DataType.fromJson(jsonSchema).asInstanceOf[StructType]
  }

  /** Used to extract the value of a given field from the MatchSummaryMessage.
   * Precondition: The value of the given field needs to be enclosed in quotation marks.
   *
   * @param mainstring should be the MatchSummaryMessage from the pattern match table
   * @param substring  should be the name of the field including the = symbol
   * @param removeUnit indicates whether or not the unit of a substring should be removed
   * @return the string value of the given field.
   */
  val splitString = (mainString: String, substring: String, removeUnit: String) => {
    // check if the field is in the MatchSummaryMessage
    if (mainString.indexOfSlice(substring) == -1) {
      null
    } else {
      // extract value of field
      val locationSubstring = mainString.indexOfSlice(substring) + substring.length
      var returnString = mainString.slice(locationSubstring, mainString.length)
      returnString = returnString.slice(returnString.indexOfSlice("\"") + 1, returnString.length)
      returnString = returnString.slice(0, returnString.indexOfSlice("\""))

      // n.a. stands for "not available". I think it is better to set it to null instead.
      // So that we have a more standardized response for fields that are not filled.
      if (returnString == "n.a.") {
        null
      } else {
        // remove unit if flag is set.
        if (removeUnit == "1") {
          val pattern = new Regex("^[+-]?[0-9]+(\\.[0-9]+)?")
          val matches = pattern.findAllIn(returnString).toList
          if (matches.length == 1) {
            matches(0)
          } else {
            returnString
          }
        }
        else {
          returnString
        }
      }
    }
  }
  val udfSplitString = udf(splitString)

  /** This function extracts the unit out of a field from a given string.
   *
   * @param mainstring should be the MatchSummaryMessage from the pattern match table
   * @param substring  should be the name of the field including the = symbol
   * @return the unit of the given field as string
   */
  val extractUnit = (mainString: String, substring: String) => {
    if (mainString.indexOfSlice(substring) == -1) {
      null
    } else {
      var locationSubstring = mainString.indexOfSlice(substring) + substring.length
      var returnString = mainString.slice(locationSubstring, mainString.length)
      returnString = returnString.slice(returnString.indexOfSlice("\"") + 1, returnString.length)
      returnString = returnString.slice(0, returnString.indexOfSlice("\""))

      val pattern = new Regex("[a-zA-Z°/*][a-zA-Z°/\\s*]*") // maybe the regex needs to be adjusted if new possible units occur
      val matches = pattern.findAllIn(returnString).toList
      if (matches.length == 1) {
        matches(0)
      } else {
        null
      }
    }
  }
  val udfExtractUnit = udf(extractUnit)

  /** Takes a timestamp in UTC time and uses the timezone offset to calculate the local time.
   *
   * @param RefDateTime       utc timestamp
   * @param RefTimeZoneOffset timezone offset
   * @return local time timestamp
   */
  val localDateTime = (RefDateTime: java.sql.Timestamp, RefTimeZoneOffset: Int) => {
    var dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    // use calender format to add the offset in seconds
    val cal: Calendar = Calendar.getInstance()
    cal.setTime(RefDateTime)
    cal.add(Calendar.SECOND, RefTimeZoneOffset)
    // convert back to timestamp format using the time in milliseconds as input
    new java.sql.Timestamp(cal.getTimeInMillis())
  }
  val udfLocalDateTime = udf(localDateTime)

}
