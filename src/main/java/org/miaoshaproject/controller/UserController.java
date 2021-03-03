package org.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.miaoshaproject.controller.ViewObject.UserVO;
import org.miaoshaproject.error.BusinessException;
import org.miaoshaproject.error.EmBusinessError;
import org.miaoshaproject.response.CommonReturnType;
import org.miaoshaproject.service.UserService;
import org.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created on 2021/2/16.
 *
 * @author CQR
 */
@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    private  UserService userService;

    @Resource
    private  HttpServletRequest httpServletRequest;

    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name="telphone")String telphone,
                                  @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(org.apache.commons.lang3.StringUtils.isEmpty(telphone)||
        StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VAUDATION_ERROR);
        }
        //用户登录服务，检验用户登录是否合法
        UserModel userModel = userService.validateLogin(telphone, this.EncodeByMd5(password));
        //将登陆凭证加入到用户登陆成功的session内
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);
        return  CommonReturnType.create(null);
    }

    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    //用户注册接口
    public CommonReturnType register(@RequestParam(name = "otpCode")String otpCode,
                                     @RequestParam(name = "telphone")String telphone,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")Integer gender,
                                     @RequestParam(name = "age")Integer age,
                                     @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和验证码
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if(!StringUtils.equals(inSessionOtpCode,otpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VAUDATION_ERROR,"短信验证码不符合");
        }
        //注册流程
        UserModel userModel=new UserModel();
        userModel.setTelphone(telphone);
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setRegisterMode("byphone");
        userModel.setEncriptPassword(this.EncodeByMd5(password));

        userService.register(userModel);

        return CommonReturnType.create(null);
    }


    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder=new BASE64Encoder();
        String newstr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

    @RequestMapping(value = "/getotp",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    //用户获取otp信息接口
    public CommonReturnType getOtp(@RequestParam(name = "telphone")String telphone){
        //生成otp验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt+=10000;
        String otpCode = String.valueOf(randomInt);
        //验证码和手机关联，用http session的方式绑定
        httpServletRequest.getSession().setAttribute(telphone,otpCode);
        //验证码通过短信发给用户
        System.out.println("telphone: "+telphone+"&"+"otpcode: "+otpCode);
        return  CommonReturnType.create(null);
    }


    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id")Integer id) throws BusinessException {
        //通过service获取对象返回给前端
        UserModel userModel = userService.getUserById(id);

        //若获取的的对应用户信息不存在
        if(userModel==null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }


        //将核心领域模型用户对象转化为可供UI使用的ViewObject
        UserVO userVO=convertFromUserModel(userModel);
        return CommonReturnType.create(userVO);
    }

    private UserVO convertFromUserModel(UserModel userModel){
        if(userModel==null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }


}
