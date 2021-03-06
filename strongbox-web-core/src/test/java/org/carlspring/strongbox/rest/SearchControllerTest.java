package org.carlspring.strongbox.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.carlspring.strongbox.artifact.generator.MavenArtifactDeployer;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Alex Oreshkevich
 * @author Martin Todorov
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class SearchControllerTest
        extends RestAssuredBaseTest
{

    private static final String STORAGE_SC_TEST = "storage-sc-test";

    private static final String REPOSITORY_RELEASES = "sc-releases-search";

    private static final File GENERATOR_BASEDIR = new File(
            ConfigurationResourceResolver.getVaultDirectory() + "/local");
    

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Override
    public void init()
            throws Exception
    {
        super.init();

        // prepare storage: create it from Java code instead of putting <storage/> in strongbox.xml
        createStorage(STORAGE_SC_TEST);

        createRepository(STORAGE_SC_TEST, REPOSITORY_RELEASES, RepositoryPolicyEnum.RELEASE.getPolicy(), true);

        MavenArtifactDeployer artifactDeployer = buildArtifactDeployer(GENERATOR_BASEDIR);
        
        Artifact a1 = generateArtifact(GENERATOR_BASEDIR, "org.carlspring.strongbox.searches:test-project:1.0.11.3");
        Artifact a2 = generateArtifact(GENERATOR_BASEDIR, "org.carlspring.strongbox.searches:test-project:1.0.11.3.1");
        Artifact a3 = generateArtifact(GENERATOR_BASEDIR, "org.carlspring.strongbox.searches:test-project:1.0.11.3.2");

        artifactDeployer.deploy(a1, STORAGE_SC_TEST, REPOSITORY_RELEASES);
        artifactDeployer.deploy(a2, STORAGE_SC_TEST, REPOSITORY_RELEASES);
        artifactDeployer.deploy(a3, STORAGE_SC_TEST, REPOSITORY_RELEASES);
        
        final RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndexer(STORAGE_SC_TEST + ":" +
                                                                                                REPOSITORY_RELEASES + ":" +
                                                                                                IndexTypeEnum.LOCAL.getType());

        assertNotNull(repositoryIndexer);

        repositoryManagementService.reIndex(STORAGE_SC_TEST, REPOSITORY_RELEASES, "org/carlspring/strongbox/searches");
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE_SC_TEST, REPOSITORY_RELEASES));

        return repositories;
    }

    @Test
    public void testSearches()
            throws Exception
    {
        String indexQuery = "g:org.carlspring.strongbox.searches a:test-project";
        String dbQuery = "groupId=org.carlspring.strongbox.searches;artifactId=test-project;";

        // testSearchPlainText
        String response = client.search(indexQuery, MediaType.TEXT_PLAIN_VALUE);

        assertTrue("Received unexpected response! \n" + response + "\n",
                   response.contains("test-project-1.0.11.3.jar") &&
                           response.contains("test-project-1.0.11.3.1.jar"));
        String dbResponse = client.search(dbQuery, MediaType.TEXT_PLAIN_VALUE);
        assertEquals("DB search response don't match!", response, dbResponse);


        // testSearchJSON
        response = client.search(indexQuery, MediaType.APPLICATION_JSON_VALUE);

        assertTrue("Received unexpected response! \n" + response + "\n",
                   response.contains("\"version\" : \"1.0.11.3\"") &&
                           response.contains("\"version\" : \"1.0.11.3.1\""));
        assertEquals("DB search response don't match!", response, client.search(dbQuery, MediaType.APPLICATION_JSON_VALUE));
        
        // testSearchXML
        response = client.search(indexQuery, MediaType.APPLICATION_XML_VALUE);

        assertTrue("Received unexpected response! \n" + response + "\n",
                   response.contains(">1.0.11.3<") && response.contains(">1.0.11.3.1<"));
        dbResponse = client.search(dbQuery, MediaType.APPLICATION_XML_VALUE);
        assertEquals("DB search response don't match!", response, dbResponse);
    }

}
