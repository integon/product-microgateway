/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.choreo.connect.tests.testcases.standalone.soap;

import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.choreo.connect.tests.common.model.API;
import org.wso2.choreo.connect.tests.common.model.ApplicationDTO;
import org.wso2.choreo.connect.tests.context.CCTestException;
import org.wso2.choreo.connect.tests.util.ApictlUtils;
import org.wso2.choreo.connect.tests.util.HttpResponse;
import org.wso2.choreo.connect.tests.util.HttpsClientRequest;
import org.wso2.choreo.connect.tests.util.TestConstant;
import org.wso2.choreo.connect.tests.util.TokenUtil;
import org.wso2.choreo.connect.tests.util.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SOAPApiBasicTestCase {

    private final String context = "/soap/1.0.0";
    private final String version = "1.0.0";

    private final String payload = "<soap:Envelope\n" +
            "\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "\txmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "\txmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "\t<soap:Body>\n" +
            "\t\t<CheckPhoneNumber\n" +
            "\t\t\txmlns=\"http://ws.cdyne.com/PhoneVerify/query\">\n" +
            "\t\t\t<PhoneNumber>18006785432</PhoneNumber>\n" +
            "\t\t\t<LicenseKey>18006785432</LicenseKey>\n" +
            "\t\t</CheckPhoneNumber>\n" +
            "\t</soap:Body>\n" +
            "</soap:Envelope>";

    private String testKey;

    @BeforeClass
    public void deployApi() throws Exception {
        ApictlUtils.login("test");
        ApictlUtils.deploySampleProject("SampleSOAPApi", "test");
        Utils.delay(TestConstant.DEPLOYMENT_WAIT_TIME, "Couldn't wait till deployment completion.");

        API api = new API();
        api.setName("DefaultVersion");
        api.setContext(context);
        api.setVersion(version);
        api.setProvider("admin");

        ApplicationDTO app = new ApplicationDTO();
        app.setName("DefaultAPIApp");
        app.setTier("Unlimited");
        app.setId((int) (Math.random() * 1000));

        testKey = TokenUtil.getJWT(api, app, "Unlimited", TestConstant.KEY_TYPE_PRODUCTION,
                3600, null, true);
    }

    @Test(description = "Invoke SOAP API with test key")
    public void invokeAPI() throws CCTestException, IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Internal-Key", testKey);
        HttpResponse response = HttpsClientRequest.doPost("https://localhost:9095" + context + "/phoneverify",
                payload, headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_OK, "Response code mismatched");
        Assert.assertTrue(response.getData().contains("<Valid>true</Valid>"), "Response body mismatched");
    }
}
