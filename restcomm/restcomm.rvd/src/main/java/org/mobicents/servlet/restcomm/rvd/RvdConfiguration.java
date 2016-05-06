package org.mobicents.servlet.restcomm.rvd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import org.apache.log4j.Logger;
import org.mobicents.servlet.restcomm.rvd.commons.http.SslMode;
import org.mobicents.servlet.restcomm.rvd.configuration.RestcommConfig;
import org.mobicents.servlet.restcomm.rvd.exceptions.RestcommConfigurationException;
import org.mobicents.servlet.restcomm.rvd.http.utils.UriUtils;
import org.mobicents.servlet.restcomm.rvd.model.RvdConfig;
import org.mobicents.servlet.restcomm.rvd.utils.RvdUtils;

import com.thoughtworks.xstream.XStream;

public class RvdConfiguration {
    static final Logger logger = Logger.getLogger(RvdConfiguration.class.getName());
    private static RvdConfiguration instance = null;

    private static final String WORKSPACE_DIRECTORY_NAME = "workspace";
    public static final String PROTO_DIRECTORY_PREFIX = "_proto";
    public static final String REST_SERVICES_PATH = "services"; // the "services" from the /restcomm-rvd/services/apps/... path
    public static final String USERS_DIRECTORY_NAME = "@users";

    public static final String WAVS_DIRECTORY_NAME = "wavs";
    private static final String RVD_PROJECT_VERSION = "1.6"; // version for rvd project syntax
    private static final String PACKAGING_VERSION = "1.0";
    private static final String RAS_APPLICATION_VERSION = "2"; // version of the RAS application specification
    public static final String STICKY_PREFIX = "sticky_"; // a  prefix for rvd sticky variable names
    public static final String MODULE_PREFIX = "module_"; // a  prefix for rvd module-scoped variable names
    public static final String CORE_VARIABLE_PREFIX = "core_"; // a prefix for rvd variables that come from Restcomm parameters
    public static final String PACKAGING_DIRECTORY_NAME = "packaging";
    public static final String TICKET_COOKIE_NAME = "rvdticket"; // the name of the cookie that is used to store ticket ids for authentication
    private static Set<String> restcommParameterNames  = new HashSet<String>(Arrays.asList(new String[] {"CallSid","AccountSid","From","To","Body","CallStatus","ApiVersion","Direction","CallerName"})); // the names of the parameters supplied by restcomm request when starting an application
    public static final String PROJECT_LOG_FILENAME = "projectLog";
    public static final String DEFAULT_APPSTORE_DOMAIN = "apps.restcomm.com";
    public static final HashSet<String> builtinRestcommParameters = new HashSet<String>(Arrays.asList(new String[] {"CallSid","AccountSid","From","To","Body","CallStatus","ApiVersion","Direction","CallerName"}));
    public static final String RESTCOMM_HEADER_PREFIX = "SipHeader_"; // the prefix added to HTTP headers from Restcomm
    public static final String RESTCOMM_HEADER_PREFIX_DIAL = "DialSipHeader_"; // another prefix

    private String workspaceBasePath;
    private RvdConfig rvdConfig;  // the configuration settings from rvd.xml
    private RestcommConfig restcommConfig;

    private String contextRootPath;
    private URI restcommBaseUri;

    public static RvdConfiguration getInstance() {
        if ( instance == null ) {
            throw new IllegalStateException("RVD configuration has not been loaded.");
        }
        return instance;
    }

    public static RvdConfiguration createOnce(ServletContext servletContext) {
        synchronized (RvdConfiguration.class) {
            if ( instance == null ) {
                instance = new RvdConfiguration(servletContext);
            }
            return instance;
        }
    }

    public static RvdConfiguration createOnce(String contextRootPath) {
        synchronized (RvdConfiguration.class) {
            if ( instance == null ) {
                instance = new RvdConfiguration(contextRootPath);
            }
            return instance;
        }
    }

    private RvdConfiguration(ServletContext servletContext) {
        contextRootPath = servletContext.getRealPath("/");
        logger.info("contextRootPath: " + contextRootPath);
        load();
    }

    private RvdConfiguration(String contextRootPath) {
        this.contextRootPath = contextRootPath;
        logger.info("contextRootPath: " + contextRootPath);
        load();
    }

