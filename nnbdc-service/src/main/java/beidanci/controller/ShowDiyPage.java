package beidanci.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.util.Util;

@Controller
@RequestMapping("/showDiyPage.do")
public class ShowDiyPage {
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Util.setPageNoCache(response);

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("diy");
	}
}
