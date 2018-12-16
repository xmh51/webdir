package ai.xmh.yunpan.webdir.service.server.domain;

import java.io.Serializable;

/**
 * @author by xmh, Date on 2018/12/8.
 */
public enum WebDirRCode implements Serializable, RCode {
    SYSTEM_IS_BUSY(-1L, "system is busy", "系统忙"),
    SUCCESS(0L, "success", "成功"),
    INVALID_PARAMS(10001, "invalid params", "参数无效"),
    SERVER_ERROR(10003, "server error, contact us please", "服务错误,请联系我们"),
    FILE_EXIST_ERROR(10004, "新文件已存在，无法操作", "新文件已存在，无法操作"),
    FILE_NOT_EXIST_ERROR(10005, "文件不已存在，无法操作", "文件不已存在，无法操作"),
    FILE_DELETE_FAIL(10006, "删除失败 可能有其他任务或者程序正在占用该文件", "删除失败 可能有其他任务或者程序正在占用该文件"),
    FILE_MOVEORCOPYFILE_FAIL(10006, "操作失败，请检查新的文件路径", "操作失败，请检查新的文件路径");

    public WebDirRCode.ErrorEntity entity = new WebDirRCode.ErrorEntity();
    @Override
    public long getErrcode() {
        return this.entity.errcode;
    }
    @Override
    public String getErrmsg() {
        return this.entity.errmsg;
    }

    private WebDirRCode(long errcode, String errmsg, String usererrmsg) {
        this.entity.errcode = errcode;
        this.entity.errmsg = errmsg;
    }

    public class ErrorEntity {
        public long errcode;
        public String errmsg;

        public ErrorEntity() {
        }
    }
}
