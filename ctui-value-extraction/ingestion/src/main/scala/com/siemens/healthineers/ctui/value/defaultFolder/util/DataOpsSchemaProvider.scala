package com.siemens.healthineers.ctui.value.defaultFolder.util

import com.amazon.deequ.schema.RowLevelSchema

/**
 * SchemaProvider Case object, a case object
 * containing schema of raw zone data
 */
case object DataOpsSchemaProvider {

    val timestampMask: String = "dd.MM.yyyy HH:mm:ss"
    val dateMask: String = "dd.MM.yyyy"

    val expectedSchema: RowLevelSchema = RowLevelSchema()
        .withIntColumn("Year", isNullable = false)
        .withIntColumn("Month", isNullable = false)
        .withIntColumn("Day", isNullable = false)
        .withStringColumn("EQ_MAT_ID", isNullable = false, matches = Some("^[0-9]*$"))
        .withStringColumn("EQ_SERIAL_ID", isNullable = false, matches = Some("^[0-9]*$"))
        .withTimestampColumn("TABLE_LOAD_DT_TM", mask = timestampMask, isNullable = false)
        .withTimestampColumn("MAT_EOD_DT", mask = dateMask, isNullable = true)

}

