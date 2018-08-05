package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.Global;
import beidanci.bo.ErrorReportBO;
import beidanci.po.ErrorReport;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/saveErrorReport.do")
public class SaveErrorReport {

	@RequestMapping(method = RequestMethod.POST)
	public String handle(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		String spell = params.get("word")[0];
		String content = params.get("content")[0];

		if (content.trim().length() == 0) {
			Util.sendBooleanResponse(false, "内容不能为空", null, response);
			return null;
		}

		// 保存单词报错内容
		ErrorReportBO errorReportDAO = Global.getErrorReportBO();
		ErrorReport errorReport = new ErrorReport();
		errorReport.setWord(spell);
		errorReport.setContent(content);
		errorReport.setCreateTime(new Timestamp(new Date().getTime()));
		errorReport.setUser(user);
		errorReport.setFixed(false);
		errorReportDAO.createEntity(errorReport);

		// Send result back to client.
		Util.sendBooleanResponse(true, String.valueOf(errorReport.getId()), content, response);

		return null;
	}

}
