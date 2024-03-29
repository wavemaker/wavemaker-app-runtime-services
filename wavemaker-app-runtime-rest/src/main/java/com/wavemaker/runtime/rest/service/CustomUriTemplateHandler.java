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

import java.util.List;

import org.springframework.web.util.DefaultUriTemplateHandler;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by srujant on 19/4/17.
 */
public class CustomUriTemplateHandler extends DefaultUriTemplateHandler {
    @Override
    protected UriComponentsBuilder initUriComponentsBuilder(String uriTemplate) {
        UriComponentsBuilder builder = CustomUriComponentsBuilder.fromUriString(uriTemplate);
        if (shouldParsePath()) {
            List<String> pathSegments = builder.build().getPathSegments();
            builder.replacePath(null);
            for (String pathSegment : pathSegments) {
                builder.pathSegment(pathSegment);
            }
        }
        return builder;
    }

}
