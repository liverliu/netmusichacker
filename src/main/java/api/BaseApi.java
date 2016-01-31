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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.Random;

/**
 * Created by liverliu on 1/12/16.
 */
public abstract class BaseApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseApi.class);

    @RequestMapping(value = "*", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String route1(HttpServletRequest request) throws Exception {
        return deal(request);
    }

    @RequestMapping(value = "*/*", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String route2(HttpServletRequest request) throws Exception {
        return deal(request);
    }

    @RequestMapping(value = "*/*/*", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String route3(HttpServletRequest request) throws Exception {
        return deal(request);
    }

    @RequestMapping(value = "*/*/*/*", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String route4(HttpServletRequest request) throws Exception {
        return deal(request);
    }

    @RequestMapping(value = "*/*/*/*/*", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String route5(HttpServletRequest request) throws Exception {
        return deal(request);
    }

    private String deal(HttpServletRequest request) throws Exception {
        String tmp="";
        try {
            LOGGER.info("-----------------------");
            LOGGER.info(request.getRequestURI());
            tmp = HttpUtil.post(ConstantUtil.getProperty("ip"), request);
            JSONObject result = new JSONObject(tmp);
            JSONObject response = modify(result, request.getRequestURI());
            return response.toString();
        } catch (Exception ex) {
            LOGGER.error("error!", ex);
        }
        return tmp;
    }

    private JSONObject modify(JSONObject result, String uri) throws Exception {
        if(uri.startsWith("/eapi/v1/album/")) {
            LOGGER.info("modify album info");
            if(result.has("songs")) {
                JSONArray songs = result.getJSONArray("songs");
                for(int i=0;i<songs.length();i++) {
                    JSONObject song = songs.getJSONObject(i);
                    modifyPrivilege(song.getJSONObject("privilege"));
                }
            }
        } else if(uri.equals("/eapi/v3/song/detail/")) {
            LOGGER.info("modify songs privileges");
            if(result.has("privileges")) {
                JSONArray privileges = result.getJSONArray("privileges");
                for(int i=0;i<privileges.length();i++) {
                    modifyPrivilege(privileges.getJSONObject(i));
                }
            }
        } else if(uri.equals("/eapi/v3/playlist/detail")) {
            LOGGER.info("modify songs info");
        } else if(uri.equals("/eapi/song/enhance/player/url")) {
            JSONObject data = result.getJSONArray("data").getJSONObject(0);
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
                data.put("md5", music.getLong("dfsId"));
            }
            LOGGER.info(result.toString());
        } else if(uri.equals("/eapi/batch")) {
            LOGGER.info("modify search result");
            JSONObject search  = result.getJSONObject("/api/cloudsearch/pc");
            if(search.getInt("code")==200) {
                JSONArray songs = search.getJSONObject("result").getJSONArray("songs");
                for(int i=0;i<songs.length();i++) {
                    modifyPrivilege(songs.getJSONObject(i).getJSONObject("privilege"));
                }
            }
        } else if(uri.equals("/eapi/cloudsearch/pc")) {
            if(result.getInt("code")==200) {
                JSONArray songs = result.getJSONObject("result").getJSONArray("songs");
                for(int i=0;i<songs.length();i++) {
                    modifyPrivilege(songs.getJSONObject(i).getJSONObject("privilege"));
                }
            }
        }
        return result;
    }

    private void modifyPrivilege(JSONObject privilege) {
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
