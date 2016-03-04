/**
 * Copyright © 2013 - 2016 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.data.model;

public class QueryParam {

    private String paramName = null;
    private String paramType = null;

    private Boolean isList = Boolean.FALSE;

    public QueryParam() {
    }

    public QueryParam(String paramName, String paramType, Boolean isList) {
        this.paramName = paramName;
        this.paramType = paramType;
        this.isList = isList;
    }

    public String getParamName() {
        return this.paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamType() {
        return this.paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public Boolean getList() {
        return this.isList;
    }

    public void setList(Boolean isList) {
        this.isList = isList;
    }

    @Override
    public String toString() {
        return this.paramName + ":" + this.paramType + ":" + this.isList;
    }
}
