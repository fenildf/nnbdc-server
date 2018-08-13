package beidanci.socket.system.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.socket.UserCmd;
import beidanci.socket.system.System;
import beidanci.socket.system.game.russia.Hall;
import beidanci.vo.UserVo;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Chat implements System {
	private static final Chat instance = new Chat();

	public static Chat getInstance() {
		return instance;
	}

	private Chat() {
	}

	@Override
	public void processUserCmd(UserVo user, UserCmd userCmd) throws InvalidMeaningFormatException, EmptySpellException,
			ParseException, IOException, IllegalAccessException {
		throw new NotImplementedException();
	}

	@Override
	public void onUserLogout(UserVo user) throws IllegalAccessException {
		throw new NotImplementedException();
	}

	@Override
	public void onUserLeaveHall(UserVo user, Hall hall) {
		throw new NotImplementedException();
	}

	@Override
	public List<UserVo> getIdleUsers(UserVo except, int count) {
		return new ArrayList<>();
	}
}
