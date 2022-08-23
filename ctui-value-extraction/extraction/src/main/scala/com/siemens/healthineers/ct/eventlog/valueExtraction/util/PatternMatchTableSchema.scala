package com.siemens.healthineers.ct.eventlog.valueExtraction.util

case class PatternMatchTableSchema(
                                    MatchParameterPoolIdentification: String,
                                    MatchAlgorithmParameterIdentifier: String,
                                    MatchAlgorithmParameterIdentifierVersion: String,
                                    Year: Int,
                                    Month: Int,
                                    Day: Int,
                                    MaterialNumber: Int,
                                    SerialNumber: Int,
                                    EventRefSourceHash: String,
                                    EventRefLine: Int,
                                    EventRefDateTime: java.sql.Timestamp,
                                    EventRefTimeZoneOffset: Int,
                                    MatchRefEventSourceHash: String,
                                    MatchRefLine: Int,
                                    MatchRefDateTime: java.sql.Timestamp,
                                    MatchRefTimeZoneOffset: Int,
                                    MatchSummaryMessage: String,
                                    MatchAlgorithmIdentification: String,
                                    MatchValue: Map[String, String]
                                  )