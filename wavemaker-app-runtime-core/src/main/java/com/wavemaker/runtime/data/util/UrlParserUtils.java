package com.wavemaker.runtime.data.util;

public class UrlParserUtils {

    /*
     * trims schemes {http, https} and port numbers and basepath
     * */
    public static String trimUrlForHostName(String host) {
        if (host != null && !host.isEmpty()) {
            String sanitisedData = host;
            sanitisedData = sanitisedData.trim();
            if (sanitisedData.startsWith("http://")) {
                sanitisedData = sanitisedData.replace("http://", "");
            } else if (sanitisedData.startsWith("https://")) {
                sanitisedData = sanitisedData.replace("https://", "");
            }
            if (sanitisedData.contains("/")) {
                sanitisedData = sanitisedData.substring(0, sanitisedData.indexOf("/"));
            }
            return sanitisedData;
        }
        return host;
    }
}
