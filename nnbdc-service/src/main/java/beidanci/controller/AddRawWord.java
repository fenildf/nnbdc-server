package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.bo.RawWordBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.RawWord;
import beidanci.po.User;
import beidanci.store.WordStore;
import beidanci.util.Util;
import beidanci.vo.WordVo;

@Controller
public class AddRawWord {

	public final static String WORD_ALREADY_IN_RAW_WORD_BOOK = "单词已经在生词本中了";

	@RequestMapping("/addRawWord.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public String handle(HttpServletRequest request, HttpServletResponse response) throws SQLException, NamingException,
			ClassNotFoundException, IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		String spell = params.get("spell")[0];
		String addManner = params.get("addManner")[0];

		// 添加到生词本
		String errMsg = addRawWord(spell, user, addManner);

		Util.sendBooleanResponse(errMsg == null, errMsg, null, response);

		return null;
	}

	public static String addRawWord(String spell, User user, String createManner)
			throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		if (spell.trim().length() == 0) {
			return "单词拼写不能为空";
		}

		// 检查单词是否在词库中存在
		WordVo word = WordStore.getInstance().getWordBySpell(spell);
		if (word == null) {
			return String.format("单词在牛牛词库中不存在");
		}

		// 检查该单词时候几经在生词本中了
		RawWordBO rawWordDAO = Global.getRawWordBO();
		boolean alreadyInRawWordBook = rawWordDAO.findRawWordOfUser_ByWordId(user, word.getId()) != null;
		if (alreadyInRawWordBook) {
			return String.format(WORD_ALREADY_IN_RAW_WORD_BOOK);
		}

		// 保存单词到生词本
		RawWord rawWord = new RawWord();
		rawWord.setCreateManner(createManner);
		rawWord.setCreateTime(new Timestamp(new Date().getTime()));
		rawWord.setUser(user);
		rawWord.setWord(Global.getWordBO().findById(word.getId()));
		rawWordDAO.createEntity(rawWord);

		return null;
	}
}
