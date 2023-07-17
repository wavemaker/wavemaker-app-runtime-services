/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.security.models;

import java.util.ArrayList;
import java.util.Collection;

public class CustomFilterList extends ArrayList<CustomFilter> {
    public CustomFilterList(int initialCapacity) {
        super(initialCapacity);
    }

    public CustomFilterList() {
    }

    public CustomFilterList(Collection<? extends CustomFilter> c) {
        super(c);
    }
}
