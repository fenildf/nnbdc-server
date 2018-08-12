package beidanci.socket.system.game.russia.state;

import beidanci.socket.UserCmd;
import beidanci.socket.system.game.russia.RussiaRoom;
import beidanci.socket.system.game.russia.UserGameData;
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
