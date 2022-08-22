package acceptance


import com.typesafe.config.ConfigFactory
import io.cucumber.scala.{EN, PendingException, ScalaDsl}

/**
 * StepDefinitions class, contains steps corresponding to gherkin feature file
 */
class StepDefinitions extends ScalaDsl with EN {

    var env: String = _
    var driver: Driver = _

    //Common set up for all the scenarios using hook
    Before {

        //Read the config file to find the value of the below variables
        val config = ConfigFactory.load("ctui-value-extraction-test.conf").getConfig("ibase")

        val bddConfig = config.getConfig("bdd")
        env = bddConfig.getString("env")

        //Select the correct driver
        driver = env match {
            case x if "local".equalsIgnoreCase(x) => new LocalDriver(config)
            case _ => throw new PendingException()
        }
        driver.before()
    }

    After {
        cleanUp()
    }

    def cleanUp(): Unit = {
        driver.after()
    }


    Given("""^new file is arriving in raw zone at "([^"]*)"$""") { (testFilePath: String) =>
        driver.givenFileInRawZone(testFilePath)
    }

    When("""^the ingestion in triggered$""") { () =>
        driver.whenIngestionTriggered()
    }

    Then("""^the input gets transformed to the defined format$""") { () =>
        driver.thenTransformationDone()
    }

    Then("""the ingested data matches the expected data""") { () =>
        driver.thenResultEquals()
    }

}