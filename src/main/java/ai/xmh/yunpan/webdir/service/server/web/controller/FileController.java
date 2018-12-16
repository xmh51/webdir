package ai.xmh.yunpan.webdir.service.server.web.controller;

import lombok.extern.slf4j.Slf4j;
import ai.xmh.yunpan.webdir.service.server.domain.WebDirRCode;
import ai.xmh.yunpan.webdir.service.server.domain.RespVo;
import ai.xmh.yunpan.webdir.service.server.domain.WebDirFile;
import ai.xmh.yunpan.webdir.service.server.service.WebDirFileService;
import ai.xmh.yunpan.webdir.service.server.utils.WebDirFileUtil;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * @author by xmh, Date on 2018/12/8.
 */
@Controller
@RequestMapping("api/file")
@Slf4j
public class FileController {
    @Autowired
    private WebDirFileService webDirFileService;
    @Value("${tempDir}")
    private String tempDir;

    /**
     * 获取全部文件列表
     *
     * @return
     */
    @RequestMapping(value = "/dir", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public RespVo<List<WebDirFile>> getFileLst(@RequestParam(name = "filePath", required = false) String path,@RequestParam(name = "onlyDir", defaultValue = "false")Boolean onlyDir) throws Exception {
        if (path == null) {
            path = "";
        }
        path = new String(Base64.getUrlDecoder().decode(path), "utf-8");
        List<WebDirFile> webDirFiles = webDirFileService.getFileList(path,onlyDir);
        return RespVo.build(WebDirRCode.SUCCESS, webDirFiles);
    }

    /**
     * 删除文件
     *
     * @param path 待删除文件或文件夹路径(base64编码)
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public RespVo delFile(@RequestParam("filePath") String path) throws Exception {
        File file = webDirFileService.getFile(path);
        if (!file.exists()) {
            return RespVo.build(WebDirRCode.FILE_NOT_EXIST_ERROR, null);
        }
        if(file.delete()){
            return RespVo.build(WebDirRCode.SUCCESS, null);
        }else {
            return RespVo.build(WebDirRCode.FILE_DELETE_FAIL, null);
        }
    }

    /**
     * 文件夹或文件重命名
     *
     * @param filePath    文件或文件夹路径(base64编码)
     * @param newFileName 新文件或文件夹名称(base64编码)
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/renameFile", method = RequestMethod.POST)
    @ResponseBody
    public RespVo rnameFile(@RequestParam("filePath") String filePath, @RequestParam("oldFileName") String oldFilename,
                            @RequestParam("newFileName") String newFileName) throws Exception {
        File oldFile = webDirFileService.getFile(filePath + "/" + oldFilename);
        if (!oldFile.exists()) {
            return RespVo.build(WebDirRCode.FILE_NOT_EXIST_ERROR, null);
        }
        File newFile =  webDirFileService.getFile(filePath + "/" + newFileName);
        if (newFile.exists()) {
            return RespVo.build(WebDirRCode.FILE_EXIST_ERROR, null);
        }
        Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return RespVo.build(WebDirRCode.SUCCESS, null);
    }

    /**
     * 在指定目录新建文件或文件夹
     *
     * @param filePath 文件或文件夹路径
     * @param fileName 文件或文件夹名称
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/createNewFile", method = RequestMethod.POST)
    @ResponseBody
    public RespVo createNewFile(@RequestParam(value = "filePath",required = false) String filePath, @RequestParam(value = "isDir", defaultValue = "false") Boolean isDir,
                                @RequestParam("fileName") String fileName)
            throws Exception {
        if(StringUtils.isEmpty(filePath)){
            filePath="";
        }else {
            filePath=filePath + "/";
        }
        File file = webDirFileService.getFile(filePath + fileName);
        if (file.exists()) {
            return RespVo.build(WebDirRCode.FILE_EXIST_ERROR, null);
        }
        if (isDir) {
            FileUtils.forceMkdir(file);
        } else {
            file.createNewFile();
        }
        return RespVo.build(WebDirRCode.SUCCESS, null);
    }

    /**
     * 移动或者复制文件 或者文件夹
     *
     * @param oldFilePath    文件或文件夹路径(base64编码)
     * @param newFilePath 新文件或文件夹名称(base64编码)
     * @param actionType 1 复制 2 移动
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "moveOrCopyFile", method = RequestMethod.POST)
    @ResponseBody
    public RespVo moveOrCopyFile(@RequestParam("oldFilePath") String oldFilePath, @RequestParam("newFilePath") String newFilePath, @RequestParam("actionType") Integer actionType) throws Exception {
        File oldFile = webDirFileService.getFile(oldFilePath);
        if (!oldFile.exists()) {
            return RespVo.build(WebDirRCode.FILE_NOT_EXIST_ERROR, null);
        }
        File newFile = webDirFileService.getFile(newFilePath);
        if (newFile.exists()) {
            return RespVo.build(WebDirRCode.FILE_EXIST_ERROR, null);
        }
       try {
           if(actionType==1){
               if(oldFile.isDirectory()){
                   FileUtils.copyDirectory(oldFile, newFile);
               }else {
                   Files.copy(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
               }
           }else if(actionType==2){
               if(oldFile.isDirectory()){
                   FileUtils.moveDirectory(oldFile, newFile);
               }else {
                   Files.move(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
               }
           }else {
               return RespVo.build(WebDirRCode.INVALID_PARAMS, null);
           }
       }catch (Exception e){
           log.error("error",e);
           return RespVo.build(WebDirRCode.FILE_MOVEORCOPYFILE_FAIL, null);
       }

        return RespVo.build(WebDirRCode.SUCCESS, null);
    }

    /**
     * 单上传文件
     *
     * @param
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public RespVo upload(HttpServletRequest request) throws IOException, FileUploadException {
        String fileSavePath=null;
        File dstfile = new File(tempDir+ "/"+UUID.randomUUID().toString().replaceAll("-",""));
        if(!dstfile.getParentFile().exists()){
            FileUtils.forceMkdir(dstfile.getParentFile());
        }
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload();
            // Parse the request
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                InputStream stream = item.openStream();
                if (item.isFormField()) {
                    String name=item.getFieldName();
                    System.out.println(name);
                    if("fileSavePath".equals(item.getFieldName())){
                        fileSavePath=Streams.asString(stream);
                    }
                }else {
                    Streams.copy(stream,
                            new FileOutputStream(dstfile), true);
                }
            }
            if(StringUtils.isEmpty(fileSavePath)){
                dstfile.delete();
                return RespVo.build(WebDirRCode.INVALID_PARAMS, null);
            }else {
                File finaFile = webDirFileService.getFile(fileSavePath);
                if(!finaFile.getParentFile().exists()){
                    FileUtils.forceMkdir(finaFile.getParentFile());
                }
                int i=1;
                while (finaFile.exists()){
                    finaFile = webDirFileService.getFile(fileSavePath+"("+i+")");
                }
                Files.move(dstfile.toPath(), finaFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
            };
            return RespVo.build(WebDirRCode.SUCCESS, null);

        }else {
            return RespVo.build(WebDirRCode.SERVER_ERROR, null);
        }

    }

    /**
     * 文件下载
     *
     * @param path
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    @ResponseBody
    public Object fileDownload(@RequestParam(name = "filePath", required = true) String path,
                               HttpServletRequest request,
                               HttpServletResponse response) throws Exception {
        path = new String(Base64.getUrlDecoder().decode(path), "utf-8");
        File file = webDirFileService.getFile(path);
        if (!file.exists()) {
            return RespVo.build(WebDirRCode.FILE_NOT_EXIST_ERROR, null);
        }
        WebDirFileUtil.filePlayOrDownload(false, file, request, response);
        return null;
    }

    /**
     * 视频播放
     *
     * @param path
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/play", method = RequestMethod.GET)
    @ResponseBody
    public Object filePlay(@RequestParam(name = "filePath", required = true) String path,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        path = new String(Base64.getUrlDecoder().decode(path), "utf-8");
        File file = webDirFileService.getFile(path);
        if (!file.exists()) {
            return RespVo.build(WebDirRCode.FILE_NOT_EXIST_ERROR, null);
        }
        WebDirFileUtil.filePlayOrDownload(true, file, request, response);
        return null;
    }
}
