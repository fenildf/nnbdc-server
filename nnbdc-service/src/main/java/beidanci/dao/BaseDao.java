package beidanci.dao;

import static org.hibernate.type.StringType.INSTANCE;

import java.beans.IntrospectionException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.*;

import beidanci.po.Po;
import beidanci.util.BeanUtils;
import beidanci.util.ReflectionUtil;
import beidanci.util.StringUtil;
import beidanci.util.Utils;
import beidanci.vo.PagedResults;

/**
 * DAO基类，支持基本的CRUD、分页、模糊查询 <br>
 * 注意：本类有状态，不要以单例使用
 *
 * @param <E>
 * @author MaYubing
 */
public abstract class BaseDao<E extends Po> {

	private final ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();

	@SuppressWarnings("unchecked")
	private final Class<E> valueClass = (Class<E>) (parameterizedType).getActualTypeArguments()[0];

	private static final String ALIAS = "{alias}.";
	private static final String LIKE = " like (?)";
	private static final String WILDCARD_ANY = "%";

	/**
	 * 用于本地SQL查询的SQL语句（注意不是HQL）
	 */
	private String sqlRestriction;

	/**
	 * 用于精确查询的示例对象
	 */
	private E preciseEntity;

	/**
	 * 用于模糊查询的示例对象
	 */
	private E fuzzyEntity;

	/**
	 * 进行“或”运算精确查询的示例对象
	 */
	private List<E> preciseOrEntities;

	/**
	 * 进行“或”运算模糊查询的示例对象
	 */
	private List<E> fuzzyOrEntities;

	private List<SortRule> sortRules;

	public void setSqlRestriction(String sqlRestriction) {
		this.sqlRestriction = sqlRestriction;
	}

	public void setPreciseEntity(E preciseEntity) {
		this.preciseEntity = preciseEntity;
	}

	public void setPreciseOrEntities(List<E> preciseOrEntities) {
		this.preciseOrEntities = preciseOrEntities;
	}

	public void setFuzzyEntity(E fuzzyEntity) {
		this.fuzzyEntity = fuzzyEntity;
	}

	public void setFuzzyOrEntities(List<E> fuzzyOrEntities) {
		this.fuzzyOrEntities = fuzzyOrEntities;
	}

	public void setSortRules(List<SortRule> orderSet) {
		this.sortRules = orderSet;
	}

	public void createEntity(Session session, E entity) {
		Date now = new Date();
		entity.setCreateTime(now);
		entity.setUpdateTime(now);
		session.persist(entity);
	}

	public List<E> queryAll(Session session, String sort, String order) {
		return pagedQuery(session, 1, Integer.MAX_VALUE, sort, order).getRows();
	}

