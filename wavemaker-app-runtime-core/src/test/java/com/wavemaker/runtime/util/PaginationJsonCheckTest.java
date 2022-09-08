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
package com.wavemaker.runtime.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.databind.JsonNode;
import com.wavemaker.runtime.commons.WMObjectMapper;

public class PaginationJsonCheckTest {

    @Test
    public void check() throws IOException {

        List<String> sampleData = new ArrayList<>();
        sampleData.add("A");
        sampleData.add("B");
        sampleData.add("C");
        Sort sort = Sort.by(Sort.Order.asc("id"));

        Pageable pageable = PageRequest.of(1, 20, sort);

        Page<String> page = new PageImpl<>(sampleData, pageable, 10);

        String pageObjectStringified = WMObjectMapper.getInstance().writeValueAsString(page);

        JsonNode node = WMObjectMapper.getInstance().readTree(pageObjectStringified);

        Assert.assertTrue(node.get("sort").isArray());

        Assert.assertEquals("ASC", node.get("sort").get(0).get("direction").asText());

        Assert.assertFalse(node.get("sort").get(0).get("ignoreCase").asBoolean());

        Assert.assertEquals("id", node.get("sort").get(0).get("property").asText());

        Assert.assertEquals("NATIVE", node.get("sort").get(0).get("nullHandling").asText());

        Assert.assertTrue(node.get("last").asBoolean());

        Assert.assertFalse(node.get("first").asBoolean());

        Assert.assertEquals(3, node.get("numberOfElements").asInt());

        Assert.assertEquals(20, node.get("size").asInt());

        Assert.assertEquals(2, node.get("totalPages").asInt());

        Assert.assertEquals(23, node.get("totalElements").asInt());

    }

}
