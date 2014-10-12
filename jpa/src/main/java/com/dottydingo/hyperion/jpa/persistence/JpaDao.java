package com.dottydingo.hyperion.jpa.persistence;

import com.dottydingo.hyperion.core.model.PersistentHistoryEntry;
import com.dottydingo.hyperion.jpa.persistence.query.JpaPersistentQueryBuilder;
import com.dottydingo.hyperion.jpa.persistence.sort.JpaPersistentOrderBuilder;
import com.dottydingo.hyperion.core.model.PersistentObject;
import com.dottydingo.hyperion.core.persistence.dao.Dao;
import com.dottydingo.hyperion.core.persistence.dao.PersistentQueryResult;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 */
public class JpaDao<P extends PersistentObject,ID extends Serializable>
        implements Dao<P,ID,JpaPersistentQueryBuilder,JpaPersistentOrderBuilder>
{
    @PersistenceContext(unitName = "hyperionEntityManager")
    protected EntityManager em;

    public void setEm(EntityManager em)
    {
        this.em = em;
    }

    @Override
    public List<P> findAll(Class<P> entityClass, List<ID> ids)
    {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<P> criteriaQuery = cb.createQuery(entityClass);
        Root<P> root = criteriaQuery.from(entityClass);
        Path<?> id = root.get("id");
        Predicate predicate = id.in(ids);
        criteriaQuery.where(predicate).orderBy(cb.asc(id));

        TypedQuery<P> query = em.createQuery(criteriaQuery);
        return query.getResultList();
    }

    @Override
    public PersistentQueryResult<P> query(Class<P> entityClass, Integer start, Integer limit,
                                          JpaPersistentOrderBuilder orderBuilder,
                                          List<JpaPersistentQueryBuilder> predicateBuilders)
    {

        PersistentQueryResult<P> result = new PersistentQueryResult<P>();

        Long totalCount = getCount(entityClass,predicateBuilders);
        result.setTotalCount(totalCount);
        if(totalCount == 0)
            return result;

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<P> criteriaQuery = cb.createQuery(entityClass);
        Root<P> root = criteriaQuery.from(entityClass);

        Predicate[] predicateArray = null;
        if(predicateBuilders.size() > 0)
        {
            List<Predicate> predicates = new ArrayList<Predicate>();
            for (JpaPersistentQueryBuilder query : predicateBuilders)
            {
                predicates.add(query.buildPredicate(root,criteriaQuery,cb));
            }

            predicateArray = predicates.toArray(new Predicate[predicates.size()]);
        }


        if(predicateArray != null)
            criteriaQuery.where(predicateArray);

        if(orderBuilder != null)
        {
            List<Order> orders = new ArrayList<Order>();
            List<Order> orderList = orderBuilder.buildOrders(root, cb);
            orders.addAll(orderList);
            if(orders.size() > 0)
                criteriaQuery.orderBy(orders.toArray(new Order[orders.size()]));
        }


        TypedQuery<P> query = em.createQuery(criteriaQuery);
        if(start != null)
            query.setFirstResult(start);
        if(limit != null)
            query.setMaxResults(limit);

        result.setResults(query.getResultList());
        return result;
    }

    protected Long getCount(Class<P> entityClass,List<JpaPersistentQueryBuilder> predicateBuilders)
    {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<P> root = cq.from(entityClass);

        Predicate[] predicateArray = null;
        if(predicateBuilders.size() > 0)
        {
            List<Predicate> predicates = new ArrayList<Predicate>();
            for (JpaPersistentQueryBuilder query : predicateBuilders)
            {
                predicates.add(query.buildPredicate(root,cq,cb));
            }

            predicateArray = predicates.toArray(new Predicate[predicates.size()]);
        }

        if(predicateArray != null)
            cq.where(predicateArray);

        if(cq.isDistinct())
        {
            // we need to set this to a distinct count
            cq.distinct(false);
            cq.select(cb.countDistinct(root));
        }
        else
            cq.select(cb.count(root));

        return em.createQuery(cq).getSingleResult();
    }

    @Override
    public P find(Class<P> entityClass, ID id)
    {
        return em.find(entityClass,id);
    }

    @Override
    public P create(P entity)
    {
        em.persist(entity);
        em.flush();
        em.refresh(entity);
        return entity;
    }

    @Override
    public P update(P entity)
    {
        em.merge(entity);
        em.flush();
        em.refresh(entity);
        return entity;
    }

    @Override
    public void delete(P entity)
    {
        em.remove(entity);
    }

    @Override
    public P reset(P entity)
    {
        em.refresh(entity);
        return entity;
    }

    @Override
    public <H extends PersistentHistoryEntry<ID>> PersistentQueryResult<H> getHistory(Class<H> historyType, String entityType,
                                                                         ID entityId, Integer start, Integer limit)
    {
        PersistentQueryResult<H> result = new PersistentQueryResult<H>();
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<H> countRoot = countQuery.from(historyType);

        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.equal(countRoot.get("entityType"),entityType),
                cb.equal(countRoot.get("entityId"),entityId));

        Long total = em.createQuery(countQuery).getSingleResult();
        result.setTotalCount(total);
        if(total > 0)
        {
            CriteriaQuery<H> criteriaQuery = cb.createQuery(historyType);
            Root<H> root = criteriaQuery.from(historyType);

            criteriaQuery.where(cb.equal(root.get("entityType"),entityType),
                    cb.equal(root.get("entityId"),entityId));

            criteriaQuery.orderBy(cb.asc(root.get("id")));

            TypedQuery<H> query = em.createQuery(criteriaQuery);
            if(start != null)
                query.setFirstResult(start - 1);
            if(limit != null)
                query.setMaxResults(limit);

            result.setResults(query.getResultList());
        }
        return result;
    }

    @Override
    public <H extends PersistentHistoryEntry<ID>> void saveHistory(H entry)
    {
        em.persist(entry);
    }

    @Override
    public Date getCurrentTimestamp()
    {
        Query query = em.createNativeQuery("select CURRENT_TIMESTAMP");
        return (Date) query.getSingleResult();
    }
}
