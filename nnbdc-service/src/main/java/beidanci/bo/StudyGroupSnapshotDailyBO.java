package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.StudyGroupSnapshotDaily;

@Service("StudyGroupSnapshotDailyBO")
@Scope("prototype")
public class StudyGroupSnapshotDailyBO extends BaseBo<StudyGroupSnapshotDaily> {
	public StudyGroupSnapshotDailyBO() {
		setDao(new BaseDao<StudyGroupSnapshotDaily>() {
		});
	}
}
