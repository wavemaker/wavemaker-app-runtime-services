/*******************************************************************************
 * Copyright (C) 2024-2025 WaveMaker, Inc.
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

package com.wavemaker.runtime.ai.controller;

import java.io.File;
import java.io.IOException;

import jakarta.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.theokanning.openai.fine_tuning.FineTuningJob;
import com.wavemaker.runtime.ai.service.OpenAiFineTuneService;
import com.wavemaker.tools.api.core.annotations.WMAccessVisibility;
import com.wavemaker.tools.api.core.models.AccessSpecifier;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private OpenAiFineTuneService openAiFineTuneService;

    @Autowired
    private ServletContext servletContext;

    @WMAccessVisibility(value = AccessSpecifier.APP_ONLY)
    @PostMapping(value = "/train", consumes = "multipart/form-data")
    public FineTuningJob uploadFile(@RequestPart(value = "filePath", required = false) MultipartFile filePath) throws IOException {
        return openAiFineTuneService.uploadFile(filePath);
    }

    @WMAccessVisibility(value = AccessSpecifier.APP_ONLY)
    @PostMapping(value = "/trainfile")
    public FineTuningJob uploadFileWithFilePath(@RequestParam(value = "filePath") String filePath) {
        try {
            String path = servletContext.getResource(filePath).getPath();
            return openAiFineTuneService.uploadFile(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @WMAccessVisibility(value = AccessSpecifier.APP_ONLY)
    @GetMapping(value = "/query")
    public String queryFineTunedModel(@RequestParam(value = "prompt", required = false) String prompt, @RequestParam(value = "fineTunedModel", required = false) String fineTunedModel) {
        return openAiFineTuneService.queryFromFineTunedModel(prompt, fineTunedModel);
    }
}

