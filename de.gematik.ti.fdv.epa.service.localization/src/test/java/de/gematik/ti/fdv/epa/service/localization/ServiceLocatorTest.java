/*
 * Copyright (c) 2019 gematik - Gesellschaft für Telematikanwendungen der Gesundheitskarte mbH
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
package de.gematik.ti.fdv.epa.service.localization;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import de.gematik.ti.fdv.epa.service.localization.api.LookupStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import de.gematik.ti.fdv.epa.service.localization.api.ServiceInterfaceName;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;

/**
 * Test {@link ServiceLocator}
 */
public class ServiceLocatorTest {
    private static final String FQDN = "My.test.fqdn";
    private static final String FQDN_ABSOLUTE = "My.test.fqdn.";
    private static final String RECORD_TXT = "\"txtvers=1\" \"hcid=1.2.276.0.76.3.1.91\" \"authn=/authn\" \"authz=/authz\" \"avzd=/avzd\" \"docv=/docv\" \"ocspf=/ocspf\" \"avzd=/avzd\" \"sgd1=/sgd1\" \"sgd2=/sgd2\"";
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
        serviceLocator.fillDnsTxtRecordValues(new Record[]{answer});
    }

    @Test
    public void testGetServiceLocations() {
        Assert.assertEquals(LookupStatus.SUCCESS, serviceLocator.getLookupStatus());

        String hicd = serviceLocator.getHomeCommunityID();
        Assert.assertEquals("1.2.276.0.76.3.1.91", hicd);

        URL authnInsurantUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.IAuthenticationInsurant);
        Assert.assertEquals("https://My.test.fqdn:443/authn/I_Authentication_Insurant", authnInsurantUrl.toString());

        URL authzInsurantUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.IAuthorizationInsurant);
        Assert.assertEquals("https://My.test.fqdn:443/authz/I_Authorization_Insurant", authzInsurantUrl.toString());

        URL authzManagementInsurantUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.IAuthorizationManagementInsurant);
        Assert.assertEquals("https://My.test.fqdn:443/authz/I_Authorization_Management_Insurant", authzManagementInsurantUrl.toString());

        URL accountManagementInsurantUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.IAccountManagementInsurant);
        Assert.assertEquals("https://My.test.fqdn:443/docv/I_Account_Management_Insurant", accountManagementInsurantUrl.toString());

        URL documentManagementConnectUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.IDocumentManagementConnect);
        Assert.assertEquals("https://My.test.fqdn:443/docv/I_Document_Management_Connect", documentManagementConnectUrl.toString());

        URL documentManagementInsurantUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.IDocumentManagementInsurant);
        Assert.assertEquals("https://My.test.fqdn:443/docv/I_Document_Management_Insurant", documentManagementInsurantUrl.toString());

        URL oscpStatusInformationUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.IOCSPStatusInformation);
        Assert.assertEquals("https://My.test.fqdn:443/ocspf/I_OCSP_Status_Information", oscpStatusInformationUrl.toString());

        URL proxyDirectoryQueryUrl = serviceLocator.endpointURLForInterface(ServiceInterfaceName.IProxyDirectoryQuery);
        Assert.assertEquals("https://My.test.fqdn:443/avzd/I_Proxy_Directory_Query", proxyDirectoryQueryUrl.toString());

        URL sgd1Url = serviceLocator.endpointURLForInterface(ServiceInterfaceName.IGetKeySgd1);
        Assert.assertEquals("https://My.test.fqdn:443/sgd1/", sgd1Url.toString());

        URL sgd2Url = serviceLocator.endpointURLForInterface(ServiceInterfaceName.IGetKeySgd2);
        Assert.assertEquals("https://My.test.fqdn:443/sgd2/", sgd2Url.toString());
    }
}
