/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wavemaker.runtime.converters;

import java.io.IOException;
import java.util.Date;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import com.wavemaker.commons.json.deserializer.WMDateDeSerializer;
import com.wavemaker.commons.json.deserializer.WMSqlDateDeSerializer;
import com.wavemaker.commons.util.WMIOUtils;
import com.wavemaker.runtime.commons.converters.WMCustomAbstractHttpMessageConverter;

/**
 * Created by srujant on 16/5/17.
 */
public class DateHttpMessageConverter extends WMCustomAbstractHttpMessageConverter<Date> {

    public DateHttpMessageConverter() {
        super(MediaType.ALL);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Date.class.isAssignableFrom(clazz);
    }

    @Override
    protected Date readInternal(Class<? extends Date> clazz, HttpInputMessage inputMessage) throws IOException {
        String date = WMIOUtils.toString(inputMessage.getBody());
        Date dateObj;
        if (clazz.equals(java.sql.Date.class)) {
            dateObj = WMSqlDateDeSerializer.getDate(date);
        } else {
            dateObj = WMDateDeSerializer.getDate(date);
        }
        return dateObj;
    }

    @Override
    protected void writeInternal(Date date, HttpOutputMessage outputMessage) throws IOException {
        outputMessage.getBody().write(date.toString().getBytes());
    }

}
