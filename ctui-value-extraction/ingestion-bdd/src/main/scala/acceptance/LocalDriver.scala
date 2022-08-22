package acceptance

import com.github.mrpowers.spark.fast.tests.DataFrameComparer
import com.siemens.healthineers.mdf.spark.SparkFactory
import com.typesafe.config.Config
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.launcher.SparkLauncher
import org.apache.spark.sql.{DataFrame, SparkSession}
import utils.{MiniDfsClusterManager, ResultSchemaProvider}

import java.io.File
import scala.reflect.io.Directory

/**
 * LocalDriver class, contains test logic for BDDs
 *
 * @param config : configurations for running BDDs
 */
class LocalDriver(config: Config) extends Driver with DataFrameComparer {

    val miniDfsClusterManager: MiniDfsClusterManager = new MiniDfsClusterManager
    val spark: SparkSession = SparkFactory("TEST").getSparkSession


    //Variables for local spark-submit (spark launcher)
    var sparkApplicationJar: String = _
    var sparkApplicationMainClass: String = _
    var master: String = _
    var sparkHome: String = _
    var sparkSubmitErrorLogPath: String = _
    var sparkSubmitSparkSessionMode: String = _
    var sparkSubmitDataSourcePath: String = _
    var sparkSubmitDataTargetPath: String = _
    var sparkSubmitDataCheckPointPath: String = _
    var sparkSubmitDataExpectedFilePath: String = _

    //Get the spark config for local spark-submit (spark launcher)
    val sparkConfig: Config = config.getConfig("spark")

    //Resulting Dataframe post BDD run
    lazy val resultDf: DataFrame = {
        spark
            .read
            .schema(ResultSchemaProvider.importSchema)
            .parquet(sparkSubmitDataTargetPath)
    }

    //Expected result of spark transformations
    lazy val expectedDf: DataFrame = {
        spark
            .read
            .schema(ResultSchemaProvider.importSchema)
            .format("csv")
            .option("header", "true")
            .load(sparkSubmitDataExpectedFilePath)
    }

    override def before(): Unit = {
        //Spark Launcher setup
        sparkApplicationJar = sparkConfig.getString("sparkApplicationJarPath")
        sparkApplicationMainClass = sparkConfig.getString("sparkApplicationMainClass")
        master = sparkConfig.getString("master")
        sparkHome = sparkConfig.getString("sparkHome")
        sparkSubmitErrorLogPath = sparkConfig.getString("sparkSubmitErrorLogPath")
        sparkSubmitSparkSessionMode = sparkConfig.getString("sparkSubmitSparkSessionMode")
        sparkSubmitDataSourcePath = sparkConfig.getString("sparkSubmitDataSourcePath")
        sparkSubmitDataTargetPath = sparkConfig.getString("sparkSubmitDataTargetPath")
        sparkSubmitDataCheckPointPath = sparkConfig.getString("sparkSubmitDataCheckPointPath")
        sparkSubmitDataExpectedFilePath = sparkConfig.getString("sparkSubmitDataExpectedFilePath")

        miniDfsClusterManager.startHDFS()
    }

    override def givenFileInRawZone(testFilePath: String): Unit = {
        assert(miniDfsClusterManager.hdfsCluster.isClusterUp, "The mini hdfs cluster has not been started.")
        val newFilePath = miniDfsClusterManager.uploadTestDataToMiniCluster(testFilePath)
        assert(miniDfsClusterManager.fileExist(newFilePath), "File doesn't exists.")
    }

    override def whenIngestionTriggered(): Unit = {
        val ingestion = new SparkLauncher()
            .setSparkHome(sparkHome)
            .setAppResource(sparkApplicationJar)
            .setMainClass(sparkApplicationMainClass)
            .addAppArgs(
                sparkSubmitSparkSessionMode,
                sparkSubmitDataSourcePath,
                sparkSubmitDataTargetPath,
                sparkSubmitDataCheckPointPath)
            .setMaster(master)
            .redirectError(new File(sparkSubmitErrorLogPath))
            .launch()

        ingestion.waitFor()
    }

    override def thenTransformationDone(): Unit = {
        val metaFolderPath = new Path(sparkSubmitDataTargetPath + "/_spark_metadata")
        val successFilePath = new Path(sparkSubmitDataTargetPath + "/_SUCCESS")
        val fsConf = new Configuration()
        val hdfsFs = FileSystem.get(fsConf)

        assert(hdfsFs.exists(metaFolderPath) || hdfsFs.exists(successFilePath), "No file in gold zone.")

        val dfCount = resultDf.count()
        assert(dfCount.equals(4L), "Not all rows were processed.")
    }

    override def thenResultEquals(): Unit = {

        val expectedDfNoDynamicCols = expectedDf

        val actualDfNoDynamicCols = resultDf

        assertSmallDataFrameEquality(actualDfNoDynamicCols, expectedDfNoDynamicCols, ignoreNullable = true, orderedComparison = false)

    }

    /**
     * Does post BDD run cleanup
     */
    override def after(): Unit = {
        miniDfsClusterManager.shutdownHDFS()

        if (sparkSubmitDataTargetPath != null) {
            val outputFolder = new Directory(new File(sparkSubmitDataTargetPath)).parent
            if (outputFolder.exists)
                outputFolder.deleteRecursively()
        }
    }

}
