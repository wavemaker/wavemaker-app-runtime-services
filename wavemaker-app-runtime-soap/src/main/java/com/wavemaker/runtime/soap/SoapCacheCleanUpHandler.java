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

package com.wavemaker.runtime.soap;

import java.util.Set;

import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import com.sun.xml.bind.v2.ClassFactory;

public class SoapCacheCleanUpHandler implements SOAPHandler<SOAPMessageContext> {
    @Override
    public Set<QName> getHeaders() {
        return Set.of();
    }

    @Override
    public boolean handleMessage(SOAPMessageContext soapMessageContext) {
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext soapMessageContext) {
        return true;
    }

    @Override
    public void close(MessageContext messageContext) {
        ClassFactory.cleanCache();
    }
}
