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

package com.wavemaker.app.security.models.jws;

import java.util.Map;

public class JWSConfiguration {

    /**
     * jws field is same as yaml property
     */
    private Map<String, JWSProviderConfiguration> jws;

    public Map<String, JWSProviderConfiguration> getJws() {
        return jws;
    }

    public void setJws(Map<String, JWSProviderConfiguration> jws) {
        this.jws = jws;
    }
}
