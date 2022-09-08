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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UrlParserUtilsTest {

    List<String> inputHostNames;

    List<String> expectedHostNames;

    @Before
    public void init() {
        inputHostNames = new ArrayList<>();
        inputHostNames.add("wavemakeronline.com");
        inputHostNames.add("https://wavemaker.atlassian.net/browse");
        inputHostNames.add("https://www.google.co.in/");
        inputHostNames.add("local-studio-example.com/init/server");

        expectedHostNames = new ArrayList<>();
        expectedHostNames.add("wavemakeronline.com");
        expectedHostNames.add("wavemaker.atlassian.net");
        expectedHostNames.add("www.google.co.in");
        expectedHostNames.add("local-studio-example.com");
    }

    @Test
    public void trimUrlForHostName() {
        List<String> sanitisedHostNames = inputHostNames.stream()
            .map(UrlParserUtils::trimUrlForHostName).collect(Collectors.toList());
        Assert.assertEquals(sanitisedHostNames, expectedHostNames);
    }
}