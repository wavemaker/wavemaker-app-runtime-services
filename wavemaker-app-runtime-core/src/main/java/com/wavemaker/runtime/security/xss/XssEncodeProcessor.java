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

package com.wavemaker.runtime.security.xss;

import java.util.Objects;

import com.wavemaker.commons.processor.ObjectInfo;
import com.wavemaker.commons.processor.ObjectProcessor;
import com.wavemaker.runtime.security.xss.handler.XSSSecurityHandler;

public class XssEncodeProcessor implements ObjectProcessor {

    private final DataFlowType dataFlowType;

    public XssEncodeProcessor(DataFlowType dataFlowType) {
        this.dataFlowType = dataFlowType;
    }

    @Override
    public void processObject(ObjectInfo objectInfo) {
        Object object = objectInfo.getObject();
        if (object instanceof String) {
            ResponseTuple responseTuple = sanitize((String) object, dataFlowType);
            if (responseTuple.modified) {
                objectInfo.updateObject(responseTuple.value);
            }
        } else if (object.getClass() == char[].class) {
            ResponseTuple responseTuple = sanitize(new String((char[]) object), dataFlowType);
            if (responseTuple.modified) {
                objectInfo.updateObject(responseTuple.value.toCharArray());
            }
        }
    }

    private ResponseTuple sanitize(String value, DataFlowType dataFlowType) {
        String sanitizedValue;
        switch (dataFlowType) {
            case OUTGOING:
                sanitizedValue = XSSSecurityHandler.getInstance().sanitizeOutgoingData(value);
                break;
            case INCOMING:
                sanitizedValue = XSSSecurityHandler.getInstance().sanitizeIncomingData(value);
                break;
            default:
                throw new IllegalStateException();
        }
        boolean modified = !Objects.equals(value, sanitizedValue);
        return new ResponseTuple(sanitizedValue, modified);
    }

    private static class ResponseTuple {
        private final String value;
        private final boolean modified;

        public ResponseTuple(final String value, final boolean modified) {
            this.value = value;
            this.modified = modified;
        }
    }
}
