package helper;

import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class OpenStreetMapLabelResolver {

    final public static String id = "http://www.openstreetmap.org";

    public static String lookup(String uri) {

        try {
            URL url = new URL(uri);
            Map<String, String> map = new LinkedHashMap<String, String>();
            String query = url.getQuery();
            for (String pair : query.split("&")) {
                String[] keyValue = pair.split("=");
                int idx = pair.indexOf("=");
                map.put(URLDecoder.decode(keyValue[0], "UTF-8"), URLDecoder.decode(keyValue[1], "UTF-8"));
            }
            return map.get("mlat") + "," + map.get("mlon");
        } catch (Exception e) {
            play.Logger.debug("", e);
        }
        return uri;
    }
}
