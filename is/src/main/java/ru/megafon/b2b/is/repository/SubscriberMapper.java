package ru.megafon.b2b.is.repository;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import ru.megafon.b2b.is.data.Subscriber;

public interface SubscriberMapper {
    @Select("select * from IS_USER.SUBSCRIBER where ACCOUNT_NUMBER = #{account_number}")
    @Results({@Result(property = "accountNumber", column = "account_number")})
    Subscriber getSubscriber(@Param("account_number") String accountNumber);
}
