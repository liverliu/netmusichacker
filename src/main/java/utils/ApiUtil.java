package utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;

/**
 * Created by liverliu on 1/31/16.
 */
public class ApiUtil {

    public static JSONObject musicDetail(String id) {
        String url = "http://music.163.com/api/song/detail?ids=[%s]";
        url = String.format(url, id);
        JSONObject response = new JSONObject(HttpUtil.get(url));
        return response.getJSONArray("songs").getJSONObject(0);
    }

    public static void main(String args[]) {
        System.out.println(musicDetail("27128037").toString());
    }
}
