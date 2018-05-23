package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.ImmutableCustomRepositoryConfiguration;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "maven-repository-configuration")
public class MavenRepositoryConfiguration
        extends CustomRepositoryConfiguration
{

    @XmlAttribute(name = "indexing-enabled")
    private boolean indexingEnabled = false;

    @XmlAttribute(name = "indexing-class-names-enabled")
    private boolean indexingClassNamesEnabled = true;


    public MavenRepositoryConfiguration()
    {
    }

    public boolean isIndexingEnabled()
    {
        return indexingEnabled;
    }

    public void setIndexingEnabled(boolean indexingEnabled)
    {
        this.indexingEnabled = indexingEnabled;
    }

    public boolean isIndexingClassNamesEnabled()
    {
        return indexingClassNamesEnabled;
    }

    public void setIndexingClassNamesEnabled(final boolean indexingClassNamesEnabled)
    {
        this.indexingClassNamesEnabled = indexingClassNamesEnabled;
    }

    @Override
    public ImmutableCustomRepositoryConfiguration getImmutable()
    {
        return new ImmutableMavenRepositoryConfiguration(this);
    }
}
