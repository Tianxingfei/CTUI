package utils

import org.apache.spark.sql.types._

/**
 * ResultSchemaProvider Case object, a case object
 * containing schema of gold zone data
 */
case object ResultSchemaProvider {
    val importSchema: StructType = new StructType()
        .add(StructField("ValueClass", StringType, nullable = false))
        .add(StructField("Year", IntegerType, nullable = false))
        .add(StructField("Month", IntegerType, nullable = false))
        .add(StructField("Day", IntegerType, nullable = false))
        .add(StructField("MaterialNumber", LongType, nullable = false))
        .add(StructField("SerialNumber", LongType, nullable = false))
}

