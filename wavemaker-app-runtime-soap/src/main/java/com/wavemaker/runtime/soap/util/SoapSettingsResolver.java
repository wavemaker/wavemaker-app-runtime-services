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
package com.wavemaker.runtime.soap.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;

import org.apache.commons.lang3.StringUtils;

import com.wavemaker.runtime.soap.SoapCacheCleanUpHandler;
import com.wavemaker.runtime.soap.SoapServiceSettings;
import com.wavemaker.runtime.ws.JAXWSProperties;

/**
 * @author Frankie Fu
 */
public class SoapSettingsResolver {

    private SoapSettingsResolver() {
    }

    @SuppressWarnings("unchecked")
    public static void setBindingProperties(BindingProvider service, SoapServiceSettings bindingProperties) {
        if (bindingProperties != null) {
            Map<String, Object> requestContext = service.getRequestContext();

            if (StringUtils.isNotEmpty(bindingProperties.getEndpointAddress())) {
                requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, bindingProperties.getEndpointAddress());
            }

            if (StringUtils.isNotEmpty(bindingProperties.getHttpBasicAuthUsername())) {
                requestContext.put(BindingProvider.USERNAME_PROPERTY, bindingProperties.getHttpBasicAuthUsername());
                requestContext.put(BindingProvider.PASSWORD_PROPERTY, bindingProperties.getHttpBasicAuthPassword());
            }

            requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, bindingProperties.getConnectionTimeout());
            requestContext.put(JAXWSProperties.REQUEST_TIMEOUT, bindingProperties.getRequestTimeout());

            if (StringUtils.isNotEmpty(bindingProperties.getSoapActionURI())) {
                requestContext.put(BindingProvider.SOAPACTION_USE_PROPERTY, true);
                requestContext.put(BindingProvider.SOAPACTION_URI_PROPERTY, bindingProperties.getSoapActionURI());
            }

            Map<String, String> httpHeaders = bindingProperties.getHttpHeaders();
            if (httpHeaders != null && !httpHeaders.isEmpty()) {
                Map<String, List<String>> reqHeaders = (Map<String, List<String>>) requestContext.get(MessageContext.HTTP_REQUEST_HEADERS);
                if (reqHeaders == null) {
                    reqHeaders = new HashMap<>();
                    requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, reqHeaders);
                }
                for (Entry<String, String> entry : httpHeaders.entrySet()) {
                    List<String> list = new ArrayList<>();
                    list.add(entry.getValue());

                    reqHeaders.put(entry.getKey(), list);
                }
            }

            Map<String, Object> customProperties = bindingProperties.getRequestContextProperties();
            if (customProperties != null && !customProperties.isEmpty()) {
                for (Entry<String, Object> entry : customProperties.entrySet()) {
                    requestContext.put(entry.getKey(), entry.getValue());
                }
            }
        }
        addSoapCacheCleanUpHandler(service);
    }

    private static void addSoapCacheCleanUpHandler(BindingProvider service) {
        List<Handler> handlerChain = service.getBinding().getHandlerChain();
        handlerChain.add(new SoapCacheCleanUpHandler());
        service.getBinding().setHandlerChain(handlerChain);
    }
}
