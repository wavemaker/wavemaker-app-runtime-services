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
package com.wavemaker.app.security.models.config.rolemapping;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;

/**
 * Created by ArjunSahasranam on 5/17/16.
 */
public class RoleMappingConfigDeSerializer extends JsonDeserializer {
    @Override
    public Object deserialize(
        final JsonParser p, final DeserializationContext ctxt) throws IOException {
        ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
        JsonNode node = objectMapper.readTree(p);
        if (node.has("roleAttributeName")) {
            return objectMapper.readValue(node.toString(), RoleAttributeNameMappingConfig.class);
        } else if (node.has("roleAttrName")) {
            return objectMapper.readValue(node.toString(), SAMLRoleMappingConfig.class);
        } else if (node.has("modelName")) {
            return objectMapper.readValue(node.toString(), DatabaseRoleMappingConfig.class);
        }
        throw new WMRuntimeException(MessageResource.create("com.wavemaker.studio.failed.to.deserialize"), node.toString());
    }
}
