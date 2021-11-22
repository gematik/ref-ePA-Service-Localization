/*
 * Copyright (c) 2021 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.gematik.ti.epa.fdv.service.localization;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Test {@link GatewayModulePathType}
 */
public class GatewayModulePathTypeTest {

    @Test
    public void testPathAndValidUntil() {
        long currentPlusTTL = System.currentTimeMillis() + 1000;
        GatewayModulePathType gatewayModulePathType = new GatewayModulePathType("/avzd/", 1000);
        Assert.assertEquals("/avzd/", gatewayModulePathType.getPath());
        Assert.assertTrue( new Date (currentPlusTTL).getTime() <= gatewayModulePathType.getValidUntil().getTime());
        currentPlusTTL = System.currentTimeMillis();
        gatewayModulePathType = new GatewayModulePathType("/avzd/", 0);
        Assert.assertTrue( new Date (currentPlusTTL).getTime() <= gatewayModulePathType.getValidUntil().getTime());
        gatewayModulePathType = new GatewayModulePathType("/avzd/", -1000);
        Assert.assertTrue( new Date (currentPlusTTL).getTime() >= gatewayModulePathType.getValidUntil().getTime());
    }
}
