package org.carlspring.strongbox.providers.search;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.ImmutableConfiguration;
import org.carlspring.strongbox.dependency.snippet.CompatibleDependencyFormatRegistry;
import org.carlspring.strongbox.dependency.snippet.DependencySynonymFormatter;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author carlspring
 */
public abstract class AbstractSearchProvider
        implements SearchProvider
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractSearchProvider.class);
    
    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;
    
    @Inject
    private ArtifactResolutionService artifactResolutionService;


    @Override
    public SearchResult findExact(SearchRequest searchRequest)
    {
        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(searchRequest.getStorageId(),
                                                                           searchRequest.getRepositoryId(),
                                                                           searchRequest.getArtifactCoordinates().toPath())
                                                          .orElse(null);

        if (artifactEntry == null)
        {
            return null;
        }
        
        SearchResult searchResult = createSearchResult(artifactEntry);

        ImmutableStorage storage = getConfiguration().getStorage(artifactEntry.getStorageId());
        ImmutableRepository repository = storage.getRepository(searchRequest.getRepositoryId());

        Map<String, DependencySynonymFormatter> implementations = compatibleDependencyFormatRegistry.getProviderImplementations(repository.getLayout());

        Map<String, String> snippets = new LinkedHashMap<>();
        for (String compatibleDependencyFormat : implementations.keySet())
        {
            DependencySynonymFormatter formatter = implementations.get(compatibleDependencyFormat);

            snippets.put(compatibleDependencyFormat,
                         formatter.getDependencySnippet(searchRequest.getArtifactCoordinates()));
        }

        searchResult.setSnippets(snippets);

        return searchResult;
    }

    @Override
    public boolean contains(SearchRequest searchRequest)
            throws SearchException
    {
        return !search(searchRequest).getResults().isEmpty();
    }

    protected SearchResult createSearchResult(ArtifactEntry a)
    {
        String storageId = a.getStorageId();
        
        URL artifactResource;
        try
        {
            RepositoryPath repositoryPath = artifactResolutionService.resolvePath(a.getStorageId(), a.getRepositoryId(), a.getArtifactPath());
            artifactResource = artifactResolutionService.resolveResource(repositoryPath);
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to resolve artifact resource for [%s]",
                                       a.getArtifactCoordinates()), e);
            return null;
        }

        return new SearchResult(storageId,
                                a.getRepositoryId(),
                                a.getArtifactCoordinates(),
                                artifactResource.toString());
    }

    public ImmutableConfiguration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
