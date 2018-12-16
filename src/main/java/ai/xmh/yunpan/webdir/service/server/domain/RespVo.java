package ai.xmh.yunpan.webdir.service.server.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * @author by xmh, Date on 2018/12/8.
 */
@ApiModel
public class RespVo<T> implements Serializable {
    private static final long serialVersionUID = -6530717831822433790L;
    @ApiModelProperty("返回数据")
    T data;
    @ApiModelProperty("返回错误码")
    long errcode;
    @ApiModelProperty("返回错误消息")
    String errmsg;
    public static final SerializerFeature[] jsonFeatures;

    public T getData() {
        return this.data;
    }

    public long getErrcode() {
        return this.errcode;
    }

    public String getErrmsg() {
        return this.errmsg;
    }

    public RespVo(RCode code, T data) {
        if (code != null) {
            this.errcode = code.getErrcode();
            this.errmsg = code.getErrmsg();
        }

        this.data = data;
    }

    public RespVo() {
    }
    @Override
    public String toString() {
        return JSON.toJSONString(this, jsonFeatures);
    }

    public static <T> RespVo<T> success(T data) {
        return new RespVo(WebDirRCode.SUCCESS, data);
    }

    public static <T> RespVo<T> build(RCode code, T data) {
        return new RespVo(code, data);
    }

    static {
        jsonFeatures = new SerializerFeature[]{SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullListAsEmpty, SerializerFeature.PrettyFormat};
    }
}