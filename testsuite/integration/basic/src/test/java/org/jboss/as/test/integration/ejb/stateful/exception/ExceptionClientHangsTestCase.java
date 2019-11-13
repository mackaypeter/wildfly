package org.jboss.as.test.integration.ejb.stateful.exception;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

@RunWith(Arquillian.class)
@RunAsClient
public class ExceptionClientHangsTestCase {

    public static final String DEPLOYMENT_NAME_EJB = "DeploymentEjb";
    public static final String DEPLOYMENT_NAME_CLIENT = "DeploymentClient";

    @Deployment(name = DEPLOYMENT_NAME_EJB)
    public static Archive<?> deploy_ejb() {
        EnterpriseArchive ear_ejb = ShrinkWrap.create(EnterpriseArchive.class, DEPLOYMENT_NAME_EJB + ".ear");
        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "ejbJar.jar");
        ejbJar.addClasses(TestException.class, SimpleRemote.class, SimpleRemoteBean.class);
        ear_ejb.addAsModule(ejbJar);
        ear_ejb.as(org.jboss.shrinkwrap.api.exporter.ZipExporter.class).exportTo(new java.io.File("/tmp/" + ear_ejb.getName()), true);
        return ear_ejb;
    }

    @Deployment(name = DEPLOYMENT_NAME_CLIENT)
    public static Archive<?> deploy_client() {
        JavaArchive clientJar = ShrinkWrap.create(JavaArchive.class, DEPLOYMENT_NAME_CLIENT + ".jar");
        clientJar.addClasses(Client.class, ClientInterface.class, SimpleRemote.class);
        clientJar.as(org.jboss.shrinkwrap.api.exporter.ZipExporter.class).exportTo(new java.io.File("/tmp/" + clientJar.getName()), true);
        return clientJar;
    }

    protected static ClientInterface getBean() throws NamingException {
        Context iniCtx = getIntialContext();
        String lookup = "ejb:/DeploymentClient/Client!org.jboss.as.test.integration.ejb.stateful.exception.ClientInterface";
        return (ClientInterface) iniCtx.lookup(lookup);
    }

    @Test
    public void testReturnExceptionFromServer() throws Exception {
        ClientInterface client = getBean();
        client.callBean();
    }

    public static Context getIntialContext() throws NamingException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        props.put(Context.PROVIDER_URL, "http://localhost:8080/wildfly-services");
        props.put(Context.SECURITY_PRINCIPAL, System.getProperty("username", "user1"));
        props.put(Context.SECURITY_CREDENTIALS, System.getProperty("password", "password1"));
        return new InitialContext(props);
    }
}
