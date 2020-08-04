package ru.megafon.b2b.is.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.megafon.b2b.is.repository.SubscriberMapper;
import ru.megafon.b2b.is.data.Subscriber;

import java.util.concurrent.CompletableFuture;

@Service
public class SubscriberService {
    @Autowired
    SubscriberMapper subscriberMapper;

    public Mono<Subscriber> getSubscriber(String accountNumber) {
        return Mono.fromFuture(
                CompletableFuture.supplyAsync(() -> subscriberMapper.getSubscriber(accountNumber))
        );
    }
}
