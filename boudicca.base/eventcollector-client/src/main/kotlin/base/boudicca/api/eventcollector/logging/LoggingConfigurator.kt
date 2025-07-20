package base.boudicca.api.eventcollector.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.layout.TTLLLayout
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.spi.ContextAwareBase
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender


class LoggingConfigurator : ContextAwareBase(), Configurator {
    override fun configure(context: LoggerContext?): Configurator.ExecutionStatus {
        addInfo("Setting up default configuration.")

        val rootLogger = context!!.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.level = Level.INFO
        rootLogger.addAppender(createConsoleAppender(context))
        rootLogger.addAppender(createOtelAppender(context))

        return Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY
    }

    private fun createConsoleAppender(context: LoggerContext): ConsoleAppender<ILoggingEvent> {
        val ca = ConsoleAppender<ILoggingEvent>()
        ca.context = context
        ca.name = "console"
        val encoder = LayoutWrappingEncoder<ILoggingEvent>()
        encoder.context = context

        // same as
        // PatternLayout layout = new PatternLayout();
        // layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} -
        // %msg%n");
        val layout = TTLLLayout()

        layout.context = context
        layout.start()
        encoder.layout = layout

        ca.encoder = encoder
        ca.addFilter(CollectionsFilter(encoder))
        ca.start()
        return ca
    }

    private fun createOtelAppender(context: LoggerContext): OpenTelemetryAppender {
        val otelA = OpenTelemetryAppender()
        otelA.context = context
        otelA.name = "otel"
        otelA.start()
        return otelA
    }
}
