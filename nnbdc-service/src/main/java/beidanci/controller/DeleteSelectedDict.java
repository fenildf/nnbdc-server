package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
public class DeleteSelectedDict {
	@RequestMapping("/deleteSelectedDict.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public String handle(HttpServletRequest request, HttpServletResponse response)
			throws ClassNotFoundException, SQLException, NamingException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		// 获取要删除的LearningDict
		Map<String, String[]> paramMap = request.getParameterMap();
		String dictName = paramMap.get("dictName")[0];

		// 从已选择的单词书中删除
		User user = Util.getLoggedInUser();
		Global.getUserBO().deleteSelectedDict(user, dictName);

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);

		return null;
	}
}
