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
package com.wavemaker.runtime.util;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;

/**
 * @author Uday Shankar
 */
public class WMRuntimeUtils {

    private WMRuntimeUtils(){}

    private static final String BYTE_ARRAY = "byte[]";

    private static final String BLOB = "Blob";

    public static boolean isLob(Class instance, String field) {
        Field declaredField;
        try {
            declaredField = instance.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.field.not.found"), e, field, instance.getName());
        }
        return declaredField != null && (Objects.equals(BYTE_ARRAY, declaredField.getType().getSimpleName())
                || Objects.equals(BLOB, declaredField.getType().getSimpleName()));
    }

    public static String getContextRelativePath(File file, HttpServletRequest request) {
      	return getContextRelativePath(file, request, null);
    }

    public static String getContextRelativePath(File file, HttpServletRequest request, String relativePath) {
        return getContextRelativePath(file, request, relativePath, false);
    }

    public static String getContextRelativePath(File file, HttpServletRequest request, String relativePath, boolean inline) {
        StringBuilder sb = new StringBuilder(request.getRequestURL());
        final int index = sb.lastIndexOf("/");
        if (index != -1) {
            sb.delete(index, sb.length());
        }
        sb.append(inline ? "/downloadFileAsInline" : "/downloadFile");
        sb.append("?file=").append(file.getName());
        if (StringUtils.isNotBlank(relativePath)) {
            sb.append("&relativePath=").append(relativePath);
        }
        sb.append("&returnName=").append(file.getName());
        return sb.toString();
    }
}
