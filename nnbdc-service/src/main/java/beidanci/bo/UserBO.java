package beidanci.bo;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.stereotype.Service;

import beidanci.Global;
import beidanci.dao.BaseDao;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.*;
import beidanci.store.WordStore;
import beidanci.util.BeanUtils;
import beidanci.util.SysParamUtil;
import beidanci.util.Util;
import beidanci.vo.Result;
import beidanci.vo.UserVo;
import beidanci.vo.WordVo;

@Service("UserBO")
@Scope("prototype")
public class UserBO extends BaseBo<User> {
	private static final Logger log = LoggerFactory.getLogger(UserBO.class);
	private static volatile User sysUser = null;

	public UserBO() {
		setDao(new BaseDao<User>() {
		});
	}

	public User getSysUser() {
		if (sysUser == null) {
			sysUser = getByUserName(SYS);
		}
		return sysUser;
	}

	@SuppressWarnings("unchecked")
	public List<User> findUsersTotalScoreMoreThan(int score, boolean includeGuest) {
		String queryString;
		if (includeGuest) {
			queryString = "from User u left join fetch u.userGames as userGames where  (exists elements(u.userGames) or exists elements(u.dakas))";
		} else {
			queryString = "from User u left join fetch u.userGames as userGames where u.userName not like 'guest%' and u.userName not like 'guess%' and u.userName not like '游客%' and (exists elements(u.userGames) or exists elements(u.dakas))";
		}

		Session session = getSession();
		Query query = session.createQuery(queryString);
		return query.list();
	}

	public static String SYS = "sys";

	private Query query;

	public void deleteUnStartedDicts(User user, HashSet<Integer> exceptFor)
			throws IllegalArgumentException, IllegalAccessException {
		for (Iterator<LearningDict> i = user.getLearningDicts().iterator(); i.hasNext();) {
			LearningDict learningDict = i.next();
			if (learningDict.getCurrentWord() == null && !exceptFor.contains(learningDict.getDict().getId())) {
				Global.getLearningDictBO().deleteEntity(learningDict);
				i.remove();
			}
		}
	}

	public void deleteFinishedLearningWords(User user) throws IllegalArgumentException, IllegalAccessException {
		for (Iterator<LearningWord> i = user.getLearningWords().iterator(); i.hasNext();) {
			LearningWord learningWord = i.next();
			if (learningWord.getLifeValue() == 0) {
				Global.getLearningWordBO().deleteEntity(learningWord);
				i.remove();
			}
		}
		updateEntity(user);
	}

	/**
	 * 删除用户收藏的某本单词书，如果该单词书还没有开始学习，则也从正在学习的单词书中删除
	 *
	 * @param user
	 * @param dictName
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public void deleteSelectedDict(User user, String dictName) throws IllegalArgumentException, IllegalAccessException {
		for (Iterator<SelectedDict> i = user.getSelectedDicts().iterator(); i.hasNext();) {
			SelectedDict selectedDict = i.next();
			if (selectedDict.getDict().getName().equals(dictName)) {
				Global.getSelectedDictBO().deleteEntity(selectedDict);
				i.remove();
			}
		}

		for (Iterator<LearningDict> i = user.getLearningDicts().iterator(); i.hasNext();) {
			LearningDict learningDict = i.next();
			if (learningDict.getDict().getName().equals(dictName) && learningDict.getCurrentWord() == null) {
				Global.getLearningDictBO().deleteEntity(learningDict);
				i.remove();
			}
		}
		updateEntity(user);
	}

	/**
	 * 从数据库中获取已生成的用户今天要学习的单词列表
	 *
	 * @param user
	 * @return
	 */
	public List<LearningWord> getTodayLearningWords(User user) {
		List<LearningWord> learningWords = new LinkedList<LearningWord>();
		for (LearningWord learningWord : (List<LearningWord>) user.getLearningWords()) {
			if (Util.isSameDay(learningWord.getLastLearningDate(), new Date())) {
				learningWords.add(learningWord);
			}
		}

		Collections.sort(learningWords, new Comparator<LearningWord>() {
			@Override
			public int compare(LearningWord o1, LearningWord o2) {
				return o1.getLearningOrder() - o2.getLearningOrder();
			}
		});

		return learningWords;
	}

