package org.jboss.as.test.integration.ejb.stateful.exception;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateful;

@Stateful
@Remote
@LocalBean
public class Client implements ClientInterface {

    private static final Logger logger = Logger.getLogger(Client.class.getName());

    public void callSFSBBean() {
        logger.info("Client invoked");

        SFSB1Interface sfsb1 = null;
        try {
            sfsb1 = getBean();
            sfsb1.userException();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        sfsb1.remove();
        logger.info("Client finished.");
    }

    private static SFSB1Interface getBean() throws NamingException {
        Context ctx = null;
        SFSB1Interface bean = null;
        try {
            ctx = getIntialContext();
            bean = (SFSB1Interface) ctx.lookup("ejb:/DeploymentEjb/ejbJar/SFSB1!org.jboss.as.test.integration.ejb.stateful.exception.SFSB1Interface");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

    public static Context getIntialContext() throws NamingException {

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");

        props.put(Context.PROVIDER_URL, String.format("%s://%s:%s/wildfly-services", "http",
                System.getProperty("host", "localhost"), System.getProperty("port", "8080")));

        props.put(Context.SECURITY_PRINCIPAL, System.getProperty("username", "user1"));
        props.put(Context.SECURITY_CREDENTIALS, System.getProperty("password", "password1"));

        return new InitialContext(props);
    }
}
