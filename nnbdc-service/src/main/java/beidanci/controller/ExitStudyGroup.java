package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.po.StudyGroup;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/exitStudyGroup.do")
public class ExitStudyGroup {
	private static final Logger log = LoggerFactory.getLogger(ExitStudyGroup.class);

	@RequestMapping
	public String handle(HttpServletRequest request, HttpServletResponse response, int groupId, int userId)
			throws SQLException, NamingException, ClassNotFoundException, IOException, IllegalArgumentException,
			IllegalAccessException {
		Util.setPageNoCache(response);

		// 退出小组
		StudyGroup group = Global.getStudyGroupBO().findById(groupId);
		User user = Global.getUserBO().findById(userId);
		String errMsg = user.exitGroup(groupId);

		if (errMsg == null) {
			log.info(String.format("用户[%s]退出了小组[%s]", Util.getNickNameOfUser(user), group.getGroupName()));
		}

		// Send result back to client.
		Util.sendBooleanResponse(errMsg == null, errMsg, null, response);

		return null;
	}
}
