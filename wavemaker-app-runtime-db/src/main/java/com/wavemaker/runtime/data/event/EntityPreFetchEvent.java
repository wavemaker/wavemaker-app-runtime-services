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

/**
 * @author Uday Shankar
 */
public class EntityPreFetchEvent<E> extends EntityCRUDEvent<E> {
    private Object entityId;

    public EntityPreFetchEvent(String serviceId, Class<E> entityClass, Object entityId) {
        super(serviceId, entityClass);
        this.entityId = entityId;
    }

    public Object getEntityId() {
        return entityId;
    }
}
