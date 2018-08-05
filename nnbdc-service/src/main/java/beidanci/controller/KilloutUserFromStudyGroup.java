package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.Global;
import beidanci.bo.StudyGroupBO;
import beidanci.po.StudyGroup;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/killoutUserFromStudyGroup.do")
public class KilloutUserFromStudyGroup {
	private static final Logger log = LoggerFactory.getLogger(KilloutUserFromStudyGroup.class);

	@RequestMapping(method = RequestMethod.GET)
	public String handle(HttpServletRequest request, HttpServletResponse response) throws SQLException, NamingException,
			ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		int groupID = Integer.parseInt(params.get("groupID")[0]);
		String userName = params.get("userName")[0];

		// 首先尝试把用户从管理员中删除
		StudyGroupBO groupDAO = Global.getStudyGroupBO();
		StudyGroup group = groupDAO.findById(groupID);
		User user = Global.getUserBO().findById(userName);
		group.getManagers().remove(user);

		// 然后把用户从组中删除
		group.getUsers().remove(user);
		groupDAO.updateEntity(group);

		log.info(String.format("用户[%s]被小组[%s]开除", Util.getNickNameOfUser(user), group.getGroupName()));

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);

		return null;
	}
}
