package com.hello2morrow.sonargraph.integration.access.model.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.hello2morrow.sonargraph.integration.access.model.IElement;
import com.hello2morrow.sonargraph.integration.access.model.IElementContainer;
import com.hello2morrow.sonargraph.integration.access.model.IMetricId;
import com.hello2morrow.sonargraph.integration.access.model.IMetricLevel;
import com.hello2morrow.sonargraph.integration.access.model.IMetricValue;
import com.hello2morrow.sonargraph.integration.access.model.INamedElement;

public abstract class NamedElementContainerImpl extends NamedElementImpl implements IElementContainer
{
    private MetricsAccessImpl m_metricsAccess;
    private ElementRegistryImpl m_elementRegistry;
    private final Map<String, Map<String, INamedElement>> m_elementMap = new HashMap<>();
    private final Map<IMetricLevel, Map<IMetricId, Map<INamedElement, IMetricValue>>> m_metricValues = new HashMap<>();

    public NamedElementContainerImpl(final String kind, final String presentationKind, final String name, final String presentationName,
            final String fqName, final String description)
    {
        super(kind, presentationKind, name, presentationName, fqName, -1, description);

    }

    protected final void setMetricsAccess(final MetricsAccessImpl metricsAccess)
    {
        assert metricsAccess != null : "Parameter 'metricsAccess' of method 'setMetricsAccess' must not be null";
        assert m_metricsAccess == null : "m_metricsAccess must be null!";
        m_metricsAccess = metricsAccess;
    }

    protected final MetricsAccessImpl getMetricsAccess()
    {
        assert m_metricsAccess != null : "'setMetricsAccess() must be called first!";
        return m_metricsAccess;
    }

    protected final void setElementRegistry(final ElementRegistryImpl elementRegistry)
    {
        assert elementRegistry != null : "Parameter 'elementRegistry' of method 'setElementRegistry' must not be null";
        assert m_elementRegistry == null : "m_elementRegistry must be null!";
        m_elementRegistry = elementRegistry;
    }

    protected final ElementRegistryImpl getElementRegistry()
    {
        assert m_elementRegistry != null : "'setElementRegistry() must be called first!";
        return m_elementRegistry;
    }

    public final boolean addElement(final INamedElement element)
    {
        assert element != null : "Parameter 'element' of method 'addElement' must not be null";

        if (!acceptElementKind(element.getKind()))
        {
            return false;
        }
        final Map<String, INamedElement> elementsOfKind;
        if (!m_elementMap.containsKey(element.getKind()))
        {
            elementsOfKind = new HashMap<>();
            m_elementMap.put(element.getKind(), elementsOfKind);
        }
        else
        {
            elementsOfKind = m_elementMap.get(element.getKind());
        }

        assert !elementsOfKind.containsKey(element.getFqName()) : "Element '" + element.getFqName() + "' has already been added";
        elementsOfKind.put(element.getFqName(), element);

        getElementRegistry().addElement(element);
        return true;
    }

    public boolean hasElement(final IElement element)
    {
        assert element != null : "Parameter 'element' of method 'hasElement' must not be null";

        //TODO: remove this check and the cast, once the type hierarchy is refactored.
        if (!(element instanceof INamedElement))
        {
            return false;
        }

        final INamedElement fqNamedElement = (INamedElement) element;

        if (!m_elementMap.containsKey(fqNamedElement.getKind()))
        {
            return false;
        }

        return m_elementMap.get(fqNamedElement.getKind()).containsKey(fqNamedElement.getFqName());
    }

    /* (non-Javadoc)
     * @see com.hello2morrow.sonargraph.integration.access.model.IElementContainer#getElements(java.lang.String)
     */
    @Override
    public final Map<String, INamedElement> getElements(final String elementKind)
    {
        assert elementKind != null && elementKind.length() > 0 : "Parameter 'elementKind' of method 'getElements' must not be empty";
        if (m_elementMap.containsKey(elementKind))
        {
            return Collections.unmodifiableMap(m_elementMap.get(elementKind));
        }

        return Collections.emptyMap();
    }

    /* (non-Javadoc)
     * @see com.hello2morrow.sonargraph.integration.access.model.IElementContainer#getElementKinds()
     */
    @Override
    public final Set<String> getElementKinds()
    {
        return Collections.unmodifiableSet(m_elementMap.keySet());
    }

