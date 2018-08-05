package beidanci.bo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.StudyGroupGrade;

@Service("StudyGroupGradeBO")
@Scope("prototype")
public class StudyGroupGradeBO extends BaseBo<StudyGroupGrade> {
	public StudyGroupGradeBO() {
		setDao(new BaseDao<StudyGroupGrade>() {
		});
	}
}
