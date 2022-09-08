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

package com.wavemaker.runtime;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import com.wavemaker.runtime.security.xss.WMRequestResponseBodyMethodProcessor;
import com.wavemaker.runtime.security.xss.handler.XSSSecurityHandler;

public class WMArgumentResolverAndReturnValueHandler implements InitializingBean {

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerMapping;

    @Override
    public void afterPropertiesSet() throws Exception {
        WMRequestResponseBodyMethodProcessor wmRequestResponseBodyMethodProcessor = new WMRequestResponseBodyMethodProcessor(requestMappingHandlerMapping.getMessageConverters());
        if (XSSSecurityHandler.getInstance().isInputSanitizationEnabled()) {
            enableInputEncoding(wmRequestResponseBodyMethodProcessor);
        }
        if (XSSSecurityHandler.getInstance().isOutputSanitizationEnabled()) {
            enableOutputEncoding(wmRequestResponseBodyMethodProcessor);
        }
    }

    public void enableInputEncoding(WMRequestResponseBodyMethodProcessor wmRequestResponseBodyMethodProcessor) {
        List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerMapping.getArgumentResolvers();
        List<HandlerMethodArgumentResolver> customArgumentResolvers = new ArrayList<>();
        for (HandlerMethodArgumentResolver handler : argumentResolvers) {
            if (handler instanceof RequestResponseBodyMethodProcessor) {
                customArgumentResolvers.add(wmRequestResponseBodyMethodProcessor);
            } else {
                customArgumentResolvers.add(handler);
            }
        }
        requestMappingHandlerMapping.setArgumentResolvers(customArgumentResolvers);
    }

    public void enableOutputEncoding(WMRequestResponseBodyMethodProcessor wmRequestResponseBodyMethodProcessor) {
        List<HandlerMethodReturnValueHandler> returnValueHandlers = requestMappingHandlerMapping.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> customReturnValueHandlers = new ArrayList<>();
        for (HandlerMethodReturnValueHandler handler : returnValueHandlers) {
            if (handler instanceof RequestResponseBodyMethodProcessor) {
                customReturnValueHandlers.add(wmRequestResponseBodyMethodProcessor);
            } else {
                customReturnValueHandlers.add(handler);
            }
        }
        requestMappingHandlerMapping.setReturnValueHandlers(customReturnValueHandlers);
    }
}
