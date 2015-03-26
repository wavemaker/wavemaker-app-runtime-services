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

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.wavemaker.runtime.ws.jaxb.DateXmlAdapter;

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
 *         &lt;element name="sObjectType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="endDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "sObjectType", "startDate", "endDate" })
@XmlRootElement(name = "getUpdated", namespace = "urn:partner.soap.sforce.com")
public class GetUpdated {

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true)
    protected String sObjectType;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, type = String.class)
    @XmlJavaTypeAdapter(DateXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected Date startDate;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, type = String.class)
    @XmlJavaTypeAdapter(DateXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected Date endDate;

    /**
     * Gets the value of the sObjectType property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSObjectType() {
        return this.sObjectType;
    }

    /**
     * Sets the value of the sObjectType property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSObjectType(String value) {
        this.sObjectType = value;
    }

    /**
     * Gets the value of the startDate property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public Date getStartDate() {
        return this.startDate;
    }

    /**
     * Sets the value of the startDate property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setStartDate(Date value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the endDate property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public Date getEndDate() {
        return this.endDate;
    }

    /**
     * Sets the value of the endDate property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setEndDate(Date value) {
        this.endDate = value;
    }

}
