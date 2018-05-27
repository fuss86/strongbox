package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.data.service.support.search.PagingCriteria;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria;
import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.remote.ImmutableRemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import static org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria.Builder.anArtifactEntrySearchCriteria;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class LocalStorageProxyRepositoryExpiredArtifactsCleaner
{

    private final Logger logger = LoggerFactory.getLogger(LocalStorageProxyRepositoryExpiredArtifactsCleaner.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;

    @Inject
    private ArtifactManagementService artifactManagementService;

    @Transactional(rollbackFor = Exception.class)
    public void cleanup(final Integer lastAccessedTimeInDays,
                        final Long minSizeInBytes)
            throws IOException, SearchException
    {
        final ArtifactEntrySearchCriteria searchCriteria = anArtifactEntrySearchCriteria()
                                                                   .withLastAccessedTimeInDays(lastAccessedTimeInDays)
                                                                   .withMinSizeInBytes(minSizeInBytes)
                                                                   .build();

        final List<ArtifactEntry> artifactEntries = artifactEntryService.findMatching(searchCriteria,
                                                                                      PagingCriteria.ALL);
        filterAccessibleProxiedArtifacts(artifactEntries);

        if (CollectionUtils.isEmpty(artifactEntries))
        {
            return;
        }

        logger.debug("Cleaning artifacts {}", artifactEntries);
        deleteFromDatabase(artifactEntries);
        deleteFromStorage(artifactEntries);
    }

    private void filterAccessibleProxiedArtifacts(final List<ArtifactEntry> artifactEntries)
    {
        if (CollectionUtils.isEmpty(artifactEntries))
        {
            return;
        }
        for (final Iterator<ArtifactEntry> it = artifactEntries.iterator(); it.hasNext(); )
        {
            final ArtifactEntry artifactEntry = it.next();
            final ImmutableStorage storage = configurationManager.getConfiguration().getStorage(
                    artifactEntry.getStorageId());
            final ImmutableRepository repository = storage.getRepository(artifactEntry.getRepositoryId());
            if (!repository.isProxyRepository())
            {
                it.remove();
                continue;
            }
            final ImmutableRemoteRepository remoteRepository = repository.getRemoteRepository();
            if (remoteRepository == null)
            {
                logger.warn("Repository {} is not associated with remote repository", repository.getId());
                it.remove();
                continue;
            }
            if (!remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository))
            {
                logger.warn("Remote repository {} is down. Artifacts won't be cleaned up.", remoteRepository.getUrl());
                it.remove();
                continue;
            }
        }

    }

    private void deleteFromStorage(final List<ArtifactEntry> artifactEntries)
            throws IOException
    {
        for (final ArtifactEntry artifactEntry : artifactEntries)
        {
            final ImmutableStorage storage = configurationManager.getConfiguration().getStorage(
                    artifactEntry.getStorageId());
            final ImmutableRepository repository = storage.getRepository(artifactEntry.getRepositoryId());
            final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
            RepositoryPath repositoryPath = layoutProvider.resolve(repository).resolve(artifactEntry);

            artifactManagementService.delete(repositoryPath, true);
        }
    }

    private void deleteFromDatabase(final List<ArtifactEntry> artifactEntries)
    {
        final int deletedCount = artifactEntryService.delete(artifactEntries);
        if (deletedCount != artifactEntries.size())
        {
            logger.warn(
                    "Unexpected differences on deleting artifact entries. Deleted count {}, artifact entries size {}",
                    deletedCount, artifactEntries.size());
        }
    }

}
