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

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * Details of a deployable application.
 * 
 * @author Phillip Webb
 */
public class ApplicationDetails {

    private final String name;

    private final String url;

    /**
     * Create a new {@link ApplicationDetails} instance.
     * 
     * @param name the name of the application
     * @param url the URI of the application
     */
    public ApplicationDetails(String name, String url) {
        super();
        Assert.notNull(name, "Name must not be null");
        Assert.notNull(url, "URL must not be null");
        this.name = name;
        this.url = url;
    }

    /**
     * Returns the name of the application.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the URL of the application.
     * 
     * @return the URL.
     */
    public String getUrl() {
        return this.url;
    }

    @Override
    public String toString() {
        return new ToStringCreator(this).append("name", this.name).append("url", this.url).toString();
    }
}
