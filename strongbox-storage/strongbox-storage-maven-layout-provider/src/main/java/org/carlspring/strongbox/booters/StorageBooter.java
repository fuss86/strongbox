package org.carlspring.strongbox.booters;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.RepositoryInitializationException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexerFactory;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Singleton
@Component("storageBooter")
@Scope("singleton")
public class StorageBooter
{

    private static final Logger logger = LoggerFactory.getLogger(StorageBooter.class);

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private RepositoryIndexerFactory repositoryIndexerFactory;

    @Inject
    private ConfigurationManager configurationManager;

    private File lockFile = new File(ConfigurationResourceResolver.getVaultDirectory(), "storage-booter.lock");


    public StorageBooter()
    {
    }

    @PostConstruct
    public void initialize()
            throws IOException,
                   PlexusContainerException,
                   ComponentLookupException
    {
        if (!lockExists())
        {
            createLockFile();
            createTempDir();

            initializeStorages();

            final Configuration configuration = configurationManager.getConfiguration();
            for (String storageKey : configuration.getStorages().keySet())
            {
                try
                {
                    Storage storage = configuration.getStorages().get(storageKey);
                    initializeRepositories(storage);
                }
                catch (IOException e)
                {
                    logger.error("Failed to initialize the repositories for storage '" + storageKey + "'.", e);

                    throw new RuntimeException("Failed to initialize the repositories for storage '" + storageKey + "'.");
                }
            }
        }
        else
        {
            logger.debug("Failed to initialize the repositories. Another JVM may have already done this.");
        }
    }

    @PreDestroy
    public void removeLock()
    {
        //noinspection ResultOfMethodCallIgnored
        lockFile.delete();

        logger.debug("Removed lock file '" + lockFile.getAbsolutePath() + "'.");
    }

    public void createTempDir()
    {
        File tempDir = new File(ConfigurationResourceResolver.getVaultDirectory(), "tmp");
        if (!tempDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            tempDir.mkdirs();

            logger.debug("Created temporary directory: " + tempDir.getAbsolutePath() + ".");
        }

        if (System.getProperty("java.tmp.dir") == null)
        {
            System.setProperty("java.tmp.dir", tempDir.getAbsolutePath());

            logger.debug("Set java.tmp.dir to " + tempDir.getAbsolutePath() + ".");
        }
        else
        {
            logger.debug("The java.tmp.dir is already set to " + System.getProperty("java.tmp.dir") + ".");
        }
    }

    private void createLockFile()
            throws IOException
    {
        //noinspection ResultOfMethodCallIgnored
        lockFile.getParentFile().mkdirs();
        //noinspection ResultOfMethodCallIgnored
        lockFile.createNewFile();

        logger.debug(" -> Created lock file '" + lockFile.getAbsolutePath() + "'...");
    }

    private boolean lockExists()
            throws IOException
    {
        if (lockFile.exists())
        {
            logger.debug(" -> Lock found: '" + ConfigurationResourceResolver.getVaultDirectory() + "'!");

            return true;
        }
        else
        {
            logger.debug(" -> No lock found.");

            return false;
        }
    }

    /**
     * @return The base directory for the storages
     */
    private File initializeStorages()
            throws IOException
    {
        logger.debug("Running Strongbox storage booter...");
        logger.debug(" -> Creating storage directory skeleton...");

        String basedir;
        if (System.getProperty("strongbox.storage.booter.basedir") != null)
        {
            basedir = System.getProperty("strongbox.storage.booter.basedir");
        }
        else
        {
            // Assuming this invocation is related to tests:
            basedir = ConfigurationResourceResolver.getVaultDirectory() + "/storages";
        }

        final Map<String, Storage> storageEntry = configurationManager.getConfiguration().getStorages();
        for (Map.Entry<String, Storage> stringStorageEntry : storageEntry.entrySet())
        {
            initializeStorage(stringStorageEntry.getValue());
        }

        return new File(basedir).getAbsoluteFile();
    }

    private File initializeStorage(Storage storage)
            throws IOException
    {
        File storagesBaseDir = new File(storage.getBasedir());
        if (!storagesBaseDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            storagesBaseDir.mkdirs();
        }

        return storagesBaseDir;
    }

    private void initializeRepositories(Storage storageId)
            throws IOException,
                   PlexusContainerException,
                   ComponentLookupException
    {
        for (Repository repository : storageId.getRepositories().values())
        {
            initializeRepository(storageId, repository.getId());
            repository.setStorage(storageId);
        }
    }

    private void initializeRepository(Storage storage,
                                      String repositoryId)
            throws IOException,
                   PlexusContainerException,
                   ComponentLookupException
    {
        final File repositoryBasedir = new File(storage.getBasedir(), repositoryId);

        logger.debug("  * Initializing '" + repositoryBasedir.getAbsolutePath() + "'...");

        repositoryManagementService.createRepository(storage.getId(), repositoryId);

        Repository repository = storage.getRepository(repositoryId);
        if (repository.isIndexingEnabled())
        {
            initializeRepositoryIndexes(storage, repositoryId);
        }
    }

    private void initializeRepositoryIndexes(Storage storage,
                                             String repositoryId)
            throws PlexusContainerException,
                   ComponentLookupException,
                   IOException
    {
        final File repositoryBasedir = new File(storage.getBasedir(), repositoryId);

        if (storage.getRepository(repositoryId).isIndexingEnabled())
        {
            initializeRepositoryIndex(storage, repositoryId, IndexTypeEnum.LOCAL.getType(), repositoryBasedir);

            if (storage.getRepository(repositoryId).isProxyRepository())
            {
                initializeRepositoryIndex(storage, repositoryId, IndexTypeEnum.REMOTE.getType(), repositoryBasedir);
            }
        }
    }

    private void initializeRepositoryIndex(Storage storage,
                                           String repositoryId,
                                           String indexType,
                                           File repositoryBasedir)
            throws RepositoryInitializationException
    {
        final File indexDir = new File(repositoryBasedir, ".index/" + indexType);

        RepositoryIndexer repositoryIndexer = repositoryIndexerFactory.createRepositoryIndexer(storage.getId(),
                                                                                               repositoryId,
                                                                                               indexType,
                                                                                               repositoryBasedir,
                                                                                               indexDir);

        repositoryIndexManager.addRepositoryIndexer(storage.getId() + ":" + repositoryId + ":" + indexType, repositoryIndexer);
    }

    public RepositoryManagementService getRepositoryManagementService()
    {
        return repositoryManagementService;
    }

    public void setRepositoryManagementService(RepositoryManagementService repositoryManagementService)
    {
        this.repositoryManagementService = repositoryManagementService;
    }

    public RepositoryIndexManager getRepositoryIndexManager()
    {
        return repositoryIndexManager;
    }

    public void setRepositoryIndexManager(RepositoryIndexManager repositoryIndexManager)
    {
        this.repositoryIndexManager = repositoryIndexManager;
    }

    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }
}
