/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.message;

import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;


/**
 * @see EE-1794
 */
public class MessagePropertyInResponseTransformersTestCase extends FunctionalTestCase
{
    @Override
    protected String getConfigResources()
    {
        return "org/mule/test/integration/messaging/message-property-in-response-transformers.xml";
    }

    public void testSend() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        MuleMessage result = client.send("http://localhost:63081/ser",
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sim=\"http://simple.component.mule.org/\"><soapenv:Header/><soapenv:Body><sim:echo><sim:echo>aaa</sim:echo></sim:echo></soapenv:Body></soapenv:Envelope>", null);
        assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root xmlns=\"http://simple.component.mule.org/\"><testval>bar</testval></root>",
            result.getPayloadAsString());
    }
}
