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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for MergeResult complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MergeResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="errors" type="{urn:partner.soap.sforce.com}Error" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="id" type="{urn:partner.soap.sforce.com}ID"/>
 *         &lt;element name="mergedRecordIds" type="{urn:partner.soap.sforce.com}ID" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="success" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="updatedRelatedIds" type="{urn:partner.soap.sforce.com}ID" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MergeResult", namespace = "urn:partner.soap.sforce.com", propOrder = { "errors", "id", "mergedRecordIds", "success",
    "updatedRelatedIds" })
public class MergeResultType {

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected List<ErrorType> errors;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, nillable = true)
    protected String id;

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected List<String> mergedRecordIds;

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected boolean success;

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected List<String> updatedRelatedIds;

    /**
     * Gets the value of the errors property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the errors property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getErrors().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ErrorType }
     * 
     * 
     */
    public List<ErrorType> getErrors() {
        if (this.errors == null) {
            this.errors = new ArrayList<ErrorType>();
        }
        return this.errors;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the mergedRecordIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the mergedRecordIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getMergedRecordIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     * 
     * 
     */
    public List<String> getMergedRecordIds() {
        if (this.mergedRecordIds == null) {
            this.mergedRecordIds = new ArrayList<String>();
        }
        return this.mergedRecordIds;
    }

    /**
     * Gets the value of the success property.
     * 
     */
    public boolean isSuccess() {
        return this.success;
    }

    /**
     * Sets the value of the success property.
     * 
     */
    public void setSuccess(boolean value) {
        this.success = value;
    }

    /**
     * Gets the value of the updatedRelatedIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the updatedRelatedIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getUpdatedRelatedIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     * 
     * 
     */
    public List<String> getUpdatedRelatedIds() {
        if (this.updatedRelatedIds == null) {
            this.updatedRelatedIds = new ArrayList<String>();
        }
        return this.updatedRelatedIds;
    }

    /**
     * Sets the value of the errors property.
     * 
     * @param errors allowed object is {@link ErrorType }
     * 
     */
    public void setErrors(List<ErrorType> errors) {
        this.errors = errors;
    }

    /**
     * Sets the value of the mergedRecordIds property.
     * 
     * @param mergedRecordIds allowed object is {@link String }
     * 
     */
    public void setMergedRecordIds(List<String> mergedRecordIds) {
        this.mergedRecordIds = mergedRecordIds;
    }

    /**
     * Sets the value of the updatedRelatedIds property.
     * 
     * @param updatedRelatedIds allowed object is {@link String }
     * 
     */
    public void setUpdatedRelatedIds(List<String> updatedRelatedIds) {
        this.updatedRelatedIds = updatedRelatedIds;
    }

}
