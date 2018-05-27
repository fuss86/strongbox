package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.ImmutableConfiguration;
import org.carlspring.strongbox.providers.AbstractMappedProviderRegistry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class LayoutProviderRegistry extends AbstractMappedProviderRegistry<LayoutProvider>
{

    private static final Logger logger = LoggerFactory.getLogger(LayoutProviderRegistry.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    public LayoutProviderRegistry()
    {
    }
    
    public void deleteTrash()
            throws IOException
    {
        for (Map.Entry entry : getConfiguration().getStorages().entrySet())
        {
            Storage storage = (Storage) entry.getValue();

            final Map<String, Repository> repositories = storage.getRepositories();
            for (Repository repository : repositories.values())
            {
                if (repository.allowsDeletion())
                {
                    logger.debug("Emptying trash for repository " + repository.getId() + "...");

                    getProvider(repository.getLayout()).deleteTrash(storage.getId(), repository.getId());
                }
                else
                {
                    logger.warn("Repository " + repository.getId() + " does not support removal of trash.");
                }
            }
        }
    }

    public void undeleteTrash()
            throws ProviderImplementationException
    {
        for (Map.Entry<String, ImmutableStorage> entry : getConfiguration().getStorages().entrySet())
        {
            ImmutableStorage storage = entry.getValue();

            final Map<String, ImmutableRepository> repositories = storage.getRepositories();
            for (ImmutableRepository repository : repositories.values())
            {
                LayoutProvider layoutProvider = getLayoutProvider(repository, this);

                final String storageId = storage.getId();
                final String repositoryId = repository.getId();

                try
                {
                    if (repository.isTrashEnabled())
                    {
                        RootRepositoryPath repositoryPath = layoutProvider.resolve(repository);
                        layoutProvider.undelete(repositoryPath);
                    }
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Unable to undelete trash for storage " + storageId + " in repository " +
                                               repositoryId, e);
                }
            }
        }
    }

    @Override
    @PostConstruct
    public void initialize()
    {
        logger.info("Initialized the layout provider registry.");
    }

    @Override
    public Map<String, LayoutProvider> getProviders()
    {
        return super.getProviders();
    }

    @Override
    public void setProviders(Map<String, LayoutProvider> providers)
    {
        super.setProviders(providers);
    }

    @Override
    public LayoutProvider getProvider(String alias)
    {
        return super.getProvider(alias);
    }

    @Override
    public LayoutProvider addProvider(String alias, LayoutProvider provider)
    {
        LayoutProvider layoutProvider = super.addProvider(alias, provider);

        configurationManagementService.setRepositoryArtifactCoordinateValidators();

        return layoutProvider;
    }

    @Override
    public void removeProvider(String alias)
    {
        super.removeProvider(alias);
    }

    public static LayoutProvider getLayoutProvider(ImmutableRepository repository,
                                                   LayoutProviderRegistry layoutProviderRegistry)
            throws ProviderImplementationException
    {
        return layoutProviderRegistry.getProvider(repository.getLayout());
    }

    public ImmutableConfiguration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    public ImmutableStorage getStorage(String storageId)
    {
        return configurationManager.getConfiguration()
                                             .getStorage(storageId);
    }

}
