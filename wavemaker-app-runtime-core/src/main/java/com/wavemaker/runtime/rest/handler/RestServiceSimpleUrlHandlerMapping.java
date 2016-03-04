/**
 * Copyright © 2013 - 2016 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.rest.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import com.wavemaker.runtime.rest.model.RestServiceInfoBean;

/**
 * Created by ArjunSahasranam on 7/10/15.
 */

/**
 * This accepts/rejects requests based on URL match pattern and HTTP Methods.
 */

public class RestServiceSimpleUrlHandlerMapping extends SimpleUrlHandlerMapping {
    private final Map<String, List<String>> urlToHttpMethodMap = new HashMap<>();

    public Map<String, List<String>> getUrlToHttpMethodMap() {
        return urlToHttpMethodMap;
    }

    public void addRestServiceInfoBean(final RestServiceInfoBean restServiceInfoBean, final String controller) {
        final String url = restServiceInfoBean.getUrl();
        final String httpMethod = restServiceInfoBean.getHttpMethod();
        final Map<String, ?> urlMap = getUrlMap();

        Map<String, Object> map = new HashMap<>();
        map.put(url, controller);
        map.putAll(urlMap);
        setUrlMap(map);

        List<String> httpMethodList = new ArrayList<>();
        httpMethodList.add(httpMethod);
        getUrlToHttpMethodMap().put(url, httpMethodList);
    }

    @Override
    protected void validateHandler(final Object handler, final HttpServletRequest request) throws Exception {
        final Map<String, List<String>> urlMap = getUrlToHttpMethodMap();
        String pathInfo = request.getPathInfo();
        String method = request.getMethod();
        for (String configUrl : urlMap.keySet()) {
            List<String> configMethods = urlMap.get(configUrl);
            if (configUrl.equals(pathInfo) && !configMethods.contains(method)) {
                throw new HttpRequestMethodNotSupportedException(request.getMethod(), configMethods);
            }
        }
    }
}
