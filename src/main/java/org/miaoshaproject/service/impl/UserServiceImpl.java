package org.miaoshaproject.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.miaoshaproject.dao.UserDOMapper;
import org.miaoshaproject.dao.UserPasswordDOMapper;
import org.miaoshaproject.dataobject.UserDO;
import org.miaoshaproject.dataobject.UserPasswordDO;
import org.miaoshaproject.error.BusinessException;
import org.miaoshaproject.error.EmBusinessError;
import org.miaoshaproject.service.UserService;
import org.miaoshaproject.service.model.UserModel;
import org.miaoshaproject.validator.ValidationResult;
import org.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.validation.Validator;

/**
 * Created on 2021/2/16.
 *
 * @author CQR
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO==null){
            return null;
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        return convertFromDataObject(userDO,userPasswordDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if(userModel==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VAUDATION_ERROR);
        }
//        if(StringUtils.isEmpty(userModel.getName())
//                ||userModel.getGender()==null
//                ||userModel.getAge()==null
//                ||StringUtils.isEmpty(userModel.getTelphone())){
//            throw new BusinessException(EmBusinessError.PARAMETER_VAUDATION_ERROR);
//        }
        ValidationResult result = validator.validate(userModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VAUDATION_ERROR,result.getErrMsg());
        }

        UserDO userDO = convertFromModel(userModel);
        try{
            userDOMapper.insertSelective(userDO);//insert:null覆盖数据库默认值 insertSelective:不覆盖，表示未设定
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VAUDATION_ERROR,"该手机号已注册过账号");
        }

        userModel.setId(userDO.getId());
        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);
        return;
    }

    @Override
    public UserModel validateLogin(String telphone, String encriptPassword) throws BusinessException {
        //通过手机获取信息
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if(userDO==null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO,userPasswordDO);
        //比对加密密码和传输进来的密码
        if(!StringUtils.equals(encriptPassword,userModel.getEncriptPassword())){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userModel;

    }

    public UserPasswordDO convertPasswordFromModel(UserModel userModel){
        if(userModel==null){
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncriptPassword());
        userPasswordDO.setUserId(userModel.getId());

        return userPasswordDO;
    }

    public UserDO convertFromModel(UserModel userModel){
        if(userModel==null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if(userDO==null){
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);
        if(userPasswordDO!=null){
            userModel.setEncriptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }
}
