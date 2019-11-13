package org.jboss.as.test.integration.ejb.stateful.exception;

import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.ejb.Remote;

@Stateless
@Remote(ClientInterface.class)
public class Client implements ClientInterface {

    private static final Logger logger = Logger.getLogger(Client.class.getName());

    public void callBean() {

        try {
            Context ctx = getIntialContext();
            final String lookupString = "ejb:DeploymentEjb/ejbJar/SimpleRemoteBean!org.jboss.as.test.integration.ejb.stateful.exception.SimpleRemote";
            SimpleRemote bean = (SimpleRemote) ctx.lookup(lookupString);
            // call the remote method
            bean.throwBadException();
        } catch (Exception e) {
            logger.info("Caught an exception:\n" + e.toString());
        }
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
