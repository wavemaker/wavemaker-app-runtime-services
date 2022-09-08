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
package com.wavemaker.runtime.webprocess.model;

public class WebProcess {
    private String processName;
    private String communicationKey;
    private String hookUrl;
    private String requestSourceType;

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getCommunicationKey() {
        return communicationKey;
    }

    public void setCommunicationKey(String communicationKey) {
        this.communicationKey = communicationKey;
    }

    public String getHookUrl() {
        return hookUrl;
    }

    public void setHookUrl(String hookUrl) {
        this.hookUrl = hookUrl;
    }

    public String getRequestSourceType() {
        return requestSourceType;
    }

    public void setRequestSourceType(String requestSourceType) {
        this.requestSourceType = requestSourceType;
    }

    @Override
    public String toString() {
        return "WebProcess{" +
                "processName='" + processName + '\'' +
                ", communicationKey='" + communicationKey + '\'' +
                ", hookUrl='" + hookUrl + '\'' +
                ", requestSourceType='" + requestSourceType + '\'' +
                '}';
    }
}
