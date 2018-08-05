package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.SessionData;
import beidanci.bo.UserBO;
import beidanci.po.User;
import beidanci.util.BeanUtils;
import beidanci.util.Util;
import beidanci.vo.Result;
import beidanci.vo.UserVo;

@Controller
public class LoginController {
	private static Logger log = LoggerFactory.getLogger(LoginController.class);

	@RequestMapping("/login.do")
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response, String clientType)
			throws SQLException, InterruptedException, NamingException, ClassNotFoundException, IOException,
			IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);
		response.setContentType("application/json");

		request.getSession().removeAttribute(SessionData.SESSION_DATA);

		if (request.getParameterMap().get("loginType") == null) {
			Util.sendBooleanResponse(false, "登录参数无效", null, response);
			return null;
		}

		final String loginType = request.getParameterMap().get("loginType")[0];
		Result<User> checkResult = checkUser(request, loginType, clientType);

		User user = checkResult.getData();
		if (user != null) {
			Result<Authentication> result = Global.getUserBO().doLogin(user.getUserName(), user.getPassword(),
					loginType, request, response);
			if (result.isSuccess()) {
				SessionData sessionData = new SessionData();
				request.getSession(true).setAttribute(SessionData.SESSION_DATA, sessionData);
			}
			Util.sendBooleanResponse(result.isSuccess(),
					result.getMsg() != null ? result.getMsg() : ("JSESSIONID=" + request.getSession().getId()), null,
					response);
		} else {
			Util.sendBooleanResponse(false, checkResult.getMsg(), null, response);
		}

		return null;
	}

	/**
	 * 判断用户是否是合法用户， 必要情况下自动创建账户（如快速体验）
	 *
	 * @param request
	 * @return 如果是合法用户，返回User对象，否则返回null
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private Result<User> checkUser(HttpServletRequest request, final String loginType, String clientType)
			throws IllegalArgumentException, IllegalAccessException {
		UserBO userBO = Global.getUserBO();
		String errMsg = "账户名或密码错误";

		// Get login parameters.
		Map<String, String[]> paramMap = request.getParameterMap();
		if (paramMap.get("loginType") == null) {
			log.warn("loginType 为 null");
			return new Result<User>(false, "loginType 为 null", null);
		}
		String userName = null;
		String password = null;
		log.info(String.format("用户正在登录... IP[%s] loginType[%s] UA[%s]", Util.getClientIP(request), loginType,
				request.getHeader("User-Agent")));

		if (loginType.equals("USER_NAME")) {
			if (paramMap.get("userName") == null || paramMap.get("password") == null) {
				log.warn(
						String.format("userName[%s] password[%s]", paramMap.get("userName"), paramMap.get("password")));
				return new Result<User>(false, "用户名过密码为空", null);
			}
			userName = paramMap.get("userName")[0].toLowerCase();
			password = paramMap.get("password")[0];

			// 为快速体验入口创建临时用户223
			if (userName.equals("guest")) {
				userName = userName + "_" + Util.getClientIP(request);
				password = "123456";
				if (userBO.getByUserName(userName) == null) {
					User tempUser = Util.genUser(userName, password, "游客", null, null);
					tempUser.setWordsPerDay(20);
					tempUser.setPassword("123456");
					userBO.createEntity(tempUser);
				}
			}
		}

		log.info(String.format("A user[userName:%s] is logining with UA[%s]: ", userName,
				request.getHeader("User-Agent")));

		User user = null;
		if (loginType.equals("USER_NAME")) {
			// 检查用户名密码是否正确.
			user = userBO.getByUserName(userName);
			if (user != null && !user.getPassword().equals(password)) {
				user = null;
				errMsg = "密码不正确";
			}
		} else if (loginType.equals("EMAIL")) {
			String email = paramMap.get("email")[0];
			if (paramMap.get("password") == null) { // 请求格式非法
				return new Result<User>(false, "密码为空", null);
			}
			password = paramMap.get("password")[0];
			List<User> users = userBO.findByEmail(email);
			if (users.size() > 0) {
				assert (users.size() == 1);
				user = users.get(0);
				if (!user.getPassword().equals(password)) {
					user = null;
					errMsg = "密码不正确";
				}
			}
			// 如果Email对应的账户不存在，且是android登录，则自动创建账户
			else if ("android".equals(clientType)) {
				if (!Util.isValidEmail(email)) {
					errMsg = "Email格式不正确";
				} else if (StringUtils.isEmpty(password)) {
					errMsg = "密码不能为空";
				} else {
					user = Util.genUser(email, password, email.split("@")[0], email, null);
					user.setWordsPerDay(20);
					userBO.createEntity(user);
				}
			}
		} else {
			throw new RuntimeException("Unknown login type: " + loginType);
		}
		return new Result(user != null, user == null ? errMsg : null, user);
	}

	@RequestMapping("/getLoggedInUser.do")
	public void getLoggedInUser(HttpServletResponse response) throws SQLException, InterruptedException,
			NamingException, ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);
		response.setContentType("application/json");

		User user = Util.getLoggedInUser();
		UserVo userVo = BeanUtils.makeVO(user, UserVo.class, new String[] { "invitedBy", "StudyGroupVo.creator",
				"StudyGroupVo.users", "StudyGroupVo.managers", "StudyGroupVo.studyGroupPosts", "userGames" });
		Util.sendJson(userVo, response);
	}
}
