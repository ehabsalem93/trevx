package trevx.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ptk on 6/25/16.
 */
public class String_ytil {
    public String edit_songName(String Name) {
        if (Name.toCharArray().length > 24)
            return Name.substring(0, 23) + "...";
        return Name;
    }

    public String getHostName(String urlInput) {
        urlInput = urlInput.toLowerCase();
        String hostName = urlInput;
        if (!urlInput.equals("")) {
            if (urlInput.startsWith("http") || urlInput.startsWith("https")) {
                try {
                    URL netUrl = new URL(urlInput);
                    String host = netUrl.getHost();
                    if (host.startsWith("www")) {
                        hostName = host.substring("www".length() + 1);
                    } else {
                        hostName = host;
                    }
                } catch (MalformedURLException e) {
                    hostName = urlInput;
                }
            } else if (urlInput.startsWith("www")) {
                hostName = urlInput.substring("www".length() + 1);
            }
            return hostName;
        } else {
            return "unDetected";
        }
    }

    public String edit_songNamefor_small_player(String Name, int length) {
        if (Name.toCharArray().length > length)
            return Name.substring(0, length) + "...";

        return Name;
    }
}
