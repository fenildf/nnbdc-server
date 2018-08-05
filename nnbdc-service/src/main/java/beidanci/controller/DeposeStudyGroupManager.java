package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.Global;
import beidanci.bo.StudyGroupBO;
import beidanci.po.StudyGroup;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/deposeStudyGroupManager.do")
public class DeposeStudyGroupManager {
	@RequestMapping(method = RequestMethod.GET)
	public String handle(HttpServletRequest request, HttpServletResponse response) throws SQLException, NamingException,
			ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		int groupID = Integer.parseInt(params.get("groupID")[0]);
		String userName = params.get("userName")[0];

		// 把要罢免的用户从组管理员删除
		boolean deleteSucc;
		String desc = "";
		StudyGroupBO groupDAO = Global.getStudyGroupBO();
		StudyGroup group = groupDAO.findById(groupID);
		User user = Global.getUserBO().findById(userName);
		if (group.getManagers().contains(user)) {
			group.getManagers().remove(user);
			groupDAO.updateEntity(group);
			deleteSucc = true;
		} else {
			deleteSucc = false;
			desc = "没有找到该管理员";
		}

		// Send result back to client.
		Util.sendBooleanResponse(deleteSucc, desc, null, response);

		return null;
	}

}
