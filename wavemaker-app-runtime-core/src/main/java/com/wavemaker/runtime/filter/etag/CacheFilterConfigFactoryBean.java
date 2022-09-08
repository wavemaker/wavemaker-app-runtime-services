/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wavemaker.runtime.filter.etag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.CollectionUtils;

/**
 * Created by srujant on 26/3/19.
 */
public class CacheFilterConfigFactoryBean implements FactoryBean<CacheFilterConfig> {

    @Value("${app.build.ui.mode}")
    private String buildMode;

    @Value("${app.build.ui.ng.config}")
    private String buildArgs;

    private List<String> angularCachedContentPath = Arrays.asList("/ng-bundle/**");

    private List<String> angularCacheExclusionPath = Arrays.asList("/ng-bundle/path_mapping.json");

    private List<String> etagContentPath = Arrays.asList("/**");


    @Override
    public CacheFilterConfig getObject() throws Exception {
        CacheFilterConfig cacheFilterConfig = new CacheFilterConfig();
        if ("angular".equals(buildMode) && StringUtils.isNotBlank(buildArgs)) {
            cacheFilterConfig.setCacheRequestMatcher(getOrRequestMatcher(angularCachedContentPath));
            cacheFilterConfig.setCacheExclusionRequestMatcher(getOrRequestMatcher(angularCacheExclusionPath));
        }
        cacheFilterConfig.setEtagRequestMatcher(getOrRequestMatcher(etagContentPath));
        return cacheFilterConfig;
    }

    @Override
    public Class<?> getObjectType() {
        return CacheFilterConfig.class;
    }

    private RequestMatcher getOrRequestMatcher(List<String> urlPatterns) {
        if (CollectionUtils.isEmpty(urlPatterns)) {
            return null;
        }

        List<RequestMatcher> antPathRequestMatchers = new ArrayList<>();
        for (String pattern : urlPatterns) {
            antPathRequestMatchers.add(new AntPathRequestMatcher(pattern, HttpMethod.GET.name()));
        }
        return new OrRequestMatcher(antPathRequestMatchers);
    }

    // Access the below setters from spring xml if required
    public void setAngularCachedContentPath(List<String> angularCachedContentPath) {
        this.angularCachedContentPath = angularCachedContentPath;
    }

    public void setAngularCacheExclusionPath(List<String> angularCacheExclusionPath) {
        this.angularCacheExclusionPath = angularCacheExclusionPath;
    }

    public void setEtagContentPath(List<String> etagContentPath) {
        this.etagContentPath = etagContentPath;
    }
}
