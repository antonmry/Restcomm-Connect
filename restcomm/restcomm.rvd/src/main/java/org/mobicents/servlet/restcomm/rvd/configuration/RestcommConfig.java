package org.mobicents.servlet.restcomm.rvd.configuration;

import org.mobicents.servlet.restcomm.rvd.commons.http.SslMode;
import org.mobicents.servlet.restcomm.rvd.exceptions.RestcommConfigNotFound;
import org.mobicents.servlet.restcomm.rvd.exceptions.RestcommConfigProcessError;
import org.mobicents.servlet.restcomm.rvd.exceptions.RestcommConfigurationException;
import org.mobicents.servlet.restcomm.rvd.utils.RvdUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;

/**
 * Loads and holds a set of restcomm.xml options that are of interest to RVD. In principle these options
 * are used in case they are not available in RVD's own configuration - rvd.xml.
 *
 * Note the importance of *authServerUrl* property. If non-empty, a keycloak auth server is used for authentication.
 * Otherwise Restcomm is used.
 *
 * @author Orestis Tsakiridis
 */
public class RestcommConfig {
    SslMode sslMode;
    String hostname;
    boolean useHostnameToResolveRelativeUrl;
    // identity-related configuration properties
    String authServerUrl; // keycloak url e.g. http://my.keycloak:8080/auth. THIS IS A FLAG!
    String realmPublicKey;
    String realm; // keycloak realm to use for auth.

    /**
     * Tries to load and process restcomm configuration (restcomm.xml) from path restcommXmlPath
     *
     * @param restcommXmlPath
     * @throws RestcommConfigurationException
     */
    public RestcommConfig(String restcommXmlPath) throws RestcommConfigurationException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        File file = new File(restcommXmlPath);
        if (!(file.exists() && file.canRead()))
            throw new RestcommConfigNotFound("Cannot find or read restcomm configuration file: " + restcommXmlPath);
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(file);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            // now read configuration elements one by one
            // ssl-mode
            XPathExpression expr = xpath.compile("/restcomm/http-client/ssl-mode/text()");
            String sslMode = (String) expr.evaluate(doc, XPathConstants.STRING);
            // use-hostname-to-resolve-relative-url
            expr = xpath.compile("/restcomm/http-client/use-hostname-to-resolve-relative-url/text()");
            String useHostname = (String) expr.evaluate(doc, XPathConstants.STRING);
            // hostname
            expr = xpath.compile("/restcomm/http-client/hostname/text()");
            String hostname = (String) expr.evaluate(doc, XPathConstants.STRING);
            // keycloak auth-server-url
            expr = xpath.compile("/restcomm/identity/auth-server-url/text()");
            this.authServerUrl = (String) expr.evaluate(doc, XPathConstants.STRING);
            // keycloak realm public key
            expr = xpath.compile("/restcomm/identity/realm-public-key/text()");
            this.realmPublicKey = (String) expr.evaluate(doc, XPathConstants.STRING);
            // keycloak realm
            expr = xpath.compile("/restcomm/identity/realm/text()");
            this.realm = (String) expr.evaluate(doc, XPathConstants.STRING);

            // process raw values if needed

            //  sslMode option
            this.sslMode = SslMode.strict;
            if ( ! RvdUtils.isEmpty(sslMode) )
                this.sslMode = SslMode.valueOf(sslMode);
            // hostname option
            this.hostname = hostname;
            // useHostnameToResolveRelativeUrl options
            try {
                this.useHostnameToResolveRelativeUrl = Boolean.parseBoolean(useHostname);
            } catch (Exception e) {
                this.useHostnameToResolveRelativeUrl = true; // default
            }
        } catch (Exception e) {
            throw new RestcommConfigProcessError("Error processing restcomm configuration file '" + restcommXmlPath + "'", e);
        }
    }

    public SslMode getSslMode() {
        return sslMode;
    }

    public String getHostname() {
        return hostname;
    }

    public boolean isUseHostnameToResolveRelativeUrl() {
        return useHostnameToResolveRelativeUrl;
    }

    /*
    public String getAuthServerUrl() {
        return authServerUrl;
    }

    public String getRealmPublicKey() {
        return realmPublicKey;
    }

    public String getRealm() {
        return realm;
    }
    */
}
