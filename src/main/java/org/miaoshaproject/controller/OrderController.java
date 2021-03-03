package org.miaoshaproject.controller;

import org.miaoshaproject.error.BusinessException;
import org.miaoshaproject.error.EmBusinessError;
import org.miaoshaproject.response.CommonReturnType;
import org.miaoshaproject.service.OrderService;
import org.miaoshaproject.service.model.OrderModel;
import org.miaoshaproject.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created on 2021/2/21.
 *
 * @author CQR
 */
@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController{

    @Autowired
    private OrderService orderService;

    @Resource
    private HttpServletRequest httpServletRequest;


    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name="itemId")Integer itemId,
                                        @RequestParam(name="amount")Integer amount,
                                        @RequestParam(name="promoId",required = false)Integer promoId) throws BusinessException {
        Boolean isLogin =  (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if(isLogin==null||!isLogin.booleanValue()){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登录，不能下单");
        }


        UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");

        OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,promoId,amount);
        return CommonReturnType.create(null);
    }
}