	/**
	 * 随机从指定的某本单词书中取一个单词。
	 *
	 * @param learningDicts
	 *            单词书列表，将从中随机选出一本，并取一个单词。注意，指定的单词书中可能也包含生词本（生词本被模拟成一本特殊的单词书）
	 * @return
	 * @throws EmptySpellException
	 * @throws InvalidMeaningFormatException
	 * @throws ParseException
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private WordVo getNewWordFromDicts(List<LearningDict> learningDicts, User user) throws IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException, IllegalArgumentException, IllegalAccessException {

		// 将单词书打乱次序，模拟随机从某本单词书取词的效果
		Collections.shuffle(learningDicts);

		// 从当前学习的某本单词书中取下一个单词.(注意：某些当前学习的单词书可能已经被用户取消选中，不能从这些单词书中继续选词了)
		WordVo wordToLearn = null;
		for (LearningDict learningDict : learningDicts) {

			if (learningDict.getDict().getName().equals("生词本")) { // 从生词本取词
				RawWordBO rawWordBo = Global.getRawWordBO();
				List<RawWord> rawWords = rawWordBo.getPage(user, 0, 1);
				if (rawWords.size() > 0) {
					RawWord rawWord = rawWords.get(0);
					WordVo word = WordStore.getInstance().getWordById(rawWord.getWord().getId());

					// 从生词本取出单词后，立即删除该单词
					rawWordBo.deleteEntity(rawWord);

					return word;
				}
			} else {// 从单词书取词
				// 获取该单词书当前的学习位置
				// Word currentWord = learningDict.getCurrentWord();
				Integer wordOrderInDict = learningDict.getCurrentWordOrder();
				if (wordOrderInDict == null) {// 尚未开始学习该单词书
					wordOrderInDict = 0;
				}

				// 如果该单词书尚未被学完，则取当前单词的下一个单词，并更新当前单词
				Dict realDict = Global.getDictBO().findById(learningDict.getDict().getId());
				while (wordOrderInDict < realDict.getWordCount()) {
					// 从单词书中取下一个单词
					Word nextWord = Global.getDictWordBO().getWordOfOrder(learningDict.getDict().getId(),
							wordOrderInDict + 1);

					// 判断该单词是否已经取出过
					List<LearningDict> allLearningDicts = new ArrayList<LearningDict>(user.getLearningDicts());// 用户所有学习中的单词书(包括当前并未选中的)
					boolean isLearned = isWordLearned(nextWord.getId(), allLearningDicts);

					// 更新该单词书的当前单词
					wordToLearn = WordStore.getInstance().getWordBySpell(nextWord.getSpell());
					learningDict.setCurrentWord(nextWord);
					learningDict.setCurrentWordOrder(wordOrderInDict + 1);
					Global.getLearningDictBO().updateEntity(learningDict);

					// 如果该单词已经学习过，则略过, 否则返回该单词
					if (isLearned) {
						wordOrderInDict++;
						continue;
					} else {
						return wordToLearn;
					}
				}
			}
		}

		return null;
	}

	/**
	 * 从用户的某本单词书中选出一个单词学习
	 *
	 * @return 某个未学过的单词，如果所有单词都学过，return null.
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
	public WordVo getNewWordToLearn(User user)
			throws SQLException, IOException, ParseException, InvalidMeaningFormatException, EmptySpellException,
			NamingException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException {

		// 从高优先级单词书中随机取一个单词
		List<LearningDict> learningDicts = getLearningDictsWithPriority(user, true);
		WordVo word = getNewWordFromDicts(learningDicts, user);

		// 从普通优先级单词书中随机取一个单词
		if (word == null) {
			learningDicts = getLearningDictsWithPriority(user, false);
			word = getNewWordFromDicts(learningDicts, user);
		}

		return word;
	}

	/**
	 * 获取指定优先级的所有学习中单词书
	 *
	 * @param user
	 * @param isHighPriority
	 *            true：获取高优先级的单词书，false：获取普通优先级的单词书
	 * @return 指定优先级的所有学习中单词书（已过滤掉用户取消选中的单词书，另外生词本可能被模拟成一本特殊单词书）
	 */
	private List<LearningDict> getLearningDictsWithPriority(User user, boolean isHighPriority) {
		// 获取用户所有学习中的单词书
		List<LearningDict> learningDicts = new ArrayList<LearningDict>(user.getLearningDicts());

		// 选出指定优先级的单词书
		for (Iterator<LearningDict> i = learningDicts.iterator(); i.hasNext();) {
			LearningDict learningDict = i.next();
			SelectedDictId id = new SelectedDictId(user.getId(), learningDict.getDict().getId());
			SelectedDict selectedDict = Global.getSelectedDictBO().findById(id);

			if (selectedDict == null) {// 单词书已经取消了选中
				i.remove();
			} else if (selectedDict.getIsPrivileged() != isHighPriority) {// 单词书不是指定的优先级
				i.remove();
			}
		}

		// 将生词本模拟成一本单词书
		if (user.getIsRawWordBookPrivileged() == isHighPriority) {
			LearningDict fakeDict = new LearningDict();
			Dict dict = new Dict();
			dict.setName("生词本");
			fakeDict.setDict(dict);
			learningDicts.add(fakeDict);
		}

		return learningDicts;
	}

