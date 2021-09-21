package com.wavemaker.runtime.util;

import java.util.HashMap;
import java.util.Map;

public class WMHeaderUtils {
    private static ThreadLocal<Map<String, String>> threadLocal = ThreadLocal.withInitial(HashMap::new);

    public static Map<String, String> getHeaders() {
        return threadLocal.get();
    }

    public static void addHeader(String key, String value) {
        threadLocal.get().put(key, value);
    }

    public static void setHeaders(Map<String, String> headers) {
        threadLocal.set(headers);
    }

    public static void clear() {
        threadLocal.set(new HashMap<>());
    }
}
