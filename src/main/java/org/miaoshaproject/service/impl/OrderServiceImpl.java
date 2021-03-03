package org.miaoshaproject.service.impl;

import org.miaoshaproject.dao.OrderDOMapper;
import org.miaoshaproject.dao.SequenceDOMapper;
import org.miaoshaproject.dataobject.OrderDO;
import org.miaoshaproject.dataobject.SequenceDO;
import org.miaoshaproject.error.BusinessException;
import org.miaoshaproject.error.EmBusinessError;
import org.miaoshaproject.service.ItemService;
import org.miaoshaproject.service.OrderService;
import org.miaoshaproject.service.UserService;
import org.miaoshaproject.service.model.ItemModel;
import org.miaoshaproject.service.model.OrderModel;
import org.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created on 2021/2/21.
 *
 * @author CQR
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId,Integer promoId,Integer amount) throws BusinessException {
        //1、校验下单状态，用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VAUDATION_ERROR,"商品信息不正确");
        }
        UserModel userModel = userService.getUserById(userId);
        if(userModel==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VAUDATION_ERROR,"用户信息不正确");
        }
        if(amount<=0||amount>90){
            throw new BusinessException(EmBusinessError.PARAMETER_VAUDATION_ERROR,"数量信息不正确");
        }

        //校验活动信息

        if(promoId!= null){
            if(promoId.intValue() != itemModel.getPromoModel().getId()){
                throw new BusinessException(EmBusinessError.PARAMETER_VAUDATION_ERROR,"活动信息不正确");
            }else if(itemModel.getPromoModel().getStatus().intValue()!=2){
                throw new BusinessException(EmBusinessError.PARAMETER_VAUDATION_ERROR,"活动未开始");
            }
        }


        //2、落单减库存，支付减库存
        boolean result = itemService.decreaseStock(itemId,amount);
        if(!result){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //3、订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if(promoId != null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));

        //生成交易流水号,订单号

        orderModel.setId(generateOrderNo());
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        itemService.increaseSales(itemId,amount);
        //4、返回前端

        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo(){
        //8位时间信息+6位自增序列+2位分库分表位
        StringBuilder stringBuilder = new StringBuilder();
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);

        int sequence = 0;
        SequenceDO sequenceDO =  sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for(int i=0;i<6-sequenceStr.length();i++){
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);

        //分库分表位暂时
        stringBuilder.append("00");

        return stringBuilder.toString();

    }


    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel == null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}
