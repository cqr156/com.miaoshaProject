package org.miaoshaproject.service;

import org.miaoshaproject.error.BusinessException;
import org.miaoshaproject.service.model.ItemModel;

import java.util.List;

/**
 * Created on 2021/2/20.
 *
 * @author CQR
 */
public interface ItemService {
    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;
    //商品列表浏览
    List<ItemModel> listItem();
    //商品详情浏览
    ItemModel getItemById(Integer id);
    //减库存
    Boolean decreaseStock(Integer itemId,Integer amount)throws BusinessException;
    //商品销量增加
    void increaseSales(Integer itemId,Integer amount)throws BusinessException;
}
