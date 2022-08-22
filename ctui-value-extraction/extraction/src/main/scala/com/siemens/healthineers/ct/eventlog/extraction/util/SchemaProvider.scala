package com.siemens.healthineers.ctui.value.defaultFolder.util

import org.apache.spark.sql.types._

/**
 * SchemaProvider Case object, a case object
 * containing schema of raw zone data
 */
case object SchemaProvider {
    val importSchema: StructType = new StructType()
        .add(StructField("Year", IntegerType, nullable = false))
        .add(StructField("Month", IntegerType, nullable = false))
        .add(StructField("Day", IntegerType, nullable = false))
        .add(StructField("EQ_MAT_ID", StringType, nullable = false))
        .add(StructField("EQ_SERIAL_ID", StringType, nullable = false))
}

