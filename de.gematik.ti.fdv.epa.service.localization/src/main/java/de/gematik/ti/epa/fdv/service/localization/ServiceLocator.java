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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import de.gematik.ti.epa.fdv.service.localization.api.LookupStatus;
import de.gematik.ti.epa.fdv.service.localization.api.ServiceInterfaceName;
import de.gematik.ti.epa.fdv.service.localization.exceptions.ServiceLocatorException;
import de.gematik.ti.epa.fdv.service.localization.spi.IServiceLocalizer;

/**
 * include::{userguide}/ESL_Structure.adoc[tag=ServiceLocator]
 */
public final class ServiceLocator implements IServiceLocalizer {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceLocator.class);
    private static final String SCHEME = "https://";
    private static final int PORT = 443;
    private String fqdn;
    private boolean running = false;
    private LookupStatus lookupStatus = LookupStatus.NOT_STARTED;
    private final Map<String, GatewayModulePathType> dnsTxtRecordValues = new HashMap();

    /**
     * start a new DNS lookup, e.g. if previous one ended erroneously
     * @param callback optional Consumer parameter to get callback the lookup status
     */
    @Override
    public void lookup(final String fqdn, final Consumer<LookupStatus> callback) {
        Record[] records;
        if (running) {
            return;
        }
        running = true;
        dnsTxtRecordValues.clear();
        this.fqdn = fqdn;
        Lookup lookup;
        try {
            lookup = new Lookup(fqdn, Type.TXT);
            records = lookup.run();
        } catch (TextParseException e) {
            lookupStatus = LookupStatus.ERROR;
            throw new RuntimeException(e);
        }
        if (records != null) {
            fillDnsTxtRecordValues(records);
        } else {
            lookupStatus = LookupStatus.MISSING_TXT_RECORD;
        }
        running = false;
        callback.accept(lookupStatus);
    }

    /**
     * After successful lookup, it returns endpointURL for a given interface name
     *
     * @param serviceInterfaceName name of the gateway interface where to get the URL for
     * @return URL of given interface name
     */
    @Override
    public URL endpointURLForInterface(final ServiceInterfaceName serviceInterfaceName) {
        boolean isValid = handleTTL(serviceInterfaceName.getModuleName());
        if (lookupStatus.equals(LookupStatus.SUCCESS) && isValid) {

            String path = dnsTxtRecordValues.get(serviceInterfaceName.getModuleName()).getPath();
            try {
                URL url;
                if(serviceInterfaceName.getServiceLocatorName().length() > 0) {
                    url = new URL(SCHEME + fqdn + ":" + PORT + path + serviceInterfaceName.getServiceLocatorName());
                } else {
                    url = new URL(SCHEME + fqdn + ":" + PORT + path);
                }
                LOG.debug("endpointURLForInterface for Interface " + serviceInterfaceName.getServiceLocatorName() + ": " + url);
                return url;
            } catch (MalformedURLException e) {
                LOG.error("Malformed URL received from module " + path + e.getMessage());
            }
        }
        return null;
    }

    void fillDnsTxtRecordValues(Record[] records) {
        for (Record record : records) {
            String rData = record.rdataToString().replaceAll("\"", "");
            List<String> rDataTokens = Arrays.asList(rData.split("\\s+"));

            TXTRecord txtRecord = new TXTRecord(record.getName(), record.getDClass(), record.getTTL(), rDataTokens);
            List<String> recordStrings = txtRecord.getStrings();

            if (recordStrings != null && !recordStrings.isEmpty()) {
                for (String string : recordStrings) {
                    if (string.contains("=")) {
                        String[] splitToken = string.trim().split("=", 2);
                        fillModuleList(record, splitToken);
                    }
                }
            }
        }
        if (dnsTxtRecordValues.size() > 0) {
            lookupStatus = LookupStatus.SUCCESS;
        } else {
            lookupStatus = LookupStatus.MISSING_TXT_RECORD;
        }
    }

    private void fillModuleList(final Record record, final String[] splitToken) {
        if (splitToken.length == 2) {
            String txtRecordName = splitToken[0];
            if (txtRecordName.equals("txtvers")) {
                checkTxtVersion(splitToken[1]);
            } else {
                GatewayModulePathType gatewayModulePathType = new GatewayModulePathType(splitToken[1], record.getTTL());
                dnsTxtRecordValues.put(splitToken[0], gatewayModulePathType);
                LOG.debug("TxtRecordName: " + txtRecordName + " gatewayModulePath: " + gatewayModulePathType.getPath()
                        + " valid until: " + gatewayModulePathType.getValidUntil().toString());
            }
        }
    }

    private void checkTxtVersion(final String version) {
        if (!"1".equals(version)) {
            throw new ServiceLocatorException("Wrong txtVersion in DNS Response found.");
        }
    }

    /**
     * Returns the status of service lookup in DNS
     * @return status of service lookup in DNS
     */
    @Override
    public LookupStatus getLookupStatus() {
        if (!running) {
            return lookupStatus;
        } else {
            return LookupStatus.IN_PROGRESS;
        }
    }

    /**
     * Returns home community (OID, which the file system provider has requested from DIMDI)
     * @return home community ID
     */
    @Override
    public String getHomeCommunityId() {
        boolean isValid = handleTTL("hcid");
        if (isValid) {
            return dnsTxtRecordValues.get("hcid").getPath();
        } else {
            return null;
        }
    }

    private boolean handleTTL(final String txtRecordName) {
        Date validUntil = dnsTxtRecordValues.get(txtRecordName).getValidUntil();
        return validUntil.getTime() >= new Date(System.currentTimeMillis()).getTime();
    }
}
