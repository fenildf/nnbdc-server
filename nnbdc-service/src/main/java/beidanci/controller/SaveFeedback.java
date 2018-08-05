package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
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
import beidanci.bo.MsgBO;
import beidanci.bo.UserBO;
import beidanci.po.Msg;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/saveFeedback.do")
public class SaveFeedback {
	private static final Logger log = LoggerFactory.getLogger(SaveFeedback.class);

	@RequestMapping(method = RequestMethod.GET)
	public String handle(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException {
		Util.setPageNoCache(response);

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		if (params.get("content") == null) {
			log.warn("content为null!");
			Util.sendBooleanResponse(false, null, null, response);
			return null;
		}
		String text = params.get("content")[0];

		// 获取当前用户
		UserBO userDAO = Global.getUserBO();
		User user = userDAO.findById("guest");
		if (Util.getLoggedInUser() != null) {
			user = Util.getLoggedInUser();
		}

		// 保存用户反馈
		Msg msg = new Msg();
		msg.setFromUser(user);
		msg.setToUser(userDAO.findById("sys"));
		msg.setContent(text);
		msg.setCreateTime(new Timestamp(new Date().getTime()));
		MsgBO adviceDAO = Global.getMsgBO();
		adviceDAO.createEntity(msg);

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);

		return null;
	}
}
