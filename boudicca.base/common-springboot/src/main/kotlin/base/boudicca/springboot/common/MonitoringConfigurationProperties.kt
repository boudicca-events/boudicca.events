package base.boudicca.springboot.common

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "boudicca.monitoring")
class MonitoringConfigurationProperties {
    var endpoint: String? = null
    var user: String? = null
    var password: String? = null
}
