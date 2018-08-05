package beidanci.socket.game.russia.state;

import beidanci.socket.game.cmd.UserCmd;
import beidanci.socket.game.russia.RussiaRoom;
import beidanci.vo.UserVo;

public abstract class RoomState {
	protected RussiaRoom room;

	public RoomState(RussiaRoom room) {
		this.room = room;
	}

	public abstract void enter();

	public abstract void processUserCmd(UserVo user, UserCmd userCmd) throws IllegalAccessException;

	public abstract void exit(UserVo user) throws IllegalAccessException;
}
