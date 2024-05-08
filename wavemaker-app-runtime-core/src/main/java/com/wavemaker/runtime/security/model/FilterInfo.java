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

package com.wavemaker.runtime.security.model;

import javax.servlet.Filter;

public class FilterInfo {

    private Class<? extends Filter> filterClass;

    private String filterBeanName;

    private String position;

    public FilterInfo(Class<? extends Filter> filterClass, String filterBeanName, String position) {
        this.filterClass = filterClass;
        this.filterBeanName = filterBeanName;
        this.position = position;
    }

    public Class<? extends Filter> getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(Class<? extends Filter> filterClass) {
        this.filterClass = filterClass;
    }

    public String getFilterBeanName() {
        return filterBeanName;
    }

    public void setFilterBeanName(String filterBeanName) {
        this.filterBeanName = filterBeanName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
