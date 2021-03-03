package org.miaoshaproject.response;

/**
 * Created on 2021/2/17.
 *
 * @author CQR
 */
public class CommonReturnType {
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
    //请求返回结果成功或失败
    private String status;
    //若status=success，返回json数据
    //若status=fail，data使用通用的错误码格式
    private Object data;

    //创建方法
    public static CommonReturnType create(Object result){
        return  CommonReturnType.create(result,"success");
    }
    public  static CommonReturnType create(Object result,String status){
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }
}
