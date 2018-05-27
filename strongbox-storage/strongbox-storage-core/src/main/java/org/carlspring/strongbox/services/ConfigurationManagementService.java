package org.carlspring.strongbox.services;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.HttpConnectionPool;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import java.util.List;

/**
 * @author mtodorov
 */
public interface ConfigurationManagementService
{

    void setConfiguration(Configuration configuration);

    void save(Configuration Configuration);

    void setInstanceName(String instanceName);

    void setBaseUrl(String baseUrl);

    void setPort(int port);

    void setProxyConfiguration(String storageId,
                               String repositoryId,
                               ProxyConfiguration proxyConfiguration);

    void saveStorage(Storage storage);

    void removeStorage(String storageId);

    void saveRepository(String storageId,
                        Repository repository);

    void removeRepositoryFromAssociatedGroups(String storageId,
                                              String repositoryId);

    void removeRepository(String storageId,
                          String repositoryId);

    void setProxyRepositoryMaxConnections(String storageId,
                                          String repositoryId,
                                          int numberOfConnections);

    boolean saveAcceptedRuleSet(RuleSet ruleSet);

    boolean saveDeniedRuleSet(RuleSet ruleSet);

    boolean removeAcceptedRuleSet(String groupRepository);

    boolean saveAcceptedRepository(String groupRepository,
                                   RoutingRule routingRule);

    boolean removeAcceptedRepository(String groupRepository,
                                     String pattern,
                                     String repositoryId);

    boolean overrideAcceptedRepositories(String groupRepository,
                                         RoutingRule routingRule);

    void setRepositoryArtifactCoordinateValidators();

    void putInService(String storageId,
                      String repositoryId);

    void putOutOfService(String storageId,
                         String repositoryId);
}
