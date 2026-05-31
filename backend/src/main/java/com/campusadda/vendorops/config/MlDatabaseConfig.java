package com.campusadda.vendorops.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "com.campusadda.vendorops.ml.repository"
        },
        entityManagerFactoryRef = "mlEntityManagerFactory",
        transactionManagerRef = "mlTransactionManager"
)
public class MlDatabaseConfig {

    @Bean(name = "mlDataSourceProperties")
    @ConfigurationProperties(prefix = "app.ml.datasource")
    public DataSourceProperties mlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "mlDataSource")
    public DataSource mlDataSource(
            @Qualifier("mlDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "mlEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mlEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mlDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(
                        "com.campusadda.vendorops.auth.entity",
                        "com.campusadda.vendorops.user.entity",
                        "com.campusadda.vendorops.vendor.entity",
                        "com.campusadda.vendorops.menu.entity",
                        "com.campusadda.vendorops.inventory.entity",
                        "com.campusadda.vendorops.order.entity",
                        "com.campusadda.vendorops.analytics.entity",
                        "com.campusadda.vendorops.forecast.entity",
                        "com.campusadda.vendorops.anomaly.entity"
                )
                .persistenceUnit("ml")
                .build();
    }

    @Bean(name = "mlTransactionManager")
    public PlatformTransactionManager mlTransactionManager(
            @Qualifier("mlEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}