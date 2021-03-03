package org.miaoshaproject.error;

/**
 * Created on 2021/2/17.
 *
 * @author CQR
 */
public interface CommonError {
    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);

}
