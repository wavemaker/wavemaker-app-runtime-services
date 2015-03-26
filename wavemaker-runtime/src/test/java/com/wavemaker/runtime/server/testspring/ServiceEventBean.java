/*
 *  Copyright (C) 2007-2009 WaveMaker Software, Inc.
 *
 *  This file is part of the WaveMaker Server Runtime.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.runtime.server.testspring;

/**
 * @author Matt Small
 */
public class ServiceEventBean {

    private long count = 0;

    public void increment() {
        this.count++;
    }

    public long getCount() {
        return this.count;
    }

    public long getValue(long input) {
        return input;
    }

    public void throwsException() throws Exception {
        throw new Exception("bla");
    }
}
