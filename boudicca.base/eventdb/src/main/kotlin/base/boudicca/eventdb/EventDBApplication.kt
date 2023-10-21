package base.boudicca.eventdb

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@OpenAPIDefinition(
    servers = [
        Server(url = "/", description = "Default Server URL")
    ]
)
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(BoudiccaEventDbProperties::class)
class EventDBApplication : WebMvcConfigurer {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests {
            it.requestMatchers("/ingest/**").hasRole("INGEST")
            it.requestMatchers("/**").permitAll()
        }
        http.httpBasic(withDefaults())
        http.csrf {
            it.disable()
        }
        return http.build()
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
    }

    //TODO this really should be done better....
    @Bean
    fun users(boudiccaEventDbProperties: BoudiccaEventDbProperties): UserDetailsService {
        val ingestUser = User.builder()
            .username("ingest")
            .password("{noop}" + boudiccaEventDbProperties.ingest.password)
            .roles("INGEST")
            .build()
        return InMemoryUserDetailsManager(ingestUser)
    }
}

fun main(args: Array<String>) {
    runApplication<EventDBApplication>(*args)
}
