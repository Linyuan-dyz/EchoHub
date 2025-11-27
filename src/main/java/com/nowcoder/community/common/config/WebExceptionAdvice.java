package com.nowcoder.community.common.config;

import com.nowcoder.community.domain.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(Exception.class)
    public Result handleRuntimeException(Exception e) {
        log.error(e.toString(), e);
        return Result.fail("服务器异常");
    }
}
