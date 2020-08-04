package ru.megafon.b2b.sus.repository;

import org.apache.ibatis.annotations.*;
import ru.megafon.b2b.sus.data.Subscriber;

public interface SubscriberMapper {

    @Select("select * from SUS_USER.SUBSCRIBER where MSISDN = #{msisdn}")
    Subscriber getByMsisdn(String msisdn);

    @Update("update SUS_USER.SUBSCRIBER set STATUS=#{status} WHERE ACCOUNT_NUMBER = #{accountNumber}")
    void updateStatus(Subscriber subscriber);

    @Insert("INSERT into SUS_USER.SUBSCRIBER(ACCOUNT_NUMBER, MSISDN, STATUS) VALUES(#{accountNumber}, #{msisdn}, #{status})")
    void insert(Subscriber subscriber);
}

