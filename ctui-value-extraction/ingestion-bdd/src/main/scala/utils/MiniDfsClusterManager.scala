package utils

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hdfs.MiniDFSCluster
import org.apache.hadoop.io.IOUtils

import java.io.{File, FileInputStream}
import scala.reflect.io.Directory

/**
 * Test helper class to simulate an hdfs storage without additional dependencies
 */
class MiniDfsClusterManager {
    // object that contains necessary functions to access the virtual cluster
    @transient var hdfsCluster: MiniDFSCluster = _

    // Local directory to store the data needed for the minidfs cluster
    val baseDir = new Directory(new File("./miniHdfs/"))
    // Default hadoop configuration
    val conf = new Configuration()

    // Test specific folder structure
    def workingDirectory: String = s"${hdfsCluster.getFileSystem.getWorkingDirectory}/2020/06/15/"

    /**
     * Starts the minidfs cluster
     */
    def startHDFS(): Unit = {
        // Attaching the local directory to the cluster
        conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir.toAbsolute.path)
        conf.set("fs.hdfs.impl", classOf[org.apache.hadoop.hdfs.DistributedFileSystem].getName)

        val builder = new MiniDFSCluster.Builder(conf)
        hdfsCluster = builder.nameNodePort(9001).manageNameDfsDirs(true).manageDataDfsDirs(true).format(true).build()
        hdfsCluster.waitClusterUp()

        // Making working directory environment independent
        val newWd = {
            new Path(
                s"${
                    ("^.+?[^/:](?=[?/]|$)" r)
                        .findFirstIn(hdfsCluster.getFileSystem.getWorkingDirectory.toString)
                        .get
                }/workdir/"
            )
        }
        hdfsCluster.getFileSystem.setWorkingDirectory(newWd)
    }

    /**
     * Stops the minidfs cluster, and cleans up the data directory
     */
    def shutdownHDFS(): Unit = {
        if (hdfsCluster != null && hdfsCluster.isClusterUp) {
            hdfsCluster.shutdown()
            baseDir.deleteRecursively()
        }
    }

    /**
     * Uploads the specified files to the minidfs cluster
     *
     * @param sourcePath Path of the local files
     * @return Hdfs path of the uploaded file
     */
    def uploadTestDataToMiniCluster(sourcePath: String): Path = {
        val targetPath = new Path(s"$workingDirectory${sourcePath.split("/").last}")
        val inputStream = new FileInputStream(sourcePath)
        val outputStream = hdfsCluster.getFileSystem.create(targetPath)
        IOUtils.copyBytes(inputStream, outputStream, conf, true)

        inputStream.close()
        outputStream.close()

        //Changing the modification time to 0, so it will be consistent for the tests
        hdfsCluster.getFileSystem.setTimes(targetPath, 0, 0)

        targetPath
    }

    def fileExist(filePath: Path): Boolean = {
        hdfsCluster.getFileSystem.isFile(filePath)
    }
}
