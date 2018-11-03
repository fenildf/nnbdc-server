package beidanci.socket.system.game.russia.state;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import beidanci.Global;
import beidanci.po.SysParam;
import beidanci.po.User;
import beidanci.po.UserGame;
import beidanci.po.UserGameId;
import beidanci.socket.UserCmd;
import beidanci.socket.system.game.russia.RussiaRoom;
import beidanci.socket.system.game.russia.UserGameData;
import beidanci.util.UserSorter;
import beidanci.util.Util;
import beidanci.vo.UserGameVo;
import beidanci.vo.UserVo;

public class GameOverProcessor {
    private static Logger log = LoggerFactory.getLogger(GameOverProcessor.class);

    /**
     * 当前游戏的失败者，谁先报告game over谁就是失败者
     */
    private UserVo loser = null;

    private RussiaRoom room;

    public GameOverProcessor(RussiaRoom room) {
        this.room = room;
    }

    public void processGameOverCmd(UserVo user, UserCmd userCmd) throws IllegalAccessException {
        if (loser == null) {
            // 判断那个玩家失败了
            String loserTag = userCmd.getArgs()[0];
            assert (loserTag.equals("A") || loserTag.equals("B"));
            if (loserTag.equals("A")) {
                loser = user;
            } else {
                loser = room.getAnotherUser(user);
            }

            UserGameData userPlayData = room.getUserPlayData(user);
            if (userPlayData.isExercise()) {
                room.sendEventToUser(user, "loser", loser.getId());
                log.info(String.format("[%s]练习结束, loserTag:[%s]", Util.getNickNameOfUser(loser), loserTag));
            } else {
                room.broadcastEvent("loser", loser.getId());
                log.info(String.format("[%s]触顶，判为失败, loserTag:[%s]", Util.getNickNameOfUser(loser), loserTag));
            }

            // 根据胜负情况对两位玩家的积分进行调整
            if (!userPlayData.isExercise()) {
                UserVo winer = room.getAnotherUser(loser);
                adjustUserScore(winer, loser);
            }
        }
        room.broadcastUsersInfo();
    }

    public void reset() {
        loser = null;
    }

    /**
     * 计算赢家的积分调整量
     *
     * @param winerScore
     * @param loserScore
     * @return
     */
    public static int calculateWinerScoreAdjustment(int winerScore, int loserScore) {
        int adjustment;
        int delta = loserScore - winerScore;
        if (delta >= 1000) {
            adjustment = 100;
        } else if (delta <= -1000) {
            adjustment = 1;
        } else {
            adjustment = 1 + (delta + 1000) * 99 / 2000;
        }
        return adjustment;
    }

    /**
     * 根据游戏胜负情况，调整玩家的积分和牛粪
     *
     * @param winerVo
     * @param loserVo
     * @throws IllegalAccessException
     */
    public void adjustUserScore(UserVo winerVo, UserVo loserVo) throws IllegalAccessException {
        User winer = Global.getUserBO().findById(winerVo.getId());
        User loser = Global.getUserBO().findById(loserVo.getId());

        // 更新赢家的胜负盘数信息
        UserGameId userGameId = new UserGameId(winer.getId(), "russia");
        UserGame userGame = Global.getUserGameBO().findById(userGameId);
        if (userGame == null) {
            userGame = new UserGame(userGameId, winer, 0, 0, 0);
            Global.getUserGameBO().createEntity(userGame);
        }
        userGame.setWinCount(userGame.getWinCount() + 1);

        // 为赢家添加积分
        final int adjustment = calculateWinerScoreAdjustment(winer.getGameScore(), loser.getGameScore());
        userGame.setScore(userGame.getScore() + adjustment);
        winer.setGameScore(winer.getGameScore() + adjustment);
        Global.getUserGameBO().updateEntity(userGame);

        // 刷新赢家用户vo的game部分
        UserGameVo userGameVo = winerVo.getGameByName("russia");
        userGameVo.setWinCount(userGame.getWinCount());
        userGameVo.setScore(userGame.getScore());

        //  赢家奖励牛粪
        Global.getUserBO().updateEntity(winer);
        SysParam sysParam = Global.getSysParamBO().findById(SysParam.COW_DUNG_PER_GAME);
        final int cowDungPerGame = Integer.parseInt(sysParam.getParamValue());
        Global.getUserBO().adjustCowDung(winer, cowDungPerGame, "游戏胜利奖励");

        // 更新赢家用户Vo
        winerVo.setCowDung(winer.getCowDung());
        winerVo.setGameScore(winer.getGameScore());

        room.sendEventToUser(winerVo, "scoreAdjust", new Object[]{adjustment, cowDungPerGame});

        // 更新输家的胜负盘数信息
        userGameId = new UserGameId(loser.getId(), "russia");
        userGame = Global.getUserGameBO().findById(userGameId);
        if (userGame == null) {
            userGame = new UserGame(userGameId, loser, 0, 0, 0);
            Global.getUserGameBO().createEntity(userGame);
        }
        userGame.setLoseCount(userGame.getLoseCount() + 1);

        // 减少输家的积分，但避免用户积分降到负数，否则太伤自尊
        int scoreDelta = adjustment > userGame.getScore() ? userGame.getScore() : adjustment;
        userGame.setScore(userGame.getScore() - scoreDelta);
        loser.setGameScore(loser.getGameScore() - scoreDelta);
        Global.getUserGameBO().updateEntity(userGame);

        // 刷新输家用户vo的game部分
        userGameVo = loserVo.getGameByName("russia");
        userGameVo.setLoseCount(userGame.getLoseCount());
        userGameVo.setScore(userGame.getScore());

        //  输家惩罚牛粪
        Global.getUserBO().updateEntity(loser);
        Global.getUserBO().adjustCowDung(loser, cowDungPerGame * (-1), "游戏胜利惩罚");

        // 更新输家用户Vo
        loserVo.setGameScore(loser.getGameScore());

        // 更新用户排名
        List<User> changedUsers = new ArrayList<User>();
        changedUsers.add(winer);
        changedUsers.add(loser);
        UserSorter.getInstance().onUserChanged(changedUsers);

        room.sendEventToUser(loserVo, "scoreAdjust", new Object[]{adjustment * (-1), cowDungPerGame * (-1)});
    }

}
