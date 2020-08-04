package ru.megafon.b2b.sus.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.megafon.b2b.data.RequestMessage;
import ru.megafon.b2b.data.ResponseMessage;
import ru.megafon.b2b.exception.AppException;
import ru.megafon.b2b.response.ResponseCode;
import ru.megafon.b2b.service.RateLimitConfigService;
import ru.megafon.b2b.sus.data.Subscriber;
import ru.megafon.b2b.sus.service.DataCheckService;
import ru.megafon.b2b.sus.service.UpdateStatusService;

@RestController
@Slf4j
public class UpdateStatusController {

    @Autowired
    private UpdateStatusService updateStatusService;

    @Autowired
    private RateLimitConfigService rateLimitConfigService;

    @Autowired
    private DataCheckService dataCheckService;

    @PostMapping("/updatestatus")
    public Mono<ResponseMessage> updateStatus(@RequestBody RequestMessage<Subscriber> request) {
        Subscriber subscriber = request.getData();
        if (!dataCheckService.isSubscriberValid(subscriber)) return Mono.just(ResponseMessage.error(ResponseCode.ESB_400));

        log.debug(subscriber.toString());
        if (rateLimitConfigService.getBucket().tryConsume(1)) {
            return updateStatusService.updateStatus(subscriber)
                    .map(s -> ResponseMessage.ok())
                    .defaultIfEmpty(ResponseMessage.error(ResponseCode.ESB_404))
                    .doOnError(t -> {
                        log.error("Failed to update status", t.getCause());
                        log.debug("Fail trace", t);
                    })
                    .onErrorResume(AppException.class, throwable -> {
                        switch (throwable.getCode()) {
                            case ESB_500:
                                return Mono.just(ResponseMessage.error(ResponseCode.ESB_503));
                            default:
                                return Mono.just(ResponseMessage.error(throwable.getCode()));
                        }
                    })
                    .onErrorReturn(ResponseMessage.error(ResponseCode.ESB_500));
        }
        return Mono.just(ResponseMessage.error(ResponseCode.ESB_429));
    }
}
