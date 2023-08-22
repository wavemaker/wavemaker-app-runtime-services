/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.wavemaker.runtime.security.constants;

public class SecurityConstants {

    private SecurityConstants() {
    }

    //providers
    public static final String AD_PROVIDER = "AD";
    public static final String LDAP_PROVIDER = "LDAP";
    public static final String DATABASE_PROVIDER = "DATABASE";
    public static final String DEMO_PROVIDER = "DEMO";
    public static final String OPENID_PROVIDER = "OPENID";
    public static final String CAS_PROVIDER = "CAS";
    public static final String SAML_PROVIDER = "SAML";
    public static final String OPAQUE_PROVIDER = "OPAQUE";
    public static final String JWS_PROVIDER = "JWS";
    public static final String CUSTOM_PROVIDER = "CUSTOM";

    //session persistence
    public static final String IN_MEMORY = "in-memory";
    public static final String JDBC = "jdbc";
    public static final String MONGODB = "mongodb";
    public static final String REDIS = "redis";

    //role Providers
    public static final String DATABASE_ROLE_PROVIDER = "Database";
    public static final String ACTIVE_DIRECTORY_ROLE_PROVIDER = "Active Directory";
    public static final String LDAP_ROLE_PROVIDER = "LDAP";
}
