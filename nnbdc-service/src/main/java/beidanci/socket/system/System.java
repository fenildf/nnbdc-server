package beidanci.socket.system;

import java.io.IOException;
import java.util.List;

import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.socket.UserCmd;
import beidanci.socket.system.game.russia.Hall;
import beidanci.vo.UserVo;

public interface System {
	String SYSTEM_RUSSIA = "russia";
	String SYSTEM_CHAT = "chat";

	void processUserCmd(UserVo user, UserCmd userCmd) throws InvalidMeaningFormatException, EmptySpellException,
			ParseException, IOException, IllegalAccessException;

	void onUserLogout(UserVo user) throws IllegalAccessException;

	void onUserLeaveHall(UserVo user, Hall hall);

	List<UserVo> getIdleUsers(UserVo except, int count);
}
