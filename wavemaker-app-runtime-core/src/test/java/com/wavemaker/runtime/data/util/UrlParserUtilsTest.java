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