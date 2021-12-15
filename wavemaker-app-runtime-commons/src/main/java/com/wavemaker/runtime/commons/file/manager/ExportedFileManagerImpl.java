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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.io.TempFilesStorageManager;
import com.wavemaker.commons.util.WMIOUtils;
import com.wavemaker.runtime.commons.file.model.ExportedFileContentWrapper;

@Service
public class ExportedFileManagerImpl implements ExportedFileManager {

    @Autowired
    private TempFilesStorageManager tempFilesStorageManager;

    @Override
    public String registerFile(String fileName, Consumer<OutputStream> consumer) {
        OutputStream outputStream = null;
        try {
            String exportedFileId = tempFilesStorageManager.registerNewFile(fileName);
            outputStream = tempFilesStorageManager.getFileOutputStream(exportedFileId);
            consumer.accept(outputStream);
            return exportedFileId;
        } catch (Exception e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.file.writing.exception"), e);
        } finally {
            WMIOUtils.closeSilently(outputStream);
        }
    }

    @Override
    public ExportedFileContentWrapper getFileContent(String fileId) {
        String fileName = tempFilesStorageManager.getFileName(fileId);
        InputStream inputStream = tempFilesStorageManager.getFileInputStream(fileId);
        return new ExportedFileContentWrapper(fileName, inputStream);
    }
}
