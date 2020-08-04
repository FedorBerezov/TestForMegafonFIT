package ru.megafon.b2b.is.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.megafon.b2b.service.RateLimitConfigService;

@Configuration
@MapperScan(basePackages = "ru.megafon.b2b.is.repository")
public class AutoConfiguration {

    @Bean
    public SqlSessionFactory sqlSessionFactory(HikariDataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }

    @Bean
    public WebClient webClient(@Value("${consul.base-url}") String url) {
        return WebClient.create(url);
    }

    @Bean
    public RateLimitConfigService rateLimitConfigService(
            @Value("${consul.rate-limit-key}") String rateLimitKey,
            WebClient webClient,
            @Value("${consul.update-rate-limit-interval}") long updateRateLimitInterval,
            @Value("${is.rps}") int defaultRps
    ) {
        return new RateLimitConfigService(rateLimitKey, webClient, updateRateLimitInterval, defaultRps);
    }
}