	/**
	 * 判断某个单词是否已经被该用户从任何一本单词书中取出过
	 *
	 * @param learningDicts
	 * @return
	 */
	private static boolean isWordLearned(Integer wordId, List<LearningDict> learningDicts) {

		for (LearningDict dict : learningDicts) {
			int wordOrder = Global.getDictWordBO().getOrderOfWord(dict.getDict().getId(), wordId);
			if (wordOrder != -1
					&& wordOrder <= (dict.getCurrentWordOrder() == null ? -1 : dict.getCurrentWordOrder())) {
				return true;
			}
		}
		return false;
	}

	public void deleteUser(User user) throws IllegalArgumentException, IllegalAccessException {
		// 删除用户选择的单词书
		for (SelectedDict dict : user.getSelectedDicts()) {
			Global.getSelectedDictBO().deleteEntity(dict);
		}
		user.getSelectedDicts().clear();
		updateEntity(user);

		// 删除用户正在学习的单词书
		for (LearningDict dict : user.getLearningDicts()) {
			Global.getLearningDictBO().deleteEntity(dict);
		}
		user.getLearningDicts().clear();
		updateEntity(user);

		// 删除用户正在学习的单词
		for (LearningWord word : user.getLearningWords()) {
			Global.getLearningWordBO().deleteEntity(word);
		}
		user.getLearningWords().clear();
		updateEntity(user);

		// 删除用户提过的意见
		for (Msg msg : user.getSentMsgs()) {
			Global.getMsgBO().deleteEntity(msg);
		}
		user.getSentMsgs().clear();
		updateEntity(user);

		// 删除用户的打卡记录
		for (Daka daka : user.getDakas()) {
			Global.getDakaBO().deleteEntity(daka);
		}
		user.getDakas().clear();
		updateEntity(user);

		// 删除用户的牛粪收支记录
		for (UserCowDungLog userCowDungLog : user.getUserCowDungLogs()) {
			Global.getUserCowDungLogBO().deleteEntity(userCowDungLog);
		}
		user.getUserCowDungLogs().clear();
		updateEntity(user);

		// 删除用户的游戏记录
		for (UserGame userGame : user.getUserGames()) {
			Global.getUserGameBO().deleteEntity(userGame);
		}
		user.getUserGames().clear();
		updateEntity(user);

		// 删除用户每日快照记录
		for (UserSnapshotDaily userLearnProgress : user.getUserSnapshotDailys()) {
			Global.getUserSnapshotDailyBO().deleteEntity(userLearnProgress);
		}
		user.getUserSnapshotDailys().clear();
		updateEntity(user);

		// 解除该用户邀请的用户对其的引用
		for (User invitedUser : user.getInvitedUsers()) {
			invitedUser.setInvitedBy(findById("nulluser"));
			updateEntity(invitedUser);
		}
		user.getInvitedUsers().clear();
		updateEntity(user);

		// 退出所在的小组
		for (StudyGroup group : user.getCreatedStudyGroups()) {
			user.exitGroup(group.getId());
		}
		for (StudyGroup group : user.getStudyGroups()) {
			user.exitGroup(group.getId());
		}
		for (StudyGroup group : user.getManagedStudyGroups()) {
			user.exitGroup(group.getId());
		}

		// 删除用户记录
		deleteEntity(user);
	}

	@SuppressWarnings("unchecked")
	public List<User> findByEmail(String email) {
		String hql = "from User u where email = :email";
		Session session = getSession();
		query = session.createQuery(hql);
		query.setParameter("email", email);
		return query.list();
	}

