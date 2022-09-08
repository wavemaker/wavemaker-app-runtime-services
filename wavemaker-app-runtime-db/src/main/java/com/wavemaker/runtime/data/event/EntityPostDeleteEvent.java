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
public class EntityPostDeleteEvent<E> extends EntityCRUDEvent<E> {
    private E entity;

    public EntityPostDeleteEvent(String serviceId, Class<E> entityClass, E entity) {
        super(serviceId, entityClass);
        this.entity = entity;
    }

    public E getEntity() {
        return entity;
    }
}
