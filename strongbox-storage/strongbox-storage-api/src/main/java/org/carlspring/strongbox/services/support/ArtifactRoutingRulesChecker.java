package org.carlspring.strongbox.services.support;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.routing.ImmutableRoutingRule;
import org.carlspring.strongbox.storage.routing.ImmutableRoutingRules;
import org.carlspring.strongbox.storage.routing.ImmutableRuleSet;

import javax.inject.Inject;
import java.io.IOException;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 * @see <a href="https://github.com/strongbox/strongbox/wiki/Artifact-Routing-Rules">Artifact Routing Rules</a>
 */
@Component
public class ArtifactRoutingRulesChecker
{

    @Inject
    private ConfigurationManager configurationManager;

    public boolean isDenied(String groupRepositoryId,
                            RepositoryPath repositoryPath)
            throws IOException
    {
        final ImmutableRoutingRules routingRules = configurationManager.getConfiguration().getRoutingRules();
        final ImmutableRuleSet denyRules = routingRules.getDenyRules(groupRepositoryId);
        final ImmutableRuleSet wildcardDenyRules = routingRules.getWildcardDeniedRules();
        final ImmutableRuleSet acceptRules = routingRules.getAcceptRules(groupRepositoryId);
        final ImmutableRuleSet wildcardAcceptRules = routingRules.getWildcardAcceptedRules();

        if (fitsRoutingRules(repositoryPath, denyRules) ||
            fitsRoutingRules(repositoryPath, wildcardDenyRules))
        {
            if (!(fitsRoutingRules(repositoryPath, acceptRules) ||
                  fitsRoutingRules(repositoryPath, wildcardAcceptRules)))
            {
                return true;
            }

        }

        return false;
    }

    public boolean isAccepted(String groupRepositoryId,
                              RepositoryPath repositoryPath)
            throws IOException
    {
        return !isDenied(groupRepositoryId, repositoryPath);
    }

    private boolean fitsRoutingRules(RepositoryPath repositoryPath,
                                     ImmutableRuleSet denyRules)
            throws IOException
    {
        ImmutableRepository repository = repositoryPath.getRepository();
        if (denyRules != null && !denyRules.getRoutingRules().isEmpty())
        {
            String artifactPath = RepositoryFiles.stringValue(repositoryPath);
            for (ImmutableRoutingRule rule : denyRules.getRoutingRules())
            {
                if (rule.getRepositories().contains(repository.getId())
                    && rule.getRegex().matcher(artifactPath).matches())
                {
                    return true;
                }
            }
        }

        return false;
    }

}
