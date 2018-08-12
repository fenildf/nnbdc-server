package beidanci.socket.game.cmd;

import beidanci.Global;
import beidanci.po.User;
import beidanci.util.Util;

public class UserCmd {
	private Integer userId;
	private String system;
	private String cmd;
	private String[] args;

	@Override
	public String toString() {
		User user = Global.getUserBO().findById(userId);
		return String.format("User[%s] system[%s] Cmd[%s] args%s", Util.getNickNameOfUser(user), system, cmd,
				Util.array2Str(args));
	}

	public Integer getUserId() {
		return userId;
	}

	public String getCmd() {
		return cmd;
	}

	public String getSystem() {
		return system;
	}

	public String[] getArgs() {
		return args;
	}
}
