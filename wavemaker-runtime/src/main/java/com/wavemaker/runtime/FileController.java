/*
 * Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.wavemaker.common.MessageResource;
import com.wavemaker.common.WMRuntimeException;
import com.wavemaker.runtime.data.DataServiceLoggers;
import com.wavemaker.runtime.module.ModuleManager;
import com.wavemaker.runtime.server.ServerUtils;
import com.wavemaker.runtime.server.ServiceResponse;

/**
 * Controller (in the MVC sense) providing the studio access to project files. Based off of the old StaticFileServlet.
 * 
 * @author Matt Small
 */
public final class FileController extends AbstractController {

    private static final String WM_BUILD_GZIPPED_URL = "/lib/build/Gzipped/";

    private static final String WM_BUILD_DOJO_THEMES_URL = "/lib/build/themes/";

    private static final String WM_BUILD_WM_THEMES_URL = "/lib/wm/base/widget/themes/";

    private static final String WM_BUILD_DOJO_FOLDER_URL = "/lib/dojo/";

    private static final String WM_BUILD_DOJO_JS_URL = "/lib/dojo/dojo/dojo_build.js";

    private static final String WM_BOOT_URL = "/lib/boot/boot.js";

    private static final String WM_RUNTIME_LOADER_URL = "/lib/runtimeLoader.js";

    private static final String WM_IMAGE_URL = "/resources/images/";

    private static final String WM_GZIPPED_URL = "/resources/gzipped/";

    private static final String WM_STUDIO_BUILD_URL = "/build/";

    private static final String WM_CONFIG_URL = "/config.js";

    private ModuleManager moduleManager;

    private ServiceResponse serviceResponse;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String path = WMAppContext.getInstance().getAppContextRoot();
        boolean isGzipped = false;
        boolean addExpiresTag = false;
        String reqPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        reqPath = reqPath.replaceAll("%20", " ");
        reqPath = reqPath.replaceAll("//", "/");

        // trim off the servlet name
        if (!contextPath.equals("") && reqPath.startsWith(contextPath)) {
            reqPath = reqPath.substring(reqPath.indexOf('/', 1));
        }

        if (reqPath.startsWith(WM_BUILD_GZIPPED_URL) || reqPath.startsWith(WM_GZIPPED_URL) || reqPath.equals(WM_BUILD_DOJO_JS_URL)) {
            isGzipped = true;
            addExpiresTag = true;
            reqPath += ".gz";
        } else if (reqPath.startsWith(WM_BUILD_DOJO_THEMES_URL) || reqPath.startsWith(WM_BUILD_WM_THEMES_URL)
            || reqPath.startsWith(WM_BUILD_DOJO_FOLDER_URL) || reqPath.equals(WM_BOOT_URL) || reqPath.equals(WM_RUNTIME_LOADER_URL)
            || reqPath.startsWith(WM_IMAGE_URL) || reqPath.startsWith(WM_STUDIO_BUILD_URL)) {
            addExpiresTag = true;
        } else if (!reqPath.contains(WM_CONFIG_URL)) {
            throw new WMRuntimeException(MessageResource.STUDIO_UNKNOWN_LOCATION, reqPath, request.getRequestURI());
        }

        File sendFile = null;
        if (!isGzipped && reqPath.lastIndexOf(".js") == reqPath.length() - 3) {
            sendFile = new File(path, reqPath + ".gz");
            if (!sendFile.exists()) {
                sendFile = null;
            } else {
                isGzipped = true;
            }
        }

        if (sendFile == null) {
            sendFile = new File(path, reqPath);
        }

        if (DataServiceLoggers.fileControllerLogger.isDebugEnabled()) {
            DataServiceLoggers.fileControllerLogger.debug("FileController: " + sendFile.getAbsolutePath() + "\t (" + reqPath + ")");
        }

        if (sendFile != null && !sendFile.exists()) {
            logger.debug("File " + reqPath + " not found in expected path: " + sendFile);
            handleError(response, "File " + reqPath + " not found in expected path: " + sendFile, HttpServletResponse.SC_NOT_FOUND);
        } else if (sendFile != null) {
            if (addExpiresTag) {
                // setting cache expire to one year.
                setCacheExpireDate(response, 365 * 24 * 60 * 60);
            }

            if (!isGzipped) {
                setContentType(response, sendFile);
            } else {
                response.setHeader("Content-Encoding", "gzip");
            }

            OutputStream os = response.getOutputStream();
            InputStream is = new FileInputStream(sendFile);
            if (reqPath.contains(WM_CONFIG_URL)) {
                StringBuilder content = new StringBuilder(IOUtils.toString(is));
                int offset = ServerUtils.getServerTimeOffset();
                int timeout =  this.serviceResponse.getConnectionTimeout();
                String timeStamp = "0";
                File timeFile = new File(sendFile.getParent(), "timestamp.txt");
                if (timeFile.exists()) {
                    InputStream isTime = new FileInputStream(timeFile);
                    timeStamp = IOUtils.toString(isTime);
                    isTime.close();
                } else {
                    System.out.println("File timestamp.txt not found, using 0");
                }
                content.append("\r\nwm.serverTimeOffset = ").append(offset).append(";");
                content.append("\r\nwm.connectionTimeout = ").append(timeout).append(";");
                content.append("\r\nwm.saveTimestamp = ").append(timeStamp).append(";");       
                
                String language = request.getHeader("accept-language");
                if (language != null && language.length() > 0) {
                    int index = language.indexOf(",");
                    language = index == -1 ? language : language.substring(0, index);
                    content.append("\r\nwm.localeString = '").append(language).append("';");
                }
                File bootFile = new File(sendFile.getParent(), "boot.js");
                if (bootFile.exists()) {
                    InputStream is2 = new FileInputStream(bootFile);
                    String bootString = IOUtils.toString(is2);
                    bootString = bootString.substring(bootString.indexOf("*/") + 2);
                    content.append(bootString);
                    is2.close();
                } else {
                    System.out.println("Boot file not found");
                }
                IOUtils.write(content.toString(), os);
            } else {
                IOUtils.copy(is, os);
            }
            os.close();
            is.close();
        }

        // we've already written our response
        return null;
    }

    public static void setCacheExpireDate(HttpServletResponse response, int seconds) {
        if (response != null) {
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.SECOND, seconds);
            response.setHeader("Cache-Control", "PUBLIC, max-age=" + seconds + ", must-revalidate");
            response.setHeader("Expires", htmlExpiresDateFormat().format(cal.getTime()));
        }
    }

    public static DateFormat htmlExpiresDateFormat() {
        DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return httpDateFormat;
    }

    protected void handleError(HttpServletResponse response, String errorMessage, int code) throws IOException {

        response.sendError(code);
        /*Writer outputWriter = response.getWriter();
        outputWriter.write(errorMessage); */
    }

    protected void setContentType(HttpServletResponse response, File file) {
        ConfigurableMimeFileTypeMap mimeFileTypeMap = new ConfigurableMimeFileTypeMap();
        mimeFileTypeMap.setMappings(new String[] { "text/css css CSS", "application/json json JSON smd SMD" });

        response.setContentType(mimeFileTypeMap.getContentType(file));
    }

    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    public void setServiceResponse(ServiceResponse serviceResponse) {
        this.serviceResponse = serviceResponse;
    }

}