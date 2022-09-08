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
package com.wavemaker.runtime.data.dao.query.types.wmql;

import com.wavemaker.runtime.data.model.JavaType;

/**
 * @author Sujith Simon
 * Created on : 26/11/18
 */
public class WMQLDefaultTypeHelper implements WMQLTypeHelper {

    @Override
    public JavaType aliasFor(JavaType javaType) {
        // JavaType aliasing is not needed by default.
        return javaType;
    }
}
