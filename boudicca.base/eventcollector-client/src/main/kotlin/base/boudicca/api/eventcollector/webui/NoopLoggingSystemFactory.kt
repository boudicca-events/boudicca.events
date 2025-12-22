package base.boudicca.api.eventcollector.webui

import org.springframework.boot.logging.LoggingSystem
import org.springframework.boot.logging.LoggingSystemFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

/**
 * we want to disable springs autoconfiguration of logging since we do this ourselves
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
class NoopLoggingSystemFactory : LoggingSystemFactory {
    override fun getLoggingSystem(classLoader: ClassLoader): LoggingSystem = object : LoggingSystem() {
        override fun beforeInitialize() {
            // nothing
        }
    }
}
