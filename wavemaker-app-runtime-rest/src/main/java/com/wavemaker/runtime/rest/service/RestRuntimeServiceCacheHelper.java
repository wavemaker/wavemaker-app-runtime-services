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
package com.wavemaker.runtime.rest.service;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.core.env.Environment;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.io.ClassPathFile;
import com.wavemaker.commons.io.File;
import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.commons.util.FileValidationUtils;
import com.wavemaker.runtime.commons.WMAppContext;
import com.wavemaker.runtime.commons.util.PropertyPlaceHolderReplacementHelper;
import com.wavemaker.runtime.rest.model.RestServiceInfoBean;
import com.wavemaker.runtime.rest.processor.RestRuntimeConfig;
import com.wavemaker.runtime.rest.processor.data.HttpRequestDataProcessor;
import com.wavemaker.runtime.rest.processor.data.XWMPrefixDataProcessor;
import com.wavemaker.tools.apidocs.tools.core.model.Swagger;

/**
 * @author Uday Shankar
 */
public class RestRuntimeServiceCacheHelper {

    private Map<String, Swagger> serviceIdVsSwaggerCache = Collections.synchronizedMap(new WeakHashMap<>());
    private PropertyPlaceHolderReplacementHelper propertyPlaceHolderReplacementHelper;
    private Environment environment;

    public Swagger getSwaggerDoc(String serviceId) {
        if (!serviceIdVsSwaggerCache.containsKey(serviceId)) {
            try {
                File apiTargetJsonFile = new ClassPathFile(Thread.currentThread().getContextClassLoader(), FileValidationUtils.validateFilePath(serviceId + "_apiTarget.json"));
                Reader reader = propertyPlaceHolderReplacementHelper.getPropertyReplaceReader(apiTargetJsonFile, environment);
                Swagger swaggerDoc = JSONUtils.toObject(reader, Swagger.class);
                serviceIdVsSwaggerCache.put(serviceId, swaggerDoc);
            } catch (IOException e) {
                throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.failed.to.read.swagger"), e, serviceId);
            }
        }
        return serviceIdVsSwaggerCache.get(serviceId);
    }

    public List<HttpRequestDataProcessor> getHttpRequestDataProcessors(String serviceId) {
        List<HttpRequestDataProcessor> httpRequestDataProcessors = new ArrayList<>();
        httpRequestDataProcessors.add(new XWMPrefixDataProcessor());
        return httpRequestDataProcessors;
    }

    public RestRuntimeConfig getAppRuntimeConfig(String serviceId) {
        RestServiceInfoBean restServiceInfoBean = WMAppContext.getInstance().getSpringBean(serviceId + "ServiceInfo");
        return restServiceInfoBean.getRestRuntimeConfig();
    }

    public void setPropertyPlaceHolderReplacementHelper(PropertyPlaceHolderReplacementHelper propertyPlaceHolderReplacementHelper) {
        this.propertyPlaceHolderReplacementHelper = propertyPlaceHolderReplacementHelper;
    }

    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }
}
