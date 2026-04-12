package com.train.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class DataSourceConfig {

    /**
     * For staging and production, secrets are resolved automatically by Spring Cloud GCP
     * For dev, local configuration is used
     */

    @Configuration
    @Profile("!prod & !staging")
    public static class DevDataSourceConfig {
        @Bean
        public DataSource devDataSource(
                org.springframework.boot.autoconfigure.jdbc.DataSourceProperties props) {
            log.info("Using DEV DataSource configuration");
            return DataSourceBuilder.create()
                    .driverClassName(props.getDriverClassName())
                    .url(props.getUrl())
                    .username(props.getUsername())
                    .password(props.getPassword())
                    .build();
        }
    }

    @Configuration
    @Profile("staging")
    public static class StagingDataSourceConfig {
        @Bean
        public DataSource stagingDataSource(
                org.springframework.boot.autoconfigure.jdbc.DataSourceProperties props) {
            log.info("Using STAGING DataSource configuration");
            return DataSourceBuilder.create()
                    .driverClassName(props.getDriverClassName())
                    .url(props.getUrl())
                    .username(props.getUsername())
                    .password(props.getPassword())
                    .build();
        }
    }

    @Configuration
    @Profile("prod")
    public static class ProductionDataSourceConfig {
        @Bean
        public DataSource productionDataSource(
                org.springframework.boot.autoconfigure.jdbc.DataSourceProperties props) {
            log.info("Using PRODUCTION DataSource configuration");
            return DataSourceBuilder.create()
                    .driverClassName(props.getDriverClassName())
                    .url(props.getUrl())
                    .username(props.getUsername())
                    .password(props.getPassword())
                    .build();
        }
    }
}
