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
 * Java class for PackageVersion complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PackageVersion">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="majorNumber" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="minorNumber" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="namespace" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PackageVersion", namespace = "urn:partner.soap.sforce.com", propOrder = { "majorNumber", "minorNumber", "namespace" })
public class PackageVersionType {

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected int majorNumber;

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected int minorNumber;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true)
    protected String namespace;

    /**
     * Gets the value of the majorNumber property.
     * 
     */
    public int getMajorNumber() {
        return this.majorNumber;
    }

    /**
     * Sets the value of the majorNumber property.
     * 
     */
    public void setMajorNumber(int value) {
        this.majorNumber = value;
    }

    /**
     * Gets the value of the minorNumber property.
     * 
     */
    public int getMinorNumber() {
        return this.minorNumber;
    }

    /**
     * Sets the value of the minorNumber property.
     * 
     */
    public void setMinorNumber(int value) {
        this.minorNumber = value;
    }

    /**
     * Gets the value of the namespace property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * Sets the value of the namespace property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setNamespace(String value) {
        this.namespace = value;
    }

}
