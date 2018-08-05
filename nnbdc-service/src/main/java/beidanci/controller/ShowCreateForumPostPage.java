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
import beidanci.bo.ForumBO;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.Forum;
import beidanci.util.Util;

@Controller
@RequestMapping("/showCreateForumPostPage.do")
public class ShowCreateForumPostPage {
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws IOException,
			ParseException, InvalidMeaningFormatException, SQLException, NamingException, ClassNotFoundException {
		Util.setPageNoCache(response);

		int forumID = Integer.parseInt(request.getParameter("forumID"));
		ForumBO forumDAO = Global.getForumBO();
		Forum forum = forumDAO.findById(forumID);
		request.setAttribute("forum", forum);

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("createForumPost");
	}
}
