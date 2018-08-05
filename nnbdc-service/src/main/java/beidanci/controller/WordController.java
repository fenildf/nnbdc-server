package beidanci.controller;

import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.SessionData;
import beidanci.bo.*;
import beidanci.dao.SortRule;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.learning.LearningMode;
import beidanci.po.*;
import beidanci.store.SentenceStore;
import beidanci.store.WordStore;
import beidanci.util.*;
import beidanci.vo.*;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;

@RestController
public class WordController {
	private static final Logger log = LoggerFactory.getLogger(WordController.class);

	@RequestMapping("/getWords.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getWords(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException, SQLException,
			NamingException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		SessionData sessionData = Util.getSessionData(request);
		UserBO userDAO = Global.getUserBO();
		User user = Util.getLoggedInUser();
		LearningWordBO learningWordDAO = Global.getLearningWordBO();
		List<LearningWord> todayWords = sessionData.getTodayWords();

		// 获取下一个单词进行学习
		// 如果今天要学习的单词数为0（用户学完了单词书的所有单词），返回特殊应答
		if (todayWords.size() == 0) {
			GetWordResult result = new GetWordResult(null, -1, null, null, null, false, true, /* 没有单词了 */null, null,
					null, null, false, null, null, null, null);
			// out.println(((JSONObject) JSONSerializer.toJSON(result,
			// makeJsonConfig())).toString());
			Util.sendJson(result, response);
			return;
		}

		// 将当前的学习位置持久化
		int currentWordIndex = sessionData.getCurrentLearningWordIndex();
		int currentLearningMode = sessionData.getCurrentLearningMode();
		LearningWord currentLearningWord = null;
		if (currentWordIndex != -1) {
			assert (currentLearningMode != -1);
			currentLearningWord = todayWords.get(currentWordIndex - currentLearningMode * 5);
			user.setLastLearningPosition(currentWordIndex);
			user.setLastLearningMode(currentLearningMode);
			userDAO.updateEntity(user);
		}

		// 如果当前单词用户选择了“已记牢”，则将其生命值设为0，今天及以后都不会再背了
		if (currentWordIndex != -1) {
			boolean isWordMastered = Boolean.parseBoolean(request.getParameter("isWordMastered"));
			if (isWordMastered) {
				currentLearningWord.setLifeValue(0);
				learningWordDAO.updateEntity(currentLearningWord);

				// 将用户掌握的单词数+1
				onWordMastered(currentLearningWord, userDAO, user);
			}
		}

		// 如果用户当前单词回答错了，将其记录到用户会话，以便背单词完成后提示用户
		if (currentWordIndex != -1) {
			assert (currentLearningMode != -1);
			boolean isAnswerCorrect = Boolean.parseBoolean(request.getParameter("isAnswerCorrect"));
			Word word = Global.getWordBO().findById(currentLearningWord.getWord().getId());
			if (!isAnswerCorrect && !user.getWrongWords().contains(word)) {
				user.getWrongWords().add(word);
				Global.getUserBO().updateEntity(user);
			}
		}

		// 如果当前单词已经完成了本日学习，并且没有答错过，则将其生命值减1
		if (currentLearningMode == LearningMode.MODE_COUNT - 1) {
			assert (currentWordIndex != -1);
			if (currentLearningWord.getLifeValue() > 0) {// 用户选择”已记牢“可以直接将单词的生命值设为0,所以需要这个判断
				Word word = Global.getWordBO().findById(currentLearningWord.getWord().getId());
				if (!user.getWrongWords().contains(word)) { // 用户没有答错过该单词
					currentLearningWord.setLifeValue(currentLearningWord.getLifeValue() - 1);
					learningWordDAO.updateEntity(currentLearningWord);

					// 如果单词的生命值降为0，则将用户已掌握单词数加1
					assert (currentLearningWord.getLifeValue() >= 0);
					if (currentLearningWord.getLifeValue() == 0) {
						onWordMastered(currentLearningWord, userDAO, user);
					}
				}
			}
		}

		// 如果客户端明确指出进入下一个学习阶段，则清空当前学习阶段的缓存
		boolean enterNextStage = Boolean.parseBoolean(request.getParameter("shouldEnterNextStage"));
		if (enterNextStage) {
			user.getStageWords().clear();
			Global.getUserBO().updateEntity(user);
		}

		// 如果当前学习阶段缓存已经达到了10个单词，则通知客户端进入阶段复习模式
		if (user.getStageWords().size() >= 10) {
			GetWordResult result = new GetWordResult(null, -1, null, null, null, false, false, null, null, null, null,
					true/* 进入阶段复习模式 */, null, null, null, null);
			// out.println(((JSONObject) JSONSerializer.toJSON(result,
			// makeJsonConfig())).toString());
			Util.sendJson(result, response);
			return;
		}

		// 如果这次要获取的是第一个单词， 则currentWordIndex 为-1，
		// 此时应将其设为用户本日的LastLearningWordPosition, 以便接着已经背过的地方继续背
		if (currentWordIndex == -1) {
			assert (currentLearningMode == -1);
			currentWordIndex = user.getLastLearningPosition();
			currentLearningMode = user.getLastLearningMode();
		}

		// 如果currentWordIndex 仍为-1，表明这是用户今天第一次开始背单词
		if (currentWordIndex == -1) {
			assert (currentLearningMode == -1);
			currentWordIndex = 0;
		}

		// 计算本日学习进度
		int[] learnProgress = new int[2];
		learnProgress[1] = todayWords.size();
		// 当前学习到第几个单词（估算值）
		learnProgress[0] = learnProgress[1] * (currentWordIndex + 1)
				/ (learnProgress[1] + 5 * (LearningMode.MODE_COUNT - 1));

		// 计算下一个单词的位置
		if (currentLearningMode == calculateEndMode(currentWordIndex)) {
			currentWordIndex += 1;
			currentLearningMode = 0;
			while (currentWordIndex - currentLearningMode * 5 >= todayWords.size()) {
				currentLearningMode += 1;
				if (currentLearningMode > calculateEndMode(currentWordIndex)) { // 今日任务完成,
					// 直接返回应答
					learnProgress[0]++;

					GetWordResult result = new GetWordResult(null, -1, null, learnProgress, null, true, false, null,
							null, null, null, false, null, null, null, null);
					// out.println(((JSONObject) JSONSerializer.toJSON(result,
					// makeJsonConfig())).toString());
					Util.sendJson(result, response);

					// 更新用户信息
					user.setLearningFinished(true);
					userDAO.updateEntity(user);
					return;
				}
			}
		} else {
			currentLearningMode += 1;
		}

		// 获取一个单词
		final int wordIndex = currentWordIndex - currentLearningMode * 5;
		LearningWord learningWord = todayWords.get(wordIndex);
		WordVo word = WordStore.getInstance().getWordById(learningWord.getId().getWordId());

		// 随机选择2个其他单词，以供用户选择
		WordVo[] otherWords = selectWordsRandomly(word, 2, todayWords);
		WordVo[] otherWords_ = new WordVo[otherWords.length];
		for (int i = 0; i < otherWords.length; i++) {
			WordVo wordVo = new WordVo();
			wordVo.setSpell(otherWords[i].getSpell());
			wordVo.setMeaningItems(otherWords[i].getMeaningItems());
			otherWords_[i] = wordVo;
		}

		// 获取词根
		String[] cigens = getCigensOfWord(learningWord.getWord().getId());

		// 获取图片
		WordImageVo[] images = Global.getWordImageBO().getImagesOfWord(learningWord.getWord().getId(),
				Util.getSessionData(request));

		// 获取各个时态（对于动词有效）
		VerbTenseVo[] verbTenses = getVerbTensesOfWord(learningWord.getWord().getId());

		// 获取用户编辑的单词相关信息
		WordAdditionalInfoVo[] infos = getAdditionalInfosOfWord(learningWord.getWord().getSpell(), user);

		// 单词报错信息
		List<ErrorReportVo> errorReports = getErrorReportsOfWord(learningWord.getWord().getSpell());

		// 单词信息
		LearningWordVo learningWordVo = BeanUtils.makeVO(learningWord, LearningWordVo.class,
				new String[] { "user", "word", "createTime", "updateTime", "addTime" });
		WordVo wordVo = learningWord.getWord();
		learningWordVo.setWord(wordVo);

		// 为例句附加UGC信息
		List<SentenceVo> adjustedSentenceVos = attatchUGCForSentences(request, wordVo.getSentences());

		// 单词英文描述的中文翻译（UGC）
		List<WordShortDescChineseVo> shortDescChineses = Global.getWordShortDescChineseBO()
				.getWordShortDescChineses(wordVo.getId(), Util.getSessionData(request));

		GetWordResult result = new GetWordResult(learningWordVo, currentLearningMode, otherWords_, learnProgress,
				Utils.getFileNameOfWordSound(wordVo.getSpell()), false, false, cigens, infos, errorReports,
				word.getShortDesc(), false, images, verbTenses, adjustedSentenceVos, shortDescChineses);

		// 将当前学习位置保存到会话中
		sessionData.setCurrentLearningWordIndex(currentWordIndex);
		sessionData.setCurrentLearningMode(currentLearningMode);

		// 将单词保存到当前学习阶段的缓存中
		if (result.getLearningMode() == 2) { // 每个单词每天会先后经历三个不同的学习模式（0：在句子中 1：只有拼写 2：只有发音）
			synchronized (user) {
				// 判断单词是否已经缓存了（避免用户刷新界面导致单词重复进入缓存）
				boolean isAlreadyInCache = false;
				for (Word cacheItem : user.getStageWords()) {
					if (cacheItem.getSpell().equals(result.getLearningWord().getWord().getSpell())) {
						isAlreadyInCache = true;
						break;
					}
				}

				if (!isAlreadyInCache) {
					user.getStageWords().add(Global.getWordBO().findById(learningWord.getWord().getId()));
					Global.getUserBO().updateEntity(user);
				}
			}
		}

		Util.sendJson(result, response);
		log.info(String.format("[%s] GetWords return [%s] ", Util.getNickNameOfUser(user), word.getSpell()));

	}

