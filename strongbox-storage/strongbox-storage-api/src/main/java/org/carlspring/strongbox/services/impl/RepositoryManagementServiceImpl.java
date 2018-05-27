package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.ImmutableConfiguration;
import org.carlspring.strongbox.event.Event;
import org.carlspring.strongbox.event.RepositoryBasedEvent;
import org.carlspring.strongbox.event.repository.RepositoryEvent;
import org.carlspring.strongbox.event.repository.RepositoryEventListenerRegistry;
import org.carlspring.strongbox.event.repository.RepositoryEventTypeEnum;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.ArtifactStorageException;
import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.validation.resource.ArtifactOperationsValidator;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("repositoryManagementService")
public class RepositoryManagementServiceImpl
        implements RepositoryManagementService
{

    private static final Logger logger = LoggerFactory.getLogger(RepositoryManagementServiceImpl.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ArtifactOperationsValidator artifactOperationsValidator;

    @Inject
    private RepositoryEventListenerRegistry repositoryEventListenerRegistry;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;


    @Override
    public void createRepository(String storageId,
                                 String repositoryId)
            throws IOException, RepositoryManagementStrategyException
    {
        LayoutProvider provider = getLayoutProvider(storageId, repositoryId);
        if (provider != null)
        {
            provider.getRepositoryManagementStrategy().createRepository(storageId, repositoryId);
        }
        else
        {
            ImmutableRepository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

            logger.warn("Layout provider '" + repository.getLayout() + "' could not be resolved. " +
                        "Using generic implementation instead.");

            RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

            if (!Files.exists(repositoryPath))
            {
                Files.createDirectories(repositoryPath);
            }
        }

        RepositoryEvent event = new RepositoryEvent(storageId,
                                                    repositoryId,
                                                    RepositoryEventTypeEnum.EVENT_REPOSITORY_CREATED.getType());

        repositoryEventListenerRegistry.dispatchEvent(event);
    }

    @Override
    public void removeRepository(String storageId,
                                 String repositoryId)
            throws IOException
    {
        LayoutProvider provider = getLayoutProvider(storageId, repositoryId);
        provider.getRepositoryManagementStrategy().removeRepository(storageId, repositoryId);

        RepositoryEvent event = new RepositoryEvent(storageId,
                                                    repositoryId,
                                                    RepositoryEventTypeEnum.EVENT_REPOSITORY_DELETED.getType());

        repositoryEventListenerRegistry.dispatchEvent(event);
    }

    @Override
    public void deleteTrash(String storageId, String repositoryId)
            throws IOException
    {
        artifactOperationsValidator.checkStorageExists(storageId);
        artifactOperationsValidator.checkRepositoryExists(storageId, repositoryId);

        try
        {
            final ImmutableStorage storage = getStorage(storageId);
            final ImmutableRepository repository = storage.getRepository(repositoryId);

            artifactOperationsValidator.checkAllowsDeletion(repository);

            LayoutProvider layoutProvider = getLayoutProvider(storageId, repositoryId);
            layoutProvider.deleteTrash(storageId, repositoryId);

            RepositoryEvent event = new RepositoryEvent(storageId,
                                                        repositoryId,
                                                        RepositoryEventTypeEnum.EVENT_REPOSITORY_EMTPY_TRASH.getType());

            repositoryEventListenerRegistry.dispatchEvent(event);
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteTrash()
            throws ArtifactStorageException
    {
        try
        {
            layoutProviderRegistry.deleteTrash();

            int type = RepositoryEventTypeEnum.EVENT_REPOSITORY_EMTPY_TRASH_FOR_ALL_REPOSITORIES.getType();
            RepositoryEvent event = new RepositoryEvent(null, null, type);

            repositoryEventListenerRegistry.dispatchEvent(event);
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void undelete(RepositoryPath repositoryPath)
            throws IOException
    {
        artifactOperationsValidator.validate(repositoryPath);

        final ImmutableRepository repository = repositoryPath.getRepository();
        final ImmutableStorage storage = repository.getStorage();

        artifactOperationsValidator.checkAllowsDeletion(repository);

        try
        {
            LayoutProvider layoutProvider = getLayoutProvider(storage.getId(), repository.getId());
            layoutProvider.undelete(repositoryPath);

            int type = RepositoryEventTypeEnum.EVENT_REPOSITORY_EMTPY_TRASH_FOR_ALL_REPOSITORIES.getType();
            Event event = new RepositoryBasedEvent<>(repositoryPath, type);

            repositoryEventListenerRegistry.dispatchEvent(event);
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void undeleteTrash(String storageId, String repositoryId)
            throws IOException
    {
        artifactOperationsValidator.checkStorageExists(storageId);
        artifactOperationsValidator.checkRepositoryExists(storageId, repositoryId);

        try
        {
            final ImmutableStorage storage = getStorage(storageId);
            final ImmutableRepository repository = storage.getRepository(repositoryId);

            if (repository.isTrashEnabled())
            {
                LayoutProvider layoutProvider = getLayoutProvider(storageId, repositoryId);
                RootRepositoryPath repositoryPath = layoutProvider.resolve(repository);
                layoutProvider.undelete(repositoryPath);

                RepositoryEvent event = new RepositoryEvent(storageId,
                                                            repositoryId,
                                                            RepositoryEventTypeEnum.EVENT_REPOSITORY_UNDELETE_TRASH
                                                                                   .getType());

                repositoryEventListenerRegistry.dispatchEvent(event);
            }
        }
        catch (IOException e)
        {
            throw new ArtifactStorageException(e.getMessage(), e);
        }
    }

    @Override
    public void undeleteTrash()
            throws ProviderImplementationException
    {
        layoutProviderRegistry.undeleteTrash();
        RepositoryEvent event = new RepositoryEvent(null,
                                                    null,
                                                    RepositoryEventTypeEnum.EVENT_REPOSITORY_UNDELETE_TRASH_FOR_ALL_REPOSITORIES
                                                                           .getType());
        repositoryEventListenerRegistry.dispatchEvent(event);
    }

    @Override
    public void putInService(String storageId,
                             String repositoryId)
    {
        configurationManagementService.putInService(storageId, repositoryId);
    }

    @Override
    public void putOutOfService(String storageId,
                                String repositoryId)
    {
        configurationManagementService.putOutOfService(storageId, repositoryId);
    }

    private LayoutProvider getLayoutProvider(String storageId,
                                             String repositoryId)
    {
        ImmutableStorage storage = getConfiguration().getStorage(storageId);
        ImmutableRepository repository = storage.getRepository(repositoryId);

        return layoutProviderRegistry.getProvider(repository.getLayout());
    }

    @Override
    public ImmutableStorage getStorage(String storageId)
    {
        return getConfiguration().getStorages().get(storageId);
    }

    public ImmutableConfiguration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
