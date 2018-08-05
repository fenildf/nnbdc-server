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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.util.Util;

@Controller
@RequestMapping("/showRegPage.do")
public class ShowRegPage {
	private static Logger log = LoggerFactory.getLogger(ShowRegPage.class);

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
			throws ClassNotFoundException, SQLException, NamingException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {

		Util.setPageNoCache(response);

		// 获取推荐人信息
		String invitor = request.getParameter("invitor");
		if (invitor != null) {
			log.info(String.format("[%s] 邀请的朋友正在注册...", invitor));
			request.setAttribute("invitor", invitor);
		}

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("register");

	}
}
