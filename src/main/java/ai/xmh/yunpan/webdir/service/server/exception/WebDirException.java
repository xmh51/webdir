package ai.xmh.yunpan.webdir.service.server.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author by xmh, Date on 2018/12/8.
 */

public class WebDirException extends Exception{
    @Getter
    private String msg;
    public WebDirException(String msg){
        this.msg=msg;
    }
}
