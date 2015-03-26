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

package com.wavemaker.json;

import java.util.ArrayList;
import java.util.List;

import com.wavemaker.json.type.TypeState;
import com.wavemaker.json.type.reflect.ReflectTypeState;

/**
 * Contains both JSON serialization/de-serialization state and configuration. State is emphasized; objects of JSONState
 * should not be shared between serialization or de-serialization running in concurrent threads.
 * 
 * @author Matt Small
 */
public class JSONState {

    /**
     * An enumeration describing possible cycle handling algorithms.
     */
    public enum CycleHandler {
        /**
         * Throw an exception when a cycle is dectected.
         */
        FAIL,

        /**
         * Insert a null when a cycle is detected at the element where the cycle would occur. If the object graph is
         * A->B->A, this will be transformed to A->B->null. The property will be left intact.
         */
        NULL,

        /**
         * Trim the property that would have resulted in a cycle off the object graph entirely. A->B->A becomes A->B.
         */
        NO_PROPERTY,
    };

    /**
     * Properties which are required. These will be allowed in cycles from JSONMarshaller.
     */
    private List<String> requiredProperties = new ArrayList<String>();

    /**
     * Number of levels to trim off of requiredProperties in JSONMarshaller. For instance, if trimStackLevel was set to
     * 1, and the marshaller is currently serializing a property:
     * 
     * foo.bar.baz
     * 
     * A requiredProperties that includes "bar.baz" would cause the element to be included.
     * 
     * This is also used for trimming properties for matching against the exclusion list.
     */
    private int trimStackLevel = 0;

    /**
     * A list of setters called in JSON -> Object conversions, in beanutils property format.
     */
    private List<String> settersCalled = new ArrayList<String>();

    /**
     * The CycleHandler; defaults to CycleHandler.NULL.
     */
    private CycleHandler cycleHandler = CycleHandler.NULL;

    /**
     * A list of properties to be excluded from serialization or deserialization.
     */
    private List<String> excludes = new ArrayList<String>();

    /**
     * Property filter.
     */
    private PropertyFilter propertyFilter;

    /**
     * A value transformer. See the class for more information; only a single entry is allowed, but
     * ChainingValueTransformers can be used to chain several transformers together.
     */
    private ValueTransformer valueTransformer;

    /**
     * This JSONState's current typeState. Defaults to a {@link ReflectTypeState}.
     */
    private TypeState typeState = new ReflectTypeState();

    /**
     * The JSON spec says that keys should be quoted, so objects look like this:
     * 
     * {"foo": "bar"}
     * 
     * In JavaScript, keys are often unquoted, like this:
     * 
     * {foo: "bar"}
     * 
     * By default, quoted keys will be generated. When unquoteKeys is set to 'true', JavaScript unquoted keys will be
     * generated.
     */
    private boolean unquoteKeys = false;

    // getters & setters
    public List<String> getRequiredProperties() {
        return this.requiredProperties;
    }

    public void setRequiredProperties(List<String> requiredProperties) {
        this.requiredProperties = requiredProperties;
    }

    public int getTrimStackLevel() {
        return this.trimStackLevel;
    }

    public void setTrimStackLevel(int trimStackLevel) {
        this.trimStackLevel = trimStackLevel;
    }

    public List<String> getSettersCalled() {
        return this.settersCalled;
    }

    public void setSettersCalled(List<String> settersCalled) {
        this.settersCalled = settersCalled;
    }

    public List<String> getExcludes() {
        return this.excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public CycleHandler getCycleHandler() {
        return this.cycleHandler;
    }

    public void setCycleHandler(CycleHandler cycleHandler) {
        this.cycleHandler = cycleHandler;
    }

    public void setPropertyFilter(PropertyFilter instance) {
        this.propertyFilter = instance;
    }

    public PropertyFilter getPropertyFilter() {
        return this.propertyFilter;
    }

    public ValueTransformer getValueTransformer() {
        return this.valueTransformer;
    }

    public void setValueTransformer(ValueTransformer valueTransformer) {
        this.valueTransformer = valueTransformer;
    }

    public void setTypeState(TypeState typeState) {
        this.typeState = typeState;
    }

    public TypeState getTypeState() {
        return this.typeState;
    }

    public void setUnquoteKeys(boolean noKeyQuotes) {
        this.unquoteKeys = noKeyQuotes;
    }

    public boolean isUnquoteKeys() {
        return this.unquoteKeys;
    }
}