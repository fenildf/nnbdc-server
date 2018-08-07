package beidanci.controller;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.bo.UserBO;
import beidanci.po.User;
import beidanci.po.UserSnapshotDaily;
import beidanci.util.BeanUtils;
import beidanci.util.UserSorter;
import beidanci.util.Util;
import beidanci.vo.UserSnapshotDailyVo;

@Controller
public class UserController {
	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	@RequestMapping("/saveUserConfig.do")
	@PreAuthorize("hasAnyRole('USER','ADMUpdateIN')")
	public void saveUserConfig(HttpServletRequest request, HttpServletResponse response, boolean autoPlayWord,
			boolean autoPlaySentence, boolean showAnswersDirectly)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();
		UserBO userDAO = Global.getUserBO();

		// 将“自动播放句子发音”标志取反
		user.setAutoPlaySentence(autoPlaySentence);
		user.setAutoPlayWord(autoPlayWord);
		user.setShowAnswersDirectly(showAnswersDirectly);
		userDAO.updateEntity(user);

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);
	}

	@RequestMapping("/getUserSnapshotDailys.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void getUserSnapshotDailys(HttpServletRequest request, HttpServletResponse response, boolean autoPlayWord,
			boolean autoPlaySentence, boolean showAnswersDirectly)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		User user = Util.getLoggedInUser();
		List<UserSnapshotDaily> snapshotDailys = user.getUserSnapshotDailys();
		List<UserSnapshotDailyVo> vos = BeanUtils.makeVos(snapshotDailys, UserSnapshotDailyVo.class,
				new String[] { "sentMsgs", "invitedBy", "user" });
		Util.sendJson(vos, response);
	}

	@RequestMapping("/getUserPaihangData.do")
	@PreAuthorize("hasAnyRole('USER','ADMIN')")
	public void getUserPaihangData(HttpServletRequest request, HttpServletResponse response)
			throws IllegalArgumentException, IllegalAccessException, IOException {
		Map<String, List<UserSorter.UserScoreRecord>> userPaihangDatas = new HashMap<>();

		// 获取所有活动用户（打过卡的或玩过游戏的）
		Collection<UserSorter.UserScoreRecord> userScoreRecords = UserSorter.getInstance().getUserScoreRecords();
		log.info("user count for 排名: " + userScoreRecords.size());

		// 按总积分排行
		List<UserSorter.UserScoreRecord> usersByTotalScore = new LinkedList<UserSorter.UserScoreRecord>(
				userScoreRecords);
		Collections.sort(usersByTotalScore, new Comparator<UserSorter.UserScoreRecord>() {
			@Override
			public int compare(UserSorter.UserScoreRecord o1, UserSorter.UserScoreRecord o2) {
				return o2.getTotalScore() - o1.getTotalScore();
			}
		});
		userPaihangDatas.put("usersByTotalScore", copyTopRecords(usersByTotalScore, 20));

		// 按打卡天数排行
		List<UserSorter.UserScoreRecord> usersByDakaCount = new LinkedList<UserSorter.UserScoreRecord>(
				userScoreRecords);
		Collections.sort(usersByDakaCount, new Comparator<UserSorter.UserScoreRecord>() {
			@Override
			public int compare(UserSorter.UserScoreRecord o1, UserSorter.UserScoreRecord o2) {
				return o2.getDakaDayCount() - o1.getDakaDayCount();
			}
		});
		userPaihangDatas.put("usersByDakaCount", copyTopRecords(usersByDakaCount, 20));

		// 按打卡率排行
		List<UserSorter.UserScoreRecord> usersByDakaRatio = new LinkedList<UserSorter.UserScoreRecord>(
				userScoreRecords);
		Collections.sort(usersByDakaRatio, new Comparator<UserSorter.UserScoreRecord>() {
			@Override
			public int compare(UserSorter.UserScoreRecord o1, UserSorter.UserScoreRecord o2) {
				int delta = (int) (o2.getDakaRatio() * 10000 - o1.getDakaRatio() * 10000);

				// 如果打卡率相等，则按打卡天数排序
				if (delta == 0) {
					delta = o2.getDakaDayCount() - o1.getDakaDayCount();
				}
				return delta;
			}
		});
		userPaihangDatas.put("usersByDakaRatio", copyTopRecords(usersByDakaRatio, 20));

		// 按连续打卡天数记录排行
		List<UserSorter.UserScoreRecord> usersByMaxContinuousDakaDayCount = new LinkedList<UserSorter.UserScoreRecord>(
				userScoreRecords);
		Collections.sort(usersByMaxContinuousDakaDayCount, new Comparator<UserSorter.UserScoreRecord>() {
			@Override
			public int compare(UserSorter.UserScoreRecord o1, UserSorter.UserScoreRecord o2) {
				int delta = (int) (o2.getMaxContinuousDakaDayCount() - o1.getMaxContinuousDakaDayCount());

				// 如果相等，则按打卡天数排序
				if (delta == 0) {
					delta = o2.getDakaDayCount() - o1.getDakaDayCount();
				}
				return delta;
			}
		});
		userPaihangDatas.put("usersByMaxContinuousDakaDayCount", copyTopRecords(usersByMaxContinuousDakaDayCount, 20));

		// 按当前连续打卡天数排行
		List<UserSorter.UserScoreRecord> usersByContinuousDakaDayCount = new LinkedList<UserSorter.UserScoreRecord>(
				userScoreRecords);
		Collections.sort(usersByContinuousDakaDayCount, new Comparator<UserSorter.UserScoreRecord>() {
			@Override
			public int compare(UserSorter.UserScoreRecord o1, UserSorter.UserScoreRecord o2) {
				int delta = (int) (o2.getContinuousDakaDayCount() - o1.getContinuousDakaDayCount());

				// 如果相等，则按打卡天数排序
				if (delta == 0) {
					delta = o2.getDakaDayCount() - o1.getDakaDayCount();
				}
				return delta;
			}
		});
		userPaihangDatas.put("usersByContinuousDakaDayCount", copyTopRecords(usersByContinuousDakaDayCount, 20));

		// 已掌握单词数排行
		List<UserSorter.UserScoreRecord> usersByMasteredWordCount = new LinkedList<UserSorter.UserScoreRecord>(
				userScoreRecords);
		Collections.sort(usersByMasteredWordCount, new Comparator<UserSorter.UserScoreRecord>() {
			@Override
			public int compare(UserSorter.UserScoreRecord o1, UserSorter.UserScoreRecord o2) {
				int delta = (int) (o2.getMasteredWordCount() - o1.getMasteredWordCount());

				// 如果相等，则按打卡天数排序
				if (delta == 0) {
					delta = o2.getDakaDayCount() - o1.getDakaDayCount();
				}
				return delta;
			}
		});
		userPaihangDatas.put("usersByMasteredWordCount", copyTopRecords(usersByMasteredWordCount, 20));

		Util.sendJson(userPaihangDatas, response);
	}

	private static List copyTopRecords(List srcList, int topNum) {
		if (srcList.size() <= topNum) {
			return new ArrayList(srcList);
		}

		List topRecords = new ArrayList(topNum);
		for (int i = 0; i < topNum; i++) {
			topRecords.add(srcList.get(i));
		}
		return topRecords;
	}
}