	@RequestMapping("/getAdditionalInfosOfWord.do")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public void getAdditionalInfosOfWord(HttpServletResponse response, String spell) throws IOException {
		WordAdditionalInfoVo[] infos = getAdditionalInfosOfWord(spell, Util.getLoggedInUser());
		Util.sendJson(infos, response);
	}

	/**
	 * 为列表中的每个例句附加UGC信息
	 *
	 * @param request
	 * @param sentenceVos
	 * @return
	 */
	private List<SentenceVo> attatchUGCForSentences(HttpServletRequest request, List<SentenceVo> sentenceVos) {
		List<SentenceVo> adjustedSentenceVos = new ArrayList<SentenceVo>();
		for (SentenceVo sentenceVo : sentenceVos) {
			List<SentenceDiyItemVo> diyItems = Global.getSentenceDiyItemBO().getSentenceDiyItems(sentenceVo.getId(),
					Util.getSessionData(request));
			SentenceVo adjustedSentenceVo = new SentenceVo();
			org.springframework.beans.BeanUtils.copyProperties(sentenceVo, adjustedSentenceVo,
					new String[] { "sentenceDiyItems" });
			adjustedSentenceVo.setSentenceDiyItems(diyItems);
			adjustedSentenceVos.add(adjustedSentenceVo);
		}
		return adjustedSentenceVos;
	}

