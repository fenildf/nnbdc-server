package beidanci.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import beidanci.Global;
import beidanci.bo.SentenceBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.Sentence;
import beidanci.util.BeanUtils;
import beidanci.util.Util;
import beidanci.vo.PageCtrl;
import beidanci.vo.PagedData;
import beidanci.vo.SentenceVo;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

/**
 * 获取已经具有了DIY条目，等待用户投票/评论的例句。
 * 
 * @author Administrator
 * 
 */
@Controller
@RequestMapping("/getSentenceToVote.do")
public class GetSentenceToVote {
	@RequestMapping(method = RequestMethod.GET)
	public String handle(HttpServletRequest request, HttpServletResponse response) throws SQLException, NamingException,
			ClassNotFoundException, IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		Util.setPageNoCache(response);
		response.setContentType("application/json");

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		int pageNo = Integer.parseInt(params.get("pageNo")[0]);
		int pageSize = Integer.parseInt(params.get("pageSize")[0]);

		// 获取待翻译例句
		SentenceBO sentenceDAO = Global.getSentenceBO();
		int totalCount = sentenceDAO.getCountOfSentencesToVote(Sentence.HUMAN_AUDIO);

		// 生成Page control 对象
		PageCtrl pageCtrl = new PageCtrl();
		pageCtrl.setTotalRecordCount(totalCount);
		pageCtrl.setPageSize(pageSize);
		int pageCount = pageCtrl.getPageCount();
		pageNo = pageNo > pageCount ? pageCount : pageNo;
		pageNo = pageNo < 1 ? 1 : pageNo;
		pageCtrl.setCurrPageNo(pageNo);

		// 获取一页例句
		int firstRow = (pageNo - 1) * pageSize;
		List<Sentence> sentences = sentenceDAO.getSentencesToVote(firstRow, pageSize, Sentence.HUMAN_AUDIO);
		SentenceVo[] sentenceVos = new SentenceVo[sentences.size()];
		for (int i = 0; i < sentences.size(); i++) {
			Sentence sentence = sentences.get(i);
			SentenceVo vo = BeanUtils.makeVO(sentence, SentenceVo.class, null);
			sentenceVos[i] = vo;
		}
		PagedData<SentenceVo> pagedSentences = new PagedData<SentenceVo>();
		pagedSentences.setPageCtrl(pageCtrl);
		pagedSentences.setData(sentenceVos);

		PrintWriter out = response.getWriter();
		try {
			JsonConfig jsonConfig = new JsonConfig();
			out.println(((JSONObject) JSONSerializer.toJSON(pagedSentences, jsonConfig)).toString());

		} finally {
			out.close();
		}
		return null;
	}

}
