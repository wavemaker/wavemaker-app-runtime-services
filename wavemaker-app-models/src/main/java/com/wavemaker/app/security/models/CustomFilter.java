/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models;

import java.util.Objects;

/**
 * @author Uday Shankar
 */
public class CustomFilter {

    private String name;
    private String ref;
    private String before;
    private String position;
    private String after;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomFilter that = (CustomFilter) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(ref, that.ref) &&
            Objects.equals(before, that.before) &&
            Objects.equals(position, that.position) &&
            Objects.equals(after, that.after);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ref, before, position, after);
    }
}
