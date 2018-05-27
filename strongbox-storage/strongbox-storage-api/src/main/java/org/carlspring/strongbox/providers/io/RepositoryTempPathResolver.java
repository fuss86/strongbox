package org.carlspring.strongbox.providers.io;

import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;

import javax.inject.Inject;
import java.io.IOException;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RepositoryTempPathResolver
{

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    public RepositoryPath resolve(final ImmutableRepository repository)
    {
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final RootRepositoryPath repositoryPath = layoutProvider.resolve(repository);
        try
        {
            return layoutProvider.getProvider(repository).getTrashPath(repositoryPath);
        }
        catch (IOException e)
        {
            throw new RepositoryTempPathConstructionException(e);
        }
    }
}
