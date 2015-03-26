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
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for DescribeSoftphoneScreenPopOption complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeSoftphoneScreenPopOption">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="matchType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="screenPopData" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="screenPopType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeSoftphoneScreenPopOption", namespace = "urn:partner.soap.sforce.com", propOrder = { "matchType", "screenPopData",
    "screenPopType" })
public class DescribeSoftphoneScreenPopOptionType {

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true)
    protected String matchType;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true)
    protected String screenPopData;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true)
    protected String screenPopType;

    /**
     * Gets the value of the matchType property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getMatchType() {
        return this.matchType;
    }

    /**
     * Sets the value of the matchType property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setMatchType(String value) {
        this.matchType = value;
    }

    /**
     * Gets the value of the screenPopData property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getScreenPopData() {
        return this.screenPopData;
    }

    /**
     * Sets the value of the screenPopData property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setScreenPopData(String value) {
        this.screenPopData = value;
    }

    /**
     * Gets the value of the screenPopType property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getScreenPopType() {
        return this.screenPopType;
    }

    /**
     * Sets the value of the screenPopType property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setScreenPopType(String value) {
        this.screenPopType = value;
    }

}
