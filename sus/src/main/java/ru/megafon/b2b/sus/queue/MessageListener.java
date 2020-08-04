package ru.megafon.b2b.sus.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.megafon.b2b.data.RequestMessage;
import ru.megafon.b2b.data.ResponseMessage;
import ru.megafon.b2b.exception.AppException;
import ru.megafon.b2b.response.ResponseCode;
import ru.megafon.b2b.sus.data.Subscriber;
import ru.megafon.b2b.sus.service.DataCheckService;
import ru.megafon.b2b.sus.service.UpdateStatusService;

@Slf4j
@Component
public class MessageListener {

    @Autowired
    private AmqpTemplate template;

    @Value("${sus.queue.out}")
    private String outQueue;

    @Autowired
    private UpdateStatusService updateStatusService;

    @Autowired
    private DataCheckService dataCheckService;

    @RabbitListener(queues = "${sus.queue.in}")
    public void in(RequestMessage<Subscriber> message) {
        log.debug("Received from queue IN: " + message);

        Subscriber subscriber = message.getData();
        if (!dataCheckService.isSubscriberValid(subscriber)) {
            template.convertAndSend(outQueue, ResponseMessage.error(ResponseCode.ESB_400, subscriber));
            return;
        }

        updateStatusService.updateStatus(message.getData())
                .doOnError(t -> {
                    log.error("Failed to update status", t.getCause());
                    log.debug("Fail trace", t);
                })
                .map(ResponseMessage::ok)
                .onErrorResume(throwable -> {
                    ResponseCode responseCode;
                    if (throwable instanceof AppException) {
                        ResponseCode code = ((AppException) throwable).getCode();
                        switch (code) {
                            case ESB_500:
                                responseCode = ResponseCode.ESB_503;
                                break;
                            default:
                                responseCode = code;
                        }
                    }
                    else {
                        responseCode = ResponseCode.ESB_500;
                    }
                    return Mono.just(ResponseMessage.error(responseCode, subscriber));
                })
                .subscribe(resp -> template.convertAndSend(outQueue, resp));
    }
}
