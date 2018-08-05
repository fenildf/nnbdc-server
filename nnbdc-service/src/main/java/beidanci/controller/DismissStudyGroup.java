package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.util.Util;

@Controller
@RequestMapping("/dismissStudyGroup.do")
public class DismissStudyGroup {

	@RequestMapping
	public String handle(HttpServletRequest request, HttpServletResponse response, int groupId, int userId)
			throws SQLException, NamingException, ClassNotFoundException, IOException, IllegalArgumentException,
			IllegalAccessException {
		Util.setPageNoCache(response);

		// 解散小组
		String errMsg = Global.getStudyGroupBO().dismissStudyGroup(groupId, userId);

		// Send result back to client.
		Util.sendBooleanResponse(errMsg == null, errMsg, null, response);

		return null;
	}

}
