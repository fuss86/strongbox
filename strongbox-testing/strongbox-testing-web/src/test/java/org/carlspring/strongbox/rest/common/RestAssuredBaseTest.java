package org.carlspring.strongbox.rest.common;

import org.carlspring.strongbox.artifact.generator.MavenArtifactDeployer;
import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationAndIndexing;
import org.carlspring.strongbox.users.domain.Roles;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.context.WebApplicationContext;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.junit.Assert.assertTrue;

/**
 * General settings for the testing sub-system.
 *
 * @author Alex Oreshkevich
 */
public abstract class RestAssuredBaseTest
        extends TestCaseWithArtifactGenerationAndIndexing
{

    public final static int DEFAULT_PORT = 48080;

    public final static String DEFAULT_HOST = "localhost";

    /**
     * Share logger instance across all tests.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Inject
    protected WebApplicationContext context;

    @Inject
    AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    @Inject
    protected RestAssuredArtifactClient client;

    @Inject
    protected ObjectMapper objectMapper;

    @Inject
    protected StorageManagementService storageManagementService;

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    private String host;

    private int port;

    private String contextBaseUrl;

    private TestCaseWithArtifactGeneration generator = new TestCaseWithArtifactGeneration();


    public RestAssuredBaseTest()
    {
        // initialize host
        host = System.getProperty("strongbox.host");
        if (host == null)
        {
            host = DEFAULT_HOST;
        }

        // initialize port
        String strongboxPort = System.getProperty("strongbox.port");
        if (strongboxPort == null)
        {
            port = DEFAULT_PORT;
        }
        else
        {
            port = Integer.parseInt(strongboxPort);
        }

        // initialize base URL
        contextBaseUrl = "http://" + host + ":" + port;
    }

    @Before
    public void init()
            throws Exception
    {
        logger.debug("Initializing RestAssured...");

        RestAssuredMockMvc.webAppContextSetup(context);

        // security settings for tests
        // by default all operations incl. deletion etc. are allowed (careful)
        // override #provideAuthorities if you wanna be more specific
        anonymousAuthenticationFilter.getAuthorities()
                                     .addAll(provideAuthorities());

        setContextBaseUrl(contextBaseUrl);
    }

    @After
    public void shutdown()
    {
        RestAssuredMockMvc.reset();
    }

    public String getContextBaseUrl()
    {
        return contextBaseUrl;
    }

    public void setContextBaseUrl(String contextBaseUrl)
    {
        this.contextBaseUrl = contextBaseUrl;

        // base URL depends only on test execution context
        client.setContextBaseUrl(contextBaseUrl);
    }

    protected Collection<? extends GrantedAuthority> provideAuthorities()
    {
        return Roles.ADMIN.getPrivileges();
    }

    public static void removeDir(String path)
    {
        removeDir(new File(path));
    }

    /**
     * Recursively removes directory or file #file and all it's content.
     *
     * @param file directory or file to be removed
     */
    public static void removeDir(File file)
    {
        if (file == null || !file.exists())
        {
            return;
        }

        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files != null)
            {
                for (File f : files)
                {
                    removeDir(f);
                }
            }
        }

        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    protected boolean pathExists(String url)
    {
        logger.trace("[pathExists] URL -> " + url);
        return given().header("user-agent", "Maven/*")
                      .contentType(MediaType.TEXT_PLAIN_VALUE)
                      .when()
                      .get(url)
                      .getStatusCode() == OK;
    }

    protected void assertPathExists(String url)
    {
        assertTrue("Path " + url + " doesn't exist.", pathExists(url));
    }

    protected MavenArtifactDeployer buildArtifactDeployer(File file)
    {
        MavenArtifactDeployer artifactDeployer = new MavenArtifactDeployer(file.getAbsolutePath());
        artifactDeployer.setClient(client);
        return artifactDeployer;
    }

    public String createSnapshotVersion(String baseSnapshotVersion,
                                        int buildNumber)
    {
        return generator.createSnapshotVersion(baseSnapshotVersion, buildNumber);
    }

    public Artifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                      String groupId,
                                                      String artifactId,
                                                      String baseSnapshotVersion,
                                                      String packaging,
                                                      String[] classifiers,
                                                      int numberOfBuilds)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return generator.createTimestampedSnapshotArtifact(repositoryBasedir,
                                                           groupId,
                                                           artifactId,
                                                           baseSnapshotVersion,
                                                           packaging,
                                                           classifiers,
                                                           numberOfBuilds);
    }

    protected void createStorage(String storageId)
            throws IOException, JAXBException
    {
        createStorage(new Storage(storageId));
    }

    protected void createStorage(Storage storage)
            throws IOException, JAXBException
    {
        configurationManagementService.saveStorage(storage);
        storageManagementService.createStorage(storage);
    }
}