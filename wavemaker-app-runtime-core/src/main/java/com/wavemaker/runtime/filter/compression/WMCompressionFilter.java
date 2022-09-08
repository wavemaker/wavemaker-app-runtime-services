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
package com.wavemaker.runtime.filter.compression;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.GenericFilterBean;

import com.wavemaker.commons.util.FileValidationUtils;
import com.wavemaker.runtime.filter.compression.gzip.GZipServletResponseWrapper;

/**
 * This filter enables the gzip compression of responses
 *
 * @author Kishore Routhu on 10/10/17 7:04 PM.
 */
public class WMCompressionFilter extends GenericFilterBean {

    public static final String GZIP_ENCODING = "gzip";
    public static final String BROTLI_ENCODING = "br";
    private static final List<String> BUILD_TIME_ENCODINGS = Arrays.asList(BROTLI_ENCODING, GZIP_ENCODING);
    @Autowired
    private CompressionFilterConfig filterConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String supportedEncodings = httpServletRequest.getHeader(HttpHeaders.ACCEPT_ENCODING);
        boolean processedCompressedFile = false;

        if (supportedEncodings != null && filterConfig.isEnableCompression()) {
            String accessingResource = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());
            for (String encodingType : BUILD_TIME_ENCODINGS) {
                if (supportedEncodings.toLowerCase().contains(encodingType)) {
                    if (accessingResource.matches("(.*)(\\..*)")) {
                        String generatedCompressedFile = accessingResource.replaceAll("(.*)(\\..*)", "$1." + encodingType + "$2");
                        URL compressedResource = getServletContext().getResource(FileValidationUtils.validateFilePath(generatedCompressedFile));
                        if (compressedResource != null) {
                            processedCompressedFile = true;
                            RequestDispatcher requestDispatcher = httpServletRequest.getRequestDispatcher("/" + generatedCompressedFile);
                            httpServletResponse.addHeader(HttpHeaders.CONTENT_ENCODING, encodingType);
                            requestDispatcher.forward(httpServletRequest, httpServletResponse);
                            break;
                        }
                    }
                }
            }
            if (!processedCompressedFile && supportedEncodings.toLowerCase().contains(GZIP_ENCODING)) {
                processedCompressedFile = true;
                GZipServletResponseWrapper gZipResponseWrapper = new GZipServletResponseWrapper(filterConfig, httpServletResponse);
                chain.doFilter(request, gZipResponseWrapper);
                gZipResponseWrapper.close();
            }
        }

        if (!processedCompressedFile) {
            chain.doFilter(request, response);
        }
    }
}
