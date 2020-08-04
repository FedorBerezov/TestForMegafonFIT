package ru.megafon.b2b.is.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.megafon.b2b.data.ResponseMessage;
import ru.megafon.b2b.service.RateLimitConfigService;
import ru.megafon.b2b.is.service.SubscriberService;
import ru.megafon.b2b.response.ResponseCode;

@Slf4j
@RestController
public class DbEnrichController {

    @Autowired
    private RateLimitConfigService rateLimitConfigService;

    @Autowired
    private SubscriberService subscriberService;

    @GetMapping("/dbenrich/subscriber/{account_number}")
    public Mono<ResponseMessage> dbEnrich(@PathVariable("account_number") String accountNumber) {
        if (rateLimitConfigService.getBucket().tryConsume(1)) {
            return subscriberService.getSubscriber(accountNumber).map(ResponseMessage::ok)
                    .defaultIfEmpty(ResponseMessage.error(ResponseCode.ESB_404))
                    .doOnError(t -> log.error("Failed to access database", t))
                    .onErrorReturn(ResponseMessage.error(ResponseCode.ESB_500));
        }
        return Mono.just(ResponseMessage.error(ResponseCode.ESB_429));
    }
}