	public User getByUserName(String userName) {
		String hql = "from User u where userName = :userName";
		Session session = getSession();
		query = session.createQuery(hql);
		query.setParameter("userName", userName);
		return (User) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<User> findAll() {
		String hql = "from User";
		Session session = getSession();
		query = session.createQuery(hql);
		return query.list();
	}

	public Result<Authentication> doLogin(String userName, String passwordFromClient, String loginType,
			HttpServletRequest request, HttpServletResponse response)
			throws IllegalArgumentException, IllegalAccessException {
		User user = getByUserName(userName);
		if (user == null) {
			return new Result(false, "用户名不存在", null);
		}

		// 登录
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userName,
				passwordFromClient);
		Authentication authenticatedUser;
		try {
			authenticatedUser = Global.getAuthenticationManager().authenticate(token);
			SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
			SessionAuthenticationStrategy sessionAuthenticationStrategy = Global.getSessionAuthenticationStrategy();
			sessionAuthenticationStrategy.onAuthentication(authenticatedUser, request, response);

			// 更新最后登录时间，保存登录日志
			if (user != null) {
				user.setLastLoginTime(new Date());
				Global.getUserBO().updateEntity(user);

				// 保存登录日志
				LoginLog loginLog = new LoginLog(Global.getUserBO().getByUserName(user.getUserName()), new Date());
				Global.getLoginLogBO().createEntity(loginLog);
			}
		} catch (BadCredentialsException e) {
			authenticatedUser = null;
		}
		return new Result<Authentication>(authenticatedUser != null, authenticatedUser == null ? "用户名或密码错误" : null,
				authenticatedUser);
	}

	public void doLogout(HttpServletRequest request) throws ServletException {
		request.logout();
		request.getSession().invalidate();
	}

	/**
	 * 保存掷骰子得到的牛粪奖励
	 * 
	 * @param delta
	 * @param reason
	 * @param user
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public String saveCowDungOfThrowingDice(int delta, String reason, User user)
			throws IllegalArgumentException, IllegalAccessException {
		if (delta == 20) { // 用户摇到了最大牛粪数，翻倍
			delta *= 2;
		}

		// 根据配置对牛粪数乘以一个倍数(节假日)
		delta *= SysParamUtil.getHolidayCowDungRatio();

		// 如果用户是因为掷骰子得到牛粪，将掷骰子机会数量减 1
		if (reason.equals("throw dice after learning")) {
			// 如果用户掷骰子的机会数都为0了，用户还在掷骰子，这样的情况应该不存在，
			// 但也可能是客户端采取了某些特殊手段
			if (user.getThrowDiceChance() == 0) {
				log.warn("发现异常情况：用户掷骰子的机会数都为0了，用户还在掷骰子, user: " + user.getUserName());
				return "保存牛粪失败";
			}

			user.setThrowDiceChance(user.getThrowDiceChance() - 1);
			updateEntity(user);

			log.info(String.format("用户[%s]打卡后掷骰子得到[%d]个牛粪", Util.getNickNameOfUser(user), delta));
		}

		// 更新用户的牛粪数
		adjustCowDung(user, delta, reason);

		return null;
	}

	public void saveWordsPerDay(User user, int wordsPerDay) throws IllegalAccessException {
		user.setWordsPerDay(wordsPerDay);
		updateEntity(user);
	}

	public UserVo getUserVoById(Integer userId) {
		User user = findById(userId);
		if (user == null) {
			return null;
		}

		UserVo userVo = BeanUtils.makeVO(user, UserVo.class, new String[] { "invitedBy", "StudyGroupVo.creator",
				"StudyGroupVo.users", "StudyGroupVo.managers", "StudyGroupVo.studyGroupPosts", "UserGameVo.user" });
		return userVo;
	}

	public void adjustCowDung(User user, int delta, String reason) throws IllegalAccessException {
		UserCowDungLogBO userCowDungLogDAO = Global.getUserCowDungLogBO();
		int currCowDung = user.getCowDung();
		UserCowDungLog userCowDungLog = new UserCowDungLog(user, delta, currCowDung + delta,
				new Timestamp(new Date().getTime()), reason);
		userCowDungLogDAO.createEntity(userCowDungLog);
		user.setCowDung(currCowDung + delta);
		updateEntity(user);
	}
}
