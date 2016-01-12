package api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import utils.ConstantUtil;
import utils.HttpUtil;

/**
 * Created by liverliu on 1/12/16.
 */
@Controller
@RequestMapping("/api")
public class HackerAPI extends BaseApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(HackerAPI.class);

    @RequestMapping(value = "/song/detail", method = {RequestMethod.GET}, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String SongDetail(@RequestParam String ids) throws Exception {
        String url = ConstantUtil.getProperty("ip")+"/api/song/detail?ids="+ids;
        LOGGER.info(url);
        JSONObject response = new JSONObject(HttpUtil.get(url));
        JSONArray songs = response.getJSONArray("songs");
        for(int i=0; i<songs.length(); i++) {
            JSONObject song = songs.getJSONObject(i);
            if(song.has("status") && song.getInt("status")<=0) {
                song.put("status", 0);
            }
        }
        return response.toString();
    }

}
