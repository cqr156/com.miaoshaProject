package org.miaoshaproject.service;

import org.miaoshaproject.error.BusinessException;
import org.miaoshaproject.service.model.OrderModel;

/**
 * Created on 2021/2/21.
 *
 * @author CQR
 */
public interface OrderService {
    //1、通过前端url上传过来秒杀活动id，然后下单接口内校验id是否对应商品且活动已经开始
    //2、直接在下单接口内判断商品是否存在秒杀活动，若存在则以秒杀价格下单
    //1的可扩展性好
    OrderModel createOrder (Integer userId, Integer itemId,Integer promoId,Integer amount) throws BusinessException;
}
