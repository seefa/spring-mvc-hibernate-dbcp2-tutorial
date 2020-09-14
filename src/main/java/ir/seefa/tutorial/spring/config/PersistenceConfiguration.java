package ir.seefa.tutorial.spring.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import java.util.Optional;
import java.util.Properties;

/**
 * @author Saman Delfani
 * @version 1.0
 * @since 09 Sep 2020 T 23:54:03
 */
// 1. Read spring-core-tutorial and spring-jdbc-mvc-tutorial codes before starting this project because primary annotations and mvc logic explained there
@Configuration
@ComponentScan(basePackages = "ir.seefa.tutorial.spring")
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
// 2. Enabling Spring JPA wrapper over introduced package
@EnableJpaRepositories(basePackages = "ir.seefa.tutorial.spring")
public class PersistenceConfiguration {

    // 3. Loading application configuration with Spring Environment class
    @Autowired
    private Environment env;

    // 4. apply main configuration Datasource from application.properties file and configuration datasource connection pool
    @Bean
    public BasicDataSource dataSource() {

        final BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName(env.getProperty("db.driver.class"));
        dataSource.setUrl(env.getProperty("db.url"));
        dataSource.setUsername(env.getProperty("db.username"));
        dataSource.setPassword(env.getProperty("db.password"));

        // apply connection pooling properties
        dataSource.setInitialSize(Integer.parseInt(Optional.ofNullable(env.getProperty("dbcp.initial.size")).orElse("0")));
        dataSource.setMaxIdle(Integer.parseInt(Optional.ofNullable(env.getProperty("dbcp.max.idle")).orElse("5")));
        dataSource.setMaxTotal(Integer.parseInt(Optional.ofNullable(env.getProperty("dbcp.max.total")).orElse("0")));
        dataSource.setMinIdle(Integer.parseInt(Optional.ofNullable(env.getProperty("dbcp.min.idle")).orElse("0")));

        return dataSource;
    }

    // 5. define Hibernate bean factory and apply its data source and its additional configuration such as Database dialect or even Cache layer 2 for example HikariCP
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan("ir.seefa.tutorial.spring");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        entityManagerFactoryBean.setJpaProperties(additionalProperties());

        return entityManagerFactoryBean;
    }

    final Properties additionalProperties() {

        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));

        hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", env.getProperty("hibernate.cache.use_second_level_cache"));
        hibernateProperties.setProperty("hibernate.cache.use_query_cache", env.getProperty("hibernate.cache.use_query_cache"));

        return hibernateProperties;
    }

    // 6. define transaction manager bean for enabling Insert/Update/Delete transaction management with @Transactional annotation over Service layer
    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory emf) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

}
