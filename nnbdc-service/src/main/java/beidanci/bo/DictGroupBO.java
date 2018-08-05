package beidanci.bo;

import java.util.List;

import org.hibernate.Query;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.DictGroup;

@Service("DictGroupBO")
@Scope("prototype")
public class DictGroupBO extends BaseBo<DictGroup> {
	public DictGroupBO() {
		setDao(new BaseDao<DictGroup>() {
		});
	}

	// 获取所有单词书分组
	@SuppressWarnings("unchecked")
	public List<DictGroup> getAllDictGroups() {
		String hql = "from DictGroup order by displayIndex asc";
		Query query = getSession().createQuery(hql);
		query.setCacheable(true);
		return (List<DictGroup>) query.list();

	}

}
