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
 * Java class for DescribeLayoutSection complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescribeLayoutSection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="columns" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="heading" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="layoutRows" type="{urn:partner.soap.sforce.com}DescribeLayoutRow" maxOccurs="unbounded"/>
 *         &lt;element name="rows" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="useCollapsibleSection" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="useHeading" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescribeLayoutSection", namespace = "urn:partner.soap.sforce.com", propOrder = { "columns", "heading", "layoutRows", "rows",
    "useCollapsibleSection", "useHeading" })
public class DescribeLayoutSectionType {

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected int columns;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true)
    protected String heading;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true)
    protected List<DescribeLayoutRowType> layoutRows;

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected int rows;

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected boolean useCollapsibleSection;

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected boolean useHeading;

    /**
     * Gets the value of the columns property.
     * 
     */
    public int getColumns() {
        return this.columns;
    }

    /**
     * Sets the value of the columns property.
     * 
     */
    public void setColumns(int value) {
        this.columns = value;
    }

    /**
     * Gets the value of the heading property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getHeading() {
        return this.heading;
    }

    /**
     * Sets the value of the heading property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setHeading(String value) {
        this.heading = value;
    }

    /**
     * Gets the value of the layoutRows property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the layoutRows property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getLayoutRows().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link DescribeLayoutRowType }
     * 
     * 
     */
    public List<DescribeLayoutRowType> getLayoutRows() {
        if (this.layoutRows == null) {
            this.layoutRows = new ArrayList<DescribeLayoutRowType>();
        }
        return this.layoutRows;
    }

    /**
     * Gets the value of the rows property.
     * 
     */
    public int getRows() {
        return this.rows;
    }

    /**
     * Sets the value of the rows property.
     * 
     */
    public void setRows(int value) {
        this.rows = value;
    }

    /**
     * Gets the value of the useCollapsibleSection property.
     * 
     */
    public boolean isUseCollapsibleSection() {
        return this.useCollapsibleSection;
    }

    /**
     * Sets the value of the useCollapsibleSection property.
     * 
     */
    public void setUseCollapsibleSection(boolean value) {
        this.useCollapsibleSection = value;
    }

    /**
     * Gets the value of the useHeading property.
     * 
     */
    public boolean isUseHeading() {
        return this.useHeading;
    }

    /**
     * Sets the value of the useHeading property.
     * 
     */
    public void setUseHeading(boolean value) {
        this.useHeading = value;
    }

    /**
     * Sets the value of the layoutRows property.
     * 
     * @param layoutRows allowed object is {@link DescribeLayoutRowType }
     * 
     */
    public void setLayoutRows(List<DescribeLayoutRowType> layoutRows) {
        this.layoutRows = layoutRows;
    }

}
