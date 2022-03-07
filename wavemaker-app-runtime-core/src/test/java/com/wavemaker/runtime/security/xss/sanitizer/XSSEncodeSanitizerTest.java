package com.wavemaker.runtime.security.xss.sanitizer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.model.security.XSSSanitizationLayer;
import com.wavemaker.commons.util.WMIOUtils;

public class XSSEncodeSanitizerTest {

    private List<String> xssInputVectors;
    private List<String> encodedOnceList;
    private List<String> encodedTwiceList;


    @Before
    public void init() {
        xssInputVectors = getFileLines("/com/wavemaker/runtime/xss/xss-attack-vector-input.txt");
        encodedOnceList = getFileLines("/com/wavemaker/runtime/xss/encoded-once.txt");
        encodedTwiceList = getFileLines("/com/wavemaker/runtime/xss/encoded-twice.txt");
    }

    @Test
    public void testSanitizeIncomingDataWithDataPreSanitizedFlagEnabled() {
        XSSSanitizer xssSanitizer = new XSSEncodeSanitizer(true, XSSSanitizationLayer.INPUT);
        List<String> actualOutput = xssInputVectors.stream().map(str -> xssSanitizer.sanitizeIncomingData(str))
                .collect(Collectors.toList());
        Assert.assertEquals(encodedOnceList, actualOutput);
    }

    @Test
    public void testSanitizeIncomingDataWithDataPreSanitizedFlagDisabled() {
        XSSSanitizer xssSanitizer = new XSSEncodeSanitizer(false, XSSSanitizationLayer.INPUT);
        List<String> actualOutput = xssInputVectors.stream().map(xssSanitizer::sanitizeOutgoingData)
                .collect(Collectors.toList());
        Assert.assertEquals(encodedOnceList, actualOutput);
    }

    @Test
    public void testSanitizeOutgoingDataWithDataPreSanitizedFlagDisabled() {
        XSSSanitizer xssSanitizer = new XSSEncodeSanitizer(false, XSSSanitizationLayer.OUTPUT);
        List<String> actualOutput = xssInputVectors.stream().map(xssSanitizer::sanitizeOutgoingData)
                .collect(Collectors.toList());
        Assert.assertEquals(encodedOnceList, actualOutput);
    }

    @Test
    public void testSanitizeOutgoingDataWithDataPreSanitizedAndFlagEnabled() {
        XSSSanitizer xssSanitizer = new XSSEncodeSanitizer(true, XSSSanitizationLayer.OUTPUT);
        List<String> actualOutput = encodedOnceList.stream().map(xssSanitizer::sanitizeOutgoingData)
                .collect(Collectors.toList());
        Assert.assertEquals(encodedOnceList, actualOutput);
    }

    @Test
    public void testSanitizeOutgoingDataWithDataPreSanitizedAndFlagDisabled() {
        XSSSanitizer xssSanitizer = new XSSEncodeSanitizer(false, XSSSanitizationLayer.OUTPUT);
        List<String> actualOutput = encodedOnceList.stream().map(xssSanitizer::sanitizeOutgoingData)
                .collect(Collectors.toList());
        Assert.assertEquals(encodedTwiceList, actualOutput);
    }

    private List<String> getFileLines(String resourcePath) {
        InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.toList());
        } catch (Exception e) {
            throw new WMRuntimeException("Failed to read inputStream");
        } finally {
            WMIOUtils.closeSilently(inputStream);
        }
    }
}