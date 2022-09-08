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
package com.wavemaker.runtime.data.dao.validators;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.wavemaker.commons.InvalidInputException;
import com.wavemaker.commons.MessageResource;

/**
 * @author <a href="mailto:anusha.dharmasagar@wavemaker.com">Anusha Dharmasagar</a>
 * @since 16/5/17
 */
public class SortValidator {


    public void validate(Pageable pageable, Class<?> entityClass) {
        if (pageable != null && pageable.getSort() != null) {
            final Sort sort = pageable.getSort();
            for (final Sort.Order order : sort) {
                final String propertyName = order.getProperty();
                if (!HqlPropertyResolver.findField(propertyName, entityClass).isPresent()) {
                    throw new InvalidInputException(MessageResource.UNKNOWN_FIELD_NAME, propertyName);
                }
            }
        }
    }


}