package org.carlspring.strongbox.providers.repository.group;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class GroupRepositorySetCollector
{

    @Inject
    private ConfigurationManager configurationManager;

    public Set<ImmutableRepository> collect(ImmutableRepository groupRepository)
    {
        return collect(groupRepository, false);
    }

    public Set<ImmutableRepository> collect(ImmutableRepository groupRepository,
                                            boolean traverse)
    {
        Set<ImmutableRepository> result = groupRepository.getGroupRepositories()
                                                         .keySet()
                                                         .stream()
                                                         .map(groupRepoId -> getRepository(groupRepository.getStorage(),
                                                                                           groupRepoId))
                                                         .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!traverse)
        {
            return result;
        }

        Set<ImmutableRepository> traverseResult = new LinkedHashSet<>();
        for (Iterator<ImmutableRepository> i = result.iterator(); i.hasNext(); )
        {
            ImmutableRepository r = i.next();
            if (CollectionUtils.isEmpty(r.getGroupRepositories().keySet()))
            {
                traverseResult.add(r);
                continue;
            }

            i.remove();
            traverseResult.addAll(collect(r, true));
        }

        return traverseResult;
    }

    private ImmutableRepository getRepository(ImmutableStorage storage,
                                              String id)
    {
        String sId = configurationManager.getStorageId(storage, id);
        String rId = configurationManager.getRepositoryId(id);

        return configurationManager.getConfiguration().getStorage(sId).getRepository(rId);
    }

}
