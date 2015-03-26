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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ApiFault complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ApiFault">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="exceptionCode" type="{urn:fault.partner.soap.sforce.com}ExceptionCode"/>
 *         &lt;element name="exceptionMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ApiFault", namespace = "urn:fault.partner.soap.sforce.com", propOrder = { "exceptionCode", "exceptionMessage" })
@XmlSeeAlso({ InvalidNewPasswordFault.class, LoginFault.class, InvalidQueryLocatorFault.class, InvalidIdFault.class, UnexpectedErrorFault.class,
    ApiQueryFaultType.class })
public class ApiFaultType {

    @XmlElement(namespace = "urn:fault.partner.soap.sforce.com", required = true)
    protected ExceptionCodeType exceptionCode;

    @XmlElement(namespace = "urn:fault.partner.soap.sforce.com", required = true)
    protected String exceptionMessage;

    /**
     * Gets the value of the exceptionCode property.
     * 
     * @return possible object is {@link ExceptionCodeType }
     * 
     */
    public ExceptionCodeType getExceptionCode() {
        return this.exceptionCode;
    }

    /**
     * Sets the value of the exceptionCode property.
     * 
     * @param value allowed object is {@link ExceptionCodeType }
     * 
     */
    public void setExceptionCode(ExceptionCodeType value) {
        this.exceptionCode = value;
    }

    /**
     * Gets the value of the exceptionMessage property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getExceptionMessage() {
        return this.exceptionMessage;
    }

    /**
     * Sets the value of the exceptionMessage property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setExceptionMessage(String value) {
        this.exceptionMessage = value;
    }

}
