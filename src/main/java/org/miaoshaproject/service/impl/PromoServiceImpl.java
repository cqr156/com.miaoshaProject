package org.miaoshaproject.service.impl;

import org.joda.time.DateTime;
import org.miaoshaproject.dao.PromoDOMapper;
import org.miaoshaproject.dataobject.PromoDO;
import org.miaoshaproject.service.PrommoService;
import org.miaoshaproject.service.model.PromoModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Created on 2021/2/22.
 *
 * @author CQR
 */
@Service
public class PromoServiceImpl implements PrommoService {


    @Autowired
    private PromoDOMapper promoDOMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {

        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        PromoModel promoModel = convertFromDataObject(promoDO);

        if(promoModel==null){
            return null;
        }

        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else {
            promoModel.setStatus(2);
        }

        return promoModel;
    }
    private PromoModel convertFromDataObject(PromoDO promoDO){
        if(promoDO==null){
            return null;
        }
        PromoModel promoModel = new PromoModel();

        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return  promoModel;
    }
}
