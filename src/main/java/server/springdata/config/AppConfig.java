package server.springdata.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import java.util.Properties;

import static org.springframework.context.annotation.ComponentScan.Filter;
import static org.springframework.context.annotation.FilterType.ANNOTATION;

@Configuration
@EnableTransactionManagement
@ComponentScan(value = "server", excludeFilters = {
        @Filter(type = ANNOTATION, value = Configuration.class)
})
@EnableJpaRepositories("server.springdata.repository")
@PropertySource("classpath:spring/db.properties")
public class AppConfig {
    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("db.driver"));
        dataSource.setUrl(env.getProperty("db.url"));
        dataSource.setUsername(env.getProperty("db.user"));
        dataSource.setPassword(env.getProperty("db.password"));
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(Database.MYSQL);
        vendorAdapter.setGenerateDdl(env.getProperty("db.generateDdl", Boolean.class));
        vendorAdapter.setShowSql(env.getProperty("db.showSql", Boolean.class));

        LocalContainerEntityManagerFactoryBean emFactory = new LocalContainerEntityManagerFactoryBean();
        emFactory.setJpaVendorAdapter(vendorAdapter);
        emFactory.setPackagesToScan("server.springdata.model");
        emFactory.setDataSource(dataSource());

        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("db.hbm2ddl.auto"));
        jpaProperties.setProperty("hibernate.max_fetch_depth", env.getProperty("db.max_fetch_depth"));
        jpaProperties.setProperty("hibernate.jdbc.fetch_size", env.getProperty("db.jdbc.fetch_size"));
        jpaProperties.setProperty("hibernate.jdbc.batch_size", env.getProperty("db.jdbc.batch_size"));

        emFactory.setJpaProperties(jpaProperties);

        return emFactory;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
}

