package ru.megafon.b2b.sus.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import ru.megafon.b2b.service.RateLimitConfigService;

@Configuration
@MapperScan(basePackages = "ru.megafon.b2b.sus.repository")
public class SusConfiguration {

    @Bean(name = "imdbClient")
    public WebClient imdbClient(@Value("${sus.service.imdb.url}") String url) {
        return WebClient
                .builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean(name = "consulClient")
    public WebClient consulClient(@Value("${consul.base-url}") String url) {
        return WebClient.create(url);
    }

    @Bean
    public RateLimitConfigService rateLimitConfigService(
            @Value("${consul.rate-limit-key}") String rateLimitKey,
            @Qualifier("consulClient") WebClient webClient,
            @Value("${consul.update-rate-limit-interval}") long updateRateLimitInterval,
            @Value("${sus.rps}") int defoultRps
    ) {
        return new RateLimitConfigService(rateLimitKey, webClient, updateRateLimitInterval, defoultRps);
    }
}
