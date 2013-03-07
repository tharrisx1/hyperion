package com.dottydingo.hyperion.service.persistence;

import com.dottydingo.hyperion.api.ApiObject;
import com.dottydingo.hyperion.exception.NotFoundException;
import com.dottydingo.hyperion.exception.ValidationException;
import com.dottydingo.hyperion.service.configuration.ApiVersionPlugin;
import com.dottydingo.hyperion.service.context.RequestContext;
import com.dottydingo.hyperion.service.model.PersistentObject;
import com.dottydingo.hyperion.service.persistence.dao.Dao;
import com.dottydingo.hyperion.service.persistence.dao.PersistentQueryResult;
import com.dottydingo.hyperion.service.persistence.query.PredicateBuilder;
import com.dottydingo.hyperion.service.persistence.query.PredicateBuilderFactory;
import com.dottydingo.hyperion.service.persistence.query.RsqlPredicateBuilderFactory;
import com.dottydingo.hyperion.service.persistence.sort.OrderBuilder;
import com.dottydingo.hyperion.service.persistence.sort.OrderBuilderFactory;
import com.dottydingo.hyperion.service.translation.Translator;

import org.springframework.transaction.annotation.Transactional;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 */
public class JpaPersistenceOperations<C extends ApiObject, P extends PersistentObject<ID>, ID extends Serializable>
        implements PersistenceOperations<C,ID>
{

    private PredicateBuilderFactory predicateBuilderFactory;
    private OrderBuilderFactory<P> orderBuilderFactory;
    private PersistenceFilter<P> persistenceFilter = new EmptyPersistenceFilter<P>();
    private Dao<P,ID> dao;

    public void setPredicateBuilderFactory(RsqlPredicateBuilderFactory predicateBuilderFactory)
    {
        this.predicateBuilderFactory = predicateBuilderFactory;
    }

    public void setOrderBuilderFactory(OrderBuilderFactory<P> orderBuilderFactory)
    {
        this.orderBuilderFactory = orderBuilderFactory;
    }

    public void setPersistenceFilter(PersistenceFilter<P> persistenceFilter)
    {
        this.persistenceFilter = persistenceFilter;
    }

    public void setDao(Dao<P,ID> dao)
    {
        this.dao = dao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<C> findByIds(List<ID> ids, RequestContext context)
    {

        ApiVersionPlugin<C,P> apiVersionPlugin = context.getApiVersionPlugin();


        List<P> iterable = dao.findAll(context.getEntityPlugin().getEntityClass(),ids);

        List<P> result = new ArrayList<P>();
        for (P p : iterable)
        {
            if(persistenceFilter.isVisible(p,context))
                result.add(p);
        }

        return apiVersionPlugin.getTranslator().convertPersistent(result,context);
    }

    @Override
    @Transactional(readOnly = true)
    public QueryResult<C> query(String query, Integer start, Integer limit, String sort, RequestContext context)
    {
        ApiVersionPlugin<C,P> apiVersionPlugin = context.getApiVersionPlugin();

        int size = limit == null ? 500 : limit;
        int pageStart = start == null ? 1 : start;


        List<PredicateBuilder<P>> predicateBuilders = new ArrayList<PredicateBuilder<P>>();
        if(query != null && query.length() > 0)
        {
            predicateBuilders.add(predicateBuilderFactory.createPredicateBuilder(query,context.getEntityPlugin()));
        }

        PredicateBuilder<P> filter = persistenceFilter.getFilterPredicateBuilder(context);
        if(filter != null)
            predicateBuilders.add(filter);

        OrderBuilder<P> orderBuilder = orderBuilderFactory.createOrderBuilder(sort,context.getEntityPlugin());

        PersistentQueryResult<P> all = dao.query(context.getEntityPlugin().getEntityClass(),pageStart,size,orderBuilder,predicateBuilders);

        List<C> converted;
        if(all.getTotalCount() > 0)
        {
            List<P> list = all.getResults();
            converted = apiVersionPlugin.getTranslator().convertPersistent(list,context);
        }
        else
            converted = Collections.emptyList();

        QueryResult<C> queryResult= new QueryResult<C>();
        queryResult.setItems(converted);
        queryResult.setResponseCount(converted.size());
        queryResult.setTotalCount(all.getTotalCount());
        queryResult.setStart(start == null ? 1 : (start));

        return queryResult;
    }

    @Override
    @Transactional(readOnly = false)
    public C createItem(C clientObject, RequestContext context)
    {
        ApiVersionPlugin<C,P> apiVersionPlugin = context.getApiVersionPlugin();

        apiVersionPlugin.getValidator().validateCreate(clientObject);

        Translator<C,P> translator = apiVersionPlugin.getTranslator();
        P persistent = translator.convertClient(clientObject, context);

        if(!persistenceFilter.canCreate(persistent,context))
            return null;

        P saved = dao.create(persistent);
        C toReturn = translator.convertPersistent(saved,context);

        return toReturn;
    }

    @Override
    @Transactional(readOnly = false)
    public C updateItem(List<ID> ids, C item, RequestContext context)
    {
        ApiVersionPlugin<C,P> apiVersionPlugin = context.getApiVersionPlugin();

        Translator<C,P> translator = apiVersionPlugin.getTranslator();

        P existing = dao.find(context.getEntityPlugin().getEntityClass(),ids.get(0));

        if(existing == null)
            throw new NotFoundException(
                    String.format("%s with id %s was not found.",context.getEntity(),ids.get(0)));

        apiVersionPlugin.getValidator().validateUpdate(item,existing);

        if(!persistenceFilter.canUpdate(existing,context))
        {
            return null;
        }

        // todo this needs a better implementation...
        ID oldId = existing.getId();

        translator.copyClient(item, existing,context);

        if(oldId != null && !oldId.equals(existing.getId()))
            throw new ValidationException("Id in URI does not match the Id in the payload.");

        return translator.convertPersistent(dao.update(existing), context);

    }

    @Override
    @Transactional(readOnly = false)
    public int deleteItem(List<ID> ids, RequestContext context)
    {

        ApiVersionPlugin<C,P> apiVersionPlugin = context.getApiVersionPlugin();
        Iterable<P> persistentItems = dao.findAll(context.getEntityPlugin().getEntityClass(),ids);
        int deleted = 0;
        for (P item : persistentItems)
        {
            if(persistenceFilter.canDelete(item,context))
            {
                apiVersionPlugin.getValidator().validateDelete(item);
                dao.delete(item);
                deleted++;
            }
        }

        return deleted;
    }

}
