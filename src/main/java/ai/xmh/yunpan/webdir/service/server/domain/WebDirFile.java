package ai.xmh.yunpan.webdir.service.server.domain;


import lombok.Data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

/**
 * @note 来自 PotatoCloudDrive
 */
@Data
public class WebDirFile {

    private boolean isDir;// 是否是目录
    private String fileType = "";// 文件类型
    private String fileName;// 文件名称
    private long fileSize;// 文件大小(单位:bytes)
    private String describeFileSize = "";// 文件描述大小
    private Date lastModifiedTime;// 文件最后修改时间
    private Date creationTime;// 文件创建时间
    private String relativeFilePath;

    /**
     * @param dirPath 当前文件的路径的相对路径
     * @param fileArg 文件
     * @param attrs   文件属性
     * @throws IOException
     */
    public WebDirFile(String dirPath, Path fileArg, BasicFileAttributes attrs) throws IOException {
        fileName = fileArg.getFileName().toString();
        relativeFilePath = dirPath+"/"+fileArg.getFileName().toString();
        lastModifiedTime = new Date(attrs.lastModifiedTime().toMillis());
        creationTime = new Date(attrs.creationTime().toMillis());
        isDir = attrs.isDirectory();
        if (!isDir) {
            int indexOf = fileName.lastIndexOf(".");
            if (indexOf > -1) {
                fileType = fileName.substring(indexOf + 1).toLowerCase();
            }
            fileSize = attrs.size();
        }
    }


}
