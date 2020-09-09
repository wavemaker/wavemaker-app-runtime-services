/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.util;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.wavemaker.commons.InvalidInputException;
import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.runtime.data.model.queries.QueryParameter;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 5/5/17
 */
public class MultipartQueryUtils {

    private MultipartQueryUtils(){}

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipartQueryUtils.class);

    public static <T> T readContent(MultipartHttpServletRequest request, Class<T> type) {
        MultipartFile multipartFile = request.getFile(WMMultipartUtils.WM_DATA_JSON);
        T instance;
        try {
            if (multipartFile == null) {
                final String wmJson = request.getParameter(WMMultipartUtils.WM_DATA_JSON);
                if (StringUtils.isBlank(wmJson)) {
                    LOGGER.error("Request does not have wm_data_json multipart data");
                    throw new InvalidInputException(MessageResource.create("com.wavemaker.runtime.invalid.wm_data_json"));
                }
                instance = JSONUtils.toObject(wmJson, type);
            } else {
                instance = JSONUtils.toObject(multipartFile.getInputStream(), type);
            }
        } catch (IOException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.wm.data.body.read.error"), e);
        }
        return instance;
    }

    public static <T extends QueryParameter> void setMultiparts(
            List<T> parameters, MultiValueMap<String, MultipartFile> parts) {
        if (!parts.isEmpty()) {
            for (final String partName : parts.keySet()) {
                if (!WMMultipartUtils.WM_DATA_JSON.equals(partName)) {
                    final T parameter = findParameter(parameters, partName);
                    try {
                        parameter.setTestValue(parts.getFirst(partName).getBytes());
                    } catch (IOException e) {
                        throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.multipart.request.read.error"), e, partName);
                    }
                }
            }
        }
    }

    private static <T extends QueryParameter> T findParameter(List<T> parameters, String name) {
        for (final T parameter : parameters) {
            if (Objects.equals(parameter.getName(), name)) {
                return parameter;
            }
        }
        throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.parameter.found"), name);
    }
}
