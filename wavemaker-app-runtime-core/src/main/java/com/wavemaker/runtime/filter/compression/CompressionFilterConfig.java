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
package com.wavemaker.runtime.filter.compression;

/**
 * @author Kishore Routhu on 10/10/17 7:15 PM.
 */
public class CompressionFilterConfig {

    private boolean enableCompression;

    private int minCompressSize;

    private String includeMimeTypes;

    private String excludeMimeTypes;

    public CompressionFilterConfig() {
    }

    public boolean isEnableCompression() {
        return enableCompression;
    }

    public void setEnableCompression(boolean enableCompression) {
        this.enableCompression = enableCompression;
    }

    public int getMinCompressSize() {
        return minCompressSize;
    }

    public void setMinCompressSize(int minCompressSize) {
        this.minCompressSize = minCompressSize;
    }

    public String getIncludeMimeTypes() {
        return includeMimeTypes;
    }

    public void setIncludeMimeTypes(String includeMimeTypes) {
        this.includeMimeTypes = includeMimeTypes;
    }

    public String getExcludeMimeTypes() {
        return excludeMimeTypes;
    }

    public void setExcludeMimeTypes(String excludeMimeTypes) {
        this.excludeMimeTypes = excludeMimeTypes;
    }

}