	@RequestMapping("/getWordImages.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void getWordImages(HttpServletRequest request, HttpServletResponse response, Integer wordId)
			throws Exception {
		WordImageVo[] images = Global.getWordImageBO().getImagesOfWord(wordId, Util.getSessionData(request));
		beidanci.util.Util.sendJson(images, response);
	}

	@RequestMapping("/getCurrentStageCache.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void getCurrentStageCache(HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<Word> words = Util.getLoggedInUser().getStageWords();

		// Vo-->PO
		List<LearningWordVo> vos = new ArrayList<LearningWordVo>();
		for (Word word : words) {
			LearningWordId id = new LearningWordId(Util.getLoggedInUser().getId(), word.getId());
			LearningWord learningWord = Global.getLearningWordBO().findById(id);
			LearningWordVo vo = BeanUtils.makeVO(learningWord, LearningWordVo.class,
					new String[] { "user", "word", "sentences", "createTime", "lastUpdateTime" });
			WordVo wordVo = BeanUtils.makeVO(word, WordVo.class, new String[] { "sentences", "createTime",
					"lastUpdateTime", "SynonymVo.meaningItem", "SynonymVo.word", "similarWords" });
			vo.setWord(wordVo);
			vos.add(vo);
		}
		beidanci.util.Util.sendJson(vos, response);
	}

	@RequestMapping("/getWord.do")
	public void getWordCount(HttpServletRequest request, HttpServletResponse response, String spell) throws Exception {
		WordVo word = WordStore.getInstance().getWordBySpell(spell);
		beidanci.util.Util.sendJson(word, response);
	}

	@RequestMapping("/getWordCount.do")
	public void getWordCount(HttpServletRequest request, HttpServletResponse response) throws Exception {
		int count = WordStore.getInstance().getWordCount();
		beidanci.util.Util.sendBooleanResponse(true, null, count, response);
	}

	private void onWordMastered(LearningWord learningWord, UserBO userDAO, User user)
			throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException,
			IllegalArgumentException, IllegalAccessException {
		user.setMasteredWordsCount(user.getMasteredWordsCount() + 1);
		userDAO.updateEntity(user);

		// 将该单词从生词本删除
		RawWordBO rawWordDAO = Global.getRawWordBO();
		RawWord rawWord = rawWordDAO.findRawWordOfUser_ByWordId(user, learningWord.getWord().getId());
		if (rawWord != null) {
			rawWordDAO.deleteEntity(rawWord);
		}
	}

	/**
	 * 获取一个单词对应的所有词根信息（单数元素为词根，双数元素为单词相应与该词根的解释）
	 *
	 * @return
	 */
	private String[] getCigensOfWord(Integer wordId) {
		List<CigenWordLink> cigenWordLinks = Global.getCigenWordLinkBO().findByWordId(wordId);
		String[] cigens = new String[cigenWordLinks.size() * 2];
		for (int i = 0; i < cigenWordLinks.size(); i++) {
			cigens[i * 2] = cigenWordLinks.get(i).getCigen().getDescription();
			cigens[i * 2 + 1] = cigenWordLinks.get(i).getTheExplain();
		}
		return cigens;
	}

	/**
	 * 获取一个单词对应的各个时态（如果单词不是动词，返回空集）
	 *
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private VerbTenseVo[] getVerbTensesOfWord(Integer wordId) throws IllegalArgumentException, IllegalAccessException {
		Word word = Global.getWordBO().findById(wordId);
		int total = word.getVerbTences().size();
		VerbTenseVo[] tenses = new VerbTenseVo[total];
		for (int i = 0; i < tenses.length; i++) {
			VerbTense po = word.getVerbTences().get(i);
			VerbTenseVo vo = BeanUtils.makeVO(po, VerbTenseVo.class, new String[] { "createTime", "lastUpdateTime",
					"SynonymVo.meaningItem", "SynonymVo.word", "similarWords" });
			BeanUtils.setPropertiesToNull(vo.getWord(), new String[] { "spell" });
			tenses[i] = vo;
		}
		return tenses;
	}

	/**
	 * 获取一个单词对应的所有相关信息
	 *
	 * @return
	 */
	private WordAdditionalInfoVo[] getAdditionalInfosOfWord(String spell, User user) {
		List<WordAdditionalInfo> infos = Global.getWordAdditionalInfoBO().findByWordSpell(spell);

		// 排序（按照赞和踩的差值）
		Collections.sort(infos, new Comparator<WordAdditionalInfo>() {
			@Override
			public int compare(WordAdditionalInfo o1, WordAdditionalInfo o2) {
				return (o2.getHandCount() - o2.getFootCount()) - (o1.getHandCount() - o1.getFootCount());
			}
		});

		WordAdditionalInfoVo[] infoVOs = new WordAdditionalInfoVo[infos.size()];
		for (int i = 0; i < infos.size(); i++) {
			WordAdditionalInfo info = infos.get(i);

			// 判断我是否已经对该内容投过票了
			InfoVoteLogId voteLogID = new InfoVoteLogId(user.getId(), info.getId());
			InfoVoteLogBO voteLogDAO = Global.getInfoVoteLogBO();
			boolean isVotedByMe = voteLogDAO.findById(voteLogID) != null;

			WordAdditionalInfoVo infoVO = new WordAdditionalInfoVo();
			infoVO.setId(info.getId());
			infoVO.setWord(info.getWord().getSpell());
			infoVO.setContent(info.getContent());
			infoVO.setFootCount(info.getFootCount());
			infoVO.setHandCount(info.getHandCount());
			infoVO.setCreatedBy(info.getUser().getUserName());
			infoVO.setCreatedByNickName(Util.getNickNameOfUser(info.getUser()));
			infoVO.setVotedByMe(isVotedByMe);
			infoVOs[i] = infoVO;
		}
		return infoVOs;
	}

	private List<ErrorReportVo> getErrorReportsOfWord(String spell) {
		List<ErrorReport> reports = Global.getErrorReportBO().findByWordSpell(spell);
		Collections.sort(reports, new Comparator<ErrorReport>() {
			@Override
			public int compare(ErrorReport o1, ErrorReport o2) {
				return o1.getId() - o2.getId();
			}
		});

		List<ErrorReportVo> vos = new ArrayList<ErrorReportVo>();
		for (ErrorReport errorReport : reports) {
			ErrorReportVo vo = new ErrorReportVo(errorReport.getId(), errorReport.getUser().getUserName(),
					Util.getNickNameOfUser(errorReport.getUser()), errorReport.getContent(), spell,
					errorReport.getFixed());
			vos.add(vo);
		}
		return vos;
	}

	/**
	 * 从词库中随机选出N个单词的意思（以供用户选择）
	 *
	 * @return
	 * @throws EmptySpellException
	 * @throws InvalidMeaningFormatException
	 * @throws ParseException
	 * @throws IOException
	 */
	public static String[] selectWordMeaningsRandomly(WordVo otherThan, int count, final int lenLimit,
			List<LearningWord> todayWords)
			throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		WordVo[] words = selectWordsRandomly(otherThan, count, todayWords);
		String[] meanings = new String[count];
		for (int i = 0; i < count; i++) {
			// 如果单词释义太长，则舍弃一部分，否则界面拥挤
			String meaningStr = words[i].getMeaningStr();
			String[] parts = meaningStr.split("；");
			StringBuilder sb = new StringBuilder();
			for (String part : parts) {
				sb.append(part).append("；");
				if (sb.toString().length() >= lenLimit) {
					break;
				}
			}

			meanings[i] = sb.toString();
		}

		return meanings;
	}

