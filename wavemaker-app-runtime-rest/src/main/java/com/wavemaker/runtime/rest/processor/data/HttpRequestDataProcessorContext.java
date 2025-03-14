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
package com.wavemaker.runtime.rest.processor.data;

import jakarta.servlet.http.HttpServletRequest;

import com.wavemaker.runtime.rest.model.HttpRequestData;

/**
 * @author Uday Shankar
 */
public class HttpRequestDataProcessorContext {

    private HttpServletRequest httpServletRequest;

    private HttpRequestData httpRequestData;

    public HttpRequestDataProcessorContext(HttpServletRequest httpServletRequest, HttpRequestData httpRequestData) {
        this.httpServletRequest = httpServletRequest;
        this.httpRequestData = httpRequestData;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public HttpRequestData getHttpRequestData() {
        return httpRequestData;
    }
}
