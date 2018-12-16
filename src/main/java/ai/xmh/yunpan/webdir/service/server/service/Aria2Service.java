package ai.xmh.yunpan.webdir.service.server.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author by xmh, Date on 2018/12/8.
 */
@Service
public class Aria2Service {
    @Value("${aria2.url}")
    private String url;
    @Value("${aria2.token}")
    private String token;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    private Response post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    public Response executeMethod(JSONObject jsonObject) throws IOException {
        setTokenToAddMethod(jsonObject);
        return post(url, JSONObject.toJSONString(jsonObject));
    }

    public Response batchExecuteMethod(JSONArray jsonArray) throws IOException {
        for(int i=0;i<jsonArray.size();i++){
            JSONObject jsonObject=jsonArray.getJSONObject(i);
            setTokenToAddMethod(jsonObject);
        }
        return post(url, JSONObject.toJSONString(jsonArray));
    }

    private void setTokenToAddMethod(JSONObject jsonObject)  {
        if(StringUtils.isNotEmpty(token)){
            String method=jsonObject.getString("method");
            if(method==null){
                method=jsonObject.getString("methodName");
            }
            if("system.multicall".equals(method)){
                JSONArray paramsJSONArray=jsonObject.getJSONArray("params").getJSONArray(0);
                for(int i=0;i<paramsJSONArray.size();i++){
                    JSONObject param=  paramsJSONArray.getJSONObject(i);
                    setTokenToAddMethod(param);
                }
            }else {
                JSONArray params= jsonObject.getJSONArray("params");
                if(params==null){
                    params=new JSONArray();
                    jsonObject.put("params",params);
                }
                boolean flag=true;
                for(int i=0;i<params.size();i++){
                    String param=  params.getString(i);
                    if(param.contains("token:")){
                        param= "token:"+token;
                        params.set(i,param);
                        flag=false;
                    }
                }
                if(flag){
                    params.add(0,"token:"+token);
                }
            }
        }
    }
}
