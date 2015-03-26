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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for Email complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Email">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bccSender" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="emailPriority" type="{urn:partner.soap.sforce.com}EmailPriority"/>
 *         &lt;element name="replyTo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="saveAsActivity" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="senderDisplayName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="subject" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="useSignature" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Email", namespace = "urn:partner.soap.sforce.com", propOrder = { "bccSender", "emailPriority", "replyTo", "saveAsActivity",
    "senderDisplayName", "subject", "useSignature" })
@XmlSeeAlso({ MassEmailMessageType.class, SingleEmailMessageType.class })
public class EmailType {

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, type = Boolean.class, nillable = true)
    protected Boolean bccSender;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, nillable = true)
    protected EmailPriorityType emailPriority;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, nillable = true)
    protected String replyTo;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, type = Boolean.class, nillable = true)
    protected Boolean saveAsActivity;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, nillable = true)
    protected String senderDisplayName;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, nillable = true)
    protected String subject;

    @XmlElement(namespace = "urn:partner.soap.sforce.com", required = true, type = Boolean.class, nillable = true)
    protected Boolean useSignature;

    /**
     * Gets the value of the bccSender property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public Boolean getBccSender() {
        return this.bccSender;
    }

    /**
     * Sets the value of the bccSender property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setBccSender(Boolean value) {
        this.bccSender = value;
    }

    /**
     * Gets the value of the emailPriority property.
     * 
     * @return possible object is {@link EmailPriorityType }
     * 
     */
    public EmailPriorityType getEmailPriority() {
        return this.emailPriority;
    }

    /**
     * Sets the value of the emailPriority property.
     * 
     * @param value allowed object is {@link EmailPriorityType }
     * 
     */
    public void setEmailPriority(EmailPriorityType value) {
        this.emailPriority = value;
    }

    /**
     * Gets the value of the replyTo property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getReplyTo() {
        return this.replyTo;
    }

    /**
     * Sets the value of the replyTo property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setReplyTo(String value) {
        this.replyTo = value;
    }

    /**
     * Gets the value of the saveAsActivity property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public Boolean getSaveAsActivity() {
        return this.saveAsActivity;
    }

    /**
     * Sets the value of the saveAsActivity property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setSaveAsActivity(Boolean value) {
        this.saveAsActivity = value;
    }

    /**
     * Gets the value of the senderDisplayName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSenderDisplayName() {
        return this.senderDisplayName;
    }

    /**
     * Sets the value of the senderDisplayName property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSenderDisplayName(String value) {
        this.senderDisplayName = value;
    }

    /**
     * Gets the value of the subject property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Sets the value of the subject property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSubject(String value) {
        this.subject = value;
    }

    /**
     * Gets the value of the useSignature property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public Boolean getUseSignature() {
        return this.useSignature;
    }

    /**
     * Sets the value of the useSignature property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setUseSignature(Boolean value) {
        this.useSignature = value;
    }

}
