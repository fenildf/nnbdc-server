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

import beidanci.Global;
import beidanci.bo.MasteredWordBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/showMasteredWords.do")
public class ShowMasteredWords {

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
			throws ClassNotFoundException, SQLException, NamingException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {

		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		// 获取生词本中的生词数
		MasteredWordBO masteredWordDAO = Global.getMasteredWordBO();
		int masteredWordCount = masteredWordDAO.findByUser(user).size();
		request.setAttribute("masteredWordCount", masteredWordCount);

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("masteredWords");

	}
}
