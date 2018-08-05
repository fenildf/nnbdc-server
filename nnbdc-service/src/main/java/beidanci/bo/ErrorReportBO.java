package beidanci.bo;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.dao.BaseDao;
import beidanci.po.ErrorReport;

@Service("ErrorReportBO")
@Scope("prototype")
public class ErrorReportBO extends BaseBo<ErrorReport> {
	public ErrorReportBO() {
		setDao(new BaseDao<ErrorReport>() {
		});
	}

	public List<ErrorReport> findByWordSpell(String spell) {
		ErrorReport exam = new ErrorReport();
		exam.setWord(spell);
		baseDao.setPreciseEntity(exam);
		return baseDao.queryAll(getSession(), null, null);
	}
}
