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

package com.activegrid.runtime.data;

import org.hibernate.cfg.Configuration;

/**
 * Wraps a Hibernate Configuration with convenience methods.
 * 
 * @author Simon Toens
 * @deprecated This is now deprecated; see {@link com.wavemaker.runtime.data.DataServiceMetaData}. This will be removed
 *             in a future release.
 */
@Deprecated
public class DataServiceMetaData extends com.wavemaker.runtime.data.hibernate.DataServiceMetaData_Hib { // salesforce

    public DataServiceMetaData(String name, Configuration cfg) {
        super(name, cfg);
    }

}
