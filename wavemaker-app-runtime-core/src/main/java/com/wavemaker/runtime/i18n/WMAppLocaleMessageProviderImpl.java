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
package com.wavemaker.runtime.i18n;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.i18n.LocaleData;
import com.wavemaker.commons.i18n.LocaleMessageProviderImpl;
import com.wavemaker.commons.json.JSONUtils;

/**
 * Created by srujant on 3/9/18.
 */
public class WMAppLocaleMessageProviderImpl extends LocaleMessageProviderImpl {

    public WMAppLocaleMessageProviderImpl(List<String> locationPatterns, ResourcePatternResolver resourcePatternResolver) {
        super(locationPatterns, resourcePatternResolver);
    }

    @Override
    protected Map<String, String> loadLocaleMessages(String locale) {
        Map<String, String> localeMessages = super.loadLocaleMessages(locale);
        Resource[] resources = getResources("/resources/i18n/", locale + ".json");
        for (Resource resource : resources) {
            try {
                LocaleData localeData = JSONUtils.toObject(resource.getInputStream(), LocaleData.class);
                localeMessages.putAll(localeData.getMessages());
            } catch (IOException e) {
                throw new WMRuntimeException("Failed to read locale resources for locale " + locale, e);
            }
        }
        return localeMessages;
    }
}