	private static JsonConfig makeJsonConfig() {
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setExcludes(new String[] { "user", "lastLearningDate", "addTime" });
		return jsonConfig;
	}

	/**
	 * 从今天学习的单词中随机选择N个与指定单词不同的词
	 *
	 * @param otherThan
	 * @param count
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws InvalidMeaningFormatException
	 * @throws EmptySpellException
	 */
	public static WordVo[] selectWordsRandomly(WordVo otherThan, int count, List<LearningWord> todayWords)
			throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		WordVo[] words = new WordVo[count];

		if (todayWords.size() > count) {
			// 要排除的单词（避免选了重复单词）
			Set<String> otherThanWords = new HashSet<String>();
			otherThanWords.add(otherThan.getSpell());

			// 从今天学习的单词中随机选N个单词
			final int wordCount = todayWords.size();
			int selectedCount = 0;
			while (selectedCount < count) {
				int wordIndex = (int) (Math.random() * wordCount);

				// 循环遍历今天学习的单词，直到遇到一个未被选择的单词为止
				WordVo word = null;
				do {
					wordIndex++;
					if (wordIndex == wordCount) {
						wordIndex = 0;
					}
					word = todayWords.get(wordIndex).getWord();
				} while (otherThanWords.contains(word.getSpell()));

				words[selectedCount] = word;
				otherThanWords.add(word.getSpell());
				selectedCount++;
			}
		} else {
			// 随机选择一本单词书，将从其中选出N个单词
			List<Dict> allDicts = Global.getDictBO().queryAll();
			int dictCount = allDicts.size();
			int dictIndex = (int) (Math.random() * dictCount);
			if (dictIndex == dictCount) {
				dictIndex--;
			}
			Dict dict = allDicts.get(dictIndex);
			assert (dict.getWordCount() > count);

			// 要排除的单词（避免选了重复单词）
			Set<String> otherThanWords = new HashSet<String>();
			otherThanWords.add(otherThan.getSpell());

			// 从单词书中随机选N个单词
			final int wordCount = dict.getWordCount();
			int selectedCount = 0;
			while (selectedCount < count) {
				int wordIndex = (int) (Math.random() * wordCount);

				// 循环遍历单词书中的单词，直到遇到一个未被选择的单词为止
				WordVo word = null;
				do {
					wordIndex++;
					if (wordIndex == wordCount) {
						wordIndex = 0;
					}
					String spell = Global.getDictWordBO().getWordOfOrder(dict.getId(), wordIndex + 1).getSpell();
					word = WordStore.getInstance().getWordBySpell(spell);
				} while (otherThanWords.contains(word.getSpell()));

				words[selectedCount] = word;
				otherThanWords.add(word.getSpell());
				selectedCount++;
			}
		}

