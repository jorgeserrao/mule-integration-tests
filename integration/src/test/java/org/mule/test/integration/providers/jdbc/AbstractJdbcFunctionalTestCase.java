/*
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) Cubis Limited. All rights reserved.
 * http://www.cubis.co.uk
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.test.integration.providers.jdbc;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.mule.MuleManager;
import org.mule.config.PoolingProfile;
import org.mule.impl.MuleModel;
import org.mule.impl.endpoint.MuleEndpointURI;
import org.mule.providers.jdbc.JdbcConnector;
import org.mule.providers.jdbc.JdbcUtils;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.tck.functional.FunctionalTestComponent;
import org.mule.umo.UMOEventContext;
import org.mule.umo.UMOManager;
import org.mule.umo.endpoint.MalformedEndpointException;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.UMOConnector;

/**
 * @author Guillaume Nodet
 * @version $Revision$
 */
public abstract class AbstractJdbcFunctionalTestCase extends AbstractMuleTestCase {

	public static final String DEFAULT_IN_URI = "jdbc://getTest?type=1";
	public static final String DEFAULT_OUT_URI = "jdbc://writeTest?type=2";
    public static final String CONNECTOR_NAME = "testConnector";
    public static final String DEFAULT_MESSAGE = "Test Message";
    
    public static final String SQL_READ = "SELECT ID, TYPE, DATA, ACK, RESULT FROM TEST WHERE TYPE = ${type} AND ACK IS NULL";
    public static final String SQL_ACK = "UPDATE TEST SET ACK = ${NOW} WHERE ID = ${id} AND TYPE = ${type} AND DATA = ${data}";
    public static final String SQL_WRITE = "INSERT INTO TEST(ID, TYPE, DATA, ACK, RESULT) VALUES(NULL, ${type}, ${payload}, NULL, NULL)";
	
    protected UMOConnector connector;
    protected static UMOManager manager;
    protected DataSource dataSource;

    protected void setUp() throws Exception {
    	// Create a new mule manager
        manager = MuleManager.getInstance();
        //Make sure we are running synchronously
        MuleManager.getConfiguration().setSynchronous(true);
        MuleManager.getConfiguration().getPoolingProfile().setInitialisationPolicy(PoolingProfile.POOL_INITIALISE_ONE_COMPONENT);
        // Create an empty model
        manager.setModel(new MuleModel());
        // Create and register connector
        connector = createConnector();
        MuleManager.getInstance().registerConnector(connector);
        // Empty table
        emptyTable();
    }
	
    protected void tearDown() throws Exception {
        MuleManager.getInstance().dispose();
    }
    
    protected void emptyTable() throws Exception {
    	try {
    		execSqlUpdate("DELETE FROM TEST");
    	} catch (Exception e) {
    		execSqlUpdate("CREATE TABLE TEST(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0)  NOT NULL PRIMARY KEY,TYPE INTEGER,DATA VARCHAR(255),ACK TIMESTAMP,RESULT VARCHAR(255))");
    	}
    }
    
    protected int execSqlUpdate(String sql) throws Exception {
    	Connection con = null;
    	try {
    		con = getConnection();
    		return new QueryRunner().update(con, sql);
    	} finally {
    		JdbcUtils.close(con);
    	}
    }
    
    protected Object[] execSqlQuery(String sql) throws Exception {
    	Connection con = null;
    	try {
    		con = getConnection();
    		return (Object[]) new QueryRunner().query(con, sql, new ArrayHandler());
    	} finally {
    		JdbcUtils.close(con);
    	}
    }

    public static class JdbcFunctionalTestComponent extends FunctionalTestComponent {
        public Object onCall(UMOEventContext context) throws Exception
        {
            if (getEventCallback() != null) {
            	getEventCallback().eventReceived(context, this);
            }
        	Map map = (Map) context.getMessage().getPayload();
        	return map.get("data") + " Received";
        }
    }

    public Connection getConnection() throws Exception {
    	Object dataSource = getDataSource();
    	if (dataSource instanceof DataSource) {
    		return ((DataSource) dataSource).getConnection();
    	} else {
    		return ((XADataSource) dataSource).getXAConnection().getConnection();
    	}
    }
	
	public DataSource getDataSource() throws Exception {
		if (dataSource == null) {
			dataSource = createDataSource();
		}
		return dataSource;
	}
	
	protected abstract DataSource createDataSource() throws Exception;
	
    public UMOConnector createConnector() throws Exception {
        JdbcConnector connector = new JdbcConnector();
        connector.setDataSource(getDataSource());
        connector.setName(CONNECTOR_NAME);
        connector.getDispatcherThreadingProfile().setDoThreading(false);
        connector.setPollingFrequency(5000);
        
        Map queries = new HashMap();
        queries.put("getTest", SQL_READ);
        queries.put("getTest.ack", SQL_ACK);
        queries.put("writeTest", SQL_WRITE);
        connector.setQueries(queries);

        return connector;
    }

    protected UMOEndpointURI getInDest() {
        try {
            return new MuleEndpointURI(DEFAULT_IN_URI);
        } catch (MalformedEndpointException e) {
            fail(e.getMessage());
            return null;
        }
    }

    protected UMOEndpointURI getOutDest() {
        try {
            return new MuleEndpointURI(DEFAULT_OUT_URI);
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

}
