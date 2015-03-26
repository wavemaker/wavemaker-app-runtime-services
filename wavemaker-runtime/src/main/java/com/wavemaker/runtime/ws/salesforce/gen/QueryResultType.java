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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for QueryResult complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="done" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="queryLocator" type="{urn:partner.soap.sforce.com}QueryLocator"/>
 *         &lt;element name="records" type="{urn:sobject.partner.soap.sforce.com}sObject" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryResult", namespace = "urn:partner.soap.sforce.com", propOrder = { "done", "queryLocator", "records", "size" })
public class QueryResultType {

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected boolean done;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, nillable = true)
    protected String queryLocator;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", nillable = true)
    protected List<SObjectType> records;

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected int size;

    /**
     * Gets the value of the done property.
     * 
     */
    public boolean isDone() {
        return this.done;
    }

    /**
     * Sets the value of the done property.
     * 
     */
    public void setDone(boolean value) {
        this.done = value;
    }

    /**
     * Gets the value of the queryLocator property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getQueryLocator() {
        return this.queryLocator;
    }

    /**
     * Sets the value of the queryLocator property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setQueryLocator(String value) {
        this.queryLocator = value;
    }

    /**
     * Gets the value of the records property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the records property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getRecords().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link SObjectType }
     * 
     * 
     */
    public List<SObjectType> getRecords() {
        if (this.records == null) {
            this.records = new ArrayList<SObjectType>();
        }
        return this.records;
    }

    /**
     * Gets the value of the size property.
     * 
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Sets the value of the size property.
     * 
     */
    public void setSize(int value) {
        this.size = value;
    }

    /**
     * Sets the value of the records property.
     * 
     * @param records allowed object is {@link SObjectType }
     * 
     */
    public void setRecords(List<SObjectType> records) {
        this.records = records;
    }

}
