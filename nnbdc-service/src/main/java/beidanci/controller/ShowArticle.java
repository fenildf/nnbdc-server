package beidanci.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.bo.ArticleBO;
import beidanci.po.Article;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/showArticle.do")
public class ShowArticle {
	private static final Logger log = LoggerFactory.getLogger(ShowArticle.class);

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
			throws IllegalArgumentException, IllegalAccessException {

		Util.setPageNoCache(response);

		// 获取指定文章
		Integer id = Integer.parseInt(request.getParameter("articleID"));
		ArticleBO articleDAO = Global.getArticleBO();
		Article article = articleDAO.findById(id);
		article.setViewedCount(article.getViewedCount() + 1);
		articleDAO.updateEntity(article);
		request.setAttribute("article", article);

		User user = Util.getLoggedInUser();
		log.info(String.format("用户[%s]正在访问文章[%s], UA[%s]", user == null ? "null" : Util.getNickNameOfUser(user),
				article.getTitle(), Util.getUserAgent(request)));

		Util.setCommonAttributesForShowingJSP(request);

		return new ModelAndView("article");

	}
}
