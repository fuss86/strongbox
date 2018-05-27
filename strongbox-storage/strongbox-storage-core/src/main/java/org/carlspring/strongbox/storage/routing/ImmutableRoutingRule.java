package org.carlspring.strongbox.storage.routing;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableRoutingRule
{

    private final String pattern;

    private final Pattern regex;

    private final Set<String> repositories;

    public ImmutableRoutingRule(final RoutingRule delegate)
    {
        this.pattern = delegate.getPattern();
        this.regex = delegate.getRegex();
        this.repositories = immuteRepositories(delegate.getRepositories());
    }

    private Set<String> immuteRepositories(Set<String> source)
    {
        return source != null ? ImmutableSet.copyOf(source) : Collections.emptySet();
    }

    public String getPattern()
    {
        return pattern;
    }

    public Pattern getRegex()
    {
        return regex;
    }

    public Set<String> getRepositories()
    {
        return repositories;
    }
}
