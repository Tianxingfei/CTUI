package acceptance

import cucumber.api.cli.Main

/**
 * BDDRunner Object, used to run BDD
 */
object BDDRunner {

  private val pluginKeyword = "--plugin"
  private val defaultOptions = Array(
    "--glue", "classpath:acceptance", "classpath:features",
    pluginKeyword, "pretty",
    pluginKeyword, "html:target/cucumber",
    pluginKeyword, "json:target/cucumber-test-report.json",
    pluginKeyword, "junit:target/cucumber-test-report.xml"
  )

  def main(args: Array[String]): Unit = {
    Main.main(defaultOptions ++ args)
  }
}