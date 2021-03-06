/*******************************************************************************
 * Copyright (c) 2014 Salesforce.com, inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Salesforce.com, inc. - initial API and implementation
 ******************************************************************************/

package com.salesforce.ide.core.remote;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.salesforce.ide.core.factories.ConnectionFactory;
import com.salesforce.ide.core.internal.context.ContainerDelegate;
import com.salesforce.ide.core.project.ForceProject;

/**
 * This is the parent class of all transport that needs to go through HTTP via a REST interface.
 * 
 * @author nchen
 * 
 */
public class HTTPConnection {
    private final ForceProject forceProject;
    private Connection connection;
    private WebTarget endpoint;
    private String activeSessionToken;
    private final String serviceEndpoint;

    public HTTPConnection(ForceProject forceProject, String serviceEndpoint) {
        this.forceProject = forceProject;
        this.serviceEndpoint = serviceEndpoint;
    }

    public WebTarget initialize() throws InsufficientPermissionsException, ForceConnectionException {
        initializeConnection();
        initializeHTTPClient();
        return getEndpoint();
    }

    private void initializeConnection() throws InsufficientPermissionsException, ForceConnectionException {
        ConnectionFactory connectionFactory =
                ContainerDelegate.getInstance().getServiceLocator().getFactoryLocator().getConnectionFactory();
        connection = connectionFactory.getConnection(forceProject);
        setActiveSessionToken(connection.getSessionId());
    }

    private void initializeHTTPClient() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(JacksonFeature.class);
        clientConfig.property(ClientProperties.READ_TIMEOUT, getConnectionTimeoutPreference());

        Client client = ClientBuilder.newClient(clientConfig);

        String serviceEndpoint = getConnection().getServerUrlRoot();
        serviceEndpoint = serviceEndpoint + constructServiceEndpoint();
        endpoint = client.target(serviceEndpoint);
    }

    protected int getConnectionTimeoutPreference() {
          return 20_000;
    }

    private String constructServiceEndpoint() {
        return serviceEndpoint + getApiVersion();
    }

    private String getApiVersion() {
        return "v" + forceProject.getEndpointApiVersion();
    }

    public WebTarget getEndpoint() {
        return endpoint;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getActiveSessionToken() {
        return activeSessionToken;
    }

    public void setActiveSessionToken(String activeSessionToken) {
        this.activeSessionToken = activeSessionToken;
    }
}
