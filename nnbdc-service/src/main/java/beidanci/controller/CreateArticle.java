package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.Global;
import beidanci.bo.ArticleBO;
import beidanci.po.Article;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/createArticle.do")
public class CreateArticle {
	private static Logger log = LoggerFactory.getLogger(CreateArticle.class);

	@RequestMapping(method = RequestMethod.POST)
	public String handle(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		// Get input.
		Map<String, String[]> paramMap = request.getParameterMap();
		String articleTitle = paramMap.get("articleTitle")[0];
		String articleContent = paramMap.get("articleContent")[0];
		String keyWords = paramMap.get("keyWords")[0];
		String description = paramMap.get("description")[0];

		// 验证文章标题.
		String errorMsg = "";
		if (articleTitle.length() == 0) {
			errorMsg += "文章标题不能为空\n";
		}

		// 验证文章内容.
		if (articleContent.length() == 0) {
			errorMsg += "文章内容不能为空\n";
		}

		// 验证文章关键字.
		if (keyWords.length() == 0) {
			errorMsg += "关键字不能为空\n";
		}

		// 验证文章描述.
		if (description.length() == 0) {
			errorMsg += "文章描述不能为空\n";
		}

		// 验证用户是否是游客
		if (user.getUserName().startsWith("guest")) {
			errorMsg += "您是游客，不能发表文章";
		}

		if (errorMsg.equals("")) {

			// 保存文章信息到数据库.
			Article article = new Article();
			article.setCreateTime(new Timestamp(new Date().getTime()));
			article.setUser(user);
			article.setTitle(articleTitle);
			article.setContent(articleContent);
			article.setKeyWords(keyWords.replaceAll("，", ","));
			article.setDescription(description);
			article.setViewedCount(0);
			ArticleBO articleDAO = Global.getArticleBO();
			articleDAO.createEntity(article);

			log.info(String.format("用户[%s]发表文章成功：标题[%s] 内容[%d]个字节", Util.getNickNameOfUser(user), article.getTitle(),
					article.getContent().length()));
		} else {
			log.info(String.format("用户[%s]发表文章失败，详情：[%s] ", Util.getNickNameOfUser(user), errorMsg));
		}

		// Send result back to client.
		Util.sendBooleanResponse(errorMsg.equals(""), errorMsg, null, response);

		return null;
	}
}
