package beidanci.socket.system.game.russia;

import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.socket.UserCmd;
import beidanci.socket.system.game.russia.state.ReadyState;
import beidanci.vo.UserVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Russia {
    private static final Logger log = LoggerFactory.getLogger(Russia.class);
    private static Russia instance = new Russia();

    public static Russia getInstance(){
        return instance;
    }

    /**
     * 系统中所有游戏大厅, key 为大厅的名字
     */
    private Map<String, Hall> gameHalls = new ConcurrentHashMap<String, Hall>();

    /**
     * 用户到大厅的map，可以查询到用户在哪个大厅里
     */
    private Map<UserVo, Hall> users = new ConcurrentHashMap<>();

    public void processUserCmd(UserVo user, UserCmd userCmd) throws InvalidMeaningFormatException, EmptySpellException, ParseException, IOException, IllegalAccessException {
        RussiaService russiaService = RussiaService.getInstance();
        if (userCmd.getCmd().equals("ENTER_GAME_HALL")) {
            // 获取用户要进入的游戏大厅
            String hallName = userCmd.getArgs()[0];

            // 获取用户不想进入的房间（用户点击了【离开】按钮）
            log.info("exceptRoom: " + userCmd.getArgs()[1]);
            int exceptRoom = -1;
            if (userCmd.getArgs()[1] != null && !userCmd.getArgs()[1].equals("")
                    && !userCmd.getArgs()[1].equals("undefined")) {
                exceptRoom = Integer.parseInt(userCmd.getArgs()[1]);
            }

            Hall hall;
            synchronized (gameHalls) {
                hall = gameHalls.get(hallName);
                if (hall == null) {
                    hall = new Hall(hallName, russiaService);
                    gameHalls.put(hallName, hall);
                }
            }

            // 用户进入游戏大厅
            Hall currHall = users.get(user);
            if (currHall != null) {
                log.warn(String.format("用户[%s]试图进入大厅，但他目前已在[%s]大厅, 强制他从目前大厅退出。", user.getDisplayNickName(),
                        currHall.getName()));
                currHall.userLeave(user);
            }
            hall.userEnter(user, exceptRoom);
            users.put(user, hall);
        } else if (userCmd.getCmd().equals("inviteUser")) {
            int targetUserId = Integer.parseInt(userCmd.getArgs()[0]);
            String gameType = userCmd.getArgs()[1];
            int room = Integer.parseInt(userCmd.getArgs()[2]);
            UserVo targetUser = russiaService.getUserById(targetUserId);
            String hallName = userCmd.getArgs()[3];
            if (targetUser != null) {
                russiaService.sendEventToUser(targetUser, "inviteYouToGame",
                        new Object[] { user, gameType, room, hallName });
            }
        } else {
            // 向用户所在的游戏大厅发送用户的命令
            Hall hall = getHallOfUser(user);
            if (hall != null) {
                hall.processUserCmd(user, userCmd);
            } else {
                log.warn(String.format("用户[%s]尚未进入大厅，就开始发送命令[%s]", user.getDisplayNickName(),
                        userCmd.getCmd()));
            }
        }
    }

    private Hall getHallOfUser(UserVo user) {
        Hall hall = users.get(user);
        return hall;
    }

    public void onUserLogout(UserVo user) throws IllegalAccessException {
        Hall hall = getHallOfUser(user);
        if (hall != null) {
            hall.userLeave(user);
        }
        users.remove(user);

        RussiaService.getInstance().broadcastOnelineCount();
    }

    public void onUserLeaveHall(UserVo user, Hall hall) {
        if (users.containsKey(user)) {
            users.remove(user);
        }
    }

    protected List<UserVo> getIdleUsers(UserVo except, int count) {
        List<UserVo> idleUsers = new ArrayList();
        List<UserVo> users = RussiaService.getInstance().getUsers();
        Collections.shuffle(users);
        for (UserVo user : users) {
            Hall currHall = getHallOfUser(user);
            if (currHall == null || currHall.getRoomOfUser(user) == null
                    || !(currHall.getRoomOfUser(user).getState() instanceof ReadyState)) {
                UserVo vo = new UserVo();
                vo.setId(user.getId());
                vo.setDisplayNickName(user.getDisplayNickName());
                if (!user.equals(except)) {
                    idleUsers.add(vo);
                }
                if (idleUsers.size() >= count) {
                    break;
                }
            }
        }
        return idleUsers;
    }

    public Map<String, Hall> getGameHalls() {
        return gameHalls;
    }
}
