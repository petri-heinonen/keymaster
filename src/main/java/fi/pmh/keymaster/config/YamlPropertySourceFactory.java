package fi.pmh.keymaster.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.NonNull;

import java.util.Properties;

public class YamlPropertySourceFactory implements PropertySourceFactory
{
    @Override
    public PropertySource<?> createPropertySource(String name, @NonNull EncodedResource encodedResource)
    {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(encodedResource.getResource());
        Properties properties = factory.getObject();
        return new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
    }
}