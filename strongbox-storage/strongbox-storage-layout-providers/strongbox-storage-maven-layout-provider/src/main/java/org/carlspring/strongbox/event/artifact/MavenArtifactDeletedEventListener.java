package org.carlspring.strongbox.event.artifact;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;

import com.google.common.base.Throwables;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenArtifactDeletedEventListener
        extends BaseMavenArtifactEventListener
{

    @Override
    public void handle(final ArtifactEvent<RepositoryPath> event)
    {
        final ImmutableRepository repository = getRepository(event);

        if (!Maven2LayoutProvider.ALIAS.equals(repository.getLayout()))
        {
            return;
        }

        if (event.getType() != ArtifactEventTypeEnum.EVENT_ARTIFACT_PATH_DELETED.getType())
        {
            return;
        }

        try
        {
            mavenMetadataGroupRepositoryComponent.cleanupGroupsContaining(event.getPath());
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
    }
}
