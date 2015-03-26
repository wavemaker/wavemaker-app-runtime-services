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

package com.wavemaker.runtime.pws;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.wavemaker.common.ConfigurationException;
import com.wavemaker.common.MessageResource;
import com.wavemaker.common.util.SpringUtils;

/**
 * @author Seung Lee
 */
public class PwsResponseProcessorBeanFactory {

    private Map<String, IPwsResponseProcessor> pwsResponseProcessors = new HashMap<String, IPwsResponseProcessor>();

    public Collection<String> getPwsResponseProcessorNames() {
        return this.pwsResponseProcessors.keySet();
    }

    public IPwsResponseProcessor getPwsResponseProcessor(String partnerName) {

        if (this.pwsResponseProcessors == null) {
            SpringUtils.throwSpringNotInitializedError(this.getClass());
        }

        if (!this.pwsResponseProcessors.containsKey(partnerName)) {
            throw new ConfigurationException(MessageResource.UNKNOWN_PWS_LOGIN_MANAGER, partnerName);
        }

        return this.pwsResponseProcessors.get(partnerName);
    }

    public void setPwsResponseProcessors(Map<String, IPwsResponseProcessor> pwsResponseProcessors) {
        this.pwsResponseProcessors = pwsResponseProcessors;

    }

}
