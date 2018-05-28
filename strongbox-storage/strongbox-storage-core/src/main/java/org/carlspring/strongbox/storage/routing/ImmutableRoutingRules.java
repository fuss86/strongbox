package org.carlspring.strongbox.storage.routing;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import static java.util.stream.Collectors.toMap;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class ImmutableRoutingRules
{

    public static final String WILDCARD = RoutingRules.WILDCARD;

    private final Map<String, ImmutableRuleSet> accepted;

    private final Map<String, ImmutableRuleSet> denied;

    public ImmutableRoutingRules(final RoutingRules delegate)
    {
        this.accepted = immuteRuleSet(delegate.getAccepted());
        this.denied = immuteRuleSet(delegate.getDenied());
    }

    private Map<String, ImmutableRuleSet> immuteRuleSet(final Map<String, RuleSet> source)
    {
        return source != null ? ImmutableMap.copyOf(source.entrySet().stream().collect(
                toMap(Map.Entry::getKey, e -> new ImmutableRuleSet(e.getValue())))) : Collections.emptyMap();
    }

    public Map<String, ImmutableRuleSet> getAccepted()
    {
        return accepted;
    }

    public Map<String, ImmutableRuleSet> getDenied()
    {
        return denied;
    }

    public ImmutableRuleSet getWildcardAcceptedRules()
    {
        return accepted.get(WILDCARD);
    }

    public ImmutableRuleSet getWildcardDeniedRules()
    {
        return denied.get(WILDCARD);
    }

    public ImmutableRuleSet getAcceptRules(String groupRepositoryId)
    {
        return accepted.get(groupRepositoryId);
    }

    public ImmutableRuleSet getWildcardDenyRules()
    {
        return getDenyRules(WILDCARD);
    }

    public ImmutableRuleSet getDenyRules(String groupRepositoryId)
    {
        return denied.get(groupRepositoryId);
    }


}
