/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.integration.routing;

import org.mule.api.context.notification.RoutingNotificationListener;
import org.mule.context.notification.RoutingNotification;
import org.mule.module.client.MuleClient;
import org.mule.tck.AbstractServiceAndFlowTestCase;
import org.mule.tck.AbstractServiceAndFlowTestCase.ConfigVariant;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

public class InboundAggregationWithTimeoutTestCase extends AbstractServiceAndFlowTestCase
{
    public InboundAggregationWithTimeoutTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{
            {ConfigVariant.SERVICE, "org/mule/test/integration/routing/multi-inbound-aggregator-with-timeout-service.xml"},
            {ConfigVariant.FLOW, "org/mule/test/integration/routing/multi-inbound-aggregator-with-timeout-flow.xml"}
        });
    }      
    
    @Test
    public void testAggregatorTimeout() throws Exception
    {
        final CountDownLatch latch = new CountDownLatch(1);

        muleContext.registerListener(new RoutingNotificationListener<RoutingNotification>()
        {
            public void onNotification(RoutingNotification notification)
            {
                if (notification.getAction() == RoutingNotification.CORRELATION_TIMEOUT)
                {
                    latch.countDown();
                }
            }
        });

        String message = "test";
        MuleClient client = new MuleClient(muleContext);
        client.dispatch("vm://distributor.queue", message, null);

        assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
    }
}
