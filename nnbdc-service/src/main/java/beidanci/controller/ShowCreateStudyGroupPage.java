package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.util.Util;

@Controller
@RequestMapping("/showCreateStudyGroupPage.do")
public class ShowCreateStudyGroupPage {

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws IOException,
			ParseException, InvalidMeaningFormatException, SQLException, NamingException, ClassNotFoundException {
		Util.setPageNoCache(response);

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("createstudygroup");

	}
}
