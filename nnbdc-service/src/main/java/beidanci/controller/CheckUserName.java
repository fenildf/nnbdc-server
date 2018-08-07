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
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.util.Util;

/**
 * 验证指定Email（用作用户名）是否存在
 *
 * @author Administrator
 */
@Controller
@RequestMapping("/checkEmail.do")
public class CheckUserName {
	private static final Logger log = LoggerFactory.getLogger(CheckUserName.class);

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException, SQLException, InterruptedException, NamingException, ClassNotFoundException {
		Util.setPageNoCache(response);

		// Get parameter from request.
		Map<String, String[]> paramMap = request.getParameterMap();
		if (paramMap.get("email") == null) {
			log.warn("email为null!");
			Util.sendBooleanResponse(false, null, null, response);
			return null;
		}
		String userName = paramMap.get("email")[0];

		// Check if the user name has already been used.
		boolean emailExists = Global.getUserBO().findByEmail(userName).size() > 0;

		// Send check result back to client.
		Util.sendBooleanResponse(emailExists, null, null, response);

		return null;
	}
}
