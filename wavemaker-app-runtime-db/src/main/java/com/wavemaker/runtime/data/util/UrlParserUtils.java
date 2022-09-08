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

package com.wavemaker.runtime.data.util;

public class UrlParserUtils {

    /*
     * trims schemes {http, https} and port numbers and basepath
     * */
    public static String trimUrlForHostName(String host) {
        if (host != null && !host.isEmpty()) {
            String sanitisedData = host;
            sanitisedData = sanitisedData.trim();
            if (sanitisedData.startsWith("http://")) {
                sanitisedData = sanitisedData.replace("http://", "");
            } else if (sanitisedData.startsWith("https://")) {
                sanitisedData = sanitisedData.replace("https://", "");
            }
            if (sanitisedData.contains("/")) {
                sanitisedData = sanitisedData.substring(0, sanitisedData.indexOf("/"));
            }
            return sanitisedData;
        }
        return host;
    }
}
