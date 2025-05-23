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

package com.wavemaker.runtime.security.xss;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import com.wavemaker.runtime.security.xss.handler.XSSSecurityHandler;

public class XSSRequestWrapper extends HttpServletRequestWrapper {

    private XSSSecurityHandler xssSecurityHandler;

    public XSSRequestWrapper(HttpServletRequest request, XSSSecurityHandler xssSecurityHandler) {
        super(request);
        this.xssSecurityHandler = xssSecurityHandler;
    }

    @Override
    public String getParameter(String name) {
        String paramValue = super.getParameter(name);
        return xssSecurityHandler.sanitizeIncomingData(paramValue);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] paramValues = super.getParameterValues(name);
        if (paramValues == null) {
            return paramValues;
        }
        int paramCount = paramValues.length;
        String[] escapedValues = new String[paramCount];
        for (int i = 0; i < paramCount; i++) {
            escapedValues[i] = xssSecurityHandler.sanitizeIncomingData(paramValues[i]);
        }
        return escapedValues;
    }

}
