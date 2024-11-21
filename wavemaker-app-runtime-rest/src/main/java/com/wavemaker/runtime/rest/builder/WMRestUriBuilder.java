/*******************************************************************************
 * Copyright (C) 2024-2025 WaveMaker, Inc.
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

package com.wavemaker.runtime.rest.builder;

import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

public class WMRestUriBuilder implements UriBuilder {

    @Override
    public String getEndpointAddress(String scheme, String host, String basePath, String pathValue, MultiValueMap<String, String> queryParameters,
                                     Map<String, String> pathParameters, EncodingMode encodingMode) {
        String url = scheme + "://" + host + getNormalizedString(basePath) + getNormalizedString(pathValue);

        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        encodingMode = (encodingMode != null) ? encodingMode : EncodingMode.TEMPLATE_AND_VALUES;
        uriBuilderFactory.setEncodingMode(encodingMode);

        org.springframework.web.util.UriBuilder uriBuilder = uriBuilderFactory.uriString(url);
        updateUrlWithQueryParams(uriBuilder, queryParameters);
        return uriBuilder.build(pathParameters).toString();
    }

    private void updateUrlWithQueryParams(org.springframework.web.util.UriBuilder uriBuilder, MultiValueMap<String, String> queryParameters) {
        queryParameters.forEach((key, valueList) -> {
            valueList.forEach(value -> uriBuilder.queryParam(key, value));
        });
    }
}
