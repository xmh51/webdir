package ai.xmh.yunpan.webdir.service.server.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import ai.xmh.yunpan.webdir.service.server.service.Aria2Service;
import ai.xmh.yunpan.webdir.service.server.service.WebDirFileService;
import okhttp3.Headers;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

@RequestMapping("/remoteDownload")
@Controller
public class RemoteDownloadController {
	@Autowired
	private Aria2Service aria2Service;
	@Autowired
	private WebDirFileService webDirFileService;
	/**
	 * aria2 下载
	 *
	 * @return
	 */
	@RequestMapping(value = "/aria2", method = RequestMethod.POST)
	public void getFileLst(@RequestBody String requstBody, HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {
		if(request.getHeader("Content-Type").contains("application/x-www-form-urlencoded")){
			Enumeration<String> parameterNames=request.getParameterNames();
			while (parameterNames.hasMoreElements()){
				String str=parameterNames.nextElement();
				if(str.contains("jsonrpc")){
					requstBody=str;
				}
			}
		}
		Response response=null;
		try {
			JSONArray jsonArray=JSON.parseArray(requstBody);
			for(int i=0;i<jsonArray.size();i++){
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				setDirToAddMethod(jsonObject);
			}
			response= aria2Service.batchExecuteMethod(jsonArray);
		}catch (Exception e){
			JSONObject jsonObject =JSON.parseObject(requstBody);
			setDirToAddMethod(jsonObject);
			response= aria2Service.executeMethod(jsonObject);
		}
		httpServletResponse.setStatus(response.code());
		Headers headers=response.headers();
		for(String name:headers.names()){
			String value=headers.get(name);
			httpServletResponse.setHeader(name,value);
		}
		okhttp3.ResponseBody responseBody=response.body();
		if(responseBody !=null){
			ServletOutputStream servletOutputStream=httpServletResponse.getOutputStream();
			servletOutputStream.write(responseBody.bytes());
			servletOutputStream.flush();
			servletOutputStream.close();
		}

	}

	private void setDirToAddMethod(JSONObject jsonObject) throws IOException {
		if(!jsonObject.toJSONString().contains("aria2.add")){
			return;
		}
		String method= (String) jsonObject.get("method");
		if(method==null){
			method=jsonObject.getString("methodName");
		}
		if("system.multicall".equals(method)){
			JSONArray paramsJSONArray=jsonObject.getJSONArray("params").getJSONArray(0);
			for(int i=0;i<paramsJSONArray.size();i++){
				JSONObject param=  paramsJSONArray.getJSONObject(i);
				setDirToAddMethod(param);
			}
		}
		if(method!=null&&method.contains("aria2.add")){
			JSONArray params= jsonObject.getJSONArray("params");
			JSONObject config=  params.getJSONObject(params.size()-1);
			String remoteDownloadDir=webDirFileService.getRemoteDownloadDir();
			config.put("dir",remoteDownloadDir);
		}
	}
}
