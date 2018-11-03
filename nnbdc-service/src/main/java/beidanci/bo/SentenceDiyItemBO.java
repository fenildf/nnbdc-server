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
import beidanci.po.*;
import beidanci.util.BeanUtils;
import beidanci.vo.EventType;
import beidanci.vo.Result;
import beidanci.vo.SentenceDiyItemVo;
import beidanci.vo.UserVo;

@Service("SentenceDiyItemBO")
@Scope("prototype")
public class SentenceDiyItemBO extends BaseBo<SentenceDiyItem> {
	public SentenceDiyItemBO() {
		setDao(new BaseDao<SentenceDiyItem>() {
		});
	}

	public void saveDiyItem(Integer sentenceId, String chinese, User user)
			throws IllegalArgumentException, IllegalAccessException {
		// 如果句子的DIY翻译已经大于等于3个了，则把最后一个删掉（末位淘汰制）
		Sentence sentence = Global.getSentenceBO().findById(sentenceId);
		List<SentenceDiyItem> diyItems = sentence.getSentenceDiyItems();
		sortDiyItems(diyItems);
		while (diyItems.size() >= 3) {
			// 删除数据库记录
			SentenceDiyItem diyItem = diyItems.remove(diyItems.size() - 1);
			deleteSentenceDiyItem(diyItem.getId(), user, false);
		}

		// 添加新的UGC例句
		SentenceDiyItem diyItem = new SentenceDiyItem();
		diyItem.setAuthor(user);
		diyItem.setContent(chinese);
		diyItem.setFootCount(0);
		diyItem.setHandCount(0);
		diyItem.setItemType(SentenceDiyItem.ITEM_TYPE_CHINESE);
		diyItem.setSentence(sentence);
		diyItem.setSentenceDiyItemRemarks(new ArrayList<SentenceDiyItemRemark>());
		createEntity(diyItem);

		diyItems.add(diyItem);
		Global.getSentenceBO().updateEntity(sentence);

		Event event = new Event(EventType.NewSentenceChinese, user, diyItem);
		Global.getEventBO().createEntity(event);
	}

	public static void sortDiyItems(List<SentenceDiyItem> diyItems) {
		Collections.sort(diyItems, new Comparator<SentenceDiyItem>() {
			@Override
			public int compare(SentenceDiyItem o1, SentenceDiyItem o2) {
				int score1 = o1.getHandCount() - o1.getFootCount();
				int score2 = o2.getHandCount() - o2.getFootCount();
				if (score1 == score2) {
					return (int) (o2.getCreateTime().getTime() - o1.getCreateTime().getTime());
				} else {
					return score2 - score1;
				}
			}
		});
	}

	public Result<Object> deleteSentenceDiyItem(int itemId, User user, boolean checkPermission) {
		SentenceDiyItem diyItem = findById(itemId);
		if (checkPermission) {
			if (!user.getIsAdmin() && (diyItem.getAuthor() == null
					|| !diyItem.getAuthor().getUserName().equalsIgnoreCase(user.getUserName()))) {
				return new Result<Object>(false, "无权限", null);
			}
		}

		// 删除相关的事件记录
		Event exam = new Event();
		exam.setSentenceDiyItem(diyItem);
		EventBO eventBO = Global.getEventBO();
		eventBO.getDAO().setPreciseEntity(exam);
		List<Event> events = eventBO.queryAll();
		for (Event event : events) {
			Global.getEventBO().deleteEntity(event);
		}

		// 删除数据库记录
		diyItem.getSentence().getSentenceDiyItems().remove(diyItem);
		diyItem.setSentence(null);
		deleteEntity(diyItem);

		return new Result<Object>(true, null, null);
	}

	/**
	 * 获取一个句子对应的前3个用户生成翻译
	 *
	 * @return
	 */
	public List<SentenceDiyItemVo> getSentenceDiyItems(Integer sentenceId, SessionData sessionData) {

		// 排序，被点赞绝对次数多的排在前面
		Sentence sentence = Global.getSentenceBO().findById(sentenceId);
		List<SentenceDiyItem> diyItems = sentence.getSentenceDiyItems();
		sortDiyItems(diyItems);

		int total = diyItems.size();
		total = total <= 3 ? total : 3;
		List<SentenceDiyItemVo> diyItemVOs = new ArrayList<SentenceDiyItemVo>();
		for (int i = 0; i < total; i++) {
			SentenceDiyItem diyItem = diyItems.get(i);
			SentenceDiyItemVo diyItemVo = BeanUtils.makeVO(diyItem, SentenceDiyItemVo.class,
					new String[] { "invitedBy", "userGames", "studyGroups" });
			UserVo author = new UserVo();
			author.setDisplayNickName(diyItem.getAuthor().getDisplayNickName());
			author.setUserName(diyItem.getAuthor().getUserName());
			author.setId(diyItem.getAuthor().getId());
			diyItemVo.setAuthor(author);

			// 判断是否被当前登录用户评价过？
			boolean hasBeenVoted = sessionData.getVotedSentenceDiyItems().contains(diyItemVo.getId());
			diyItemVo.setHasBeenVoted(hasBeenVoted);

			diyItemVOs.add(diyItemVo);
		}

		return diyItemVOs;
	}

	public Result<Integer> handSentenceDiyItem(Integer diyItemId, User user)
			throws IllegalArgumentException, IllegalAccessException {
		SentenceDiyItem diyItem = findById(diyItemId);
		diyItem.setHandCount(diyItem.getHandCount() + 1);
		updateEntity(diyItem);

		// 对作者进行奖励
		Global.getUserBO().adjustCowDung(diyItem.getAuthor(), 1, "例句UGC翻译得到了赞");

		Event event = new Event(EventType.HandWordImage, user, diyItem);
		Global.getEventBO().createEntity(event);

		return new Result<Integer>(true, null, diyItem.getHandCount());
	}

	public Result<Integer> footSentenceDiyItem(Integer diyItemId, User user)
			throws IllegalArgumentException, IllegalAccessException {
		SentenceDiyItem diyItem = findById(diyItemId);
		diyItem.setFootCount(diyItem.getFootCount() + 1);
		updateEntity(diyItem);

		// 如果该内容被踩的次数比被赞的次数多3（或以上），删除该图片
		if (diyItem.getFootCount() - diyItem.getHandCount() >= 3) {
			deleteSentenceDiyItem(diyItemId, user, false);
		}

		Event event = new Event(EventType.FootWordImage, user, diyItem);
		Global.getEventBO().createEntity(event);

		return new Result<Integer>(true, null, diyItem.getFootCount());
	}

}