	public PagedResults<E> pagedQuery(Session session, int pageNo, int pageSize, String sort, String order) {
		assert (pageNo >= 0 && pageSize >= 1);

		if (!StringUtils.isEmpty(sort)) {
			if (sortRules == null) {
				sortRules = new ArrayList<>();
			}
			SortRule sortRule = SortRule.makeSortRule(sort + " " + (StringUtils.isEmpty(order) ? "asc" : order));
			sortRules.add(sortRule);
		}

		PagedResults<E> pagedResults = new PagedResults<E>();

		Criteria criteria = session.createCriteria(valueClass);

		if (null != sqlRestriction) {
			criteria.add(Restrictions.sqlRestriction(sqlRestriction));
		}
		if (null != preciseEntity) {
			addPreciseRestrictions(criteria);
		}
		if (null != preciseOrEntities) {
			addPreciseOrRestrictions(criteria);
		}
		if (null != fuzzyEntity) {
			addFuzzyRestrictions(criteria);
		}
		if (null != fuzzyOrEntities) {
			addFuzzyOrRestrictions(criteria);
		}

		Integer total = ((Long) criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();
		pagedResults.setTotal(total);
		criteria.setProjection(null);

		if (null != sortRules && !sortRules.isEmpty()) {
			for (SortRule sortRule : sortRules) {
				String fieldName = sortRule.getFieldName();

				// 属性名中可能含有.号（即关联对象的属性），需要创建Alias才能正常工作
				String[] parts = fieldName.split("\\.");
				if (parts.length >= 2) {
					for (int i = 0; i <= parts.length - 2; i++) {
						criteria.createAlias(parts[i], parts[i]);
					}
					// criteria.setResultTransformer(Criteria.ROOT_ENTITY);
				}

				if (sortRule.getAsc()) {
					criteria.addOrder(Order.asc(fieldName));
				} else {
					criteria.addOrder(Order.desc(fieldName));
				}
			}
		}

		if (pageSize != Integer.MAX_VALUE) {
			int offset = (pageSize * (pageNo - 1));
			offset = (0 < offset ? offset : 0);
			criteria.setFirstResult(offset);
			criteria.setMaxResults(pageSize);
		}

		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		List<E> entities = Utils.abstractEntityFromList(criteria.list(), valueClass);
		pagedResults.setRows(entities);

		return pagedResults;
	}

	/**
	 * 更新entity，包括entity的所有字段，即使那些值为null的字段也要更新
	 *
	 * @param session
	 * @param entity
	 * @param flush
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public void updateEntity(Session session, E entity, boolean flush, boolean updateUpdateTime)
			throws IllegalArgumentException, IllegalAccessException {
		if (entity.getCreateTime() == null) {
			E obj = (E) session.load(entity.getClass(), BeanUtils.getIdOfPo(entity));
			entity.setCreateTime(obj.getCreateTime());
		}
		if (updateUpdateTime) {
			entity.setUpdateTime(new Date());
		}
		session.merge(entity);
		if (flush) {
			session.flush();
		}
	}

	/**
	 * 更新entity，只更新那些值不为null的字段，值为null的字段忽略（仍保持数据库中的现有值不变）
	 *
	 * @param session
	 * @param entity
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 */
	public void updateEntityNotNullOnly(Session session, E entity, boolean flush, boolean updateUpdateTime)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IntrospectionException {
		evict(session, entity);
		E existing = getEntityById(session, BeanUtils.getIdOfPo(entity));
		List<Field> fields = BeanUtils.getFields(entity.getClass(), true);
		String fieldName = null;
		Object fieldValue = null;
		for (Field field : fields) {
			fieldName = field.getName();
			fieldValue = ReflectionUtil.getFieldValue(entity, fieldName);
			if (fieldValue != null) {
				ReflectionUtil.setFieldValue(existing, fieldName, fieldValue);
			}
		}

		updateEntity(session, existing, flush, updateUpdateTime);
	}

	public void deleteEntity(Session session, E entity) {
		session.delete(entity);
	}

	public E getEntityById(Session session, Serializable id) {
		return (E) session.get(valueClass, id);
	}

	private void addPreciseRestrictions(Criteria criteria) {
		List<Field> fields = BeanUtils.getFields(valueClass, true);
		String fieldName = null;
		Object fieldValue = null;
		for (Field field : fields) {
			fieldName = field.getName();
			fieldValue = ReflectionUtil.getFieldValue(preciseEntity, fieldName);
			if (null != fieldValue) {
				criteria.add(Restrictions.eq(fieldName, fieldValue));
			}
		}
	}

	private void addPreciseOrRestrictions(Criteria criteria) {
		List<Field> fields = BeanUtils.getFields(valueClass, true);
		String fieldName = null;
		Object fieldValue = null;
		Disjunction disjunction = Restrictions.disjunction();
		for (Field field : fields) {
			fieldName = field.getName();
			for (int i = 0; i < preciseOrEntities.size(); i++) {
				E preciseOrEntity = preciseOrEntities.get(i);
				fieldValue = ReflectionUtil.getFieldValue(preciseOrEntity, fieldName);
				if (null != fieldValue) {
					disjunction.add(Restrictions.eq(fieldName, fieldValue));
				}
			}
		}
		criteria.add(disjunction);
	}

	private void addFuzzyRestrictions(Criteria criteria) {
		Field[] fields = valueClass.getDeclaredFields();
		String fieldName = null;
		Object fieldValue = null;
		Column column = null;
		StringBuffer criteriaSQL = null;
		StringBuffer criteriaValue = null;
		for (Field field : fields) {
			fieldName = field.getName();
			fieldValue = ReflectionUtil.getFieldValue(fuzzyEntity, fieldName);
			column = field.getAnnotation(Column.class);
			if (null != fieldValue && null != column && StringUtil.isValuableStr(column.name())) {
				if (field.getType().equals(Boolean.class)) {
					criteria.add(Restrictions.eq(fieldName, fieldValue));
				} else {
					criteriaSQL = new StringBuffer().append(ALIAS).append(column.name()).append(LIKE);
					criteriaValue = new StringBuffer().append(WILDCARD_ANY).append(fieldValue).append(WILDCARD_ANY);
					criteria.add(
							Restrictions.sqlRestriction(criteriaSQL.toString(), criteriaValue.toString(), INSTANCE));
				}
			}
		}
	}

	private void addFuzzyOrRestrictions(Criteria criteria) {
		List<Field> fields = BeanUtils.getFields(valueClass, true);
		String fieldName = null;
		Object fieldValue = null;
		Column column = null;
		StringBuffer criteriaSQL = null;
		StringBuffer criteriaValue = null;
		Disjunction disjunction = Restrictions.disjunction();
		for (Field field : fields) {
			fieldName = field.getName();
			for (int i = 0; i < fuzzyOrEntities.size(); i++) {
				E fuzzyOrEntity = fuzzyOrEntities.get(i);
				fieldValue = ReflectionUtil.getFieldValue(fuzzyOrEntity, fieldName);
				column = field.getAnnotation(Column.class);
				if (null != fieldValue && null != column && StringUtil.isValuableStr(column.name())) {
					if (field.getType().equals(Boolean.class)) {
						disjunction.add(Restrictions.eq(fieldName, fieldValue));
					} else {
						criteriaSQL = new StringBuffer().append(ALIAS).append(column.name()).append(LIKE);
						criteriaValue = new StringBuffer().append(WILDCARD_ANY).append(fieldValue).append(WILDCARD_ANY);
						disjunction.add(Restrictions.sqlRestriction(criteriaSQL.toString(), criteriaValue.toString(),
								INSTANCE));
					}
				}
			}
		}
		criteria.add(disjunction);
	}

	/**
	 * 从session缓存中清除指定对象
	 *
	 * @param entity
	 * @throws Exception
	 */
	public void evict(Session session, E entity) {
		session.evict(entity);
	}

	public List<SortRule> getSortRules() {
		return sortRules;
	}

}
