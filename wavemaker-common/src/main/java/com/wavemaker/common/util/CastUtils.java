/*
 *  Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.common.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Frankie Fu
 */
@SuppressWarnings("unchecked")
public final class CastUtils {

    public static <T, U> Map<T, U> cast(Map<?, ?> p) {
        return (Map<T, U>) p;
    }

    public static <T> List<T> cast(List<?> p) {
        return (List<T>) p;
    }

    public static <T> List<T> cast(List<?> p, Class<T> cls) {
        return (List<T>) p;
    }

    public static <T> Collection<T> cast(Collection<?> p) {
        return (Collection<T>) p;
    }

    public static <T> Set<T> cast(Set<?> p) {
        return (Set<T>) p;
    }

    public static <T> Collection<T> cast(Collection<?> p, Class<T> cls) {
        return (Collection<T>) p;
    }

    public static <T> Iterator<T> cast(Iterator<?> p) {
        return (Iterator<T>) p;
    }

    public static <T> Enumeration<T> cast(Enumeration<?> e) {
        return (Enumeration<T>) e;
    }

}
