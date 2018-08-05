package beidanci.bo;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.dao.SortRule;
import beidanci.po.UpdateLog;

@Service("UpdateLogBO")
@Scope("prototype")
public class UpdateLogBO extends BaseBo<UpdateLog> {
	public UpdateLogBO() {
		setDao(new BaseDao<UpdateLog>() {
		});
	}

	public UpdateLog getLastestLog() {
		List<SortRule> orderBy = SortRule.makeSortRules(new String[] { "time desc" });
		baseDao.setSortRules(orderBy);
		List<UpdateLog> logs = pagedQuery(1, 1).getRows();
		return logs.size() > 0 ? logs.get(0) : null;
	}
}
