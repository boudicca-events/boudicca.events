package events.boudicca.eventcollector

import base.boudicca.api.eventcollector.configuration.EventCollectorsConfigurationProperties
import base.boudicca.api.eventcollector.debugger.EventCollectorDebugger
import base.boudicca.api.eventcollector.runner.buildRunnerFor
import base.boudicca.fetcher.FileBackedFetcherCache
import events.boudicca.eventcollector.collectors.ZuckerfabrikCollector
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.File

@Profile("debug")
@SpringBootApplication
@EnableScheduling
class LocalEventCollectorDebuggerApp(
    val configuration: EventCollectorsConfigurationProperties,
) : CommandLineRunner {
    private val debugRunner =
        buildRunnerFor(
            listOf(
                ZuckerfabrikCollector(),
            ),
        ).withFetcherCache(FileBackedFetcherCache(File("./fetcher.cache"))).buildDebugRunner()

    @Bean
    fun eventCollectionRunner() = debugRunner.runner

    override fun run(vararg args: String) {
        EventCollectorDebugger(
            debugRunner,
            configuration,
            verboseDebugging = true,
            verboseValidation = true,
        ).runDebug()
    }
}

/**
 * this allows you to test a EventCollector locally, while giving you some nice benefits:
 * 1) It also starts the local webui at http://localhost:8083/ where you can follow the progress and see all errors in a better way
 * 2) It caches network calls into a file called "fetcher.cache", so all calls to the external server will only be done once, massively increasing speed the next time you run it
 * 3) Also allows you to enable remote or local enricher
 * 4) Also allows you to ingest the data into your local eventdb
 */
fun main(args: Array<String>) {
    val app =
        SpringApplicationBuilder(LocalEventCollectorDebuggerApp::class.java)
            .web(WebApplicationType.SERVLET) // Enable embedded Tomcat
            .build()
    app.run(*args)
}
