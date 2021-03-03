package org.miaoshaproject.service;

import org.miaoshaproject.error.BusinessException;
import org.miaoshaproject.service.model.UserModel;

/**
 * Created on 2021/2/16.
 *
 * @author CQR
 */
public interface UserService {
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;

    /**
     * telphone:用户注册手机
     * password:用户加密后的密码
     */
    UserModel validateLogin(String telphone,String encriptPassword) throws BusinessException;
}
