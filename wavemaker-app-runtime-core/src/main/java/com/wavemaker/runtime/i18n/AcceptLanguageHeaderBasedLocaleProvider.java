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
package com.wavemaker.runtime.i18n;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.wavemaker.commons.i18n.DefaultLocaleProvider;
import com.wavemaker.runtime.web.filter.WMRequestFilter;

/**
 * @author Uday Shankar
 */
public class AcceptLanguageHeaderBasedLocaleProvider extends DefaultLocaleProvider {
    
    @Override
    public String[] getLocales() {
        HttpServletRequest httpServletRequest = WMRequestFilter.getCurrentThreadHttpServletRequest();
        if (httpServletRequest != null) {
            String acceptLanguageHeader = httpServletRequest.getHeader("Accept-Language");
            if (StringUtils.isNotBlank(acceptLanguageHeader)) {
                Enumeration<Locale> locales = httpServletRequest.getLocales();
                List<String> localesList = new ArrayList<>();
                while (locales.hasMoreElements()) {
                    localesList.add(locales.nextElement().toLanguageTag());
                }
                return localesList.toArray(new String[0]);
            }
        }
        return super.getLocales();
    }
}
