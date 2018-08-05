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
import beidanci.bo.DictWordBO;
import beidanci.dao.PaginationResults;
import beidanci.dao.SortRule;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.*;
import beidanci.util.BeanUtils;
import beidanci.util.Util;
import beidanci.vo.*;

@Controller
public class DictController {
	/**
	 * 获取用户选中的正在学习的单词书
	 *
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("getSelectedLearningDicts.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getSelectedLearningDicts(HttpServletResponse response) throws IOException {

		User user = Util.getLoggedInUser();
		List<LearningDict> dicts = Util.getSelectedLearningDicts(user);
		List<LearningDictVo> dictVOs = new ArrayList<LearningDictVo>();
		for (LearningDict dict : dicts) {
			LearningDictVo vo = BeanUtils.makeVO(dict, LearningDictVo.class,
					new String[] { "createTime", "lastUpdateTime", "UserVo.invitedBy" });
			dictVOs.add(vo);
		}
		Util.sendJson(dictVOs, response);

	}

	@RequestMapping("/showDictPage.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ModelAndView showDictPage(HttpServletRequest request, HttpServletResponse response) {
		User user = Util.getLoggedInUser();
		if (user != null) {
			// 获取用户已经选择的单词书
			List<SelectedDict> dicts = user.getSelectedDicts();
			request.setAttribute("selectedDicts", dicts);
		}

		// 获取系统所有的单词书分类
		List<DictGroup> dictGroups = Global.getDictGroupBO().getAllDictGroups();
		request.setAttribute("dictGroups", dictGroups);

		// 获取自定义单词书
		List<Dict> myDicts = Global.getDictBO().getOwnDicts(user);
		request.setAttribute("myDicts", myDicts);

		request.setAttribute("returnPage", request.getParameter("returnPage")); // 用户选择完成后返回也页面
		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("dict");

	}

	/**
	 * 获取当前用户的自定义单词书
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/getMyDicts.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getMyDicts(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User user = Util.getLoggedInUser();
		List<Dict> myDicts = Global.getDictBO().getOwnDicts(user);
		List<DictVo> vos = BeanUtils.makeVos(myDicts, DictVo.class,
				new String[] { "invitedBy", "studyGroups", "userGames", "dictWords" });

		Util.sendJson(vos, response);

	}

	/**
	 * 获取所有单词书分组
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/getDictGroups.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getDictGroups(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// 获取系统所有的单词书分类
		List<DictGroup> dictGroups = Global.getDictGroupBO().getAllDictGroups();

		// Po-->Vo
		List<DictGroupVo> vos = new ArrayList<DictGroupVo>();
		for (DictGroup po : dictGroups) {
			DictGroupVo vo = BeanUtils.makeVO(po, DictGroupVo.class,
					new String[] { "DictGroupVo.dictGroup", "UserVo.invitedBy", "DictVo.owner", "dictWords" });
			if (po.getDictGroup() != null) {
				DictGroupVo parent = new DictGroupVo();
				parent.setName(po.getDictGroup().getName());
				vo.setDictGroup(parent);
			}
			if (!vo.getName().equals("其他") && !vo.getName().equals("少儿") && !vo.getName().equals("小学")) {
				vos.add(vo);
			}
		}
		Util.sendJson(vos, response);

	}

	/**
	 * 获取用户已经选中的单词书
	 *
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/getSelectedDicts.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getSelectedDicts(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// 获取用户已经选择的单词书
		User user = Util.getLoggedInUser();
		List<SelectedDict> dicts = user.getSelectedDicts();
		request.setAttribute("selectedDicts", dicts);

		// Po-->Vo
		List<SelectedDictVo> vos = new ArrayList<SelectedDictVo>();
		for (SelectedDict po : dicts) {
			SelectedDictVo vo = BeanUtils.makeVO(po, SelectedDictVo.class, new String[] { "user", "createTime",
					"lastUpdateTime", "UserVo.invitedBy", "dictWords", "studyGroups" });
			vos.add(vo);
		}
		Util.sendJson(vos, response);
	}

	@RequestMapping("/showEditDictPage.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ModelAndView showEditDictPage(HttpServletRequest request, HttpServletResponse response, String dictName) {
		Dict dict = Global.getDictBO().findById(dictName);
		request.setAttribute("dict", dict);
		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("editDict");
	}

	/**
	 * 获取自定义单词书中的所有单词（只含拼写、单词发音文件URL、例句发音文件URL）
	 *
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("getWordsOfDict.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getWordsOfDict(String dictName, int pageNo, int pageSize, HttpServletResponse response)
			throws IOException {
		PaginationResults<DictWord> dictWords = Global.getDictWordBO().getDictWords(dictName, pageNo, pageSize,
				"createTime asc");

		// Po-->Vo
		PaginationResults<WordVo> result = new PaginationResults<WordVo>();
		List<WordVo> vos = new ArrayList<WordVo>();
		for (DictWord dictWord : dictWords.getRows()) {
			WordVo vo = BeanUtils.makeVO(dictWord.getWord(), WordVo.class, new String[] { "lastUpdateTime" });
			vos.add(vo);
		}

		result.setTotal(dictWords.getTotal());
		result.setRows(vos);
		Util.sendJson(result, response);
	}

	/**
	 * 获取所有的系统单词书
	 *
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("getAllSysDicts.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getAllSysDicts(HttpServletResponse response) throws IOException {
		List<Dict> dicts = Global.getDictBO().getAllSysDicts();

		// Po-->Vo
		List<DictVo> vos = new ArrayList<DictVo>();
		for (Dict dict : dicts) {
			DictVo vo = BeanUtils.makeVO(dict, DictVo.class, new String[] { "owner", "dictWords" });
			vos.add(vo);
		}

		Util.sendJson(vos, response);
	}

	/**
	 * 向指定的单词书添加单词
	 */
	@RequestMapping("addWordToDict.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void addWordToDict(HttpServletResponse response, Integer dictId, Integer wordId)
			throws IOException, IllegalAccessException {
		Result<Object> result = Global.getDictWordBO().addWord(dictId, wordId, false);
		Util.sendJson(result, response);
	}

	/**
	 * 从一本单词书向另外一本单词书导入单词
	 */
	@RequestMapping("importFromDict.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void importFromDict(HttpServletResponse response, int fromDictId, int toDictId)
			throws IOException, IllegalAccessException {
		Result<Object> result = Global.getDictWordBO().importFromDict(fromDictId, toDictId);
		Util.sendJson(result, response);
	}

	/**
	 * 删除指定单词书中的指定单词
	 */
	@RequestMapping("removeWordFromDict.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void removeWordFromDict(HttpServletResponse response, Integer dictId, Integer wordId)
			throws IOException, IllegalAccessException {
		Result<Object> result = Global.getDictWordBO().deleteWord(dictId, wordId);
		Util.sendJson(result, response);
	}

	/**
	 * 清空单词书中的所有单词
	 */
	@RequestMapping("clearWordsOfDict.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void clearWordsOfDict(HttpServletResponse response, Integer dictId)
			throws IOException, IllegalAccessException {
		Result<Object> result = Global.getDictWordBO().clearWordsOfDict(dictId);
		Util.sendJson(result, response);
	}

	/**
	 * 完成对指定单词书的编辑
	 *
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@RequestMapping("finishEditingDict.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void finishEditingDict(HttpServletResponse response, Integer dictId)
			throws IOException, IllegalArgumentException, IllegalAccessException {
		Result<Object> result = Global.getDictBO().finishEditingDict(dictId);
		Util.sendJson(result, response);
	}

	/**
	 * 完成对指定单词书的编辑
	 *
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@RequestMapping("createNewDict.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void createNewDict(HttpServletResponse response, String dictName)
			throws IOException, IllegalArgumentException, IllegalAccessException {
		User user = Util.getLoggedInUser();
		Result<DictVo> result = Global.getDictBO().createNewDict(dictName, user);
		Util.sendJson(result, response);
	}

	@RequestMapping("getDictById.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getDictById(int dictId, HttpServletResponse response) throws IOException {
		Dict dict = Global.getDictBO().findById(dictId);
		DictVo dictVo = BeanUtils.makeVO(dict, DictVo.class,
				new String[] { "owner", "dict", "synonyms", "similarWords", "dictWords" });
		Util.sendJson(dictVo, response);
	}

	@RequestMapping("/getDictWordsForAPage.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void getDictWordsForAPage(HttpServletRequest request, HttpServletResponse response, int pageNo, int pageSize,
			int dictId) throws SQLException, NamingException, ClassNotFoundException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {

		DictWord exam = new DictWord();
		exam.setDict(Global.getDictBO().findById(dictId));

		DictWordBO bo = Global.getDictWordBO();
		bo.getDAO().setPreciseEntity(exam);
		bo.getDAO().setSortRules(SortRule.makeSortRules(new String[] { "createTime asc" }));

		PagedResults<DictWord> dictWords = bo.pagedQuery(pageNo, pageSize);
		PagedResults<DictWordVo> vos = BeanUtils.makePagedVos(dictWords, DictWordVo.class,
				new String[] { "dict", "synonyms", "similarWords" });

		Util.sendJson(vos, response);
	}
}
