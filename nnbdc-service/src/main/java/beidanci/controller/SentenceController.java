package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.bo.SentenceDiyItemBO;
import beidanci.po.Sentence;
import beidanci.po.User;
import beidanci.util.BeanUtils;
import beidanci.util.Util;
import beidanci.vo.Result;
import beidanci.vo.SentenceDiyItemVo;
import beidanci.vo.SentenceVo;

@Controller
public class SentenceController {
	@RequestMapping("/getSentence.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void getSentence(HttpServletRequest request, HttpServletResponse response, Integer sentenceId)
			throws SQLException, InterruptedException, NamingException, ClassNotFoundException, IOException,
			IllegalArgumentException, IllegalAccessException {
		Sentence sentence = Global.getSentenceBO().findById(sentenceId);
		SentenceVo vo = BeanUtils.makeVO(sentence, SentenceVo.class,
				new String[] { "invitedBy", "userGames", "studyGroups" });
		List<SentenceDiyItemVo> diyItems = Global.getSentenceDiyItemBO().getSentenceDiyItems(sentenceId,
				Util.getSessionData(request));
		vo.setSentenceDiyItems(diyItems);
		Util.sendJson(vo, response);
	}

	@RequestMapping("/saveSentenceDiyItem.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void saveSentenceDiyItem(HttpServletRequest request, HttpServletResponse response, Integer sentenceId,
			String chinese) throws SQLException, InterruptedException, NamingException, ClassNotFoundException,
			IOException, IllegalArgumentException, IllegalAccessException {
		Global.getSentenceDiyItemBO().saveDiyItem(sentenceId, chinese, Util.getLoggedInUser());

		// 返回更新后的例句对象
		Sentence sentence = Global.getSentenceBO().findById(sentenceId);
		SentenceVo vo = BeanUtils.makeVO(sentence, SentenceVo.class,
				new String[] { "invitedBy", "userGames", "studyGroups" });

		Util.sendBooleanResponse(true, null, vo, response);
	}

	@RequestMapping("/handSentenceDiyItem.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void handSentenceDiyItem(HttpServletRequest request, HttpServletResponse response, Integer id)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User user = Util.getLoggedInUser();
		SentenceDiyItemBO bo = Global.getSentenceDiyItemBO();
		Result<Integer> result = bo.handSentenceDiyItem(id, user);
		Util.getSessionData(request).getVotedSentenceDiyItems().add(id);// 阻止该用户再次对同一内容进行投票
		Util.sendAjaxResult(result, response);
	}

	@RequestMapping("/footSentenceDiyItem.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void footSentenceDiyItem(HttpServletRequest request, HttpServletResponse response, Integer id)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User user = Util.getLoggedInUser();
		SentenceDiyItemBO bo = Global.getSentenceDiyItemBO();
		Result<Integer> result = bo.footSentenceDiyItem(id, user);
		Util.getSessionData(request).getVotedSentenceDiyItems().add(id); // 阻止该用户再次对同一内容进行投票
		Util.sendAjaxResult(result, response);
	}

	@RequestMapping("/deleteSentenceDiyItem.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void deleteSentenceDiyItem(HttpServletRequest request, HttpServletResponse response, Integer id)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User user = Util.getLoggedInUser();
		SentenceDiyItemBO bo = Global.getSentenceDiyItemBO();
		Result<Object> result = bo.deleteSentenceDiyItem(id, user, true);
		Util.sendAjaxResult(result, response);
	}
}
