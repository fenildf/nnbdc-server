package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.Global;
import beidanci.bo.SentenceBO;
import beidanci.bo.SentenceDiyItemBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.Sentence;
import beidanci.po.SentenceDiyItem;
import beidanci.po.User;
import beidanci.util.Util;

@Controller
@RequestMapping("/addChineseForSentence.do")
public class AddChineseForSentence {

	@RequestMapping(method = RequestMethod.POST)
	public String handle(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		int sentenceId = Integer.parseInt(params.get("sentenceId")[0]);
		String chinese = params.get("chinese")[0];

		// 获取例句对象
		SentenceBO sentenceDAO = Global.getSentenceBO();
		Sentence sentence = sentenceDAO.findById(sentenceId);

		// 保存例句的中文翻译
		SentenceDiyItem diyItem = new SentenceDiyItem();
		diyItem.setAuthor(user);
		diyItem.setCreateTime(new Timestamp(new Date().getTime()));
		diyItem.setFootCount(0);
		diyItem.setHandCount(0);
		diyItem.setItemType(SentenceDiyItem.ITEM_TYPE_CHINESE);
		diyItem.setSentence(sentence);
		diyItem.setContent(chinese);
		SentenceDiyItemBO sentenceDiyItemDAO = Global.getSentenceDiyItemBO();
		sentenceDiyItemDAO.createEntity(diyItem);

		// 更新例句的最新DIY修改时间
		sentence.setLastDiyUpdateTime(new Timestamp(new Date().getTime()));
		sentenceDAO.updateEntity(sentence);

		Util.sendBooleanResponse(true, null, null, response);

		return null;
	}

}
