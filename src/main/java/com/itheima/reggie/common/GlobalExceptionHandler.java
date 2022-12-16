package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class, Controller.class})
@Slf4j
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception)
    {
        log.error(exception.getMessage());
        String errorMsg=exception.getMessage();
        if(errorMsg.contains("Duplicate entry"))
        {
            String [] words=errorMsg.split(" ");
            return R.error(words[2]+"已存在");
        }
        return R.error("未知错误");
    }

    @ExceptionHandler
    public R<String>exceptionHandler(CustomerException customerException)
    {
        String message= customerException.getMessage();
        return R.error(message);
    }



}
