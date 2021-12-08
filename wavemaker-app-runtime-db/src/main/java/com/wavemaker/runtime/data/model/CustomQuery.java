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
package com.wavemaker.runtime.data.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

public class CustomQuery {

	@NotBlank
	@NotEmpty
    private String queryStr;

    private boolean nativeSql;

    private List<CustomQueryParam> queryParams = new ArrayList<>();

    public CustomQuery() {
		super();
	}

	public CustomQuery(String queryStr, List<CustomQueryParam> queryParams) {
		super();
		this.queryStr = queryStr;
		this.queryParams = queryParams;
	}

    public String getQueryStr() {
        return queryStr;
    }

    public void setQueryStr(String queryStr) {
        this.queryStr = queryStr;
    }
    
    public List<CustomQueryParam> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<CustomQueryParam> queryParams) {
        this.queryParams = queryParams;
    }

    public boolean isNativeSql() {
        return nativeSql;
    }

    public void setNativeSql(boolean nativeSql) {
        this.nativeSql = nativeSql;
    }
}
