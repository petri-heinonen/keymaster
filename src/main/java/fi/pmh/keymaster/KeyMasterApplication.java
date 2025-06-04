package fi.pmh.keymaster;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.stream.StreamSupport;

@Slf4j
@SpringBootApplication
public class KeyMasterApplication {
    @Autowired
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(KeyMasterApplication.class, args);
    }

    @PostConstruct
    public void constructed()
    {
        final MutablePropertySources sources = ((AbstractEnvironment)env).getPropertySources();
        StreamSupport.stream(sources.spliterator(), false)
            .filter(EnumerablePropertySource.class::isInstance)
            .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
            .flatMap(Arrays::stream)
            .distinct()
            .forEach(prop ->
            {
                if (prop.startsWith("settings.") ||
                    prop.startsWith("data.") ||
                    prop.startsWith("spring.") ||
                    prop.startsWith("logging.") ||
                    prop.startsWith("server.") ||
                    prop.startsWith("x."))
                {
                    log.info("{}: {}", prop, env.getProperty(prop));
                }
            });
    }
}
