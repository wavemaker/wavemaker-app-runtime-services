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
package com.wavemaker.runtime.data.replacers;

import com.wavemaker.runtime.commons.variable.Scope;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 16/6/16
 */
public class ListenerContext {

    private Object entity;
    private Scope phase;

    public ListenerContext(final Object entity, final Scope phase) {
        this.entity = entity;
        this.phase = phase;
    }

    public Object getEntity() {
        return entity;
    }

    public Scope getPhase() {
        return phase;
    }
}
