package org.jboss.as.test.integration.ejb.stateful.exception;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.Deployer;

@RunWith(Arquillian.class)
public class ExceptionClientHangsTestCase {

    public static final String DEPLOYMENT_NAME_EJB = "DeploymentEjb";
    private static final String DEPLOYMENT_NAME_CLIENT = "DeploymentClient";

    @Deployment(name = DEPLOYMENT_NAME_EJB, managed = false)
    public static Archive<?> deploy_ejb() {

        EnterpriseArchive ear_ejb = ShrinkWrap.create(EnterpriseArchive.class, DEPLOYMENT_NAME_EJB + ".ear");

        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "ejbJar.jar");
        ejbJar.addClasses(TestException.class, SFSB1Interface.class, SFSB1.class, DestroyMarkerBeanInterface.class, DestroyMarkerBean.class);

        ear_ejb.addAsModule(ejbJar);

        ear_ejb.as(org.jboss.shrinkwrap.api.exporter.ZipExporter.class).exportTo(new java.io.File("/home/dcihak/Work/" + ear_ejb.getName()), true);

        return ear_ejb;
    }

    @Deployment(name = DEPLOYMENT_NAME_CLIENT, managed = true)
    public static Archive<?> deploy_client() {

        EnterpriseArchive ear_client = ShrinkWrap.create(EnterpriseArchive.class, DEPLOYMENT_NAME_CLIENT + ".ear");

        final JavaArchive clientJar = ShrinkWrap.create(JavaArchive.class, "clientJar.jar");
        clientJar.addClasses(ExceptionClientHangsTestCase.class, Client.class, ClientInterface.class, SFSB1Interface.class);

        ear_client.addAsModule(clientJar);

        ear_client.as(org.jboss.shrinkwrap.api.exporter.ZipExporter.class).exportTo(new java.io.File("/home/dcihak/Work/" + ear_client.getName()), true);

        return ear_client;
    }

    @ArquillianResource
    private InitialContext iniCtx;

    @ArquillianResource
    private Deployer deployer;

    protected ClientInterface getBean() throws NamingException {
        return ClientInterface.class.cast(iniCtx.lookup("java:global/DeploymentClient/clientJar/Client!org.jboss.as.test.integration.ejb.stateful.exception.ClientInterface"));
    }

    @Before
    public void before() throws NamingException {
    }

    @Test
    public void testReturnExceptionFromServer() throws Exception {
        deployer.deploy(DEPLOYMENT_NAME_EJB);

        ClientInterface client = getBean();
        client.callSFSBBean();

        deployer.undeploy(DEPLOYMENT_NAME_EJB);
    }
}
