package beidanci.controller;

import beidanci.Global;
import beidanci.bo.DakaBO;
import beidanci.bo.UserBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.Daka;
import beidanci.po.DakaId;
import beidanci.po.User;
import beidanci.util.UserSorter;
import beidanci.util.Util;
import beidanci.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class SaveDakaRecord {
    private static Logger log = LoggerFactory.getLogger(SaveDakaRecord.class);

    @RequestMapping("/saveDakaRecord.do")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws ClassNotFoundException, SQLException, NamingException, IOException, ParseException,
            InvalidMeaningFormatException, EmptySpellException, IllegalArgumentException, IllegalAccessException {
        Util.setPageNoCache(response);

        // 获取输入
        Map<String, String[]> params = request.getParameterMap();
        String text = params.get("text")[0];

        // 保存打卡记录
        UserBO userDAO = Global.getUserBO();
        User user = Util.getLoggedInUser();
        DakaId id = new DakaId(user.getId(), user.getLastLearningDate());
        DakaBO dakaDAO = Global.getDakaBO();
        if (dakaDAO.findById(id) == null) {
            Daka daka = new Daka(id, user, text);
            dakaDAO.createEntity(daka);
            log.info(String.format("保存用户[%s]的打卡记录[%s]成功！", Util.getNickNameOfUser(user),
                    new SimpleDateFormat("yyyy-MM-dd").format(user.getLastLearningDate())));

            // 更新用户打卡天数
            user.setDakaDayCount(user.getDakaDayCount() + 1);
        } else {
            log.warn("奇怪！打卡记录已经存在了，用户还在打卡");
            Util.sendBooleanResponse(false, "今天已经打过卡了", null, response);
            return;
        }

        // 更新用户的当前连续打卡天数、最大连续打卡天数、最近打卡日期、总打卡积分
        long dateDiff = user.getLastDakaDate() == null ? Long.MAX_VALUE
                : Utils.getDifferenceDays(user.getLastDakaDate(), user.getLastLearningDate());
        if (dateDiff <= 1) {
            user.setContinuousDakaDayCount(user.getContinuousDakaDayCount() + 1);
            if (user.getContinuousDakaDayCount() > user.getMaxContinuousDakaDayCount()) {
                user.setMaxContinuousDakaDayCount(user.getContinuousDakaDayCount());
            }
        } else {
            user.setContinuousDakaDayCount(1);
        }
        user.setLastDakaDate(user.getLastLearningDate());
        Integer dakaScore = 10 + (user.getContinuousDakaDayCount() - 1);
        user.setDakaScore(user.getDakaScore() + dakaScore);

        // 给用户一次掷骰子机会
        user.setThrowDiceChance(user.getThrowDiceChance() + 1);
        userDAO.updateEntity(user);
        log.info(String.format("用户[%s]得到一次掷骰子机会", Util.getNickNameOfUser(user)));

        // 更新用户排名
        List<User> changedUsers = new ArrayList<User>();
        changedUsers.add(user);
        UserSorter.getInstance().onUserChanged(changedUsers);

        // 把打卡相关的信息保存在Session里(在掷骰子页面显示给用户)
        Util.getSessionData(request).setLastDakaScore(dakaScore);

        Util.sendBooleanResponse(true, null, dakaScore, response);

    }
}
