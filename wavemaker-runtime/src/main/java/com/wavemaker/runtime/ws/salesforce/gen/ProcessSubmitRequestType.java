/*
 * Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
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

package com.wavemaker.runtime.ws.salesforce.gen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ProcessSubmitRequest complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProcessSubmitRequest">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:partner.soap.sforce.com}ProcessRequest">
 *       &lt;sequence>
 *         &lt;element name="objectId" type="{urn:partner.soap.sforce.com}ID"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessSubmitRequest", namespace = "urn:partner.soap.sforce.com", propOrder = { "objectId" })
public class ProcessSubmitRequestType extends ProcessRequestType {

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true)
    protected String objectId;

    /**
     * Gets the value of the objectId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getObjectId() {
        return this.objectId;
    }

    /**
     * Sets the value of the objectId property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setObjectId(String value) {
        this.objectId = value;
    }

}
