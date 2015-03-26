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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.wavemaker.runtime.ws.jaxb.DateXmlAdapter;

/**
 * <p>
 * Java class for GetUpdatedResult complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetUpdatedResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ids" type="{urn:partner.soap.sforce.com}ID" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="latestDateCovered" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="sforceReserved" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetUpdatedResult", namespace = "urn:partner.soap.sforce.com", propOrder = { "ids", "latestDateCovered", "sforceReserved" })
public class GetUpdatedResultType {

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected List<String> ids;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, type = String.class)
    @XmlJavaTypeAdapter(DateXmlAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected Date latestDateCovered;

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected String sforceReserved;

    /**
     * Gets the value of the ids property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the ids property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     * 
     * 
     */
    public List<String> getIds() {
        if (this.ids == null) {
            this.ids = new ArrayList<String>();
        }
        return this.ids;
    }

    /**
     * Gets the value of the latestDateCovered property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public Date getLatestDateCovered() {
        return this.latestDateCovered;
    }

    /**
     * Sets the value of the latestDateCovered property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setLatestDateCovered(Date value) {
        this.latestDateCovered = value;
    }

    /**
     * Gets the value of the sforceReserved property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSforceReserved() {
        return this.sforceReserved;
    }

    /**
     * Sets the value of the sforceReserved property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSforceReserved(String value) {
        this.sforceReserved = value;
    }

    /**
     * Sets the value of the ids property.
     * 
     * @param ids allowed object is {@link String }
     * 
     */
    public void setIds(List<String> ids) {
        this.ids = ids;
    }

}
