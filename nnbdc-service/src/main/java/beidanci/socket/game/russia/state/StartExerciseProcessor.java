package beidanci.socket.game.russia.state;

import beidanci.socket.game.cmd.UserCmd;
import beidanci.socket.game.russia.RussiaRoom;
import beidanci.socket.game.russia.UserGameData;
import beidanci.vo.UserVo;

public class StartExerciseProcessor {
	private RussiaRoom room;

	public StartExerciseProcessor(RussiaRoom room) {
		this.room = room;
	}

	public void process(UserVo user, UserCmd userCmd) {
		UserGameData userPlayData = room.getUserPlayData(user);
		userPlayData.setExercise(true);
		room.sendEventToUser(user, "sysCmd", "BEGIN_EXERCISE");
	}
}
