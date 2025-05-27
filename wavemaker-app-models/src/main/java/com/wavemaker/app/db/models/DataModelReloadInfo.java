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
package com.wavemaker.app.db.models;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 17/8/16
 */
public class DataModelReloadInfo {

    private boolean reload;
    private Reason reason;
    private String message;
    private boolean reImport;

    public DataModelReloadInfo() {
        this.reload = false;
    }

    public DataModelReloadInfo(final boolean reload, final Reason reason, final String message) {
        this.reload = reload;
        this.reason = reason;
        this.message = message;
    }

    public boolean isReload() {
        return reload;
    }

    public void setReload(final boolean reload) {
        this.reload = reload;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(final Reason reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public boolean isReImport() {
        return reImport;
    }

    public void setReImport(boolean reImport) {
        this.reImport = reImport;
    }

    public enum Reason {
        MIGRATION_FAILED,
        UPDATED_FAILED
    }
}
