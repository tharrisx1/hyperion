package com.dottydingo.hyperion.service.configuration;

import com.dottydingo.hyperion.api.ApiObject;
import com.dottydingo.hyperion.service.endpoint.HttpMethod;
import com.dottydingo.hyperion.service.model.PersistentObject;
import com.dottydingo.hyperion.service.persistence.PersistenceOperations;
import com.dottydingo.hyperion.service.key.KeyConverter;
import com.dottydingo.hyperion.service.sort.SortBuilder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class EntityPlugin<C extends ApiObject,P extends PersistentObject,ID extends Serializable>
{
    private String endpointName;
    private KeyConverter<ID> keyConverter;
    private PersistenceOperations<C,ID> persistenceOperations;
    private ApiVersionRegistry<C,P> apiVersionRegistry;
    private Set<HttpMethod> limitMethods = new HashSet<HttpMethod>();
    private Map<String,SortBuilder> sortBuilders;
    private Class<P> entityClass;

    public String getEndpointName()
    {
        return endpointName;
    }

    public void setEndpointName(String endpointName)
    {
        this.endpointName = endpointName;
    }

    public KeyConverter<ID> getKeyConverter()
    {
        return keyConverter;
    }

    public void setKeyConverter(KeyConverter<ID> keyConverter)
    {
        this.keyConverter = keyConverter;
    }

    public PersistenceOperations<C,ID> getPersistenceOperations()
    {
        return persistenceOperations;
    }

    public void setPersistenceOperations(PersistenceOperations<C,ID> persistenceOperations)
    {
        this.persistenceOperations = persistenceOperations;
    }

    public ApiVersionRegistry<C,P> getApiVersionRegistry()
    {
        return apiVersionRegistry;
    }

    public void setApiVersionRegistry(ApiVersionRegistry<C,P> apiVersionRegistry)
    {
        this.apiVersionRegistry = apiVersionRegistry;
    }


    public void setLimitMethods(Set<HttpMethod> limitMethods)
    {
        this.limitMethods = limitMethods;
    }

    public boolean isMethodAllowed(HttpMethod method)
    {
        return limitMethods.isEmpty() || limitMethods.contains(method);
    }

    public Map<String, SortBuilder> getSortBuilders()
    {
        return sortBuilders;
    }

    public void setSortBuilders(Map<String, SortBuilder> sortBuilders)
    {
        this.sortBuilders = sortBuilders;
    }

    public Class<P> getEntityClass()
    {
        return entityClass;
    }

    public void setEntityClass(Class<P> entityClass)
    {
        this.entityClass = entityClass;
    }
}
