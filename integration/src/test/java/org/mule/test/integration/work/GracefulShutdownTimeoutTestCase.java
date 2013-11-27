/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.integration.work;

import org.mule.api.MuleEventContext;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.service.Service;
import org.mule.construct.Flow;
import org.mule.tck.AbstractServiceAndFlowTestCase;
import org.mule.tck.functional.EventCallback;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.util.concurrent.Latch;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertTrue;

public class GracefulShutdownTimeoutTestCase extends FunctionalTestCase
{

    @Override
    protected String getConfigFile()
    {
        return "org/mule/test/integration/work/graceful-shutdown-timeout-flow.xml";
    }

    @Override
    protected boolean isGracefulShutdown()
    {
        return true;
    }

    /**
     * Dispatch an event to a service component that takes longer than default
     * graceful shutdown time to complete and customize the graceful shutdown timeout
     * in configuration so that component execution is not interrupted. This tests
     * services but the same applies to the graceful shutdown of
     * receivers/dispatchers etc.
     *
     * @throws Exception
     */
    @Test
    public void testGracefulShutdownTimeout() throws Exception
    {
        final Latch latch = new Latch();

        FlowConstruct service = muleContext.getRegistry().lookupFlowConstruct("TestService");
        FunctionalTestComponent testComponent = (FunctionalTestComponent) getComponent(service);
        testComponent.setEventCallback(new EventCallback()
        {
            @Override
            public void eventReceived(MuleEventContext context, Object component) throws Exception
            {
                Thread.sleep(5500);
                latch.countDown();

            }
        });

        ((Flow) service).process(getTestEvent("test"));
        Thread.sleep(200);
        ((Flow) service).dispose();
        assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
    }
}