		return words;
	}

	private static int calculateEndMode(int wordIndex) {
		int endLearningMode = wordIndex / 5;
		if (endLearningMode > LearningMode.MODE_COUNT - 1) {
			endLearningMode = LearningMode.MODE_COUNT - 1;
		}
		return endLearningMode;
	}

	@RequestMapping("/searchWord.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void searchWord(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException, SQLException, NamingException, ClassNotFoundException {
		Util.setPageNoCache(response);
		response.setContentType("application/json");

		// 获取要查找的单词拼写
		Map<String, String[]> paramMap = request.getParameterMap();
		String spell = paramMap.get("word")[0].trim();

		spell = Utils.purifySpell(spell);

		if (StringUtils.isEmpty(spell)) {
			Util.sendJson(new SearchWordResult(null, null, null), response);
			return;
		}

		// 获取一个单词
		WordStore wordStore = WordStore.getInstance();
		WordVo word = wordStore.getWordBySpell(spell);
		if (word == null && spell.endsWith("s")) {// 如birds
			word = wordStore.getWordBySpell(spell.substring(0, spell.length() - 1));
		}
		if (word == null && spell.endsWith("es")) { // 如indexes
			word = wordStore.getWordBySpell(spell.substring(0, spell.length() - 2));
		}
		if (word == null && (spell.endsWith("'s") || spell.endsWith("’s"))) { // 如government’s
			word = wordStore.getWordBySpell(spell.substring(0, spell.length() - 2));
		}
		if (word == null && spell.endsWith("ies")) {// 如opportunities
			word = wordStore.getWordBySpell(spell.substring(0, spell.length() - 3) + "y");
		}
		if (word == null && spell.endsWith("ed")) {// 如tested
			word = wordStore.getWordBySpell(spell.substring(0, spell.length() - 2));
		}
		if (word == null && spell.endsWith("ed")) {// 如improved
			word = wordStore.getWordBySpell(spell.substring(0, spell.length() - 1));
		}
		if (word == null && spell.endsWith("ing")) {// 如testing
			word = wordStore.getWordBySpell(spell.substring(0, spell.length() - 3));
		}
		if (word == null && spell.endsWith("ing")) {// 如manufacturing
			word = wordStore.getWordBySpell(spell.substring(0, spell.length() - 3) + "e");
		}
		if (word == null) {
			word = wordStore.getWordBySpell(spell.toLowerCase());
		}
		if (word == null) {
			word = wordStore.getWordBySpell(spell.substring(0, 1).toUpperCase() + spell.substring(1));
		}

		if (word == null && Util.isStringEnglishWord(spell)) {
			try {
				word = syncWordFromYoudao(spell, wordStore);
			} catch (Exception e) {
				log.warn("从网络同步单词失败", e);
			}
		}

		SearchWordResult result;
		if (word != null) {
			// 为例句附加UGC信息
			List<SentenceVo> sentencesWithUGC = attatchUGCForSentences(request, word.getSentences());

			result = new SearchWordResult(word, Utils.getFileNameOfWordSound(word.getSpell()), sentencesWithUGC);
		} else {
			result = new SearchWordResult(null, null, null);
		}

		Util.sendJson(result, response);
	}

	private WordVo syncWordFromYoudao(String spell, WordStore wordStore) throws IOException {
		WordVo wordVo = YoudaoWordImporter.getWordFromYoudao(spell, SysParamUtil.getSoundPath());
		if (wordVo != null) {
			WordVo existingWord = wordStore.getWordBySpell(wordVo.getSpell());
			if (existingWord == null) {
				Word wordPO = new Word();
				wordPO.setSpell(wordVo.getSpell());
				wordPO.setAmericaPronounce(wordVo.getAmericaPronounce());
				wordPO.setBritishPronounce(wordVo.getBritishPronounce());
				wordPO.setPronounce(wordVo.getAmericaPronounce());
				wordPO.setGroupInfo(wordVo.getGroupInfo());
				List<MeaningItem> meaningItems = new ArrayList<MeaningItem>();
				wordPO.setMeaningItems(meaningItems);
				for (MeaningItemVo item : wordVo.getMeaningItems()) {
					MeaningItem itemPO = new MeaningItem();
					itemPO.setCiXing(item.getCiXing() == null ? "" : item.getCiXing());
					itemPO.setMeaning(item.getMeaning());
					List<Synonym> synonyms = new ArrayList<Synonym>();
					itemPO.setSynonyms(synonyms);
					itemPO.setWord(wordPO);
					meaningItems.add(itemPO);
				}
				wordPO.setPopularity(wordVo.getPopularity() == null ? 0 : wordVo.getPopularity());
				List<Sentence> sentences = new ArrayList<Sentence>();
				for (SentenceVo sentenceVo : wordVo.getSentences()) {
					SentenceBO sentenceBO = Global.getSentenceBO();
					Sentence sentence = sentenceBO.getSentenceByDigest(sentenceVo.getEnglishDigest());
					if (sentence == null) {
						sentence = new Sentence();
						sentence.setChinese(sentenceVo.getChinese());
						sentence.setEnglishDigest(sentenceVo.getEnglishDigest());
						sentence.setEnglish(sentenceVo.getEnglish());
						sentence.setTheType(sentenceVo.getTheType());
						Global.getSentenceBO().createEntity(sentence);
						sentenceVo.setId(sentence.getId());
						SentenceStore.getInstance().addSentence(sentenceVo);
						log.info(String.format("向例句库添加了单词[%s]的例句", wordPO.getSpell()));
					} else {
						sentenceVo.setId(sentence.getId());
					}
					sentences.add(sentence);
				}
				if (wordPO.isPhrase()) {
					wordPO.setSentences(sentences);
				}

				Global.getWordBO().createEntity(wordPO);
				wordVo.setId(wordPO.getId());
				wordStore.addWord(wordVo);
				log.info(String.format("向词库添加了单词[%s]", wordPO.getSpell()));
			} else {
				wordVo = existingWord;
			}
		}
		return wordVo;
	}

	@RequestMapping("/handImage.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void handImage(HttpServletRequest request, HttpServletResponse response, Integer id)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User user = Util.getLoggedInUser();
		WordImageBO bo = Global.getWordImageBO();
		Result<Integer> result = bo.handImage(id, user);
		Util.getSessionData(request).getVotedWordImages().add(id);// 阻止该用户再次对同一图片进行投票
		Util.sendAjaxResult(result, response);
	}

	@RequestMapping("/footImage.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void footImage(HttpServletRequest request, HttpServletResponse response, Integer id)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User user = Util.getLoggedInUser();
		WordImageBO bo = Global.getWordImageBO();
		Result<Integer> result = bo.footImage(id, user);
		Util.getSessionData(request).getVotedWordImages().add(id); // 阻止该用户再次对同一图片进行投票
		Util.sendAjaxResult(result, response);
	}

	@RequestMapping("/deleteImage.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void deleteImage(HttpServletRequest request, HttpServletResponse response, Integer id)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User user = Util.getLoggedInUser();
		WordImageBO bo = Global.getWordImageBO();
		Result<Object> result = bo.deleteWordImage(id, user, true);
		Util.sendAjaxResult(result, response);
	}

	@RequestMapping(value = "/uploadWordImg.do", method = RequestMethod.POST)
	public void uploadWordImg(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 验证用户是否已经登录(这里的验证逻辑比较特殊，没有直接使用request对象中的session。这是
		// 因为客户端上传组件在上传文件时会创建一个新的session)
		// String jsessionid = request.getParameter("jsessionid");
		// User user = Util.getUserBySessionId(jsessionid);
		User user = Util.getLoggedInUser();
		if (user == null) {
			Util.sendBooleanResponse(false, "用户未登录", null, response);
			return;
		}

		// 图片文件上传
		String fileName = null;
		Integer wordId = null;
		File targetFile = null;
		if (ServletFileUpload.isMultipartContent(request)) {
			DiskFileItemFactory dff = new DiskFileItemFactory();// 创建该对象
			dff.setRepository(new File(SysParamUtil.getTempDirForUpload()));// 指定上传文件的临时目录
			dff.setSizeThreshold(1024 * 1024);// 指定在内存中缓存数据大小,单位为byte
			ServletFileUpload sfu = new ServletFileUpload(dff);// 创建该对象
			sfu.setFileSizeMax(2 * 1024 * 1024);// 指定单个上传文件的最大尺寸
			sfu.setSizeMax(2 * 1024 * 1024);// 指定一次上传多个文件的总尺寸
			FileItemIterator fii = sfu.getItemIterator(request);// 解析request请求,并返回FileItemIterator集合

			while (fii.hasNext()) {
				FileItemStream fis = fii.next();// 从集合中获得一个文件流

				// 获取单词的拼写
				if (fis.getFieldName().equals("word")) {
					InputStream stream = fis.openStream();
					wordId = Integer.parseInt(Streams.asString(stream));
					fileName = wordId + "_" + System.currentTimeMillis() + ".jpg";
					targetFile = new File(SysParamUtil.getImageBaseDir() + "/word/" + fileName);
				}

				if (!fis.isFormField() && fis.getName().length() > 0) {// 过滤掉表单中非文件域
					BufferedInputStream in = new BufferedInputStream(fis.openStream());// 获得文件输入流
					File tempTargetFile = new File(SysParamUtil.getImageBaseDir() + "/tmp/tmp_" + fileName);
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempTargetFile));// 获得文件输出流
					Streams.copy(in, out, true);// 开始把文件写到你指定的上传文件夹

					// 通过图像缩放生成大图
					int targetWidth = 120;
					int targetHeight = 120;
					MyImage.resizeImage(tempTargetFile, targetFile, targetWidth, targetHeight, "JPEG");

					// 删除临时文件
					if (!tempTargetFile.delete()) {
						tempTargetFile.deleteOnExit();
					}

					// 更新数据库
					Word word2 = Global.getWordBO().findById(wordId);
					WordImage wordImage = new WordImage(word2, fileName, 0, 0, user);
					Global.getWordImageBO().addWordImage(wordImage, user);

					break;// 只支持上传一个文件
				}
			}
		}

		// Send result back to client.
		Util.sendBooleanResponse(true, null, fileName, response);
	}

	@RequestMapping("/showRawWordPage.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ModelAndView showRawWordPage(HttpServletRequest request, HttpServletResponse response)
			throws ClassNotFoundException, SQLException, NamingException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {
		User user = Util.getLoggedInUser();

		// 获取生词本中的生词数
		RawWordBO rawWordDAO = Global.getRawWordBO();
		int rawWordCount = rawWordDAO.findByUser(user).size();
		request.setAttribute("rawWordCount", rawWordCount);

		Util.setCommonAttributesForShowingJSP(request);

		return new ModelAndView("rawWord");

	}

	@RequestMapping("/saveWordShortDescChinese.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void saveWordShortDescChinese(HttpServletRequest request, HttpServletResponse response, Integer wordId,
			String chinese) throws SQLException, InterruptedException, NamingException, ClassNotFoundException,
			IOException, IllegalArgumentException, IllegalAccessException {
		Global.getWordShortDescChineseBO().saveUgcChinese(wordId, chinese, Util.getLoggedInUser());

		Util.sendBooleanResponse(true, null, null, response);
	}

	@RequestMapping("/getShortDescChinesesOfWord.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void getShortDescChinesesOfWord(HttpServletRequest request, HttpServletResponse response, Integer wordId)
			throws SQLException, InterruptedException, NamingException, ClassNotFoundException, IOException,
			IllegalArgumentException, IllegalAccessException {
		List<WordShortDescChineseVo> chineses = Global.getWordShortDescChineseBO().getWordShortDescChineses(wordId,
				Util.getSessionData(request));

		Util.sendJson(chineses, response);
	}

	@RequestMapping("/handShortDescChinese.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void handShortDescChinese(HttpServletRequest request, HttpServletResponse response, Integer id)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User user = Util.getLoggedInUser();
		WordShortDescChineseBO bo = Global.getWordShortDescChineseBO();
		Result<Integer> result = bo.handShortDescChinese(id, user);
		Util.getSessionData(request).getVotedWordShortDescChineses().add(id);// 阻止该用户再次对同一内容进行投票
		Util.sendAjaxResult(result, response);
	}

	@RequestMapping("/footShortDescChinese.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void footShortDescChinese(HttpServletRequest request, HttpServletResponse response, Integer id)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User user = Util.getLoggedInUser();
		WordShortDescChineseBO bo = Global.getWordShortDescChineseBO();
		Result<Integer> result = bo.footShortDescChinese(id, user);
		Util.getSessionData(request).getVotedWordShortDescChineses().add(id); // 阻止该用户再次对同一内容进行投票
		Util.sendAjaxResult(result, response);
	}

	@RequestMapping("/deleteShortDescChinese.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void deleteShortDescChinese(HttpServletRequest request, HttpServletResponse response, Integer id)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User user = Util.getLoggedInUser();
		WordShortDescChineseBO bo = Global.getWordShortDescChineseBO();
		Result<Object> result = bo.deleteShortDescChinese(id, user, true);
		Util.sendAjaxResult(result, response);
	}

	@RequestMapping("/getRawWordsForAPage.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void getRawWordsForAPage(HttpServletRequest request, HttpServletResponse response, int pageNo, int pageSize)
			throws SQLException, NamingException, ClassNotFoundException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {

		RawWord exam = new RawWord();
		exam.setUser(Util.getLoggedInUser());

		RawWordBO bo = Global.getRawWordBO();
		bo.getDAO().setPreciseEntity(exam);
		bo.getDAO().setSortRules(SortRule.makeSortRules(new String[] { "id desc" }));

		PagedResults<RawWord> rawWords = bo.pagedQuery(pageNo, pageSize);
		PagedResults<RawWordVo> vos = BeanUtils.makePagedVos(rawWords, RawWordVo.class, new String[] { "user",
				"sentences", "synonyms", "shortDesc", "createTime", "updateTime", "similarWords" });

		Util.sendJson(vos, response);
	}

	@RequestMapping("/deleteRawWord.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public Result deleteRawWord(HttpServletRequest request, HttpServletResponse response, Integer id)
			throws SQLException, NamingException, ClassNotFoundException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		// 从生词本中删除指定单词
		RawWordBO rawWordDAO = Global.getRawWordBO();
		rawWordDAO.deleteById(id);
		return Result.SUCCESS;
	}

	@RequestMapping("/exportWordsForAndroid.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void exportWordsForAndroid(HttpServletRequest request, HttpServletResponse response) throws IOException {
		List<Word> allWords = Global.getWordBO().getAllWords();

		// 把单词库导出到文本文件
		File file = new File(SysParamUtil.getExportFileDir() + "/words.json");
		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		List<Map<String, Object>> wordObjects = new ArrayList<>();
		for (Word word : allWords) {
			Map<String, Object> wordObject = new HashMap<>();
			wordObject.put("spell", word.getSpell());
			wordObject.put("meaningStr", word.getMeaningStr());
			wordObjects.add(wordObject);
		}
		String json = (JSONSerializer.toJSON(wordObjects)).toString();
		json = json.replaceAll("\"spell\":null,", "\"spell\":\"null\",");
		out.write(json);
		out.flush();
		out.close();
		Util.sendBooleanResponse(true, null, SysParamUtil.getExportFileUrl() + "/words.json", response);
		log.info("词库文件已导出到：" + file.getAbsoluteFile());
	}

	@RequestMapping("/getWordById.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void getWordById(HttpServletRequest request, HttpServletResponse response, Integer id)
			throws SQLException, NamingException, ClassNotFoundException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {
		Util.sendJson(WordStore.getInstance().getWordById(id), response);
	}

	@RequestMapping("/getWordBySpell.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void getWordBySpell(HttpServletRequest request, HttpServletResponse response, String spell)
			throws SQLException, NamingException, ClassNotFoundException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {
		Util.sendJson(WordStore.getInstance().getWordBySpell(spell), response);
	}

	@RequestMapping("/updateWord.do")
	@PreAuthorize("hasAnyRole('INPUTOR')")
	public void updateWord(HttpServletRequest request, HttpServletResponse response, @RequestBody WordVo word)
			throws SQLException, NamingException, ClassNotFoundException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException, IllegalAccessException, IntrospectionException,
			InvocationTargetException {
		String msg = Global.getWordBO().updateWord(word);
		Util.sendBooleanResponse(msg == null, msg, WordStore.getInstance().getWordById(word.getId()), response);
	}
}
