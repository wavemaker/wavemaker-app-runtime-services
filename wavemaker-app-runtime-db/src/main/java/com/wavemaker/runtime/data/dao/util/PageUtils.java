/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.data.dao.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * @author Dilip Kumar
 * @since 11/4/18
 */
public interface PageUtils {

    int DEFAULT_PAGE_NUMBER = 0;
    int DEFAULT_PAGE_SIZE = 20;

    static Pageable defaultIfNull(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
        } else if (pageable.getSort() == null) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
        } else {
            return pageable;
        }
    }

    static Pageable overrideExportSize(final Pageable pageable, final Integer exportSize) {
        final Pageable validPageable;
        if (exportSize == null || exportSize <= 0) {
            if (pageable == null) {
                validPageable = new SortedUnPagedRequest(0, -1);
            } else {
                validPageable = new SortedUnPagedRequest(0, -1, pageable.getSort());
            }
        } else {
            if (pageable == null) {
                validPageable = PageRequest.of(0, exportSize);
            } else {
                validPageable = PageRequest.of(pageable.getPageNumber(), exportSize, pageable.getSort());
            }
        }
        return validPageable;
    }
}