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

package com.wavemaker.json.type;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic FieldDefinition implementation. This provides getters & setters access for all required attributes.
 * 
 * @author Matt Small
 */
public class GenericFieldDefinition implements FieldDefinition {

    public GenericFieldDefinition() {

    }

    public GenericFieldDefinition(TypeDefinition typeDefinition) {
        this();
        this.setTypeDefinition(typeDefinition);
    }

    public GenericFieldDefinition(TypeDefinition typeDefinition, List<ListTypeDefinition> arrayTypes) {
        this();
        this.setTypeDefinition(typeDefinition);
        this.setArrayTypes(arrayTypes);
    }

    /**
     * A list of Array/List types, in order. If this FieldDefinition represents a List<Set<String>>, the arrayTypes
     * would be [List.class, Set.class] (as ListTypeDefinitions, of course).
     */
    private List<ListTypeDefinition> arrayTypes;

    private TypeDefinition typeDefinition;

    private boolean allowNull;

    private String subType; // salesforce

    private String name;

    private List<OperationEnumeration> require = new ArrayList<OperationEnumeration>();

    private List<OperationEnumeration> noChange = new ArrayList<OperationEnumeration>();

    private List<OperationEnumeration> exclude = new ArrayList<OperationEnumeration>();

    @Override
    public List<ListTypeDefinition> getArrayTypes() {
        return this.arrayTypes;
    }

    public void setArrayTypes(List<ListTypeDefinition> arrayTypes) {
        this.arrayTypes = arrayTypes;
    }

    @Override
    public int getDimensions() {
        if (this.getArrayTypes() == null) {
            return 0;
        } else {
            return this.getArrayTypes().size();
        }
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        return this.typeDefinition;
    }

    public void setTypeDefinition(TypeDefinition typeDefinition) {
        this.typeDefinition = typeDefinition;
    }

    @Override
    public boolean isAllowNull() {
        return this.allowNull;
    }

    public void setAllowNull(boolean allowNull) {
        this.allowNull = allowNull;
    }

    @Override
    public String getSubType() {
        return this.subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<OperationEnumeration> getExclude() {
        return this.exclude;
    }

    public void setExclude(List<OperationEnumeration> exclude) {
        this.exclude = exclude;
    }

    @Override
    public List<OperationEnumeration> getNoChange() {
        return this.noChange;
    }

    public void setNoChange(List<OperationEnumeration> noChange) {
        this.noChange = noChange;
    }

    @Override
    public List<OperationEnumeration> getRequire() {
        return this.require;
    }

    public void setRequire(List<OperationEnumeration> require) {
        this.require = require;
    }

    @Override
    public String toString() {
        return "FieldDefinition[" + getDimensions() + "] of type (" + (getTypeDefinition() == null ? null : getTypeDefinition().toString() + ")");
    }
}