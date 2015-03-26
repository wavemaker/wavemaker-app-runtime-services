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

package com.wavemaker.json.type.reflect;

import org.apache.commons.beanutils.PropertyUtilsBean;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * @author Seung Lee
 */
public class WMPropertyUtilsBean extends PropertyUtilsBean {

    private String name = null;

    //Convert the first letter of the field name to upper case if appropriate
    @Override
    public PropertyDescriptor[] getPropertyDescriptors(Class klass) {
        PropertyDescriptor[] pds = super.getPropertyDescriptors(klass);

        for (PropertyDescriptor pd : pds) {
            String name = pd.getName();
            Field fld = null;
            try {
                fld = klass.getDeclaredField(name);
            } catch (NoSuchFieldException ex) {
            }

            if (fld != null) {
                continue;
            }

            String shifted = name.substring(0, 1).toUpperCase();

            String newName = shifted + name.substring(1);

            fld = null;
            try {
                fld = klass.getDeclaredField(newName);
            } catch (NoSuchFieldException ex) {
            }

            if (fld != null) {
                pd.setName(newName);
            }
        }

        return pds;
        }
    }
