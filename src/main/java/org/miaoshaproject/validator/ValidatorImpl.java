package org.miaoshaproject.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * Created on 2021/2/19.
 *
 * @author CQR
 */
@Component
public class ValidatorImpl implements InitializingBean {

    private Validator validator;
    //实现校验 方法并返回结果
    public ValidationResult validate(Object bean){
        final ValidationResult result = new ValidationResult();
        Set<ConstraintViolation<Object>> constraintViolationSet =validator.validate(bean);
        if(constraintViolationSet.size()>0){
            //有错
            result.setHasErrors(true);
            constraintViolationSet.forEach(constraintViolation->{
                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                result.getErrorMsgMap().put(propertyName,errMsg);
            });
        }
        return result;
    }


    @Override
    //通过工厂初始化的方式实例化
    public void afterPropertiesSet() throws Exception {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
