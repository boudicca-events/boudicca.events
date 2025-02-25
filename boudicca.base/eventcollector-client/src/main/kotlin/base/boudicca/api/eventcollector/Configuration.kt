package base.boudicca.api.eventcollector

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.util.*


object Configuration {
    private val properties: Properties = Properties()
    private val logger = KotlinLogging.logger {}

    init {
        val propsFromClassPath = this.javaClass.getResourceAsStream("/application.properties")
        if (propsFromClassPath != null) {
            properties.load(propsFromClassPath)
            propsFromClassPath.close()
        }
        val propsFromFile = File("application.properties")
        if (propsFromFile.exists()) {
            if (!propsFromFile.canRead()) {
                logger.error { "found application.properties file but could not read from it!" }
            }
            val inputStream = propsFromFile.inputStream()
            properties.load(inputStream)
            inputStream.close()
        }
    }

    fun getProperty(name: String): String? {
        var property = System.getProperty(name)
        if (property != null) {
            return property
        }
        property = System.getenv(toEnvName(name))
        if (property != null) {
            return property
        }
        return properties.getProperty(name)
    }

    private fun toEnvName(name: String): String {
        return name
            .split('.')
            .joinToString("_") { it.uppercase() }
    }
}