    private void load() {
        // load configuration from rvd.xml file
        rvdConfig = loadRvdXmlConfig(contextRootPath + "WEB-INF/rvd.xml");
        // workspaceBasePath option
        String workspaceBasePath = contextRootPath + WORKSPACE_DIRECTORY_NAME;
        if (rvdConfig.getWorkspaceLocation() != null  &&  !"".equals(rvdConfig.getWorkspaceLocation()) ) {
            if ( rvdConfig.getWorkspaceLocation().startsWith("/") )
                workspaceBasePath = rvdConfig.getWorkspaceLocation(); // this is an absolute path
            else
                workspaceBasePath = contextRootPath + rvdConfig.getWorkspaceLocation(); // this is a relative path hooked under RVD context
        }
        this.workspaceBasePath = workspaceBasePath;
        logger.info("Using workspace at " + workspaceBasePath);
        // load configuration from restcomm.xml file
        restcommConfig = loadRestcommXmlConfig(contextRootPath + "../restcomm.war/WEB-INF/conf/restcomm.xml");
    }

    /**
     * Loads rvd.xml into an RvdConfig. Returns null if the file is not found
     * @param pathToXml
     * @return
     */
    private RvdConfig loadRvdXmlConfig(String pathToXml) {
        try {
            FileInputStream input = new FileInputStream(pathToXml);
            XStream xstream = new XStream();
            xstream.alias("rvd", RvdConfig.class);
            rvdConfig = (RvdConfig) xstream.fromXML( input );
            return rvdConfig;
        } catch (FileNotFoundException e) {
            logger.warn("RVD configuration file not found: " + pathToXml);
            return null;
        }
    }

    /**
     * Load configuration options from restcomm.xml that are needed by RVD. Return null in case of failure.
     *
     * @param pathToXml
     * @return a valid RestcommConfig object or null
     */
    private RestcommConfig loadRestcommXmlConfig(String pathToXml) {
        try {
            RestcommConfig restcommConfig = new RestcommConfig(pathToXml);
            return restcommConfig;
        } catch (RestcommConfigurationException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public String getWorkspaceBasePath() {
        return this.workspaceBasePath;
    }

    public String getProjectBasePath(String projectName) {
        return this.workspaceBasePath + File.separator + projectName;
    }

    public static String getRvdProjectVersion() {
        return RVD_PROJECT_VERSION;
    }

    public static String getPackagingVersion() {
        return PACKAGING_VERSION;
    }

    public static String getRasApplicationVersion() {
        return RAS_APPLICATION_VERSION;
    }

    public static Set<String> getRestcommParameterNames() {
        return restcommParameterNames;
    }

    public SslMode getSslMode() {
        return restcommConfig.getSslMode();
    }

    public boolean getUseHostnameToResolveRelativeUrl() {
        return restcommConfig.isUseHostnameToResolveRelativeUrl();
    }

    public String getHostnameOverride() {
        return restcommConfig.getHostname();
    }

    // this is lazy loaded because HttpConnector enumeration (done in resolve()) fails otherwise
    public URI getRestcommBaseUri() {
        if (this.restcommBaseUri == null) {
            // check rvd.xml override first
            String rawUrl = rvdConfig.getRestcommBaseUrl();
            if ( ! RvdUtils.isEmpty(rawUrl) ) {
                try {
                    URI uri = new URI(rawUrl);
                    if ( ! RvdUtils.isEmpty(uri.getScheme()) && !RvdUtils.isEmpty(uri.getHost()) )
                        this.restcommBaseUri = uri;
                } catch (URISyntaxException e) { /* do nothing */}
            }
            // if no override value in rvd.xml use the automatic way
            if (this.restcommBaseUri == null) {
                UriUtils uriUtils = new UriUtils(this);
                try {
                    URI uri = new URI("/");
                    this.restcommBaseUri = uriUtils.resolve(uri);
                } catch (URISyntaxException e) { /* we should never reach here */
                    throw new IllegalStateException();
                }
            }
            logger.info("Using Restcomm server at " + this.restcommBaseUri.toString());
        }
        return restcommBaseUri;
    }

    /**
     * Returns a valid base url of the authorization server or null
     *
     * @return
     */
    public String getAuthServerUrl() {
        if (restcommConfig != null && ! RvdUtils.isEmpty(restcommConfig.getAuthServerUrl()) )
            return restcommConfig.getAuthServerUrl();
        return null;
    }

    public String getRealm() {
        if (restcommConfig != null)
            return restcommConfig.getRealm();
        return null;
    }

    public String getRealmPublicKey() {
        if (restcommConfig != null)
            return restcommConfig.getRealmPublicKey();
        return null;
    }

    /**
     * Returns whether keycloak has been configured or not. It's possible that keylcoak is enabled but the
     * Restcomm instance.is not registered. In that case the function will still return true.
     *
     * @return
     */
    public boolean keycloakEnabled() {
        if (getAuthServerUrl() != null)
            return true;
        return false;
    }
}
