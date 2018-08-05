package beidanci.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.mail.EmailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import beidanci.Global;
import beidanci.bo.UserBO;
import beidanci.po.GetPwdLog;
import beidanci.po.User;
import beidanci.util.Util;
import beidanci.vo.Result;

@Controller
@RequestMapping("/getPwd.do")
public class GetPwd {
	private static Logger log = LoggerFactory.getLogger(GetPwd.class);

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Result<Object> handle(HttpServletRequest request, HttpServletResponse response)
			throws EmailException, IOException {
		Util.setPageNoCache(response);

		// Get parameter from request.
		Map<String, String[]> paramMap = request.getParameterMap();
		String email = paramMap.get("email")[0];

		// 查找邮箱对应的用户，并发送密码到该邮箱
		Result<Object> result;
		UserBO userDAO = Global.getUserBO();
		if (email != null && email.trim().length() > 0) {
			List<User> users = userDAO.findByEmail(email);
			if (users.size() > 0) {
				StringBuilder content = new StringBuilder();
				content.append("您在牛牛背单词的帐户信息：\r\n");
				for (User user : users) {
					content.append(String.format("用户名：%s  密码：%s\r\n", user.getEmail(), user.getPassword()));
				}

				sendPwdByEmail(email, Util.getNickNameOfUser(users.get(0)), content.toString());
				result = new Result<>(true, email, null);
			} else {
				result = new Result<>(false, "Email在系统中不存在", null);
			}
		} else {
			result = new Result<>(false, "Email不能为空", null);
		}

		return result;
	}

	/**
	 * 用Email方式发送用户的密码到用户邮箱
	 * 
	 * @param toEmail
	 * @param toName
	 * @param content
	 */
	private void sendPwdByEmail(String toEmail, String toName, String content) {
		String sendResult = "success";
		try {
			Util.sendSimpleEmail(toEmail, toName, "您在牛牛背单词的密码", content);
		} catch (EmailException e) {
			log.warn("", e);
			sendResult = e.getMessage();
		}

		// 写日志
		GetPwdLog getPwdLog = new GetPwdLog(toEmail, new Date((new Date()).getTime()), content, sendResult);
		Global.getGetPwdLogBO().createEntity(getPwdLog);
	}
}
