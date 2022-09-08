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
package com.wavemaker.runtime.webprocess.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringSubstitutor;
import org.apache.http.entity.ContentType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.auth.oauth2.OAuth2Constants;
import com.wavemaker.commons.crypto.CryptoUtils;
import com.wavemaker.commons.io.ClassPathFile;
import com.wavemaker.commons.json.JSONUtils;
import com.wavemaker.commons.util.WMIOUtils;
import com.wavemaker.runtime.app.AppFileSystem;
import com.wavemaker.runtime.commons.WMAppContext;
import com.wavemaker.runtime.commons.util.WMRandomUtils;
import com.wavemaker.runtime.webprocess.WebProcessHelper;
import com.wavemaker.runtime.webprocess.model.WebProcess;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@RestController
@Api(value = "/webprocess", description = "Exposes APIs to work with webprocess")
@RequestMapping(value = "/webprocess")
public class WebProcessController {

    private static final String WEB_PROCESS_RESPONSE_TEMPLATE = "templates/web_process_response.ftl";

    private static final int ENCRYPTION_KEY_SIZE = 128;

    private String customUrlScheme;

    @GetMapping(value = "/prepare")
    @ApiOperation(value = "Returns a url to use to start the web process.")
    public String prepare(String hookUrl, String processName, String requestSourceType, HttpServletRequest request, HttpServletResponse response) {
        try {
            WebProcess webProcess = new WebProcess();
            webProcess.setProcessName(processName);
            webProcess.setHookUrl(hookUrl);
            webProcess.setCommunicationKey(WMRandomUtils.getRandomString(ENCRYPTION_KEY_SIZE / 8));
            webProcess.setRequestSourceType(requestSourceType);
            String webProcessJSON = WebProcessHelper.encodeWebProcess(webProcess);
            WebProcessHelper.addWebProcessCookie(request, response, webProcessJSON);
            return webProcessJSON;
        } catch (IOException e) {
            throw new WMRuntimeException(e);
        }
    }

    @GetMapping(value = "/start")
    @ApiOperation(value = "starts a web process by redirecting the user to process hook url.")
    public void start(String process, HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebProcessHelper.addWebProcessCookie(request, response, process);
        WebProcess webProcess = WebProcessHelper.decodeWebProcess(process);
        response.sendRedirect(request.getServletContext().getContextPath() + webProcess.getHookUrl());
    }

    @GetMapping(value = "/end")
    @ApiOperation(value = "ends a web process and shows a page that has encoded output")
    public void end(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie cookie = WebProcessHelper.getCookie(request.getCookies(), WebProcessHelper.WEB_PROCESS_COOKIE_NAME);
        if (cookie != null) {
            WebProcess webProcess = WebProcessHelper.decodeWebProcess(cookie.getValue());
            String processOutput = (String) request.getAttribute(WebProcessHelper.WEB_PROCESS_OUTPUT);
            processOutput = CryptoUtils.encrypt(webProcess.getCommunicationKey(), processOutput);
            String redirectUrl = "://services/webprocess/" + webProcess.getProcessName() + "?process_output=" + URLEncoder.encode(processOutput, WebProcessHelper.UTF_8);
            String urlScheme = "com.wavemaker.wavelens";
            if ("MOBILE".equals(webProcess.getRequestSourceType())) {
                urlScheme = getCustomUrlScheme();
            }
            Map<String, Object> input = new HashMap<>();
            input.put("appLink", urlScheme + redirectUrl);
            String processResponse = StringSubstitutor.replace(new ClassPathFile(WEB_PROCESS_RESPONSE_TEMPLATE).getContent().asString(), input);
            response.setContentType(ContentType.TEXT_HTML.getMimeType());
            response.getWriter().write(processResponse);
        } else {
            throw new WMRuntimeException("Web Process cookie is not found");
        }
    }

    @GetMapping(value = "/decode")
    @ApiOperation(value = "ends a web process and shows a page that has encoded output")
    public String decode(String encodedProcessdata, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie cookie = WebProcessHelper.getCookie(request.getCookies(), WebProcessHelper.WEB_PROCESS_COOKIE_NAME);
        if (cookie != null) {
            WebProcess webProcess = WebProcessHelper.decodeWebProcess(cookie.getValue());
            WebProcessHelper.removeWebProcessCookie(request, response);
            return CryptoUtils.decrypt(webProcess.getCommunicationKey(), encodedProcessdata);
        } else {
            throw new WMRuntimeException("Web Process cookie is not found");
        }
    }


    private String getCustomUrlScheme() {
        if (customUrlScheme == null) {
            synchronized (this) {
                InputStream inputStream = null;
                try {
                    AppFileSystem appFileSystem = WMAppContext.getInstance().getSpringBean(AppFileSystem.class);
                    inputStream = appFileSystem.getWebappResource("config.json");
                    Map<String, String> configJsonObject = JSONUtils.toObject(inputStream, Map.class);
                    customUrlScheme = configJsonObject.get(OAuth2Constants.CUSTOM_URL_SCHEME);
                } catch (IOException e) {
                    throw new WMRuntimeException(e);
                } finally {
                    WMIOUtils.closeSilently(inputStream);
                }
            }
        }
        return customUrlScheme;
    }

}
