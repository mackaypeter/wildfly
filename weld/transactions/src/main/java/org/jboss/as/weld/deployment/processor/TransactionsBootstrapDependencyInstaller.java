/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.weld.deployment.processor;

import javax.transaction.UserTransaction;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.txn.service.UserTransactionService;
import org.jboss.as.weld.ServiceNames;
import org.jboss.as.weld.services.bootstrap.WeldTransactionServices;
import org.jboss.as.weld.spi.BootstrapDependencyInstaller;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 *
 * @author Martin Kouba
 */
public class TransactionsBootstrapDependencyInstaller implements BootstrapDependencyInstaller {

    @Override
    public ServiceName install(ServiceTarget serviceTarget, DeploymentUnit deploymentUnit, boolean jtsEnabled) {
        final WeldTransactionServices weldTransactionServices = new WeldTransactionServices(jtsEnabled);

        final ServiceName weldTransactionServiceName = deploymentUnit.getServiceName().append(WeldTransactionServices.SERVICE_NAME);

        serviceTarget.addService(weldTransactionServiceName, weldTransactionServices)
                // Ensure the local transaction provider is started before we start
                .addDependency(ServiceNames.capabilityServiceName(deploymentUnit, "org.wildfly.transactions.global-default-local-provider"))
                .addDependency(UserTransactionService.SERVICE_NAME, UserTransaction.class, weldTransactionServices.getInjectedTransaction()).install();

        return weldTransactionServiceName;
    }

}
