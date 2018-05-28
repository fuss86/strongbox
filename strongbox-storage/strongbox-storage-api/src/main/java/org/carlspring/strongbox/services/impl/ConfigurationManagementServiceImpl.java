package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationFileManager;
import org.carlspring.strongbox.configuration.ImmutableConfiguration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.event.repository.RepositoryEvent;
import org.carlspring.strongbox.event.repository.RepositoryEventListenerRegistry;
import org.carlspring.strongbox.event.repository.RepositoryEventTypeEnum;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.HttpConnectionPool;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.stereotype.Service;

/**
 * @author mtodorov
 */
@Service
public class ConfigurationManagementServiceImpl
        implements ConfigurationManagementService
{

    private final ReadWriteLock configurationLock = new ReentrantReadWriteLock();

    @Inject
    private ConfigurationFileManager configurationFileManager;

    @Inject
    private RepositoryEventListenerRegistry repositoryEventListenerRegistry;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    /**
     * Yes, this is a state object.
     * It is protected by the {@link #configurationLock} here
     * and should not be exposed to the world.
     *
     * @see #getConfiguration()
     */
    private Configuration configuration;

    @Override
    public ImmutableConfiguration getConfiguration()
    {
        final Lock readLock = configurationLock.readLock();
        readLock.lock();

        try
        {
            return new ImmutableConfiguration(configuration);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void setConfiguration(Configuration newConf)
    {
        Objects.requireNonNull(configuration, "Configuration cannot be null");

        modifyInLock(configuration ->
                     {
                         ConfigurationManagementServiceImpl.this.configuration = newConf;
                         setProxyRepositoryConnectionPoolConfigurations();
                         setRepositoryStorageRelationships();
                         setAllows();
                     }, false);
    }

    @Override
    public void setInstanceName(String instanceName)
    {
        modifyInLock(configuration -> configuration.setInstanceName(instanceName));
    }

    @Override
    public void setBaseUrl(String baseUrl)
    {
        modifyInLock(configuration -> configuration.setBaseUrl(baseUrl));
    }

    @Override
    public void setPort(int port)
    {
        modifyInLock(configuration -> configuration.setPort(port));
    }

    @Override
    public void setProxyConfiguration(String storageId,
                                      String repositoryId,
                                      ProxyConfiguration proxyConfiguration)
    {
        modifyInLock(configuration ->
                     {
                         if (storageId != null && repositoryId != null)
                         {
                             configuration.getStorage(storageId)
                                          .getRepository(repositoryId)
                                          .setProxyConfiguration(proxyConfiguration);
                         }
                         else
                         {
                             configuration.setProxyConfiguration(proxyConfiguration);
                         }
                     });
    }

    @Override
    public void saveStorage(Storage storage)
    {
        modifyInLock(configuration -> configuration.addStorage(storage));
    }

    @Override
    public void removeStorage(String storageId)
    {
        modifyInLock(configuration -> configuration.getStorages().remove(storageId));
    }

    @Override
    public void saveRepository(String storageId,
                               Repository repository)
    {
        modifyInLock(configuration -> configuration.getStorage(storageId).addRepository(repository));
    }

    @Override
    public void removeRepositoryFromAssociatedGroups(String storageId,
                                                     String repositoryId)
    {
        modifyInLock(configuration ->
                     {
                         List<ImmutableRepository> includedInGroupRepositories = getConfiguration().getGroupRepositoriesContaining(
                                 storageId, repositoryId);

                         if (!includedInGroupRepositories.isEmpty())
                         {
                             for (ImmutableRepository repository : includedInGroupRepositories)
                             {
                                 configuration.getStorage(repository.getStorage().getId())
                                              .getRepository(repository.getId())
                                              .getGroupRepositories().remove(repositoryId);
                             }
                         }
                     });
    }

    @Override
    public void removeRepository(String storageId,
                                 String repositoryId)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId).removeRepository(repositoryId);
                         removeRepositoryFromAssociatedGroups(storageId, repositoryId);
                     });
    }

    @Override
    public void setProxyRepositoryMaxConnections(String storageId,
                                                 String repositoryId,
                                                 int numberOfConnections)
    {
        modifyInLock(configuration ->
                     {
                         Repository repository = configuration.getStorage(storageId).getRepository(repositoryId);
                         if (repository.getHttpConnectionPool() == null)
                         {
                             repository.setHttpConnectionPool(new HttpConnectionPool());
                         }

                         repository.getHttpConnectionPool().setAllocatedConnections(numberOfConnections);
                     });
    }

    @Override
    public boolean saveAcceptedRuleSet(RuleSet ruleSet)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getRoutingRules().addAcceptRule(ruleSet.getGroupRepository(), ruleSet);
                     });

        return true;
    }

    @Override
    public boolean saveDeniedRuleSet(RuleSet ruleSet)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getRoutingRules().addDenyRule(ruleSet.getGroupRepository(), ruleSet);
                     });

        return true;
    }

    @Override
    public boolean removeAcceptedRuleSet(String groupRepository)
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(configuration ->
                     {
                         final Map<String, RuleSet> accepted = configuration.getRoutingRules().getAccepted();

                         if (accepted.containsKey(groupRepository))
                         {
                             result.setTrue();
                             accepted.remove(groupRepository);
                         }
                     });

        return result.isTrue();
    }

    @Override
    public boolean saveAcceptedRepository(String groupRepository,
                                          RoutingRule routingRule)
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(configuration ->
                     {
                         final Map<String, RuleSet> acceptedRulesMap = configuration.getRoutingRules().getAccepted();
                         if (acceptedRulesMap.containsKey(groupRepository))
                         {
                             for (RoutingRule rl : acceptedRulesMap.get(groupRepository).getRoutingRules())
                             {
                                 if (routingRule.getPattern().equals(rl.getPattern()))
                                 {
                                     result.setTrue();
                                     rl.getRepositories().addAll(routingRule.getRepositories());
                                 }
                             }
                         }
                     });

        return result.isTrue();
    }

    @Override
    public boolean removeAcceptedRepository(String groupRepository,
                                            String pattern,
                                            String repositoryId)
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(configuration ->
                     {
                         final Map<String, RuleSet> acceptedRules = configuration.getRoutingRules().getAccepted();
                         if (acceptedRules.containsKey(groupRepository))
                         {
                             for (RoutingRule routingRule : acceptedRules.get(groupRepository).getRoutingRules())
                             {
                                 if (pattern.equals(routingRule.getPattern()))
                                 {
                                     result.setTrue();
                                     routingRule.getRepositories().remove(repositoryId);
                                 }
                             }
                         }
                     });

        return result.isTrue();
    }

    @Override
    public boolean overrideAcceptedRepositories(String groupRepository,
                                                RoutingRule routingRule)
    {
        final MutableBoolean result = new MutableBoolean();
        modifyInLock(configuration ->
                     {
                         if (configuration.getRoutingRules().getAccepted().containsKey(groupRepository))
                         {
                             for (RoutingRule rule : configuration.getRoutingRules()
                                                                  .getAccepted()
                                                                  .get(groupRepository)
                                                                  .getRoutingRules())
                             {
                                 if (routingRule.getPattern().equals(rule.getPattern()))
                                 {
                                     result.setTrue();
                                     rule.setRepositories(routingRule.getRepositories());
                                 }
                             }
                         }
                     });

        return result.isTrue();
    }

    @Override
    public void addRepositoryToGroup(String storageId,
                                     String repositoryId,
                                     String repositoryGroupMemberId)
    {
        modifyInLock(configuration ->
                     {
                         final Repository repository = configuration.getStorage(storageId).getRepository(repositoryId);
                         repository.addRepositoryToGroup(repositoryGroupMemberId);
                     });
    }

    private void setAllows()
    {
        modifyInLock(configuration ->
                     {
                         final Map<String, Storage> storages = configuration.getStorages();

                         if (storages != null && !storages.isEmpty())
                         {
                             for (Storage storage : storages.values())
                             {
                                 if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                                 {
                                     for (Repository repository : storage.getRepositories().values())
                                     {
                                         if (repository.getType().equals(RepositoryTypeEnum.GROUP.getType()))
                                         {
                                             repository.setAllowsDelete(false);
                                             repository.setAllowsDeployment(false);
                                             repository.setAllowsRedeployment(false);
                                         }
                                         if (repository.getType().equals(RepositoryTypeEnum.PROXY.getType()))
                                         {
                                             repository.setAllowsDeployment(false);
                                             repository.setAllowsRedeployment(false);
                                         }
                                     }
                                 }
                             }
                         }
                     });
    }

    /**
     * Sets the repository <--> storage relationships explicitly, as initially, when these are deserialized from the
     * XML, they have no such relationship.
     */
    private void setRepositoryStorageRelationships()
    {
        modifyInLock(configuration ->
                     {
                         final Map<String, Storage> storages = configuration.getStorages();

                         if (storages != null && !storages.isEmpty())
                         {
                             for (Storage storage : storages.values())
                             {
                                 if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                                 {
                                     for (Repository repository : storage.getRepositories().values())
                                     {
                                         repository.setStorage(storage);
                                     }
                                 }
                             }
                         }
                     });
    }

    @Override
    public void setRepositoryArtifactCoordinateValidators()
    {
        modifyInLock(configuration ->
                     {
                         final Map<String, Storage> storages = configuration.getStorages();

                         if (storages != null && !storages.isEmpty())
                         {
                             for (Storage storage : storages.values())
                             {
                                 if (storage.getRepositories() != null && !storage.getRepositories().isEmpty())
                                 {
                                     for (Repository repository : storage.getRepositories().values())
                                     {
                                         LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(
                                                 repository.getLayout());

                                         // Generally, this should not happen. However, there are at least two cases where it may occur:
                                         // 1) During testing -- various modules are not added as dependencies and a layout provider
                                         //    is thus not registered.
                                         // 2) Syntax error, or some other mistake leading to an incorrectly defined layout
                                         //    for a repository.
                                         if (layoutProvider != null)
                                         {
                                             @SuppressWarnings("unchecked")
                                             Set<String> defaultArtifactCoordinateValidators = layoutProvider.getDefaultArtifactCoordinateValidators();
                                             if ((repository.getArtifactCoordinateValidators() == null ||
                                                  (repository.getArtifactCoordinateValidators() != null &&
                                                   repository.getArtifactCoordinateValidators().isEmpty())) &&
                                                 defaultArtifactCoordinateValidators != null)
                                             {
                                                 repository.setArtifactCoordinateValidators(
                                                         defaultArtifactCoordinateValidators);
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     });
    }

    @Override
    public void putInService(final String storageId,
                             final String repositoryId)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId)
                                      .getRepository(repositoryId)
                                      .setStatus(RepositoryStatusEnum.IN_SERVICE.getStatus());

                         RepositoryEvent event = new RepositoryEvent(storageId,
                                                                     repositoryId,
                                                                     RepositoryEventTypeEnum.EVENT_REPOSITORY_PUT_IN_SERVICE.getType());

                         repositoryEventListenerRegistry.dispatchEvent(event);
                     });
    }

    @Override
    public void putOutOfService(final String storageId,
                                final String repositoryId)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId)
                                      .getRepository(repositoryId)
                                      .setStatus(RepositoryStatusEnum.OUT_OF_SERVICE.getStatus());

                         RepositoryEvent event = new RepositoryEvent(storageId,
                                                                     repositoryId,
                                                                     RepositoryEventTypeEnum.EVENT_REPOSITORY_PUT_OUT_OF_SERVICE
                                                                             .getType());

                         repositoryEventListenerRegistry.dispatchEvent(event);
                     });
    }

    @Override
    public void setArtifactMaxSize(final String storageId,
                                   final String repositoryId,
                                   final long value)
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorage(storageId)
                                      .getRepository(repositoryId)
                                      .setArtifactMaxSize(value);
                     });
    }

    private void setProxyRepositoryConnectionPoolConfigurations()
    {
        modifyInLock(configuration ->
                     {
                         configuration.getStorages().values().stream()
                                      .filter(storage -> MapUtils.isNotEmpty(storage.getRepositories()))
                                      .flatMap(storage -> storage.getRepositories().values().stream())
                                      .filter(repository -> repository.getHttpConnectionPool() != null &&
                                                            repository.getRemoteRepository() != null &&
                                                            repository.getRemoteRepository().getUrl() != null)
                                      .forEach(
                                              repository -> proxyRepositoryConnectionPoolConfigurationService.setMaxPerRepository(
                                                      repository.getRemoteRepository().getUrl(),
                                                      repository.getHttpConnectionPool().getAllocatedConnections()));
                     });
    }

    private void modifyInLock(final Consumer<Configuration> operation)
    {
        modifyInLock(operation, true);
    }

    private void modifyInLock(final Consumer<Configuration> operation,
                              final boolean storeInFile)
    {
        final Lock writeLock = configurationLock.writeLock();
        writeLock.lock();

        try
        {
            operation.accept(configuration);

            if (storeInFile)
            {
                configurationFileManager.store(configuration);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

}
