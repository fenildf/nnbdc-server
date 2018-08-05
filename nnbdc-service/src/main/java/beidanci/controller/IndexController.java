package beidanci.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.bo.UserBO;
import beidanci.po.UpdateLog;
import beidanci.po.User;
import beidanci.util.IPSeeker;
import beidanci.util.Util;

@Controller
public class IndexController {
	private static Logger log = LoggerFactory.getLogger(IndexController.class);

	@RequestMapping("/showIndexPage.do")
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (Util.isAjaxRequest(request)) {// AJAX
			// 方式不能把登录页面返回给JS，因为JS难以处理。所以替代的做法是给JS发一个需要登录的通知，由JS决定如何处理
			response.setHeader("command", "login");
			// response.setContentType("text/plain;charset=UTF-8");
			PrintWriter out = response.getWriter();
			try {
				log.debug(String.format("A XMLHttpRequest [%s] was responsed as timeout.", request.getRequestURI()));
				out.println("login");
			} finally {
				out.close();
			}
			return null;
		} else { // 普通HTML页面
			Util.setPageNoCache(response);

			Util.setCommonAttributesForShowingJSP(request);
			request.setAttribute("email", request.getParameter("email"));

			request.getSession().setAttribute("isTest", "true".equals(request.getParameter("istest")));

			// 获取推荐人信息
			String invitor = request.getParameter("inv");
			UserBO userDAO = Global.getUserBO();
			User invitorUser = null;
			if (invitor != null) {
				invitorUser = userDAO.findById(invitor);
			}
			String invitorNickName = invitor;
			if (invitorUser != null) {
				invitorNickName = Util.getNickNameOfUser(invitorUser);
			}
			String remoteAddr = Util.getClientIP(request);
			try {
				log.info(String.format("[%s|%s|%s]邀请人[%s]正在访问本站... UA[%s]", remoteAddr,
						IPSeeker.getInstance().getCountry(remoteAddr), IPSeeker.getInstance().getArea(remoteAddr),
						invitorNickName, request.getHeader("User-Agent")));
			} catch (Exception e) {
				log.error("", e);
			}

			if (invitor != null) {
				invitor = invitor.trim();
			}
			request.getSession().setAttribute("invitor", invitor);
			request.getSession().setAttribute("updateLog", Global.getUpdateLogBO().getLastestLog());

			return new ModelAndView("index");
		}
	}

	@RequestMapping("/getLastUpdateLog.do")
	public void getLastUpdateLog(HttpServletResponse response) throws IOException {
		UpdateLog lastUpdateLog = Global.getUpdateLogBO().getLastestLog();
		Util.sendJson(lastUpdateLog, response);
	}
}
