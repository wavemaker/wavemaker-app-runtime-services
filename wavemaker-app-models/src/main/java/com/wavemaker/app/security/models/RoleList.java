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

public class RoleList extends ArrayList<Role> {
    public RoleList(int initialCapacity) {
        super(initialCapacity);
    }

    public RoleList() {
    }

    public RoleList(Collection<? extends Role> c) {
        super(c);
    }
}
