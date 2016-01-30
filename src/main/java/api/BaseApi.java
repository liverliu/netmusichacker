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
import sun.misc.BASE64Encoder;
import utils.ApiUtil;
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
import java.security.MessageDigest;
import java.util.Random;

/**
 * Created by liverliu on 1/12/16.
 */
public abstract class BaseApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseApi.class);

    @RequestMapping(value = "*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route1(@RequestBody String body, HttpServletRequest request) throws Exception {
        return deal(body, request);
    }

    @RequestMapping(value = "*/*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route2(@RequestBody String body, HttpServletRequest request) throws Exception {
        return deal(body, request);
    }

    @RequestMapping(value = "*/*/*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route3(@RequestBody String body, HttpServletRequest request) throws Exception {
        return deal(body, request);
    }

    @RequestMapping(value = "*/*/*/*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route4(@RequestBody String body, HttpServletRequest request) throws Exception {
        return deal(body, request);
    }

    @RequestMapping(value = "*/*/*/*/*", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String route5(@RequestBody String body, HttpServletRequest request) throws Exception {
        return deal(body, request);
    }

    private String deal(String body, HttpServletRequest request) throws Exception {
        try {
            LOGGER.info("-----------------------");
            LOGGER.info(request.getRequestURI());
            JSONObject response = new JSONObject(HttpUtil.post(ConstantUtil.getProperty("ip"), request, body));
            return modify(body, response, request.getRequestURI());
        } catch (Exception ex) {
            LOGGER.error("error!", ex);
            return "";
        }
    }

    private String modify(String body, JSONObject response, String uri) throws Exception {
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
            JSONObject data = response.getJSONArray("data").getJSONObject(0);
            if(data.getInt("code") != 200) {
                LOGGER.info("尝试生成Url");
                String id = String.valueOf(data.getLong("id"));
                JSONObject song = ApiUtil.musicDetail(id);
                JSONObject music = song.getJSONObject("hMusic");
                data.put("code", 200);
                data.put("type", "mp3");
                String url = genUrl(song);
                data.put("url", url);
                data.put("gain", music.getInt("volumeDelta"));
                data.put("br", music.getInt("bitrate"));
                data.put("size", music.getInt("size"));
                data.put("md5", music.getInt("dfsId"));
            }
            LOGGER.info(response.toString());
        }
        return response.toString();
    }

    private String genUrl(JSONObject song) throws Exception {
        JSONObject music = song.getJSONObject("hMusic");
        String songId = String.valueOf(music.getLong("dfsId"));
        String encId = encrypt(songId);
        return String.format("http://m%d.music.126.net/%s/%s.mp3",
                new Random().nextInt(2)+1, encId, songId);
    }

    private String encrypt(String id) throws Exception {
        byte magic[] = "3go8&$8*3*3h0k(2)2".getBytes("utf-8");
        byte songId[] = id.getBytes("utf-8");
        int magic_len = magic.length;
        for(int i=0; i<songId.length; i++) {
            songId[i] = (byte) (songId[i]^magic[i%magic_len]);
        }
        String result = md_5(songId);
        result = result.replace("/", "_");
        result = result.replace("+", "-");
        return result;
    }

    private String md_5(byte str[]) throws Exception {
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        return base64en.encode(md5.digest(str));
    }

}
