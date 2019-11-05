/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
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
package org.jboss.as.test.integration.weld.alternative;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
//import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;

/**
 *
 * A test of the CDI alternatives. This tests that the alternative
 * information in the beans.xml file is being parsed correctly.
 *
 * @author Stuart Douglas
 * @author Peter Mackay
 */
@RunWith(Arquillian.class)
@ServerSetup(WeldAlternativeTestCase.WeldAlternativeTestCaseServerSetup.class)
public class WeldAlternativeTestCase {

    public static final String SUBSTITUTION_DEPLOYMENT = "substitutionDeployment";

    @Deployment
    public static Archive<?> deploy() {
        return getDeployment(false);
    }

    @Deployment(name = SUBSTITUTION_DEPLOYMENT)
    public static Archive<?> deployWithSubstitution() {
        return getDeployment(true);
    }

    public static Archive<?> getDeployment(boolean substitution) {
        String alternativeClass = substitution ? "${alt.bean:" + AlternativeBean.class.getName() + "}" : AlternativeBean.class.getName();
        String beansXmlString = "<beans><alternatives><class>" + alternativeClass + "</class></alternatives></beans>";
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addPackage(WeldAlternativeTestCase.class.getPackage())
                .addAsManifestResource(new StringAsset(beansXmlString), "beans.xml");
        return jar;
    }

    @Inject
    private SimpleBean bean;

    @Test
    public void testAlternatives() {
        testAlternative();
    }

    @Test
    @OperateOnDeployment(SUBSTITUTION_DEPLOYMENT)
    public void testAlternativesWithSubstitution() {
        testAlternative();
    }

    private void testAlternative() {
        Assert.assertEquals("Hello World", bean.sayHello());
    }

    public static class WeldAlternativeTestCaseServerSetup implements ServerSetupTask {

        @Override
        public void setup(final ManagementClient managementClient, final String containerId) throws Exception {
            final ModelNode op = new ModelNode();
            op.get(OP_ADDR).set(SUBSYSTEM, "ee");
            op.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
            op.get(NAME).set("spec-descriptor-property-replacement");
            op.get(VALUE).set(true);
            managementClient.getControllerClient().execute(op);
        }

        @Override
        public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
            final ModelNode op = new ModelNode();
            op.get(OP_ADDR).set(SUBSYSTEM, "ee");
            op.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
            op.get(NAME).set("spec-descriptor-property-replacement");
            op.get(VALUE).set(false);
            managementClient.getControllerClient().execute(op);
        }
    }
}
