package org.miaoshaproject.controller;

import org.miaoshaproject.error.BusinessException;
import org.miaoshaproject.error.EmBusinessError;
import org.miaoshaproject.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2021/2/17.
 *
 * @author CQR
 */
public class BaseController {

    public static final String  CONTENT_TYPE_FORMED="application/x-www-form-urlencoded";

    //定义exceptionhandler解决未被controller层吸收的exception
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception exception){
        Map<String,Object> responseData = new HashMap<>();
        if(exception instanceof BusinessException){
            BusinessException businessException = (BusinessException)exception;
            responseData.put("errCode", businessException.getErrCode());
            responseData.put("errMsg",businessException.getErrMsg());
        }else{
            responseData.put("errCode", EmBusinessError.UNKNOWN_VAUDATION_ERROR.getErrCode());
            responseData.put("errMsg",EmBusinessError.UNKNOWN_VAUDATION_ERROR.getErrMsg());
        }
        return  CommonReturnType.create(responseData,"fail");
    }
}
