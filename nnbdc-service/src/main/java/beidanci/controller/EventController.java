package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.bo.RawWordBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
public class EventController {
	@RequestMapping("/showEventPage.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ModelAndView showEventPage(HttpServletRequest request, HttpServletResponse response)
			throws ClassNotFoundException, SQLException, NamingException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {

		User user = Util.getLoggedInUser();

		// 获取生词本中的生词数
		RawWordBO rawWordDAO = Global.getRawWordBO();
		int rawWordCount = rawWordDAO.findByUser(user).size();
		request.setAttribute("rawWordCount", rawWordCount);

		Util.setCommonAttributesForShowingJSP(request);

		return new ModelAndView("event");

	}
}
