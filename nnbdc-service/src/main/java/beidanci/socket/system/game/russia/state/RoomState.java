package beidanci.socket.system.game.russia.state;

import beidanci.socket.UserCmd;
import beidanci.socket.system.game.russia.RussiaRoom;
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
