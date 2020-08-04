package ru.megafon.b2b.sus.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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

import java.util.HashMap;

@Configuration
@EnableRabbit
public class RabbitMqConfiguration {
    @Autowired
    private ConnectionFactory connectionFactory;

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange directExchange() {
        return new TopicExchange("test.direct", true, false);
    }

    @Bean
    @Qualifier("inQueue")
    public Queue inQueue(@Value("${sus.queue.in}") String name) {
        return new Queue(name, true, false, false,
                new HashMap<String, Object>() {{
                    put("x-queue-type", "classic");
                }}
        );
    }

    @Bean
    public Queue outQueue(@Value("${sus.queue.out}") String name) {
        return new Queue(name, true, false, false,
                new HashMap<String, Object>() {{
                    put("x-queue-type", "classic");
                }}
        );
    }

    @Bean
    public Binding inboundEmailExchangeBinding(@Qualifier("inQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(directExchange()).with("");
    }
}
