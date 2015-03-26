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
 *         &lt;element name="pairs" type="{urn:partner.soap.sforce.com}DataCategoryGroupSobjectTypePair" maxOccurs="100" minOccurs="0"/>
 *         &lt;element name="topCategoriesOnly" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "pairs", "topCategoriesOnly" })
@XmlRootElement(name = "describeDataCategoryGroupStructures", namespace = "urn:partner.soap.sforce.com")
public class DescribeDataCategoryGroupStructures {

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected List<DataCategoryGroupSobjectTypePairType> pairs;

    @XmlElement(namespace = "urn:partner.soap.sforce.com")
    protected boolean topCategoriesOnly;

    /**
     * Gets the value of the pairs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the pairs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getPairs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link DataCategoryGroupSobjectTypePairType }
     * 
     * 
     */
    public List<DataCategoryGroupSobjectTypePairType> getPairs() {
        if (this.pairs == null) {
            this.pairs = new ArrayList<DataCategoryGroupSobjectTypePairType>();
        }
        return this.pairs;
    }

    /**
     * Gets the value of the topCategoriesOnly property.
     * 
     */
    public boolean isTopCategoriesOnly() {
        return this.topCategoriesOnly;
    }

    /**
     * Sets the value of the topCategoriesOnly property.
     * 
     */
    public void setTopCategoriesOnly(boolean value) {
        this.topCategoriesOnly = value;
    }

    /**
     * Sets the value of the pairs property.
     * 
     * @param pairs allowed object is {@link DataCategoryGroupSobjectTypePairType }
     * 
     */
    public void setPairs(List<DataCategoryGroupSobjectTypePairType> pairs) {
        this.pairs = pairs;
    }

}
