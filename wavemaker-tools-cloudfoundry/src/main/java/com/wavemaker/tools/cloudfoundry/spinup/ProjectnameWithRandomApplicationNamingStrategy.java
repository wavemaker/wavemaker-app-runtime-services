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

package com.wavemaker.tools.cloudfoundry.spinup;

/**
 * {@link ApplicationNamingStrategy} that generates application URLs based on the logged in used combined with random
 * characters. The application name is fixed.
 * 
 * @author Ed Callahan
 */
public class ProjectnameWithRandomApplicationNamingStrategy extends AbstractRandomApplicationNamingStrategy {

    private final String applicationName;

    public ProjectnameWithRandomApplicationNamingStrategy(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    protected String getApplicationName() {
        return this.applicationName;
    }

}
