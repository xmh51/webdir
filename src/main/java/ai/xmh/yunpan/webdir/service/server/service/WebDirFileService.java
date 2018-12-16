package ai.xmh.yunpan.webdir.service.server.service;

import ai.xmh.yunpan.webdir.service.server.domain.WebDirFile;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author by xmh, Date on 2018/12/8.
 */
@Service
public class WebDirFileService implements InitializingBean {
    @Value("${webdir.filePath}")
    private String rootPath;

    @Override
    public void afterPropertiesSet() throws Exception {
        assert rootPath!=null;
        Path path=new File(rootPath).toPath();
        FileUtils.forceMkdir(path.toFile());
    }

    public List<WebDirFile> getFileList(final String relativePath, Boolean onlyDir) throws IOException {
        final String finalPath=rootPath+"/"+relativePath;
        List<WebDirFile> filelsit = new ArrayList<>();
        Files.walkFileTree(Paths.get(finalPath), new HashSet<>(), 1, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                WebDirFile df = new WebDirFile(relativePath, file, attrs);
                if(onlyDir){
                    if(df.isDir()){
                        filelsit.add(df);
                    }
                }else {
                    filelsit.add(df);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return filelsit;
    }


    public File getFile(String relativePath) throws IOException {
        final String finalPath=rootPath+"/"+relativePath;
        return new File(finalPath);
    }


    public String getRemoteDownloadDir() throws IOException {
        String finalPath=rootPath+"/remoteDownload";
        File file=new File(finalPath);
        if(!file.exists()){
            FileUtils.forceMkdir(file);
        }
        return finalPath;
    }
}
