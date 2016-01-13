package api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import utils.ConstantUtil;
import utils.HttpUtil;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by liverliu on 1/12/16.
 */
public abstract class BaseApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseApi.class);

    @RequestMapping(value = "*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route1(HttpServletRequest request) throws Exception {
        return deal(request);
    }

    @RequestMapping(value = "*/*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route2(HttpServletRequest request) throws Exception {
        return deal(request);
    }

    @RequestMapping(value = "*/*/*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route3(HttpServletRequest request) throws Exception {
        return deal(request);
    }

    @RequestMapping(value = "*/*/*/*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route4(HttpServletRequest request) throws Exception {
        return deal(request);
    }

    @RequestMapping(value = "*/*/*/*/*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route5(HttpServletRequest request) throws Exception {
        return deal(request);
    }

    private String deal(HttpServletRequest request) {
        try {
            LOGGER.info("-----------------------");
            LOGGER.info(request.getRequestURI());
            JSONObject response = new JSONObject(HttpUtil.post(ConstantUtil.getProperty("ip"), request));
            return modify(response, request.getRequestURI());
        } catch (Exception ex) {
            LOGGER.error("error!", ex);
            return "";
        }
    }

    private String modify(JSONObject response, String uri) {
        if(uri.startsWith("/eapi/v1/album/")) {
            LOGGER.info("modify album info");
            if(response.has("songs")) {
                JSONArray songs = response.getJSONArray("songs");
                for(int i=0;i<songs.length();i++) {
                    JSONObject song = songs.getJSONObject(i);
                    JSONObject privilege = song.getJSONObject("privilege");
                    if(privilege != null && privilege.has("st") && privilege.getInt("st")<0) {
                        privilege.put("st", 0);
                        privilege.put("cs", false);
                        privilege.put("subp", 1);
                        privilege.put("fl", privilege.getInt("maxbr"));
                        privilege.put("dl", privilege.getInt("maxbr"));
                        privilege.put("pl", privilege.getInt("maxbr"));
                        privilege.put("sp", 7);
                        privilege.put("cp", 1);
                    }
                }
            }
        } else if(uri.equals("/eapi/v3/song/detail/")) {
            LOGGER.info("modify songs privileges");
            if(response.has("privileges")) {
                JSONArray privileges = response.getJSONArray("privileges");
                for(int i=0;i<privileges.length();i++) {
                    JSONObject privilege = privileges.getJSONObject(i);
                    if(privilege != null && privilege.has("st") && privilege.getInt("st")<0) {
                        privilege.put("st", 0);
                        privilege.put("cs", false);
                        privilege.put("subp", 1);
                        privilege.put("fl", privilege.getInt("maxbr"));
                        privilege.put("dl", privilege.getInt("maxbr"));
                        privilege.put("pl", privilege.getInt("maxbr"));
                        privilege.put("sp", 7);
                        privilege.put("cp", 1);
                    }
                }
            }
        } else if(uri.equals("/eapi/v3/playlist/detail")) {
            LOGGER.info("modify songs info");
        } else if(uri.equals("/eapi/copyright/restrict/")) {

        } else if(uri.equals("/eapi/song/enhance/player/url")) {
            LOGGER.info("FUCK");
        }
        return response.toString();
    }

}
