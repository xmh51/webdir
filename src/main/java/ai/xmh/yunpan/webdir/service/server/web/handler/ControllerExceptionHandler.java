package ai.xmh.yunpan.webdir.service.server.web.handler;

import lombok.extern.slf4j.Slf4j;
import ai.xmh.yunpan.webdir.service.server.domain.RespVo;
import ai.xmh.yunpan.webdir.service.server.domain.WebDirRCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhangwei13
 */

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public RespVo jsonErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        log.info("controller exception with webdir = @{}@,req uri exception = {}", req,req.getRequestURI(), e);
        e.printStackTrace();
        return RespVo.build(WebDirRCode.SYSTEM_IS_BUSY, null);
    }
}
