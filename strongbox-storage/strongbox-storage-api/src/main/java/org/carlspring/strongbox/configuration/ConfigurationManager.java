package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;

import javax.inject.Inject;
import java.net.URI;

import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class ConfigurationManager
{

    @Inject
    private ConfigurationManagementService configurationService;

    public ImmutableRepository getRepository(String storageAndRepositoryId)
    {
        String[] elements = storageAndRepositoryId.split(":");
        String storageId = elements[0];
        String repositoryId = elements[1];

        return getConfiguration().getStorage(storageId).getRepository(repositoryId);
    }

    public ImmutableRepository getRepository(String storageId,
                                             String repositoryId)
    {
        return getConfiguration().getStorage(storageId).getRepository(repositoryId);
    }

    public String getStorageId(ImmutableStorage storage,
                               String storageAndRepositoryId)
    {
        String[] storageAndRepositoryIdTokens = storageAndRepositoryId.split(":");

        return storageAndRepositoryIdTokens.length == 2 ? storageAndRepositoryIdTokens[0] : storage.getId();
    }

    public String getRepositoryId(String storageAndRepositoryId)
    {
        String[] storageAndRepositoryIdTokens = storageAndRepositoryId.split(":");

        return storageAndRepositoryIdTokens[storageAndRepositoryIdTokens.length < 2 ? 0 : 1];
    }

    public ImmutableConfiguration getConfiguration()
    {
        return configurationService.getConfiguration();
    }

    public URI getBaseUri()
    {
        try
        {
            return URI.create(getConfiguration().getBaseUrl());
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidConfigurationException(e);
        }
    }


}
