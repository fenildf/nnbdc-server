package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.util.UserSorter;
import beidanci.util.UserSorter.UserScoreRecord;
import beidanci.util.Util;

@Controller
@RequestMapping("/showPaiHangPage.do")
public class ShowPaiHangPage {
	private static Logger log = LoggerFactory.getLogger(ShowPaiHangPage.class);

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
			throws ClassNotFoundException, SQLException, NamingException, IOException, ParseException,
			InvalidMeaningFormatException, EmptySpellException {

		Util.setPageNoCache(response);

		// 获取所有活动用户（打过卡的或玩过游戏的）
		Collection<UserScoreRecord> userScoreRecords = UserSorter.getInstance().getUserScoreRecords();
		log.info("user count for 排名: " + userScoreRecords.size());

		// 按总积分排行
		List<UserScoreRecord> usersByTotalScore = new LinkedList<UserScoreRecord>(userScoreRecords);
		Collections.sort(usersByTotalScore, new Comparator<UserScoreRecord>() {
			@Override
			public int compare(UserScoreRecord o1, UserScoreRecord o2) {
				return o2.getTotalScore() - o1.getTotalScore();
			}
		});
		request.setAttribute("usersByTotalScore", usersByTotalScore);

		// 按打卡天数排行
		List<UserScoreRecord> usersByDakaCount = new LinkedList<UserScoreRecord>(userScoreRecords);
		Collections.sort(usersByDakaCount, new Comparator<UserScoreRecord>() {
			@Override
			public int compare(UserScoreRecord o1, UserScoreRecord o2) {
				return o2.getDakaDayCount() - o1.getDakaDayCount();
			}
		});
		request.setAttribute("usersByDakaCount", usersByDakaCount);

		// 按打卡率排行
		List<UserScoreRecord> usersByDakaRatio = new LinkedList<UserScoreRecord>(userScoreRecords);
		Collections.sort(usersByDakaRatio, new Comparator<UserScoreRecord>() {
			@Override
			public int compare(UserScoreRecord o1, UserScoreRecord o2) {
				int delta = (int) (o2.getDakaRatio() * 10000 - o1.getDakaRatio() * 10000);

				// 如果打卡率相等，则按打卡天数排序
				if (delta == 0) {
					delta = o2.getDakaDayCount() - o1.getDakaDayCount();
				}
				return delta;
			}
		});
		request.setAttribute("usersByDakaRatio", usersByDakaRatio);

		// 按连续打卡天数记录排行
		List<UserScoreRecord> usersByMaxContinuousDakaDayCount = new LinkedList<UserScoreRecord>(userScoreRecords);
		Collections.sort(usersByMaxContinuousDakaDayCount, new Comparator<UserScoreRecord>() {
			@Override
			public int compare(UserScoreRecord o1, UserScoreRecord o2) {
				int delta = (int) (o2.getMaxContinuousDakaDayCount() - o1.getMaxContinuousDakaDayCount());

				// 如果相等，则按打卡天数排序
				if (delta == 0) {
					delta = o2.getDakaDayCount() - o1.getDakaDayCount();
				}
				return delta;
			}
		});
		request.setAttribute("usersByMaxContinuousDakaDayCount", usersByMaxContinuousDakaDayCount);

		// 按当前连续打卡天数排行
		List<UserScoreRecord> usersByContinuousDakaDayCount = new LinkedList<UserScoreRecord>(userScoreRecords);
		Collections.sort(usersByContinuousDakaDayCount, new Comparator<UserScoreRecord>() {
			@Override
			public int compare(UserScoreRecord o1, UserScoreRecord o2) {
				int delta = (int) (o2.getContinuousDakaDayCount() - o1.getContinuousDakaDayCount());

				// 如果相等，则按打卡天数排序
				if (delta == 0) {
					delta = o2.getDakaDayCount() - o1.getDakaDayCount();
				}
				return delta;
			}
		});
		request.setAttribute("usersByContinuousDakaDayCount", usersByContinuousDakaDayCount);

		// 已掌握单词数排行
		List<UserScoreRecord> usersByMasteredWordCount = new LinkedList<UserScoreRecord>(userScoreRecords);
		Collections.sort(usersByMasteredWordCount, new Comparator<UserScoreRecord>() {
			@Override
			public int compare(UserScoreRecord o1, UserScoreRecord o2) {
				int delta = (int) (o2.getMasteredWordCount() - o1.getMasteredWordCount());

				// 如果相等，则按打卡天数排序
				if (delta == 0) {
					delta = o2.getDakaDayCount() - o1.getDakaDayCount();
				}
				return delta;
			}
		});
		request.setAttribute("usersByMasteredWordCount", usersByMasteredWordCount);

		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("paiHang");

	}
}
