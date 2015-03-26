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

package com.wavemaker.json.type;

import java.util.LinkedHashMap;

/**
 * Objects provide mappings from a fixed set of keys to their values. Currently, all keys must be of type String, but
 * value types can be anything.
 * 
 * @author Matt Small
 */
public interface ObjectTypeDefinition extends TypeDefinition {

    /**
     * Get the set of fields for this Object definition.
     * 
     * @return The set of fields.
     */
    public LinkedHashMap<String, FieldDefinition> getFields();
}