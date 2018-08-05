package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.UserSnapshotDaily;

@Service("UserSnapshotDailyBO")
@Scope("prototype")
public class UserSnapshotDailyBO extends BaseBo<UserSnapshotDaily> {
	public UserSnapshotDailyBO() {
		setDao(new BaseDao<UserSnapshotDaily>() {
		});
	}
}
