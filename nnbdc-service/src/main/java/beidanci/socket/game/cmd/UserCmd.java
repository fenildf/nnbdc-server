package beidanci.socket.game.cmd;

import beidanci.Global;
import beidanci.po.User;
import beidanci.util.Util;

public class UserCmd {
	private Integer userId;
	private String cmd;
	private String[] args;

	@Override
	public String toString() {
		User user = Global.getUserBO().findById(userId);
		return String.format("User[%s] Cmd[%s] args%s", Util.getNickNameOfUser(user), cmd, Util.array2Str(args));
	}

	public Integer getUserId() {
		return userId;
	}

	public String getCmd() {
		return cmd;
	}

	public String[] getArgs() {
		return args;
	}
}
