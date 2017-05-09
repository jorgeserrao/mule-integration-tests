/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.module.http.functional.listener;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.service.http.api.HttpConstants.HttpStatus.OK;
import static org.mule.test.allure.AllureConstants.HttpFeature.HTTP_EXTENSION;
import org.mule.runtime.core.api.MuleEventContext;
import org.mule.runtime.core.api.lifecycle.Callable;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.module.http.functional.AbstractHttpTestCase;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Rule;
import org.junit.Test;
import ru.yandex.qatools.allure.annotations.Features;

@Features(HTTP_EXTENSION)
public class FeistLocoTestCase extends AbstractHttpTestCase implements Callable {

  @Rule
  public DynamicPort listenPort = new DynamicPort("port");

  @Rule
  public DynamicPort requesterPort = new DynamicPort("requesterPort");

  @Override
  protected String getConfigFile() {
    return "http-feist-loco-config.xml";
  }

  @Test
  public void feistLoco() throws Exception {
    String url = String.format("http://localhost:%s/%s", listenPort.getNumber(), "feistLoco");
    HttpResponse response = Request.Get(url).execute().returnResponse();

    assertThat(response.getStatusLine().getStatusCode(), is(OK.getStatusCode()));

  }

  @Override
  public Object onCall(MuleEventContext eventContext) throws Exception {
    return RandomStringUtils.randomAlphabetic(1024*1024);
  }
}
