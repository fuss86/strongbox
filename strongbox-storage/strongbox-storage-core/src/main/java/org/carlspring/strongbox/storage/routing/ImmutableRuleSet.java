package org.carlspring.strongbox.storage.routing;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class ImmutableRuleSet
{

    private final String groupRepository;

    private final List<ImmutableRoutingRule> routingRules;

    public ImmutableRuleSet(final RuleSet delegate)
    {
        this.groupRepository = delegate.getGroupRepository();
        this.routingRules = immuteRoutingRules(delegate.getRoutingRules());
    }

    private List<ImmutableRoutingRule> immuteRoutingRules(final List<RoutingRule> source)
    {
        return source != null ? ImmutableList.copyOf(source.stream().map(ImmutableRoutingRule::new).collect(
                Collectors.toList())) : Collections.emptyList();
    }

    public String getGroupRepository()
    {
        return groupRepository;
    }

    public List<ImmutableRoutingRule> getRoutingRules()
    {
        return routingRules;
    }
}
