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

package com.wavemaker.runtime.data.event;

import com.wavemaker.runtime.data.model.FetchQuery;

/**
 * @author Uday Shankar
 */
public class EntityPreFetchListEvent<E> extends EntityCRUDEvent<E> {
    private FetchQuery fetchQuery;

    public EntityPreFetchListEvent(String serviceId, Class<E> entityClass, FetchQuery fetchQuery) {
        super(serviceId, entityClass);
        this.fetchQuery = fetchQuery;
    }

    public FetchQuery getFetchQuery() {
        return fetchQuery;
    }
}
