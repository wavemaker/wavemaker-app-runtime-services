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
package com.wavemaker.runtime.rest.processor.response;

/**
 * Created by srujant on 21/5/17.
 */
public abstract class AbstractHttpResponseProcessor implements HttpResponseProcessor {
    protected boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void process(HttpResponseProcessorContext httpResponseProcessorContext) {
        if (isEnabled()) {
            doProcess(httpResponseProcessorContext);
        }
    }

    protected abstract void doProcess(HttpResponseProcessorContext httpResponseProcessorContext);
}
