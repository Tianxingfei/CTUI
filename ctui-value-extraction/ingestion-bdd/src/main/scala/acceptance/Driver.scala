package acceptance

/**
 * Driver trait, contains steps to be implemented by BDD drivers
 */
trait Driver {

    def before(): Unit

    def givenFileInRawZone(testFilePath: String): Unit

    def whenIngestionTriggered(): Unit

    def thenTransformationDone(): Unit

    def thenResultEquals(): Unit

    def after(): Unit
}