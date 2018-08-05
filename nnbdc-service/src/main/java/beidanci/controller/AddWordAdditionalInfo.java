package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.Global;
import beidanci.bo.WordAdditionalInfoBO;
import beidanci.po.User;
import beidanci.po.Word;
import beidanci.po.WordAdditionalInfo;
import beidanci.util.Util;

@Controller
@RequestMapping("/addWordAdditionalInfo.do")
public class AddWordAdditionalInfo {

	@RequestMapping(method = RequestMethod.POST)
	public String handle(Integer wordId, String content, HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		if (content.trim().length() == 0) {
			Util.sendBooleanResponse(false, "内容不能为空", null, response);
			return null;
		}

		// 保存单词附加内容
		WordAdditionalInfoBO wordAdditionalInfoDAO = Global.getWordAdditionalInfoBO();
		WordAdditionalInfo wordAdditionalInfo = new WordAdditionalInfo();
		Word word = Global.getWordBO().findById(wordId);
		wordAdditionalInfo.setWord(word);
		wordAdditionalInfo.setContent(content);
		wordAdditionalInfo.setCreateTime(new Timestamp(new Date().getTime()));
		wordAdditionalInfo.setFootCount(0);
		wordAdditionalInfo.setHandCount(0);
		wordAdditionalInfo.setUser(user);
		wordAdditionalInfoDAO.createEntity(wordAdditionalInfo);

		// Send result back to client.
		Util.sendBooleanResponse(true, String.valueOf(wordAdditionalInfo.getId()), content, response);

		return null;
	}

}
