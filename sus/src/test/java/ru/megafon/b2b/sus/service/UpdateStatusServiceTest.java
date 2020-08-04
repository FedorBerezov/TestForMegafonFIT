package ru.megafon.b2b.sus.service;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.megafon.b2b.data.ResponseMessage;
import ru.megafon.b2b.exception.AppException;
import ru.megafon.b2b.response.ResponseCode;
import ru.megafon.b2b.sus.data.Subscriber;
import ru.megafon.b2b.sus.repository.SubscriberMapper;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStatusServiceTest {

    @Mock
    private SubscriberMapper subscriberMapper;

    @Mock
    private WebClient imdbClient;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock
    private WebClient.RequestBodySpec requestBodyMock;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock
    private WebClient.ResponseSpec responseMock;

    @InjectMocks
    private UpdateStatusService updateStatusService;

    @Test
    public void fullChainSuccess() {
        Subscriber subscriberReq = Subscriber.builder().accountNumber("4").status("new_status").build();
        Subscriber subscriberImdb = Subscriber.builder().accountNumber("4").msisdn("1").build();
        Subscriber subscriberActual = Subscriber.builder().accountNumber("4").msisdn("1").status("new_status").build();

        when(imdbClient.get()).thenReturn(requestHeadersUriMock);
        when(requestHeadersUriMock.uri("/enrich/subscriber/{accountNumber}", "4")).thenReturn(requestHeadersMock);
        when(requestHeadersMock.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersMock);
        when(responseMock.bodyToMono(new ParameterizedTypeReference<ResponseMessage<Subscriber>>() {
        })).thenReturn(Mono.just(ResponseMessage.ok(subscriberImdb)));
        when(requestHeadersMock.retrieve()).thenReturn(responseMock);

        Mono<Subscriber> resultMono = updateStatusService.updateStatus(subscriberReq);
        Optional<Subscriber> result = resultMono.blockOptional();
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(subscriberActual, result.get());
        verify(imdbClient, times(1)).get();
        verify(subscriberMapper, times(1)).getByMsisdn("1");
        verify(subscriberMapper, times(0)).updateStatus(any());
        verify(subscriberMapper, times(1)).insert(subscriberActual);
    }

    @Test
    public void fullChainSuccessWithHaveLocal() {
        Subscriber subscriberReq = Subscriber.builder().accountNumber("4").status("new_status").build();
        Subscriber subscriberImdb = Subscriber.builder().accountNumber("4").msisdn("1").build();
        Subscriber subscriberSusLocal = Subscriber.builder().accountNumber("4").msisdn("1").status("old_status").build();
        Subscriber subscriberActual = Subscriber.builder().accountNumber("4").msisdn("1").status("new_status").build();

        given(subscriberMapper.getByMsisdn("1")).willReturn(subscriberSusLocal);
        when(imdbClient.get()).thenReturn(requestHeadersUriMock);
        when(requestHeadersUriMock.uri("/enrich/subscriber/{accountNumber}", "4")).thenReturn(requestHeadersMock);
        when(requestHeadersMock.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersMock);
        when(responseMock.bodyToMono(new ParameterizedTypeReference<ResponseMessage<Subscriber>>() {
        })).thenReturn(Mono.just(ResponseMessage.ok(subscriberImdb)));
        when(requestHeadersMock.retrieve()).thenReturn(responseMock);

        Mono<Subscriber> resultMono = updateStatusService.updateStatus(subscriberReq);
        Optional<Subscriber> result = resultMono.blockOptional();
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(subscriberActual, result.get());
        verify(imdbClient, times(1)).get();
        verify(subscriberMapper, times(1)).getByMsisdn("1");
        verify(subscriberMapper, times(1)).updateStatus(subscriberActual);
        verify(subscriberMapper, times(0)).insert(any());
    }

    @Test
    public void haveInLocalStoreStatusMatch() {
        Subscriber subscriberReq = Subscriber.builder().accountNumber("4").msisdn("1").status("status").build();

        Subscriber subscriberSusLocal = Subscriber.builder().accountNumber("4").msisdn("1").status("status").build();
        given(subscriberMapper.getByMsisdn("1")).willReturn(subscriberSusLocal);

        Mono<Subscriber> resultMono = updateStatusService.updateStatus(subscriberReq);
        Optional<Subscriber> result = resultMono.blockOptional();
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(subscriberSusLocal, result.get());
        verify(imdbClient, times(0)).get();
    }

    @Test
    public void haveInLocalStoreStatusNotMatch() {
        Subscriber subscriberReq = Subscriber.builder().accountNumber("4").msisdn("1").status("new_status").build();

        Subscriber subscriberSusLocal = Subscriber.builder().accountNumber("4").msisdn("1").status("old_status").build();
        given(subscriberMapper.getByMsisdn("1")).willReturn(subscriberSusLocal);

        Mono<Subscriber> resultMono = updateStatusService.updateStatus(subscriberReq);
        Optional<Subscriber> result = resultMono.blockOptional();
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(subscriberReq, result.get());
        verify(imdbClient, times(0)).get();
        verify(subscriberMapper, times(1)).updateStatus(eq(subscriberReq));
    }

    @Test
    public void notHaveInLocalStore() {
        Subscriber subscriberReq = Subscriber.builder().accountNumber("4").msisdn("1").status("new_status").build();
        Subscriber subscriberImdb = Subscriber.builder().accountNumber("4").msisdn("1").build();
        Subscriber subscriberActual = Subscriber.builder().accountNumber("4").msisdn("1").status("new_status").build();

        given(subscriberMapper.getByMsisdn("1")).willReturn(null);
        when(imdbClient.get()).thenReturn(requestHeadersUriMock);
        when(requestHeadersUriMock.uri("/enrich/subscriber/{accountNumber}", "4")).thenReturn(requestHeadersMock);
        when(requestHeadersMock.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersMock);
        when(responseMock.bodyToMono(new ParameterizedTypeReference<ResponseMessage<Subscriber>>() {
        })).thenReturn(Mono.just(ResponseMessage.ok(subscriberImdb)));
        when(requestHeadersMock.retrieve()).thenReturn(responseMock);

        Mono<Subscriber> resultMono = updateStatusService.updateStatus(subscriberReq);
        Optional<Subscriber> result = resultMono.blockOptional();
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(subscriberActual, result.get());
        verify(imdbClient, times(1)).get();
        verify(subscriberMapper, times(0)).updateStatus(any());
        verify(subscriberMapper, times(1)).insert(subscriberActual);
    }

    @Test
    public void rpcImdbError() {
        Subscriber subscriberReq = Subscriber.builder().accountNumber("4").status("new_status").build();

        when(imdbClient.get()).thenReturn(requestHeadersUriMock);
        when(requestHeadersUriMock.uri("/enrich/subscriber/{accountNumber}", "4")).thenReturn(requestHeadersMock);
        when(requestHeadersMock.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersMock);
        when(responseMock.bodyToMono(new ParameterizedTypeReference<ResponseMessage<Subscriber>>() {
        })).thenReturn(Mono.just(ResponseMessage.error(ResponseCode.ESB_500)));
        when(requestHeadersMock.retrieve()).thenReturn(responseMock);

        AppException appException = Assertions.assertThrows(AppException.class, () -> updateStatusService.updateStatus(subscriberReq).blockOptional());
        Assert.assertEquals(ResponseCode.ESB_500, appException.getCode());
        verify(imdbClient, times(1)).get();
        verify(subscriberMapper, times(0)).getByMsisdn(any());
        verify(subscriberMapper, times(0)).updateStatus(any());
        verify(subscriberMapper, times(0)).insert(any());
    }

}