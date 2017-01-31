/**
 * Copyright © 2013 - 2017 WaveMaker, Inc.
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
package com.wavemaker.studio.json.type.reflect;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.springframework.util.ClassUtils;

import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.studio.json.type.TypeDefinition;
import com.wavemaker.studio.json.type.TypeState;

/**
 * @author Matt Small
 */
public class ReflectTypeState implements TypeState {

    private final Map<String, TypeDefinition> knownTypes = new HashMap<String, TypeDefinition>();

    private final PropertyUtilsBean propertyUtilsBean = new WMPropertyUtilsBean();

    @Override
    public void addType(TypeDefinition typeDefinition) {
        this.knownTypes.put(typeDefinition.getTypeName(), typeDefinition);
    }

    @Override
    public TypeDefinition getType(String typeName) {

        if (this.knownTypes.containsKey(typeName)) {
            return this.knownTypes.get(typeName);
        } else {
            try {
                TypeDefinition td = ReflectTypeUtils.getTypeDefinition(ClassUtils.forName(typeName, typeName.getClass().getClassLoader()), this, false);
                addType(td);
                return td;
            } catch (ClassNotFoundException e) {
                throw new WMRuntimeException(e);
            } catch (LinkageError e) {
                throw new WMRuntimeException(e);
            }
        }
    }

    @Override
    public boolean isTypeKnown(String typeName) {

        return this.knownTypes.containsKey(typeName);
    }

    public PropertyUtilsBean getPropertyUtilsBean() {
        return this.propertyUtilsBean;
    }

    public Map<String, TypeDefinition> getKnownTypes() {
        return this.knownTypes;
    }
}