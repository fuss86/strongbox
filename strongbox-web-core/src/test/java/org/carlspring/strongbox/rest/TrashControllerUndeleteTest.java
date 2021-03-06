package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Todorov
 * @author Alex Oreshkevich
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TrashControllerUndeleteTest
        extends RestAssuredBaseTest
{

    private static final File BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory()).getAbsoluteFile();

    private static final String REPOSITORY_WITH_TRASH = "tcut-releases-with-trash";

    private static final String REPOSITORY_RELEASES = "tcut-releases";

    private static final String REPOSITORY_WITH_TRASH_BASEDIR = BASEDIR.getAbsolutePath() +
                                                                "/storages/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH;

    private static final File ARTIFACT_FILE_IN_TRASH = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
                                                                "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                                "test-artifact-undelete-1.0.jar").getAbsoluteFile();

    @Inject
    private ConfigurationManager configurationManager;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @PostConstruct
    public void initialize()
            throws Exception
    {
        super.init();

        Storage storage = configurationManager.getConfiguration().getStorage(STORAGE0);

        // Notes:
        // - Used by testForceDeleteArtifactNotAllowed()
        // - Forced deletions are not allowed
        // - Has enabled trash
        Repository repositoryWithTrash = new Repository(REPOSITORY_WITH_TRASH);
        repositoryWithTrash.setStorage(storage);
        repositoryWithTrash.setAllowsForceDeletion(false);
        repositoryWithTrash.setTrashEnabled(true);
        repositoryWithTrash.setIndexingEnabled(false);

        createRepository(repositoryWithTrash);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH).getAbsolutePath(),
                         "org.carlspring.strongbox.undelete:test-artifact-undelete",
                         new String[] { "1.0" });

        // Notes:
        // - Used by testForceDeleteArtifactAllowed()
        // - Forced deletions are allowed
        Repository repositoryReleases = new Repository(REPOSITORY_RELEASES);
        repositoryReleases.setStorage(storage);
        repositoryReleases.setAllowsForceDeletion(false);
        repositoryReleases.setIndexingEnabled(false);

        createRepository(repositoryReleases);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES).getAbsolutePath(),
                         "org.carlspring.strongbox.undelete:test-artifact-undelete",
                         new String[] { "1.1" });
    }

    @Before
    public void setUp()
            throws Exception
    {
        // Delete the artifact (this one should get placed under the .trash)
        client.delete(STORAGE0,
                      REPOSITORY_WITH_TRASH,
                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar");

        // Delete the artifact (this one shouldn't get placed under the .trash)
        client.delete(STORAGE0,
                      REPOSITORY_RELEASES,
                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.1/test-artifact-undelete-1.1.jar");
    }

    @PreDestroy
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_WITH_TRASH));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));

        return repositories;
    }

    @Test
    public void testUndeleteArtifactFromTrashForRepository()
            throws Exception
    {
        String url = getContextBaseUrl() + "/trash/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH;
        url += "/org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar";

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .post(url)
               .peek()
               .then()
               .statusCode(200);

        File artifactFileRestoredFromTrash = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/" +
                                                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                      "test-artifact-undelete-1.0.jar").getAbsoluteFile();

        assertFalse("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                    ARTIFACT_FILE_IN_TRASH.exists());
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                   artifactFileRestoredFromTrash.exists());
    }

    @Test
    public void testUndeleteArtifactsForAllRepositories()
            throws Exception
    {
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                   ARTIFACT_FILE_IN_TRASH.getParentFile().exists());

        String url = getContextBaseUrl() + "/trash";

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .post(url)
               .peek()
               .then()
               .statusCode(200);

        File artifactFileRestoredFromTrash = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/" +
                                                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                      "test-artifact-undelete-1.0.jar").getAbsoluteFile();

        assertFalse("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                    ARTIFACT_FILE_IN_TRASH.exists());
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH +
                   "' (" + artifactFileRestoredFromTrash.getAbsolutePath() + " does not exist)!",
                   artifactFileRestoredFromTrash.exists());
    }

}
