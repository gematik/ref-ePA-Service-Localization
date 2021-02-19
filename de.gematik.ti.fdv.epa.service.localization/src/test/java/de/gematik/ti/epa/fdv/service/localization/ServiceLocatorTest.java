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

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;

import de.gematik.ti.epa.fdv.service.localization.api.LookupStatus;
import de.gematik.ti.epa.fdv.service.localization.api.ServiceInterfaceName;

/**
 * Test {@link ServiceLocator}
 */
public class ServiceLocatorTest {
    private static final String FQDN = "My.test.fqdn";
    private static final String FQDN_ABSOLUTE = "My.test.fqdn.";
    private static final String RECORD_TXT = "\"txtvers=1\" \"hcid=1.2.276.0.76.3.1.91\" \"authn=/authn/\" \"authz=/authz/\" \"avzd=/avzd/\" \"docv=/docv/\" \"ocspf=/ocspf/\" \"sgd1=/sgd1/\" \"sgd2=/sgd2/\"";
    private static final int TYPE_TXT = 16;
    private Consumer<LookupStatus> callback;

    private ServiceLocator serviceLocator;

    @Before
    public void init() throws IOException {
        callback = lookupStatus -> {
        };
        serviceLocator = new ServiceLocator();
        serviceLocator.lookup(FQDN, callback);
        // fill dummy textRecord
        Name current = Name.fromString(FQDN_ABSOLUTE);
        Record answer = Record.fromString(current, TYPE_TXT, DClass.ANY, 0x20000, RECORD_TXT, current);
        serviceLocator.fillDnsTxtRecordValues(new Record[] { answer });
    }

    @Test
    public void testGetServiceLocations() {
        Assert.assertEquals(LookupStatus.SUCCESS, serviceLocator.getLookupStatus());

        String hicd = serviceLocator.getHomeCommunityId();
        Assert.assertEquals("1.2.276.0.76.3.1.91", hicd);

        URL authnInsurantUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.I_AUTHENTICATION_INSURANT);
        Assert.assertEquals("https://My.test.fqdn:443/authn/I_Authentication_Insurant", authnInsurantUrl.toString());

        URL authzInsurantUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.I_AUTHORIZATION_INSURANT);
        Assert.assertEquals("https://My.test.fqdn:443/authz/I_Authorization_Insurant", authzInsurantUrl.toString());

        URL authzManagementInsurantUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.I_AUTHORIZATION_MANAGEMENT_INSURANT);
        Assert.assertEquals("https://My.test.fqdn:443/authz/I_Authorization_Management_Insurant", authzManagementInsurantUrl.toString());

        URL accountManagementInsurantUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.I_ACCOUNT_MANAGEMENT_INSURANT);
        Assert.assertEquals("https://My.test.fqdn:443/docv/I_Account_Management_Insurant", accountManagementInsurantUrl.toString());

        URL documentManagementConnectUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.I_DOCUMENT_MANAGEMENT_CONNECT);
        Assert.assertEquals("https://My.test.fqdn:443/docv/I_Document_Management_Connect", documentManagementConnectUrl.toString());

        URL documentManagementInsurantUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.I_DOCUMENT_MANAGEMENT_INSURANT);
        Assert.assertEquals("https://My.test.fqdn:443/docv/I_Document_Management_Insurant", documentManagementInsurantUrl.toString());

        URL oscpStatusInformationUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.IOCSP_STATUS_INFORMATION);
        Assert.assertEquals("https://My.test.fqdn:443/ocspf/I_OCSP_Status_Information", oscpStatusInformationUrl.toString());

        URL proxyDirectoryQueryUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.I_PROXY_DIRECTORY_QUERY);
        Assert.assertEquals("https://My.test.fqdn:443/avzd/I_Proxy_Directory_Query", proxyDirectoryQueryUrl.toString());

        URL sgd1Url = serviceLocator.endpointURLForInterface(ServiceInterfaceName.I_GET_KEY_SGD_1);
        Assert.assertEquals("https://My.test.fqdn:443/sgd1/", sgd1Url.toString());

        URL sgd2Url = serviceLocator.endpointURLForInterface(ServiceInterfaceName.I_GET_KEY_SGD_2);
        Assert.assertEquals("https://My.test.fqdn:443/sgd2/", sgd2Url.toString());
    }
}
