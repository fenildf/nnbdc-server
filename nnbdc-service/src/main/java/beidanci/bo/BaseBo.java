package beidanci.bo;

import java.beans.IntrospectionException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import beidanci.dao.BaseDao;
import beidanci.po.Po;
import beidanci.vo.PagedResults;

public abstract class BaseBo<E extends Po> {
	@Resource(name = "sessionFactory")
	protected SessionFactory sessionFactory;

	protected BaseDao<E> baseDao;

	protected void setDao(BaseDao<E> dao) {
		this.baseDao = dao;
	}

	public BaseDao<E> getDAO() {
		return baseDao;
	}

	public PagedResults<E> pagedQuery(int pageNo, int pageSize) {
		return pagedQuery(pageNo, pageSize, null, null);
	}

	/**
	 * 分页查询
	 *
	 * @param pageNo
	 * @param pageSize
	 * @param sort
	 *            排序字段名
	 * @param order
	 *            升序还是降序， 可取值 asc 或 desc
	 * @return
	 */
	public PagedResults<E> pagedQuery(int pageNo, int pageSize, String sort, String order) {
		PagedResults<E> paginationResults = baseDao.pagedQuery(getSession(), pageNo, pageSize, sort, order);
		return paginationResults;
	}

	public List<E> queryAll() {
		return queryAll(null, null);
	}

	public List<E> queryAll(String sort, String order) {
		return baseDao.queryAll(getSession(), sort, order);
	}

	public E queryUnique() {
		List<E> entities = pagedQuery(1, 2).getRows();
		if (entities.size() == 0) {
			return null;
		} else {
			assert (entities.size() == 1);
			return entities.get(0);
		}
	}

	/**
	 * 更新entity，包括entity的所有字段，即使那些值为null的字段也要更新
	 */
	public void updateEntity(E entity) throws IllegalArgumentException, IllegalAccessException {
		baseDao.updateEntity(getSession(), entity, false, true);
	}

	public void updateEntity(E entity, boolean updateUpdateTime) throws IllegalAccessException {
		baseDao.updateEntity(getSession(), entity, false, updateUpdateTime);
	}

	/**
	 * 更新entity，只更新那些值不为null的字段，值为null的字段忽略（仍保持数据库中的现有值不变）
	 *
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 */
	public void updateEntityNotNullOnly(E entity)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IntrospectionException {
		baseDao.updateEntityNotNullOnly(getSession(), entity, false, true);
	}

	public void deleteEntity(E entity) {
		baseDao.deleteEntity(getSession(), entity);
	}

	public void deleteById(Serializable id) {
		E entity = findById(id);
		deleteEntity(entity);
	}

	public void createEntity(E entity) {
		baseDao.createEntity(getSession(), entity);
	}

	public E findById(Serializable id) {
		E entity = baseDao.getEntityById(getSession(), id);
		return entity;
	}

	public void deleteEntitys(List<E> entitys) {
		for (E entity : entitys) {
			baseDao.deleteEntity(getSession(), entity);
		}
	}

	public Session getSession() {
		Session session = sessionFactory.getCurrentSession();
		return session;
	}

	/**
	 * 从session缓存中清除指定对象
	 *
	 * @param entity
	 * @throws Exception
	 */
	public void evict(E entity) {
		Session session = getSession();
		session.evict(entity);
	}
}
