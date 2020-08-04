package ru.megafon.b2b.sus.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.megafon.b2b.data.ResponseMessage;
import ru.megafon.b2b.exception.AppException;
import ru.megafon.b2b.response.ResponseCode;
import ru.megafon.b2b.sus.repository.SubscriberMapper;
import ru.megafon.b2b.sus.data.Subscriber;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class UpdateStatusService {
    @Autowired
    private SubscriberMapper subscriberMapper;

    @Autowired
    @Qualifier("imdbClient")
    private WebClient imdbClient;

    public Mono<Subscriber> updateStatus(Subscriber subscriber) {
        return Mono.defer(() -> {
                    if (subscriber.getMsisdn() != null) {
                        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> subscriberMapper.getByMsisdn(subscriber.getMsisdn())));
                    }
                    else {
                        return Mono.empty();
                    }
                }
        ).map(subscriberLocal -> {
            if (!subscriberLocal.getStatus().equals(subscriber.getStatus())) {
                subscriberLocal.setStatus(subscriber.getStatus());
                subscriberMapper.updateStatus(subscriberLocal);
            }
            return subscriberLocal;
        }).switchIfEmpty(Mono.defer(() ->
                getSubscriberFromImdb(subscriber).map(subscriberImdb -> {
                    if (subscriber.getMsisdn() != null) {
                        subscriberImdb.setStatus(subscriber.getStatus());
                        subscriberMapper.insert(subscriberImdb);
                        return subscriberImdb;
                    }
                    else {
                        Subscriber subscriberLocal = subscriberMapper.getByMsisdn(subscriberImdb.getMsisdn());
                        if (subscriberLocal == null) {
                            subscriberLocal = Subscriber.builder()
                                    .accountNumber(subscriberImdb.getAccountNumber())
                                    .msisdn(subscriberImdb.getMsisdn())
                                    .status(subscriber.getStatus())
                                    .build();
                            subscriberMapper.insert(subscriberLocal);
                        }
                        else {
                            subscriberLocal.setStatus(subscriber.getStatus());
                            subscriberMapper.updateStatus(subscriberLocal);
                        }
                        return subscriberLocal;
                    }
                }))
        );
    }

    private Mono<Subscriber> getSubscriberFromImdb(final Subscriber subscriber) {
        return imdbClient
                .get()
                .uri("/enrich/subscriber/{accountNumber}", subscriber.getAccountNumber())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ResponseMessage<Subscriber>>() {
                })
                .handle((sub, sink) -> {
                    ResponseCode code = ResponseCode.of(sub.getStatus().getCode());
                    if (code != ResponseCode.ESB_OK) {
                        sink.error(new AppException(code));
                    }
                    else {
                        sink.next(sub.getData());
                    }
                });
    }

}
