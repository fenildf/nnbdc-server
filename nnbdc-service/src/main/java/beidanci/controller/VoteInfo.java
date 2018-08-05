package beidanci.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import beidanci.Global;
import beidanci.bo.InfoVoteLogBO;
import beidanci.bo.WordAdditionalInfoBO;
import beidanci.po.InfoVoteLog;
import beidanci.po.InfoVoteLogId;
import beidanci.po.User;
import beidanci.po.WordAdditionalInfo;
import beidanci.util.Util;

@Controller
@RequestMapping("/voteInfo.do")
public class VoteInfo {
	private static final Logger log = LoggerFactory.getLogger(SendMsg.class);

	@RequestMapping
	public String handle(HttpServletRequest request, HttpServletResponse response) throws SQLException, NamingException,
			ClassNotFoundException, IOException, IllegalArgumentException, IllegalAccessException {
		Util.setPageNoCache(response);

		User user = Util.getLoggedInUser();

		// 获取输入
		Map<String, String[]> params = request.getParameterMap();
		Integer infoID = Integer.parseInt(params.get("infoID")[0]);
		String voteType = params.get("voteType")[0];

		// 参数验证
		if (!voteType.equals("hand") && !voteType.equals("foot")) {
			log.warn(String.format("未知的voteType[%s]", voteType));
			Util.sendBooleanResponse(false, "参数错误", null, response);
			return null;
		}

		// 验证infoID对应的内容是否存在(投票后，某内容可能会被动态删除)
		WordAdditionalInfoBO wordAdditionalInfoDAO = Global.getWordAdditionalInfoBO();
		WordAdditionalInfo additionalInfo = wordAdditionalInfoDAO.findById(infoID);
		if (additionalInfo == null) {
			Util.sendBooleanResponse(false, "该内容不存在或已被删除", null, response);
			return null;
		}

		// 验证当前用户是否已经对信息投过票（同一用户只能对同一内容投一次票）
		InfoVoteLogBO infoVoteLogDAO = Global.getInfoVoteLogBO();
		InfoVoteLogId logId = new InfoVoteLogId(user.getId(), infoID);
		if (infoVoteLogDAO.findById(logId) != null) {
			Util.sendBooleanResponse(false, "您已经对本条内容投过票啦", null, response);
			return null;
		}

		// 验证用户是否对自己发布的内容投票（不允许投自己）
		if (additionalInfo.getUser().getUserName().equalsIgnoreCase(user.getUserName())) {
			Util.sendBooleanResponse(false, "不能对自己发布的内容投票", null, response);
			return null;
		}

		// 为内容增加赞成票/反对票
		if (voteType.equals("hand")) {
			additionalInfo.setHandCount(additionalInfo.getHandCount() + 1);

			// 对作者进行奖励
			Global.getUserBO().adjustCowDung(additionalInfo.getUser(), 1, "单词共享笔记UGC得到了赞");
		} else {
			additionalInfo.setFootCount(additionalInfo.getFootCount() + 1);
		}
		wordAdditionalInfoDAO.updateEntity(additionalInfo);

		// 保存投票记录
		InfoVoteLog voteLog = new InfoVoteLog();
		voteLog.setId(logId);
		voteLog.setVoteTime(new Timestamp(new Date().getTime()));
		voteLog.setVoteType(voteType);
		voteLog.setUser(user);
		voteLog.setWordAdditionalInfo(additionalInfo);
		infoVoteLogDAO.createEntity(voteLog);

		log.info(String.format("[%s]%s了单词附加内容[%s]", Util.getNickNameOfUser(user),
				voteType.equalsIgnoreCase("hand") ? "赞" : "踩", additionalInfo.getContent()));

		// 如果该内容反对票比赞成票多10票以上，删除该内容
		if (additionalInfo.getFootCount() - additionalInfo.getHandCount() >= 10) {
			for (InfoVoteLog vote : additionalInfo.getVoteLogs()) {
				infoVoteLogDAO.deleteEntity(vote);
			}
			additionalInfo.getVoteLogs().clear();
			wordAdditionalInfoDAO.deleteEntity(additionalInfo);
		}

		// Send result back to client.
		Util.sendBooleanResponse(true, null, null, response);

		return null;
	}
}
