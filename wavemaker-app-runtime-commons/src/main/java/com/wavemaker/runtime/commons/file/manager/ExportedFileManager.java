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
package com.wavemaker.runtime.commons.file.manager;

import java.io.OutputStream;
import java.util.function.Consumer;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.wavemaker.runtime.commons.file.model.ExportedFileContentWrapper;

public interface ExportedFileManager {
    default String registerAndGetURL(String fileName, Consumer<OutputStream> callback) {
        String fileID = registerFile(fileName, callback);
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/services/files/exported/{path}")
                .buildAndExpand(fileID)
                .toUriString();
    }

    String registerFile(String fileName, Consumer<OutputStream> callback);

    ExportedFileContentWrapper getFileContent(String fileId);
}
