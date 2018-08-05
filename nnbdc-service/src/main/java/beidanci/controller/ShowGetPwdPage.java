package beidanci.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.util.Util;

@Controller
@RequestMapping("/showGetPwdPage.do")
public class ShowGetPwdPage {
	private static Logger log = LoggerFactory.getLogger(ShowGetPwdPage.class);

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) {
		Util.setPageNoCache(response);

		// 获取推荐人信息
		String invitor = request.getParameter("invitor");
		if (invitor != null) {
			log.info(String.format("[%s] 邀请的朋友正在找回密码（有点奇怪哦）...", invitor));
			request.setAttribute("invitor", invitor);
		}

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("getpwd");

	}
}
