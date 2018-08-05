package beidanci.socket.game.russia.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import beidanci.socket.game.cmd.UserCmd;
import beidanci.socket.game.russia.RussiaRoom;
import beidanci.vo.UserVo;

public class WaitState extends RoomState {
	private static Logger log = LoggerFactory.getLogger(WaitState.class);

	private GetNextWordProcessor getNextWordProcessor;
	private GameOverProcessor gameOverProcessor;
	private StartExerciseProcessor startExerciseProcessor;

	public WaitState(RussiaRoom room) {
		super(room);
		getNextWordProcessor = new GetNextWordProcessor(room);
		gameOverProcessor = new GameOverProcessor(room);
		startExerciseProcessor = new StartExerciseProcessor(room);
	}

	@Override
	public void enter() {
		room.broadcastEvent("enterWait", null);
	}

	@Override
	public void processUserCmd(UserVo user, UserCmd userCmd) throws IllegalAccessException {
		if (userCmd.getCmd().equals("GET_NEXT_WORD")) {
			getNextWordProcessor.processGetNextWordCmd(user, userCmd);
		} else if (userCmd.getCmd().equals("START_EXERCISE")) {// 单人练习命令
			getNextWordProcessor.reset();
			gameOverProcessor.reset();
			startExerciseProcessor.process(user, userCmd);
		} else if (userCmd.getCmd().equals("GAME_OVER")) {
			gameOverProcessor.processGameOverCmd(user, userCmd);
		} else {
			log.warn(String.format("WaitState received an unexpected command: [%s]", userCmd.getCmd()));
		}
	}

	@Override
	public void exit(UserVo user) {

	}

}
