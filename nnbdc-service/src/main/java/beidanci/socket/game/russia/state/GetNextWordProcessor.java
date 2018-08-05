package beidanci.socket.game.russia.state;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import beidanci.Global;
import beidanci.controller.AddRawWord;
import beidanci.po.User;
import beidanci.socket.game.cmd.UserCmd;
import beidanci.socket.game.russia.RussiaRoom;
import beidanci.socket.game.russia.UserGameData;
import beidanci.util.Utils;
import beidanci.vo.UserVo;
import beidanci.vo.WordVo;

public class GetNextWordProcessor {
	private static Logger log = LoggerFactory.getLogger(GetNextWordProcessor.class);
	private RussiaRoom room;

	public GetNextWordProcessor(RussiaRoom room) {
		this.room = room;
		reset();
	}

	/**
	 * 保存当前游戏的单词，以使两个玩家得到一样的单词，保证公平<br/>
	 * Object[0]: word<br/>
	 * Object[1]: meanings of two other words
	 */
	private List<Object[]> words = new LinkedList<Object[]>();

	public void processGetNextWordCmd(UserVo user, UserCmd userCmd) {
		try {
			Object[] wordObj;
			int wordIndex = Integer.parseInt(userCmd.getArgs()[0]);
			String currWord = userCmd.getArgs()[2];
			UserGameData userPlayData = room.getUserPlayData(user);

			// 更新连对次数
			String answerResult = userCmd.getArgs()[1];
			if (answerResult.equals("true")) {// 答对了
				userPlayData.setCorrectCount(userPlayData.getCorrectCount() + 1);
			} else {// 答错了
				userPlayData.setCorrectCount(0);

				// 将答错的单词自动添加到生词本
				if (wordIndex >= 1) {
					User userPo = Global.getUserBO().findById(user.getId());
					AddRawWord.addRawWord(currWord, userPo, "游戏");
					log.info(String.format("自动将单词[%s]添加到生词本", currWord));
				}
			}

			// 如果连对5次，奖励道具
			if (userPlayData.getCorrectCount() == 5) {
				userPlayData.setCorrectCount(0);
				int props = (int) (System.currentTimeMillis() % 3);
				props = props <= 1 ? 0 : 1;
				userPlayData.getPropsCounts()[props]++;
				room.sendEventToUser(user, "giveProps",
						new int[] { props, userPlayData.getPropsCounts()[(int) props] });
			}

			if (wordIndex < words.size()) {
				wordObj = words.get(wordIndex);
			} else {
				// 随机选择一个单词
				WordVo word = room.getHall().getWordRandomly(null);

				// 随机选择其他两个单词的意思，用以迷惑用户
				WordVo word2 = room.getHall().getWordRandomly(word);
				WordVo word3 = room.getHall().getWordRandomly(word);
				String[] meanings = new String[] { word2.getMeaningStr(), word3.getMeaningStr() };

				// 单词的发音
				String soundUrl = Utils.getFileNameOfWordSound(word.getSpell());

				wordObj = new Object[] { word, meanings, soundUrl };
				words.add(wordObj);

			}

			room.sendEventToUser(user, "wordA", wordObj);

			// 给对手也发一份，让对手看到我的进度
			UserVo anotherUser = room.getAnotherUser(user);
			if (anotherUser != null) {// 对方有可能突然不在线了(或者是单人练习模式)，所以要判断一下
				String spell = ((WordVo) wordObj[0]).getSpell();
				room.sendEventToUser(anotherUser, "wordB", new Object[] { answerResult, spell });
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public void reset() {
		words.clear();
	}
}
