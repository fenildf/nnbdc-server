package beidanci.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.bo.UserBO;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/showAdminPage.do")
public class ShowAdminPage {
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) {
		Util.setPageNoCache(response);

		UserBO userDAO = Global.getUserBO();
		List<User> users = new ArrayList<User>(userDAO.findAll());
		request.setAttribute("users", users);

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("admin");

	}
}
