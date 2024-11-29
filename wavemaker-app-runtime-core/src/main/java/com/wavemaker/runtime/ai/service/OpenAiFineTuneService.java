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

package com.wavemaker.runtime.ai.service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.file.File;
import com.theokanning.openai.fine_tuning.FineTuningJob;
import com.theokanning.openai.fine_tuning.FineTuningJobRequest;
import com.theokanning.openai.fine_tuning.Hyperparameters;
import com.theokanning.openai.service.OpenAiService;

public class OpenAiFineTuneService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiFineTuneService.class);

    private final OpenAiService openAiService;

    public OpenAiFineTuneService(@Value("${openai.api.key}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey);
    }

    public FineTuningJob uploadFile(MultipartFile filePath) throws IOException {
        java.io.File aidatasets = Files.createTempDirectory("aidatasets").toFile();
        java.io.File uploadedFile = new java.io.File(aidatasets, "train.jsonl");
        filePath.transferTo(uploadedFile);
        File file = openAiService.uploadFile("fine-tune", uploadedFile.getAbsolutePath());
        return fineTuneModel(file.getId()); // Return file ID for further use
    }

    public FineTuningJob uploadFile(java.io.File filePath) throws IOException {
        File file = openAiService.uploadFile("fine-tune", filePath.getAbsolutePath());
        return fineTuneModel(file.getId()); // Return file ID for further use
    }

    // Start fine-tuning
    public FineTuningJob fineTuneModel(String fileId) {
        Hyperparameters hyperparameters = new Hyperparameters();
        hyperparameters.setNEpochs(20);
        FineTuningJobRequest request = FineTuningJobRequest.builder()
            .trainingFile(fileId)
            .model("gpt-4o-2024-08-06")// Base model for fine-tuning
            .suffix("chat-crafters")
            .hyperparameters(hyperparameters)
            .build();

        FineTuningJob fineTuningJob = openAiService.createFineTuningJob(request);
        return fineTuningJob; // Fine-tune job ID
    }

    public String queryFromFineTunedModel(String prompt, String fineTunedModel) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(fineTunedModel) // Fine-tuned model name
            .messages(Collections.singletonList(new ChatMessage("user", prompt)))
            .maxTokens(150)
            .temperature(0.7)
            .build();

        return openAiService.createChatCompletion(request)
            .getChoices()
            .get(0)
            .getMessage()
            .getContent();
    }
}
