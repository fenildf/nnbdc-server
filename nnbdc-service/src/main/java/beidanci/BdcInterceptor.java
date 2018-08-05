package beidanci;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import beidanci.po.User;
import beidanci.util.Util;

public class BdcInterceptor implements HandlerInterceptor {
	private static Logger log = LoggerFactory.getLogger(BdcInterceptor.class);

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e)
			throws Exception {

	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		Util.setPageNoCache(response);
		request.setCharacterEncoding("utf-8");
		response.setContentType("application/xml;charset=UTF-8");
		response.addHeader("P3P", "CP=CAO PSA OUR");

		// 处理用户提交的内容中存在的潜在风险（Xss攻击）
		Map<String, String[]> params = request.getParameterMap();
		for (Entry<String, String[]> param : params.entrySet()) {
			String[] values = param.getValue();
			for (int i = 0; i < values.length; i++) {
				values[i] = Util.purifyContent(values[i]);
			}
		}

		User user = Util.getLoggedInUser();
		if (request.getRequestURI().indexOf("getMsgs.do") == -1
				&& request.getRequestURI().indexOf("getEyeMode.do") == -1
				&& request.getRequestURI().indexOf("getWords.do") == -1) {
			log.info(String.format("用户【%s】正在访问 %s", user == null ? "null" : Util.getNickNameOfUser(user),
					request.getRequestURI()));
		}

		return true;
	}
}
