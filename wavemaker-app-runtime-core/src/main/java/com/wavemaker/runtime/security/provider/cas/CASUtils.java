package com.wavemaker.runtime.security.provider.cas;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ArjunSahasranam on 9/6/16.
 */
public class CASUtils {
    private CASUtils(){

    }

    public static boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    public static String getServiceUrl(HttpServletRequest request){
        StringBuffer requestURL = request.getRequestURL();
        String contextPath = request.getContextPath();

        String serviceHostUrl = requestURL.substring(0, requestURL.lastIndexOf(contextPath));
        String serviceUrl = serviceHostUrl + contextPath;
        return serviceUrl;
    }
}
