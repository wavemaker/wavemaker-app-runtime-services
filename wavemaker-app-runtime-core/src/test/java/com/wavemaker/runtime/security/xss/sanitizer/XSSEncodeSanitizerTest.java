package com.wavemaker.runtime.security.xss.sanitizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.util.WMIOUtils;

public class XSSEncodeSanitizerTest {

    private List<String> xssInputVectors;
    private List<String> xssEncodedVectors;
    private List<String> xssOutputVector;


    @Before
    public void init() {
        InputStream xssEncodeVectorStream = this.getClass().getResourceAsStream("/com/wavemaker/runtime/xss/xss-encode-vectors-input.txt");
        InputStream xssVectorStream = this.getClass().getResourceAsStream("/com/wavemaker/runtime/xss/xss-attack-vector-input.txt");
        InputStream xssEncodeResultStream = this.getClass().getResourceAsStream("/com/wavemaker/runtime/xss/xss-encode-vectors-output.txt");

        xssInputVectors = streamToListOfStrings(xssVectorStream);
        xssOutputVector = streamToListOfStrings(xssEncodeResultStream);
        xssEncodedVectors = streamToListOfStrings(xssEncodeVectorStream);

        WMIOUtils.closeSilently(xssEncodeVectorStream);
        WMIOUtils.closeSilently(xssVectorStream);
        WMIOUtils.closeSilently(xssEncodeResultStream);
    }

    @Test
    public void testSanitizeRequestDataWhenBackwardCompatibilityTrue() throws IOException {
        XSSEncodeSanitizer xssEncodeSanitizer = new XSSEncodeSanitizer(true);
        List<String> xssEncodedVectorsRes = xssEncodedVectors.stream().map(str -> xssEncodeSanitizer.sanitizeRequestData(str))
                .collect(Collectors.toList());
        Assert.assertTrue(compareLists(xssEncodedVectorsRes, xssOutputVector));
    }

    @Test
    public void testSanitizeRequestDataWhenBackwardCompatibilityFalse() {
        XSSEncodeSanitizer xssEncodeSanitizer = new XSSEncodeSanitizer(false);
        List<String> xssEncodedVectorsRes = xssInputVectors.stream().map(str -> xssEncodeSanitizer.sanitizeRequestData(str))
                .collect(Collectors.toList());
        Assert.assertTrue(compareLists(xssEncodedVectorsRes, xssOutputVector));
    }


    private boolean compareLists(List<String> input, List<String> output) {
        for (int i = 0; i < input.size(); i++) {
            if (!input.get(i).equals(output.get(i))) {
                return false;
            }
        }
        return true;
    }

    private List<String> streamToListOfStrings(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.toList());
        } catch (Exception e) {
            throw new WMRuntimeException("Failed to read inputStream");
        }
    }
}