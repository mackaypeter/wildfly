/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.as.test.integration.ejb.stateful.exception;

import javax.ejb.EJBException;
import javax.ejb.NoSuchEJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.test.integration.ejb.stateful.exceptionclass.Dummy;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sun.tools.jar.resources.jar;
import org.jboss.arquillian.container.test.api.RunAsClient;

/**
 * Tests that post construct callbacks are not called on system exception,
 * and that the bean is destroyed
 *
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
public class ExceptionTestCase {

    protected static final String ARCHIVE_NAME = "ExceptionTestCase";

    @Deployment
    public static Archive<?> deploy() {

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, ARCHIVE_NAME + ".ear");

        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "ejbJar.jar");
        ejbJar.addPackage(ExceptionTestCase.class.getPackage());

        final JavaArchive libJar = ShrinkWrap.create(JavaArchive.class, "libJar.jar");
        libJar.addPackage(Dummy.class.getPackage());

        ear.addAsModule(ejbJar);
        ear.addAsLibrary(libJar);

        ear.as(org.jboss.shrinkwrap.api.exporter.ZipExporter.class).exportTo(new java.io.File("/home/dcihak/Work/" + ear.getName()), true);

        return ear;
    }

    @ArquillianResource
    private InitialContext iniCtx;

    private <T> T lookup(Class<T> beanType) throws NamingException {
        return beanType.cast(iniCtx.lookup("java:global/" + ARCHIVE_NAME + "/ejbJar/" + beanType.getSimpleName() + "!" + beanType.getName()));
    }

    protected SFSB1Interface getBean() throws NamingException {
        return lookup(SFSB1.class);
    }

    @Before
    public void before() throws NamingException {
    }

    /**
     * Throwing non {@link RuntimeException} which does not cause
     * SFSB being removed.
     */
    @Test
    @RunAsClient
    public void testUserExceptionDoesNothing() throws Exception {

        SFSB1Interface sfsb1 = getBean();

        try {
            sfsb1.userException();
            //Assert.fail("It was expected a user exception being thrown");
        } catch (Exception e) {
            throw new RuntimeException(e);
            //Assert.assertTrue(e.getMessage().contains(SFSB1.MESSAGE));
        }
        sfsb1.remove();
        //Assert.assertTrue("As remove was called preDestroy callback is expected", isPreDestroy.is());
    }
}
