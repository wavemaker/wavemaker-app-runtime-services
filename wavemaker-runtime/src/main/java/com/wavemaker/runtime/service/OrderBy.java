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

package com.wavemaker.runtime.service;

import com.wavemaker.common.util.ObjectUtils;

/**
 * @author Simon Toens
 */
public class OrderBy {

    private static final String ERROR_MSG = "orderBy format must be: asc|desc:<propertyPath>";

    private static final String ORDER_BY_SEP = ":";

    public static enum SortOrder {

        ASC(), DESC();

        @Override
        public String toString() {
            if (this == ASC) {
                return "asc:";
            } else {
                return "desc:";
            }
        }
    }

    public static OrderBy newInstance(String orderBy) {

        if (ObjectUtils.isNullOrEmpty(orderBy)) {
            throw new IllegalArgumentException("orderBy cannot be null or empty");
        }

        orderBy = orderBy.trim();

        int i = orderBy.indexOf(ORDER_BY_SEP);
        if (i == -1 || i == 0 || i == orderBy.length() - 1) {
            throw new IllegalArgumentException(ERROR_MSG);
        }

        OrderBy rtn = new OrderBy();

        String propertyPath = orderBy.substring(i + 1).trim();
        String order = orderBy.substring(0, i).trim();

        if (order.equalsIgnoreCase("asc")) {
            rtn.setAsc(propertyPath);
        } else if (order.equals("desc")) {
            rtn.setDesc(propertyPath);
        } else {
            throw new IllegalArgumentException("\"" + orderBy + "\" " + ERROR_MSG);
        }

        return rtn;
    }

    private String propertyPath = null;

    private SortOrder sortOrder = null;

    public OrderBy() {
    }

    public String getPropertyPath() {
        return this.propertyPath;
    }

    public void setAsc(String propertyPath) {
        this.sortOrder = SortOrder.ASC;
        this.propertyPath = propertyPath;
    }

    public void setDesc(String propertyPath) {
        this.sortOrder = SortOrder.DESC;
        this.propertyPath = propertyPath;
    }

    public boolean isAsc() {
        return this.sortOrder == SortOrder.ASC;
    }

    public boolean isDesc() {
        return this.sortOrder == SortOrder.DESC;
    }

    @Override
    public String toString() {
        StringBuilder rtn = new StringBuilder();
        if (this.sortOrder != null) {
            rtn.append(this.sortOrder.toString());
        }
        rtn.append(this.propertyPath);
        return rtn.toString();
    }
}
