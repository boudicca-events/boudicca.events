package base.boudicca.api.eventcollector.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@ConfigurationProperties(prefix = "boudicca.collector")
class EventCollectorsConfigurationProperties {
    lateinit var collectionInterval: Duration
    var webui = WebUiConfig()
    var enricherUrl: String? = null
    var eventdbUrl: String? = null
    var ingestAuth: String? = null

    class WebUiConfig {
        var enabled: Boolean = false
        var port: Int? = null
        var timeZoneId: String = "Europe/Vienna"
    }
}
