/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
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
