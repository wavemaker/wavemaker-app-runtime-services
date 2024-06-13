/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/

package com.wavemaker.app.memory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

public class ClassUtils {
    private ClassUtils() {
    }

    public static Field findField(Class klass, String name) {
        Field field = ReflectionUtils.findField(klass, name);
        if (field != null) {
            ReflectionUtils.makeAccessible(field);
        }
        return field;
    }

    public static Method findMethod(Class klass, String name, Class... paramTypes) {
        Method method = ReflectionUtils.findMethod(klass, name, paramTypes);
        if (method != null) {
            ReflectionUtils.makeAccessible(method);
        }
        return method;
    }
}
