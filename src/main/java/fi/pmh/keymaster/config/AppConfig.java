package fi.pmh.keymaster.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(basePackages = "fi.pmh.keymaster")
@EnableJpaRepositories(basePackages = "fi.pmh.keymaster.persistence")
@EnableScheduling
public class AppConfig {
}
