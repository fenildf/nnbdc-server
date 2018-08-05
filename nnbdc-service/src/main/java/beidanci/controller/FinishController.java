package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.Daka;
import beidanci.po.DakaId;
import beidanci.po.User;
import beidanci.po.Word;
import beidanci.util.BeanUtils;
import beidanci.util.Util;
import beidanci.util.Utils;
import beidanci.vo.WordVo;

@Controller
public class FinishController {
	@RequestMapping("/showFinishPage.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		DakaId id = new DakaId(user.getId(), user.getLastLearningDate());
		Daka daka = Global.getDakaBO().findById(id);
		request.setAttribute("dakaRecord", daka);

		request.setAttribute("answerWrongWords", user.getWrongWords());

		// 获取所有答错单词的发音
		String[] soundFiles = new String[user.getWrongWords().size()];
		for (int i = 0; i < user.getWrongWords().size(); i++) {
			soundFiles[i] = Utils.getFileNameOfWordSound(user.getWrongWords().get(i).getSpell());
		}
		request.setAttribute("soundFiles", soundFiles);

		request.setAttribute("soundBaseUrl", Util.getSoundBaseUrl());
		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("finish");
	}

	@RequestMapping("/getAnswerWrongWords.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getAnswerWrongWords(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, NamingException, ClassNotFoundException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {
		User user = Util.getLoggedInUser();
		List<Word> wrongWords = user.getWrongWords();

		// PO-->VO
		List<WordVo> vos = new ArrayList<WordVo>();
		for (Word po : wrongWords) {
			WordVo vo = BeanUtils.makeVO(po, WordVo.class,
					new String[] { "SynonymVo.meaningItem", "SynonymVo.word", "similarWords" });
			vos.add(vo);
		}

		Util.sendJson(vos, response);
	}
}
