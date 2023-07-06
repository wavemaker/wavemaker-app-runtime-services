/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of WaveMaker, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the source code license agreement you entered into with WaveMaker, Inc.
 ******************************************************************************/
package com.wavemaker.app.web.http;

/**
 * Created by nileshk on 12/1/15.
 */
public enum HttpMethod {
    GET(false),
    POST(true),
    PUT(true),
    DELETE(true),
    HEAD(false),
    OPTIONS(true),
    TRACE(true),
    PATCH(true);

    private boolean requestBodySupported;

    HttpMethod(boolean requestBodySupported) {
        this.requestBodySupported = requestBodySupported;
    }

    public boolean isRequestBodySupported() {
        return requestBodySupported;
    }

}
