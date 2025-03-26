package org.edem.carvue;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;


@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    private final ParameterStoreService parameterStoreService;


    @Bean
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(parameterStoreService.getParameter("/carvue/db/url"));
        dataSource.setUsername(parameterStoreService.getParameter("/carvue/db/username"));
        dataSource.setPassword(parameterStoreService.getParameter("/carvue/db/password"));

        return dataSource;
    }
}
