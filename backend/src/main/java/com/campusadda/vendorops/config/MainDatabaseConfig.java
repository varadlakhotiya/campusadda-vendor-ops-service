package com.campusadda.vendorops.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "com.campusadda.vendorops.auth.repository",
                "com.campusadda.vendorops.user.repository",
                "com.campusadda.vendorops.vendor.repository",
                "com.campusadda.vendorops.menu.repository",
                "com.campusadda.vendorops.inventory.repository",
                "com.campusadda.vendorops.order.repository",
                "com.campusadda.vendorops.alert.repository",
                "com.campusadda.vendorops.analytics.repository",
                "com.campusadda.vendorops.etl.repository",
                "com.campusadda.vendorops.outbox.repository",
                "com.campusadda.vendorops.onboarding.repository"
        },
        entityManagerFactoryRef = "mainEntityManagerFactory",
        transactionManagerRef = "mainTransactionManager"
)
public class MainDatabaseConfig {

    @Primary
    @Bean(name = "mainDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties mainDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "mainDataSource")
    public DataSource mainDataSource(
            @Qualifier("mainDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean(name = "mainEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mainDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(
                        "com.campusadda.vendorops.auth.entity",
                        "com.campusadda.vendorops.user.entity",
                        "com.campusadda.vendorops.vendor.entity",
                        "com.campusadda.vendorops.menu.entity",
                        "com.campusadda.vendorops.inventory.entity",
                        "com.campusadda.vendorops.order.entity",
                        "com.campusadda.vendorops.alert.entity",
                        "com.campusadda.vendorops.analytics.entity",
                        "com.campusadda.vendorops.etl.entity",
                        "com.campusadda.vendorops.outbox.entity",
                        "com.campusadda.vendorops.onboarding.entity"
                )
                .persistenceUnit("main")
                .build();
    }

    @Primary
    @Bean(name = "mainTransactionManager")
    public PlatformTransactionManager mainTransactionManager(
            @Qualifier("mainEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}