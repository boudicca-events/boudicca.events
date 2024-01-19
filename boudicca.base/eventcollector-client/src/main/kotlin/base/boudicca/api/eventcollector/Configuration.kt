package base.boudicca.api.eventcollector

import org.slf4j.LoggerFactory
import java.io.File
import java.util.*


object Configuration {
    private val properties: Properties = Properties()
    private val LOG = LoggerFactory.getLogger(this.javaClass)

    init {
        val propsFromClassPath = this.javaClass.getResourceAsStream("/application.properties")
        if (propsFromClassPath != null) {
            properties.load(propsFromClassPath)
            propsFromClassPath.close()
        }
        val propsFromFile = File("application.properties")
        if (propsFromFile.exists()) {
            if (!propsFromFile.canRead()) {
                LOG.error("found application.properties file but could not read from it!")
            }
            val inputStream = propsFromFile.inputStream()
            properties.load(inputStream)
            inputStream.close()
        }
    }

    fun getProperty(name: String): String? {
        return properties.getProperty(name)
    }
}