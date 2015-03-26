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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for EmailPriority.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="EmailPriority">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Highest"/>
 *     &lt;enumeration value="High"/>
 *     &lt;enumeration value="Normal"/>
 *     &lt;enumeration value="Low"/>
 *     &lt;enumeration value="Lowest"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "EmailPriority", namespace = "urn:partner.soap.sforce.com")
@XmlEnum
public enum EmailPriorityType {

    @XmlEnumValue("Highest")
    HIGHEST("Highest"), @XmlEnumValue("High")
    HIGH("High"), @XmlEnumValue("Normal")
    NORMAL("Normal"), @XmlEnumValue("Low")
    LOW("Low"), @XmlEnumValue("Lowest")
    LOWEST("Lowest");

    private final String value;

    EmailPriorityType(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static EmailPriorityType fromValue(String v) {
        for (EmailPriorityType c : EmailPriorityType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
