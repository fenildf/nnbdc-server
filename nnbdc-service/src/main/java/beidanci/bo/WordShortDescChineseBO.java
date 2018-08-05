package beidanci.bo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import beidanci.Global;
import beidanci.SessionData;
import beidanci.dao.BaseDao;
import beidanci.po.Event;
import beidanci.po.User;
import beidanci.po.Word;
import beidanci.po.WordShortDescChinese;
import beidanci.util.BeanUtils;
import beidanci.vo.EventType;
import beidanci.vo.Result;
import beidanci.vo.UserVo;
import beidanci.vo.WordShortDescChineseVo;

@Service("WordShortDescChineseBO")
@Scope("prototype")
public class WordShortDescChineseBO extends BaseBo<WordShortDescChinese> {
	public WordShortDescChineseBO() {
		setDao(new BaseDao<WordShortDescChinese>() {
		});
	}

	/**
	 * 获取一个句子对应的前3个用户生成翻译
	 *
	 * @return
	 */
	public List<WordShortDescChineseVo> getWordShortDescChineses(Integer wordId, SessionData sessionData) {

		// 排序，被点赞绝对次数多的排在前面
		Word word = Global.getWordBO().findById(wordId);
		List<WordShortDescChinese> diyItems = word.getWordShortDescChineses();
		sortShortDescChineses(diyItems);

		int total = diyItems.size();
		total = total <= 3 ? total : 3;
		List<WordShortDescChineseVo> diyItemVOs = new ArrayList<WordShortDescChineseVo>();
		for (int i = 0; i < total; i++) {
			WordShortDescChinese po = diyItems.get(i);
			WordShortDescChineseVo vo = BeanUtils.makeVO(po, WordShortDescChineseVo.class,
					new String[] { "invitedBy", "word", "userGames", "studyGroups" });
			UserVo author = new UserVo();
			author.setDisplayNickName(po.getAuthor().getDisplayNickName());
			author.setUserName(po.getAuthor().getUserName());
			vo.setAuthor(author);

			// 判断是否被当前登录用户评价过？
			boolean hasBeenVoted = sessionData.getVotedWordShortDescChineses().contains(vo.getId());
			vo.setHasBeenVoted(hasBeenVoted);

			diyItemVOs.add(vo);
		}

		return diyItemVOs;
	}

	private static void sortShortDescChineses(List<WordShortDescChinese> wordImages) {
		Collections.sort(wordImages, new Comparator<WordShortDescChinese>() {
			@Override
			public int compare(WordShortDescChinese o1, WordShortDescChinese o2) {
				int score1 = o1.getHand() - o1.getFoot();
				int score2 = o2.getHand() - o2.getFoot();
				if (score1 == score2) {
					return (int) (o2.getCreateTime().getTime() - o1.getCreateTime().getTime());
				} else {
					return score2 - score1;
				}
			}
		});
	}

	public Result<Object> deleteShortDescChinese(int chineseId, User user, boolean checkPermission) {
		WordShortDescChinese diyItem = findById(chineseId);
		if (checkPermission) {
			if (!user.getIsAdmin() && (diyItem.getAuthor() == null
					|| !diyItem.getAuthor().getUserName().equalsIgnoreCase(user.getUserName()))) {
				return new Result<Object>(false, "无权限", null);
			}
		}

		// 删除相关的事件记录
		Event exam = new Event();
		exam.setWordShortDescChinese(diyItem);
		EventBO eventBO = Global.getEventBO();
		eventBO.getDAO().setPreciseEntity(exam);
		List<Event> events = eventBO.queryAll();
		for (Event event : events) {
			Global.getEventBO().deleteEntity(event);
		}

		// 删除数据库记录
		diyItem.getWord().getWordShortDescChineses().remove(diyItem);
		diyItem.setWord(null);
		deleteEntity(diyItem);

		return new Result<Object>(true, null, null);
	}

	public Result<Integer> handShortDescChinese(Integer chineseId, User user)
			throws IllegalArgumentException, IllegalAccessException {
		WordShortDescChinese chinese = findById(chineseId);
		chinese.setHand(chinese.getHand() + 1);
		updateEntity(chinese);

		// 对作者进行奖励
		Global.getUserBO().adjustCowDung(chinese.getAuthor(), 1, "单词英文讲解UGC翻译得到了赞");

		Event event = new Event(EventType.HandWordShortDescChinese, user, chinese);
		Global.getEventBO().createEntity(event);

		return new Result<Integer>(true, null, chinese.getHand());
	}

	public Result<Integer> footShortDescChinese(Integer imageId, User user)
			throws IllegalArgumentException, IllegalAccessException {
		WordShortDescChinese chinese = findById(imageId);
		chinese.setFoot(chinese.getFoot() + 1);
		updateEntity(chinese);

		// 如果该图片被踩的次数比被赞的次数多3（或以上），删除该图片
		if (chinese.getFoot() - chinese.getHand() >= 3) {
			deleteShortDescChinese(imageId, user, false);
		}

		Event event = new Event(EventType.FootWordShortDescChinese, user, chinese);
		Global.getEventBO().createEntity(event);

		return new Result<Integer>(true, null, chinese.getFoot());
	}

	public void saveUgcChinese(Integer wordId, String chinese, User user)
			throws IllegalArgumentException, IllegalAccessException {
		// 如果句子的DIY翻译已经大于等于3个了，则把最后一个删掉（末位淘汰制）
		Word word = Global.getWordBO().findById(wordId);
		List<WordShortDescChinese> chineses = word.getWordShortDescChineses();
		sortShortDescChineses(chineses);
		while (chineses.size() >= 3) {
			// 删除数据库记录
			WordShortDescChinese lastImage = chineses.remove(chineses.size() - 1);
			deleteShortDescChinese(lastImage.getId(), user, false);
		}

		// 添加新的UGC翻译
		WordShortDescChinese shortDescChinese = new WordShortDescChinese();
		shortDescChinese.setAuthor(user);
		shortDescChinese.setContent(chinese);
		shortDescChinese.setFoot(0);
		shortDescChinese.setHand(0);
		shortDescChinese.setWord(word);
		createEntity(shortDescChinese);

		chineses.add(shortDescChinese);
		Global.getWordBO().updateEntity(word);

		Event event = new Event(EventType.NewWordShortDescChinese, user, shortDescChinese);
		Global.getEventBO().createEntity(event);
	}
}
