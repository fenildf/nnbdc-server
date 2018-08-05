package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import beidanci.Global;
import beidanci.SessionData;
import beidanci.bo.LearningWordBO;
import beidanci.bo.UserBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.LearningWord;
import beidanci.po.LearningWordId;
import beidanci.po.User;
import beidanci.util.Util;
import beidanci.util.Utils;
import beidanci.vo.UserVo;
import beidanci.vo.WordVo;

@Controller
public class ShowBeforeBdcPage {
	private static final Logger log = LoggerFactory.getLogger(ShowBeforeBdcPage.class);

	@RequestMapping("/needSelectDictBeforeStudy.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void needSelectDictBeforeStudy(HttpServletResponse response) throws IOException {
		User user = Util.getLoggedInUser();
		Util.sendBooleanResponse(true, null, Util.needSelectDictBeforeStudy(user), response);
	}

	/**
	 * 准备今天要学习的单词
	 */
	@RequestMapping("/prepareForStudy.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void prepareForStudy(HttpServletRequest request, HttpServletResponse response)
			throws IllegalAccessException, ParseException, InvalidMeaningFormatException, IOException, SQLException,
			NamingException, EmptySpellException, ClassNotFoundException {
		int[] wordCounts = doPrepare(request);
		Util.sendBooleanResponse(true, null, wordCounts, response);
	}

	@RequestMapping("/showBeforeBdcPage.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException, IllegalArgumentException, IllegalAccessException, SQLException, ParseException,
			InvalidMeaningFormatException, EmptySpellException, NamingException, ClassNotFoundException {
		Util.setPageNoCache(response);

		// 如果用户还没有选择单词书，提示用户进行选择
		User user = Util.getLoggedInUser();
		if (Util.needSelectDictBeforeStudy(user)) {
			log.info(String.format("用户[%s]还未选择单词书，重定向到选择单词书页面", Util.getNickNameOfUser(user)));
			request.getRequestDispatcher("showDictPage.do?returnPage=showBeforeBdcPage.do").forward(request, response);
			return null;
		}

		// 准备今天要学习的单词
		int[] wordCounts = doPrepare(request);

		request.setAttribute("newWordCount", wordCounts[0]);
		request.setAttribute("oldWordCount", wordCounts[1]);
		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("beforebdc", "soundBaseUrl", Util.getSoundBaseUrl());
	}

	private int[] doPrepare(HttpServletRequest request) throws IllegalAccessException, ParseException, IOException,
			ClassNotFoundException, SQLException, NamingException, EmptySpellException, InvalidMeaningFormatException {
		UserVo userVo = Util.getLoggedInUserVO();
		synchronized (userVo) {// 避免用户快速多次点击学习按钮引起并发问题
			// 如果用户的最近学习日期不是今天，则重置相关数据
			SessionData sessionData = Util.getSessionData(request);
			UserBO userDAO = Global.getUserBO();
			User user = Util.getLoggedInUser();
			if (!Util.isSameDay(user.getLastLearningDate(), new Date())) {
				user.setLastLearningDate(Utils.getPureDate(new Date()));
				user.setLearnedDays(user.getLearnedDays() + 1);
				user.setLastLearningPosition(-1);
				user.setLastLearningMode(-1);
				user.setLearningFinished(false);
				user.getStageWords().clear();
				user.getWrongWords().clear();
				userDAO.updateEntity(user);
			}

			sessionData.setCurrentLearningWordIndex(-1);
			sessionData.setCurrentLearningMode(-1);

			// 生成今日要学习的单词列表
			List<LearningWord> todayWords = generateTodayWords(user);
			sessionData.setTodayWords(todayWords);

			// 计算新词(今天加入学习的词)数
			int newWordCount = 0;
			Date today = new Date();
			for (LearningWord word : todayWords) {
				if (Util.isSameDay(word.getAddTime(), today)) {
					newWordCount++;
				}
			}

			return new int[] { newWordCount, todayWords.size() - newWordCount };
		}
	}

	/**
	 * 产生今天要学习的单词列表，并把该列表更新到数据库
	 *
	 * @param user
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws ParseException
	 * @throws InvalidMeaningFormatException
	 * @throws EmptySpellException
	 * @throws NamingException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private List<LearningWord> generateTodayWords(User user)
			throws SQLException, IOException, ParseException, InvalidMeaningFormatException, EmptySpellException,
			NamingException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {

		// 尝试直接从数据库中读取今日学习单词(如果今日学习单词已经产生了)
		Date startTime = new Date();
		List<LearningWord> todayLearningWords = Global.getUserBO().getTodayLearningWords(user);
		if (todayLearningWords.size() > 0) {
			return todayLearningWords;
		}
		Date endTime = new Date();
		log.info("直接从数据中读取今日学习单词，耗时:" + (endTime.getTime() - startTime.getTime()));

		// 删除生命值为0的单词
		startTime = new Date();
		Global.getUserBO().deleteFinishedLearningWords(user);
		endTime = new Date();
		log.info("删除生命值为0的单词，耗时:" + (endTime.getTime() - startTime.getTime()));

		// 获取所有正在学习中的单词(将从他们中间选出今日学习的单词)
		startTime = new Date();
		List<LearningWord> allLearningWords = user.getLearningWords();
		endTime = new Date();
		log.info("获取所有正在学习中的单词，耗时:" + (endTime.getTime() - startTime.getTime()));

		// 通过查询最新加入到学习列表的单词，得知今天是第几天添加单词, 并且判断今天是否需要加入新单词（如果今天已经加过则不需要再加）
		startTime = new Date();
		LearningWord latestWord = null;
		for (LearningWord learningWord : allLearningWords) {
			if (latestWord == null || learningWord.getAddTime().after(latestWord.getAddTime())) {
				latestWord = learningWord;
			}
		}
		int todayDayNumber = 1;
		boolean needAddNewWords = true;
		if (latestWord != null) {
			if (Util.isSameDay(latestWord.getAddTime(), new Date())) {
				todayDayNumber = latestWord.getAddDay();
				needAddNewWords = false;
			} else {
				todayDayNumber = latestWord.getAddDay() + 1;
			}
		}
		endTime = new Date();
		log.info("判断今天是否需要加入新单词，耗时:" + (endTime.getTime() - startTime.getTime()));

		// 如果需要，添加新单词到learning words
		startTime = new Date();
		if (needAddNewWords) {
			List<LearningWord> newLearningWords = addNewLearningWords(user, allLearningWords, todayDayNumber);
			allLearningWords.addAll(newLearningWords);
		}
		endTime = new Date();
		log.info("如果需要，添加新单词到learning words，耗时:" + (endTime.getTime() - startTime.getTime()));

		// 取{ 0, 1, 3, 6, 14 }天之前加入的单词，正常情况下（没有bug，并且用户近期没有调整每日单词量）,
		// 这样取一遍就能得到足够的单词供本日学习了
		startTime = new Date();
		int[] fetchDays = new int[] { 0, 1, 3, 6, 14 };
		todayLearningWords = new ArrayList<LearningWord>();
		for (int day : fetchDays) {
			List<LearningWord> learningWordsOfADay = getLearningWordsAddedAtDay(todayDayNumber - day, allLearningWords);

			for (LearningWord word : learningWordsOfADay) {
				todayLearningWords.add(word);
				allLearningWords.remove(word);
				if (todayLearningWords.size() == user.getWordsPerDay()) {
					updateTodayLearningWords(todayLearningWords);
					return todayLearningWords;
				}
			}
		}
		endTime = new Date();
		log.info("取{ 0, 1, 3, 6, 14 }天之前加入的单词，耗时:" + (endTime.getTime() - startTime.getTime()));

		// 如果没有取到足够单词，则从最早的单词一直往前(较新单词的方向)取，这样一定能够取到足够单词（除非单词书中的单词耗尽了），因为:
		// (所有学习中单词的总生命值 L) = 29/5 * N(每日单词量), 所以学习中的单词总数至少有 L/5 = 29/(5*5) * N
		// > N
		startTime = new Date();
		while (todayLearningWords.size() < user.getWordsPerDay()) {
			LearningWord oldestWord = getOldestLearningWord(allLearningWords);

			// 取不到单词了，如果单词书中单词耗尽就会出现这样的情况
			if (oldestWord == null) {
				break;
			}

			todayLearningWords.add(oldestWord);
			allLearningWords.remove(oldestWord);
		}
		endTime = new Date();
		log.info("如果没有取到足够单词，则从最早的单词一直往前(较新单词的方向)取，耗时:" + (endTime.getTime() - startTime.getTime()));

		// 将今日的学习单词更新到数据库
		startTime = new Date();
		updateTodayLearningWords(todayLearningWords);
		endTime = new Date();
		log.info("将今日的学习单词更新到数据库，耗时:" + (endTime.getTime() - startTime.getTime()));

		return todayLearningWords;
	}

	/**
	 * 将今日的学习单词更新到数据库
	 *
	 * @param todayLearningWords
	 * @throws ClassNotFoundException
	 * @throws NamingException
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private void updateTodayLearningWords(List<LearningWord> todayLearningWords) throws SQLException, NamingException,
			ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		int learningOrder = 1;
		for (LearningWord learningWord : todayLearningWords) {
			learningWord.setLastLearningDate(new Date());
			learningWord.setLearningOrder(learningOrder);
			learningOrder++;
			Global.getLearningWordBO().updateEntity(learningWord);
		}
	}

	/**
	 * 获取最早加入的那些单词，越早加入的单词越靠前
	 *
	 * @return
	 */
	private LearningWord getOldestLearningWord(List<LearningWord> allLearningWords) {
		LearningWord oldestWord = null;
		for (LearningWord learningWord : allLearningWords) {
			if (oldestWord == null
					|| (Util.isSameDay(learningWord.getAddTime(), oldestWord.getAddTime())
							&& learningWord.getLifeValue() > oldestWord.getLifeValue())
					|| (learningWord.getAddTime().before(oldestWord.getAddTime())
							&& !Util.isSameDay(learningWord.getAddTime(), oldestWord.getAddTime()))) {
				oldestWord = learningWord;
			}
		}

		return oldestWord;
	}

	/**
	 * 获取指定的天数以前的那一天加入的learning words
	 *
	 * @param addDay
	 * @return
	 */
	private List<LearningWord> getLearningWordsAddedAtDay(int addDay, List<LearningWord> allLearningWords) {
		List<LearningWord> learningWords = new LinkedList<LearningWord>();

		// 获取该天添加的所有单词
		for (LearningWord learningWord : allLearningWords) {
			if (learningWord.getAddDay() == addDay) {
				learningWords.add(learningWord);
			}
		}

		// 对该天的单词进行排序，生命值大的排在前面，以便被优先选为本日学习单词
		Collections.sort(learningWords, new Comparator<LearningWord>() {
			@Override
			public int compare(LearningWord o1, LearningWord o2) {
				return o2.getLifeValue() - o1.getLifeValue();
			}
		});

		return learningWords;
	}

	/**
	 * 添加新单词到正在学习的单词列表（本日要学习的单词将从该列表选出）
	 *
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NamingException
	 * @throws EmptySpellException
	 * @throws InvalidMeaningFormatException
	 * @throws ParseException
	 * @throws IOException
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private List<LearningWord> addNewLearningWords(User user, final List<LearningWord> currentLearningWords,
			int todayDayNumber)
			throws SQLException, IOException, ParseException, InvalidMeaningFormatException, EmptySpellException,
			NamingException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
		List<LearningWord> newLearningWords = new LinkedList<LearningWord>();

		// 计算目前所有的 learning words 的总生命值
		int totalLifeValue = 0;
		for (LearningWord word : currentLearningWords) {
			totalLifeValue += word.getLifeValue();
		}

		// 計算期望的总生命值
		final int expectedTotalLifeValue = user.getWordsPerDay() * 29 / 5;

		// 如果需要，添加新的单词到 learning words 以达到期望的总生命值
		Date startTime = new Date();
		long time1 = 0;
		long time2 = 0;
		while (totalLifeValue < expectedTotalLifeValue && newLearningWords.size() < user.getWordsPerDay()) {
			Date s1 = new Date();
			WordVo newWord = Global.getUserBO().getNewWordToLearn(user);
			Date e1 = new Date();
			time1 += e1.getTime() - s1.getTime();

			s1 = new Date();
			// 如果用户单词书中已没有新词，只好返回当前添加的单词（不够也没有办法， 但是程序其他地方
			// 能够根据单词书状态得知用户已经学完了所有单词书，并给用户适当的提示）
			if (newWord == null) {
				break;
			}

			LearningWordBO learningWordBo = Global.getLearningWordBO();
			LearningWordId id = new LearningWordId(user.getId(), newWord.getId());

			// 检验该单词是否已经正在学习了(由于生词本，一个单词可能先后被取出多次)，如果是的话，则略过该单词
			LearningWord learningWord = learningWordBo.findById(id);
			if (learningWord == null) {
				learningWord = new LearningWord(id, user, new Timestamp(new Date().getTime()),
						new Integer(todayDayNumber), new Integer(5));
				learningWordBo.createEntity(learningWord);
				newLearningWords.add(learningWord);
				totalLifeValue += learningWord.getLifeValue();
			} else {// 该单词已经正在学习了
				continue;
			}
			e1 = new Date();
			time2 += e1.getTime() - s1.getTime();
		}
		Date endTime = new Date();
		log.info("time1，耗时：" + (time1));
		log.info("time2，耗时：" + (time2));
		log.info("从单词书取新词，耗时：" + (endTime.getTime() - startTime.getTime()));

		return newLearningWords;
	}
}
