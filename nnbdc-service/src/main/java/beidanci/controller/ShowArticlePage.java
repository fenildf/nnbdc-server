package beidanci.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.po.Article;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/showArticlePage.do")
public class ShowArticlePage {
	private static final Logger log = LoggerFactory.getLogger(ShowArticle.class);

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) {

		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();
		log.info(String.format("用户[%s]正在访问文章总页面, UA[%s]", user == null ? "null" : Util.getNickNameOfUser(user),
				Util.getUserAgent(request)));

		// 获取所有文章并按时间排序
		List<Article> articles = Global.getArticleBO().queryAll();
		Collections.sort(articles, new Comparator<Article>() {
			@Override
			public int compare(Article o1, Article o2) {
				return o2.getCreateTime().compareTo(o1.getCreateTime());
			}
		});
		request.setAttribute("articles", articles);

		// 验证用户是否可以发表文章
		boolean canWrite = user != null && !user.getUserName().startsWith("guest");
		request.setAttribute("canWrite", canWrite);

		Util.setCommonAttributesForShowingJSP(request);

		return new ModelAndView("articles");

	}
}
