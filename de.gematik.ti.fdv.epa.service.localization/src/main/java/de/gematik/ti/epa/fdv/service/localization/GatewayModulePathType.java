/*
 * Copyright (c) 2021 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.gematik.ti.epa.fdv.service.localization;

import java.util.Date;

/**
 * include::{userguide}/ESL_Structure.adoc[tag=GatewayModulePathType]
 */
public class GatewayModulePathType {
    private final String path;
    private final Date validUntil;

    /**
     * Constructor
     * @param path
     *          path to the ePA file system component
     * @param ttl
     *          time to live
     */
    public GatewayModulePathType(final String path, final long ttl) {
        this.path = path;
        validUntil = new Date(System.currentTimeMillis() + ttl);
    }

    /**
     * Getter for date and time until the gateway path is valid
     * @return date and time until the gateway path is valid
     */
    public Date getValidUntil() {
        return validUntil;
    }

    /**
     * Getter for the path to the ePA file system component
     * @return path to the ePA file system component
     */
    public String getPath() {
        return path;
    }
}
