/*
 *  Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
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

package com.wavemaker.runtime.ws.jaxb;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Frankie Fu
 */
public class DateXmlAdapter extends XmlAdapter<String, Date> {

    @Override
    public String marshal(Date value) {
        return com.wavemaker.runtime.ws.jaxb.DateAdapter.printDate(value);
    }

    @Override
    public Date unmarshal(String value) {
        return com.wavemaker.runtime.ws.jaxb.DateAdapter.parseDate(value);
    }

}
