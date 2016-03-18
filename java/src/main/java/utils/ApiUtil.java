package utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by liverliu on 1/31/16.
 */
public class ApiUtil {

    public static JSONObject musicDetail(String id) {
        String url = "%s/api/song/detail?ids=[%s]";
        url = String.format(url, ConstantUtil.getProperty("ip"), id);
        JSONObject response = new JSONObject(HttpUtil.get(url));
        JSONObject song = response.getJSONArray("songs").getJSONObject(0);
        if(!song.has("hMusic") && !song.has("mMusic") && !song.has("lMusic") && !song.has("bMusic")) {
            JSONObject album = albumDetail(song);
            JSONArray songs = album.getJSONArray("songs");
            for(int i=0; i<songs.length(); i++) {
                JSONObject song1 = songs.getJSONObject(i);
                if(song.getLong("id") == song1.getLong("id")) {
                    return song1;
                }
            }
        }
        return song;
    }

    public static JSONObject albumDetail(JSONObject song) {
        String url = "%s/api/album/%s";
        url = String.format(url, ConstantUtil.getProperty("ip"), song.getJSONObject("album").getLong("id"));
        JSONObject response = new JSONObject(HttpUtil.get(url));
        return response.getJSONObject("album");
    }

    public static void main(String args[]) {
        System.out.println(musicDetail("27128037").toString());
    }
}
