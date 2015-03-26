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

package com.wavemaker.runtime.server.view;

import org.springframework.web.servlet.View;

import com.wavemaker.json.type.FieldDefinition;

/**
 * @author Matt Small
 */
public interface TypedView extends View {

    /**
     * Get the current root type for this view.
     * 
     * @return The root type.
     */
    public FieldDefinition getRootType();

    /**
     * Set the current root type for this view.
     * 
     * @param type The new type.
     */
    public void setRootType(FieldDefinition type);
}
