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
package com.wavemaker.runtime.rest.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.runtime.rest.service.RestRuntimeService;

/**
 * @author Uday Shankar
 */
public class RestRuntimeController {

    @Autowired
    private RestRuntimeService restRuntimeService;

    public void handleRequest(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws Exception {
        String pathInfo = httpServletRequest.getPathInfo();
        String[] split = pathInfo.split("/");
        if (split.length < 3) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.invalid.rest.service.url"));
        }
        final String serviceId = split[1];
        if (StringUtils.isBlank(serviceId)) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.empty.serviceId"));
        }
        final String path = pathInfo.replaceFirst("/" + serviceId, "");
        if (StringUtils.isBlank(path)) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.empty.path"));
        }

        try {
            restRuntimeService.executeRestCall(serviceId, path, httpServletRequest, httpServletResponse, EncodingMode.TEMPLATE_AND_VALUES);
        } catch (WMRuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new WMRuntimeException(MessageResource.REST_SERVICE_INVOKE_FAILED, e);
        }
    }
}
