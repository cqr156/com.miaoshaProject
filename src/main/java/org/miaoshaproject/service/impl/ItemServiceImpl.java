package org.miaoshaproject.service.impl;

import org.miaoshaproject.dao.ItemDOMapper;
import org.miaoshaproject.dao.ItemStockDOMapper;
import org.miaoshaproject.dataobject.ItemDO;
import org.miaoshaproject.dataobject.ItemStockDO;
import org.miaoshaproject.error.BusinessException;
import org.miaoshaproject.error.EmBusinessError;
import org.miaoshaproject.service.ItemService;
import org.miaoshaproject.service.PrommoService;
import org.miaoshaproject.service.model.ItemModel;
import org.miaoshaproject.service.model.PromoModel;
import org.miaoshaproject.validator.ValidationResult;
import org.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created on 2021/2/20.
 *
 * @author CQR
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PrommoService prommoService;

    private ItemDO convertItemDOFromItemModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult result =  validator.validate(itemModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VAUDATION_ERROR,result.getErrMsg());
        }

        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);

        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());
        ItemStockDO itemStockDO = this.convertItemStockDOFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);
        return this.getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel> itemModelList=itemDOList.stream().map(itemDO -> {
           ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
           ItemModel itemModel = this.convertModelFromDataObject(itemDO,itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO==null){
            return  null;
        }
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());

        ItemModel itemModel = convertModelFromDataObject(itemDO,itemStockDO);
        PromoModel promoByItemId = prommoService.getPromoByItemId(itemModel.getId());
        if(promoByItemId !=null && promoByItemId.getStatus()!=3){
            itemModel.setPromoModel(promoByItemId);
        }
        return itemModel;
    }

    @Override
    @Transactional
    public Boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        int affectedRow =itemStockDOMapper.decreaseStock(itemId,amount);
        if(affectedRow>0){
            return  true;
        }else{
            return false;
        }
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId,amount);
    }

    private  ItemModel convertModelFromDataObject(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return  itemModel;
    }
}
