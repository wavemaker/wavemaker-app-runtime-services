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
package com.wavemaker.runtime.rest.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.util.StreamUtils;

import com.wavemaker.commons.rest.WmFileSystemResource;
import com.wavemaker.commons.util.WMIOUtils;

/**
 * Created by srujant on 14/2/17.
 */
public class WmFileSystemResourceConverter extends AbstractHttpMessageConverter<WmFileSystemResource> {


    @Override
    protected boolean supports(Class<?> clazz) {
        return WmFileSystemResource.class == clazz;
    }

    @Override
    protected boolean canRead(MediaType mediaType) {
        return false;
    }

    @Override
    protected WmFileSystemResource readInternal(
            Class<? extends WmFileSystemResource> clazz, HttpInputMessage inputMessage) throws IOException {
        return null;
    }

    @Override
    protected MediaType getDefaultContentType(WmFileSystemResource wmFileSystemResource) throws IOException {
        String contentType = wmFileSystemResource.getContentType();
        if (StringUtils.isNotBlank(contentType)) {
            return MediaType.parseMediaType(contentType);
        } else {
            return super.getDefaultContentType(wmFileSystemResource);
        }
    }

    @Override
    protected void writeInternal(WmFileSystemResource wmFileSystemResource, HttpOutputMessage outputMessage)
            throws IOException {

        InputStream in = wmFileSystemResource.getInputStream();
        try {
            StreamUtils.copy(in, outputMessage.getBody());
        } finally {
            WMIOUtils.closeSilently(in);
        }
    }

}
