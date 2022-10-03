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

import java.util.Set;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wavemaker.runtime.data.expression.QueryFilter;
import com.wavemaker.runtime.data.spring.WMPageImpl;

/**
 * @author <a href="mailto:anusha.dharmasagar@wavemaker.com">Anusha Dharmasagar</a>
 * @since 25/5/16
 */
public abstract class CriteriaUtils {

    public static final String SEARCH_PROPERTY_DELIMITER = ".";

    public static CriteriaQuery createCriterion(QueryFilter queryFilter,CriteriaBuilder builder,CriteriaQuery criteria, Root from) {
        Object attributeValue = queryFilter.getAttributeValue();
        String attributeName = queryFilter.getAttributeName();
        return queryFilter.getFilterCondition().criterion(builder, criteria, from, attributeName, attributeValue);
    }

    public static Page executeAndGetPageableData(CriteriaQuery criteriaQuery, Session session, Pageable pageable, Long count) {
        TypedQuery typedQuery = session.createQuery(criteriaQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        if (pageable != null) {
            return new WMPageImpl(typedQuery.getResultList(), pageable, count);
        } else {
            return new WMPageImpl(typedQuery.getResultList());
        }
    }

    public static CriteriaQuery criteriaForRelatedProperty(
        CriteriaQuery criteria, Root from, final String attributeName, final Set<String> aliases) {
        final int indexOfDot = attributeName.lastIndexOf(SEARCH_PROPERTY_DELIMITER);
        if (indexOfDot != -1) {
            String relatedEntityName = attributeName.substring(0, indexOfDot);
            if (aliases == null) {
                return criteria.select(from.get(relatedEntityName).alias(relatedEntityName));
            } else if (!aliases.contains(relatedEntityName)) {
                aliases.add(relatedEntityName);
                return criteria.select(from.get(relatedEntityName).alias(relatedEntityName));
            }
        }
        return criteria;
    }

}
