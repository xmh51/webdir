package ai.xmh.yunpan.webdir.service.server.utils;


import lombok.extern.slf4j.Slf4j;
import ai.xmh.yunpan.webdir.service.server.exception.WebDirException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @note 来自 PotatoCloudDrive
 */
@Slf4j
public class WebDirFileUtil {
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d*)-(?<end>\\d*)");
    /**
     * 文件下载
     *
     * @param file
     * @param request
     * @param response
     * @throws WebDirException
     * @throws IOException
     */
    public static void filePlayOrDownload(boolean playFlag, File file, HttpServletRequest request, HttpServletResponse response)
            throws WebDirException, IOException {
        Path path=file.toPath();
        if (!Files.exists(path)&&Files.isDirectory(path)) {
            throw new WebDirException("文件不存在");
        }

        long fileSize = file.length();

        long[] pointArray = getStartEndPointFromHeaderRange(fileSize, request);

        response.reset();
        if (!StringUtils.isEmpty(request.getHeader("Range"))) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range",
                    String.format("bytes %s-%s/%s", pointArray[0], pointArray[1], fileSize));
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }

        response.setBufferSize(10485760);
        response.setHeader("Content-Disposition",
                String.format("inline;filename=\"%s\"", encodeFileName(request,path.getFileName().toString())));
        response.setHeader("Accept-Ranges", "bytes");
        response.setDateHeader("Last-Modified", Files.getLastModifiedTime(path).toMillis());
        response.setDateHeader("Expires", System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        response.setContentType(playFlag ? Files.probeContentType(path) : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Length", String.format("%s", pointArray[1] - pointArray[0] + 1));
        SeekableByteChannel input = null;
        OutputStream output = null;
        try {
            input = Files.newByteChannel(path, StandardOpenOption.READ);
            output = response.getOutputStream();
            ByteBuffer buffer = ByteBuffer.allocate(10485760);
            input.position(pointArray[0]);
            int hasRead;
            while ((hasRead = input.read(buffer)) != -1) {
                buffer.clear();
                output.write(buffer.array(), 0, hasRead);
            }
            response.flushBuffer();
        } catch (IllegalStateException e) {
            log.error("filePlayOrDownload",e);
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.flush();
                output.close();
            }
        }
    }

    /**
     * 从请求头Range中获取开始和结束点
     *
     * @param request
     * @return
     */
    private static long[] getStartEndPointFromHeaderRange(long fileSize, HttpServletRequest request) {
        long[] pointArray = new long[2];
        // 开始点
        long startPoint = 0;
        // 结束点
        long endPoint = fileSize - 1;
        String range = request.getHeader("Range");
        range = StringUtils.isEmpty(range) ? "" : range;
        Matcher matcher = RANGE_PATTERN.matcher(range);
        if (matcher.matches()) {
            String startGroup = matcher.group("start");
            startPoint =  StringUtils.isEmpty(startGroup) ? startPoint : Integer.valueOf(startGroup);
            startPoint = startPoint < 0 ? 0 : startPoint;

            String endGroup = matcher.group("end");
            endPoint =  StringUtils.isEmpty(endGroup) ? endPoint : Integer.valueOf(endGroup);
            endPoint = endPoint > fileSize - 1 ? fileSize - 1 : endPoint;

        }
        pointArray[0] = startPoint;
        pointArray[1] = endPoint;
        return pointArray;
    }

    /**
     * 下载文件名中含有中文的处理
     * @param req
     * @param headName
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encodeFileName(HttpServletRequest req, String headName)
            throws UnsupportedEncodingException {
        String filename="";//IE9之前包括IE9都包含MSIE;IE10之后都包含Trident;edge浏览器包含Edge
        String userAgent=req.getHeader("User-Agent");
        if (userAgent.contains("MSIE") ||userAgent.contains("Trident")||userAgent.contains("Edge")) {
            filename = URLEncoder.encode(headName, "UTF-8");
        } else {
            filename = new String(headName.getBytes("UTF-8"), "ISO8859-1");
        }
        return filename;
    }
}
