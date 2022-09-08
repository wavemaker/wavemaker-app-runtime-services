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

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 29/12/17
 */
public class Mappers {

    public static <E, R> Page<R> map(Page<E> page, Pageable pageable, Function<E, R> mappingFunction) {
        final List<R> newList = map(page.getContent(), mappingFunction);
        return new PageImpl<>(newList, pageable, page.getTotalElements());
    }

    public static <E, R> List<R> map(List<E> list, Function<E, R> mappingFunction) {
        if (list != null && !list.isEmpty()) {
            return list.stream()
                    .map(mappingFunction)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}
