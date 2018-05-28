package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ImmutableConfiguration;
import org.carlspring.strongbox.providers.io.RepositoryFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author carlspring
 */
public abstract class AbstractRepositoryManagementStrategy
        implements RepositoryManagementStrategy
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractRepositoryManagementStrategy.class);

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Override
    public void createRepository(String storageId,
                                 String repositoryId)
            throws IOException, RepositoryManagementStrategyException
    {
        logger.debug(String.format("Creating repository [%s/%s]...", storageId, repositoryId));

        ImmutableStorage storage = getStorage(storageId);
        ImmutableRepository repository = storage.getRepository(repositoryId);
        createRepositoryStructure(repository);
        createRepositoryInternal(storage, getRepository(storageId, repositoryId));
    }

    @Override
    public void createRepositoryStructure(final ImmutableRepository repository)
            throws IOException
    {
        final RepositoryPath rootRepositoryPath = repositoryPathResolver.resolve(repository);
        if (!Files.exists(rootRepositoryPath))
        {
            Files.createDirectories(rootRepositoryPath);
        }
        final RepositoryPath trashRepositoryPath = rootRepositoryPath.resolve(RepositoryFileSystem.TRASH);
        if (!Files.exists(trashRepositoryPath))
        {
            Files.createDirectories(trashRepositoryPath);
        }
    }

    protected void createRepositoryInternal(ImmutableStorage storage,
                                            ImmutableRepository repository)
            throws IOException, RepositoryManagementStrategyException
    {
        // override if needed
    }

    protected ImmutableStorage getStorage(String storageId)
    {
        return getConfiguration().getStorage(storageId);
    }

    protected ImmutableRepository getRepository(String storageId,
                                       String repositoryId)
    {
        return getStorage(storageId).getRepository(repositoryId);
    }

    @Override
    public void removeRepository(String storageId,
                                 String repositoryId)
            throws IOException
    {
        removeDirectoryStructure(storageId, repositoryId);
    }

    @Override
    public void removeDirectoryStructure(String storageId,
                                         String repositoryId)
            throws IOException
    {
        ImmutableRepository repository = getRepository(storageId, repositoryId);

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        if (Files.exists(repositoryPath))
        {
            Files.delete(repositoryPath);

            logger.debug(String.format("Removed directory structure for repository '%s'.", repositoryPath));
        }
        else
        {
            throw new IOException(String.format("Failed to delete non-existing repository '%s'.", repositoryPath));
        }
    }


    protected ImmutableConfiguration getConfiguration()
    {
        return configurationManagementService.getConfiguration();
    }

}
