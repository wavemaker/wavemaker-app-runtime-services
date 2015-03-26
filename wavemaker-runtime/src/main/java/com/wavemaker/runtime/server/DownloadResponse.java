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

package com.wavemaker.runtime.server;

import java.io.InputStream;

/**
 * A class containing a download response. This should be used as the return type for any operation which handles
 * download requests.
 * 
 * @author Matt Small
 */
public class DownloadResponse implements Downloadable {

    private InputStream contents;

    private String contentType;

    private String fileName;

    public DownloadResponse() {
    }

    public DownloadResponse(InputStream contents, String contentType, String fileName) {
        this.contents = contents;
        this.contentType = contentType;
        this.fileName = fileName;
    }

    @Override
    public InputStream getContents() {
        return this.contents;
    }

    public void setContents(InputStream contents) {
        this.contents = contents;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Integer getContentLength() {
        return null;
    }
}