    public final void addMetricValueForElement(final IMetricValue value, final INamedElement element)
    {
        assert value != null : "Parameter 'value' of method 'addMetricValue' must not be null";
        final IMetricId metricId = value.getId();
        assert m_metricsAccess.getMetricIds().containsKey(metricId.getName()) : "MetricId '" + metricId.getName() + "'has not been added";
        assert element != null : "Parameter 'element' of method 'addMetricValueForElement' must not be null";

        final IMetricLevel level = value.getLevel();
        assert m_metricsAccess.getMetricLevels().containsKey(level.getName()) : "Level '" + level.getName() + "' has not been added";

        final Map<IMetricId, Map<INamedElement, IMetricValue>> valuesOfLevel;
        if (!m_metricValues.containsKey(level))
        {
            valuesOfLevel = new HashMap<>();
            m_metricValues.put(level, valuesOfLevel);
        }
        else
        {
            valuesOfLevel = m_metricValues.get(level);
        }

        final Map<INamedElement, IMetricValue> valuesForMetric;
        if (!valuesOfLevel.containsKey(metricId))
        {
            valuesForMetric = new HashMap<>();
            valuesOfLevel.put(metricId, valuesForMetric);
        }
        else
        {
            valuesForMetric = valuesOfLevel.get(metricId);
        }

        valuesForMetric.put(element, value);
    }

    public final Optional<IMetricValue> getMetricValueForElement(final IMetricId metricId, final IMetricLevel metricLevel, final String elementName)
    {
        assert metricId != null : "Parameter 'metricId' of method 'getMetricValueForElement' must not be null";
        assert elementName != null && elementName.length() > 0 : "Parameter 'elementName' of method 'getMetricValueForElement' must not be empty";

        if (!m_metricValues.containsKey(metricLevel))
        {
            return Optional.empty();
        }

        final Map<IMetricId, Map<INamedElement, IMetricValue>> metricIdsOfLevel = m_metricValues.get(metricLevel);
        if (!metricIdsOfLevel.containsKey(metricId))
        {
            return Optional.empty();
        }

        final Optional<INamedElement> element = getElementRegistry().getElement(elementName);
        if (!element.isPresent())
        {
            return Optional.empty();
        }

        final IElement namedElement = element.get();
        final Map<INamedElement, IMetricValue> valuesForMetricId = metricIdsOfLevel.get(metricId);
        if (!valuesForMetricId.containsKey(namedElement))
        {
            return Optional.empty();
        }

        return Optional.of(valuesForMetricId.get(namedElement));
    }

    public final void addMetricLevel(final IMetricLevel level)
    {
        assert level != null : "Parameter 'level' of method 'addMetricLevel' must not be null";
        m_metricsAccess.addMetricLevel(level);
    }

    public final Map<String, IMetricLevel> getAllMetricLevels()
    {
        return m_metricsAccess.getMetricLevels();
    }

    public abstract Map<String, IMetricLevel> getMetricLevels();

    public List<IMetricId> getMetricIdsForLevel(final IMetricLevel metricLevel)
    {
        assert metricLevel != null : "Parameter 'metricLevel' of method 'getMetricIdsForLevel' must not be null";
        if (!m_metricValues.containsKey(metricLevel))
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(m_metricValues.get(metricLevel).keySet()));
    }

    public Map<INamedElement, IMetricValue> getMetricValues(final String metricLevel, final String metricId)
    {
        assert metricLevel != null && metricLevel.length() > 0 : "Parameter 'metricLevel' of method 'getMetricValues' must not be empty";
        assert metricId != null && metricId.length() > 0 : "Parameter 'metricId' of method 'getMetricValues' must not be empty";

        final Map<String, IMetricLevel> metricLevels = getAllMetricLevels();
        if (!metricLevels.containsKey(metricLevel))
        {
            return Collections.emptyMap();
        }

        final IMetricLevel level = metricLevels.get(metricLevel);
        final Map<IMetricId, Map<INamedElement, IMetricValue>> metricIdValueMap = m_metricValues.get(level);

        final Optional<IMetricId> optionalId = metricIdValueMap.keySet().stream().filter((final IMetricId id) -> id.getName().equals(metricId))
                .findAny();

        if (!optionalId.isPresent())
        {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(metricIdValueMap.get(optionalId.get()));
    }

    public Map<INamedElement, IMetricValue> getMetricValues(final IMetricLevel metricLevel, final IMetricId metricId)
    {
        assert metricLevel != null : "Parameter 'metricLevel' of method 'getMetricValues' must not be null";
        assert metricId != null : "Parameter 'metricId' of method 'getMetricValues' must not be null";

        if (!m_metricValues.containsKey(metricLevel))
        {
            return Collections.emptyMap();
        }

        final Map<IMetricId, Map<INamedElement, IMetricValue>> metricIdValueMap = m_metricValues.get(metricLevel);
        if (!metricIdValueMap.containsKey(metricId))
        {
            return Collections.emptyMap();
        }

        return Collections.unmodifiableMap(metricIdValueMap.get(metricId));
    }

    protected abstract boolean acceptElementKind(String elementKind);

}
