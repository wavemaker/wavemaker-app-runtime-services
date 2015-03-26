/**
 * Copyright (C) 2014 WaveMaker, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.ws.salesforce.gen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="client" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="defaultNamespace" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "client", "defaultNamespace" })
@XmlRootElement(name = "CallOptions", namespace = "urn:partner.soap.sforce.com")
public class CallOptions {

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, nillable = true)
    protected String client;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, nillable = true)
    protected String defaultNamespace;

    /**
     * Gets the value of the client property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getClient() {
        return this.client;
    }

    /**
     * Sets the value of the client property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setClient(String value) {
        this.client = value;
    }

    /**
     * Gets the value of the defaultNamespace property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDefaultNamespace() {
        return this.defaultNamespace;
    }

    /**
     * Sets the value of the defaultNamespace property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setDefaultNamespace(String value) {
        this.defaultNamespace = value;
    }

}
