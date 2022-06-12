/**
 * Copyright (C) 2020 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.data.controller;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.wavemaker.runtime.data.model.DesignServiceResponse;
import com.wavemaker.runtime.data.model.queries.RuntimeQuery;
import com.wavemaker.runtime.data.service.QueryDesignService;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 29/3/19
 */
@RestController
@RequestMapping("/")
public class QueryTestRunController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTestRunController.class);

    @Autowired
    private QueryDesignService queryDesignService;

    @PostConstruct
    public void init() {
        LOGGER.info("-------------Query Test Controller enabled----------");
    }

    @PostMapping(value = "/{serviceId}/queries/test_run")
    public DesignServiceResponse testRunQuery(
            @PathVariable("serviceId") String serviceId, MultipartHttpServletRequest request, Pageable pageable) {
        return queryDesignService.testRunQuery(serviceId, request, pageable);
    }

    @PostMapping(value = "/{serviceId}/queries/execute")
    public Object executeQuery(
            @PathVariable("serviceId") String serviceId, @RequestBody RuntimeQuery query, Pageable pageable) {
        return queryDesignService.executeQuery(serviceId, query, pageable);
    }

}
