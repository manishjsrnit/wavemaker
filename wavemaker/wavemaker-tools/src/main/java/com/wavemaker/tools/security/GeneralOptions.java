/*
 *  Copyright (C) 2007-2013 VMware, Inc. All rights reserved.
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

package com.wavemaker.tools.security;

/**
 * @author Frankie Fu
 */
public class GeneralOptions {

    public static final String DEMO_TYPE = "Demo";

    public static final String DATABASE_TYPE = "Database";

    public static final String LDAP_TYPE = "LDAP";
    
    public static final String AD_TYPE = "Active Directory";

    public static final String CAS_TYPE = "CAS";

    private boolean enforceSecurity;

    private boolean enforceIndexHtml;

    private boolean useSSL;

    private String sslPort;

    private String dataSourceType;

    public boolean isEnforceSecurity() {
        return this.enforceSecurity;
    }

    public void setEnforceSecurity(boolean enforceSecurity) {
        this.enforceSecurity = enforceSecurity;
    }

    public boolean isEnforceIndexHtml() {
        return this.enforceIndexHtml;
    }

    public boolean getUseSSL() {
        return this.useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public boolean isSSLUsed() {
        return this.useSSL;
    }

    public void setSslPort(String sslPort) {
        this.sslPort = sslPort;
    }

    public String getSslPort() {
        return this.sslPort;
    }

    public void setEnforceIndexHtml(boolean enforceIndexHtml) {
        this.enforceIndexHtml = enforceIndexHtml;
    }

    public String getDataSourceType() {
        return this.dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

}
