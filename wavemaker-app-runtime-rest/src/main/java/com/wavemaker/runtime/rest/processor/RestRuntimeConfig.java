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
package com.wavemaker.runtime.rest.processor;

import java.util.ArrayList;
import java.util.List;

import com.wavemaker.runtime.rest.processor.request.HttpRequestProcessor;
import com.wavemaker.runtime.rest.processor.response.HttpResponseProcessor;

/**
 * Created by srujant on 19/5/17.
 */
public class RestRuntimeConfig {

    List<HttpRequestProcessor> httpRequestProcessorList;
    List<HttpResponseProcessor> httpResponseProcessorList;

    public RestRuntimeConfig() {
    }

    public RestRuntimeConfig(final RestRuntimeConfig restRuntimeConfig) {
        this.httpRequestProcessorList = new ArrayList<>(restRuntimeConfig.getHttpRequestProcessorList());
        this.httpResponseProcessorList = new ArrayList<>(restRuntimeConfig.getHttpResponseProcessorList());
    }

    public List<HttpRequestProcessor> getHttpRequestProcessorList() {
        return httpRequestProcessorList;
    }

    public void setHttpRequestProcessorList(List<HttpRequestProcessor> httpRequestProcessorList) {
        this.httpRequestProcessorList = httpRequestProcessorList;
    }

    public List<HttpResponseProcessor> getHttpResponseProcessorList() {
        return httpResponseProcessorList;
    }

    public void setHttpResponseProcessorList(List<HttpResponseProcessor> httpResponseProcessorList) {
        this.httpResponseProcessorList = httpResponseProcessorList;
    }

}
