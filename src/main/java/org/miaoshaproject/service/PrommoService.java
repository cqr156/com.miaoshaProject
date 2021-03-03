package org.miaoshaproject.service;

import org.miaoshaproject.service.model.PromoModel;

/**
 * Created on 2021/2/22.
 *
 * @author CQR
 */
public interface PrommoService {
    PromoModel getPromoByItemId(Integer itemId);
}
