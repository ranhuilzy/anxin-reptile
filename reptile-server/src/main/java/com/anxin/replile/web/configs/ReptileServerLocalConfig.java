package com.anxin.replile.web.configs;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by RANHUI on 2017/5/26.
 */
@Configuration
@PropertySource({"${comConfig.file:classpath:configs/comms-config.properties}"})
public class ReptileServerLocalConfig extends WebMvcConfigurerAdapter implements EnvironmentAware {
    private Environment env;

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }

}
