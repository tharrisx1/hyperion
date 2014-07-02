package com.dottydingo.hyperion.core.configuration;

import com.dottydingo.hyperion.api.ApiObject;
import com.dottydingo.hyperion.api.Endpoint;
import com.dottydingo.hyperion.core.persistence.CreateKeyProcessor;
import com.dottydingo.hyperion.core.registry.ApiVersionPlugin;
import com.dottydingo.hyperion.core.registry.EntityQueryBuilder;
import com.dottydingo.hyperion.core.registry.EntitySortBuilder;
import com.dottydingo.hyperion.core.translation.Translator;
import com.dottydingo.hyperion.core.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 */
public class ApiVersionPluginBuilder
{
    private static Logger logger = LoggerFactory.getLogger(ApiVersionPluginBuilder.class);

    protected Class<? extends ApiObject> apiClass;
    protected Translator translator;
    protected Validator validator;

    protected Map<String,EntitySortBuilder> sortBuilders;
    protected Map<String,EntityQueryBuilder> queryBuilders;
    protected CreateKeyProcessor createKeyProcessor;

    public ApiVersionPlugin build() throws Exception
    {
        validateRequired();

        ApiVersionPlugin plugin = new ApiVersionPlugin();
        plugin.setVersion(getVersion(apiClass));
        plugin.setApiClass(apiClass);
        plugin.setTranslator(translator);
        plugin.setValidator(validator);
        plugin.setCreateKeyProcessor(createKeyProcessor);

        plugin.setSortBuilders(getSortBuilder(sortBuilders));
        if(plugin.getSortBuilders() == null)
            logger.warn("No sortBuilders specified for apiClass: {}",apiClass);

        plugin.setQueryBuilders(getQueryBuilders(queryBuilders));
        if(plugin.getQueryBuilders() == null)
            logger.warn("No queryBuilders specified for apiClass: {}",apiClass);

        return plugin;
    }

    protected void validateRequired()
    {
        if(apiClass == null)
            throw new RuntimeException("apiClass must be specified");

        if(translator == null)
            throw new RuntimeException("translator must be specified");

        if(validator == null)
            throw new RuntimeException("validator must be specified");
    }

    protected Map<String,EntitySortBuilder> getSortBuilder(Map<String,EntitySortBuilder> sortBuilders)
    {
        return sortBuilders;
    }

    protected Map<String,EntityQueryBuilder> getQueryBuilders(Map<String,EntityQueryBuilder> queryBuilders)
    {
        return queryBuilders;
    }

    protected int getVersion(Class<? extends ApiObject> apiClass)
    {
        Endpoint endpoint = apiClass.getAnnotation(Endpoint.class);
        if(endpoint == null)
            throw new RuntimeException(String.format("Missing @Endpoint annotation in apiClass %s",apiClass));

        return endpoint.version();
    }


    public void setApiClass(Class<? extends ApiObject> apiClass)
    {
        this.apiClass = apiClass;
    }

    public void setTranslator(Translator translator)
    {
        this.translator = translator;
    }

    public void setValidator(Validator validator)
    {
        this.validator = validator;
    }

    public void setSortBuilders(Map<String, EntitySortBuilder> sortBuilders)
    {
        this.sortBuilders = sortBuilders;
    }

    public void setQueryBuilders(Map<String, EntityQueryBuilder> queryBuilders)
    {
        this.queryBuilders = queryBuilders;
    }

    public void setCreateKeyProcessor(CreateKeyProcessor createKeyProcessor)
    {
        this.createKeyProcessor = createKeyProcessor;
    }
}
