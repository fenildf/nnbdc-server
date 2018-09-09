package beidanci.store;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import beidanci.Global;
import beidanci.IFlyTech;
import beidanci.po.Sentence;
import beidanci.util.SysParamUtil;
import beidanci.util.Utils;

/**
 * 本线程负责为指定的例句（多个）生成语音（TTS方式）
 */
public class SentenceTtsThread extends Thread {
	private static Logger log = LoggerFactory.getLogger(SentenceTtsThread.class);

	private static SentenceTtsThread instance;

	public static SentenceTtsThread getInstance() {
		if (instance == null) {
			synchronized (SentenceTtsThread.class) {
				if (instance == null) {
					instance = new SentenceTtsThread();
					instance.setName("sent-tts-thread");
					instance.setDaemon(true);
					instance.start();
				}
			}
		}
		return instance;
	}

	private SentenceTtsThread() {
	}

	/**
	 * 需要生成语音的例句ID列表
	 */
	private BlockingQueue<Integer> sentenceIds = new LinkedBlockingQueue<>(1000000);

	/**
	 * 添加一个要进行TTS的例句到等待队列
	 * 
	 * @param sentenceId
	 * @return 如果添加成功，返回true；如果队列满，返回false
	 */
	public boolean schedule(Integer sentenceId) {
		return sentenceIds.offer(sentenceId);
	}

	@Override
	public void run() {
		while (true) {
			try {
				// 从数据库查询例句信息
				Integer sentenceId = sentenceIds.take();
				Sentence sentence = Global.getSentenceBO().findById(sentenceId);

				// 通过科大讯飞的TTS服务对例句进行TTS
				final String english = sentence.getEnglish();
				byte[] sound = IFlyTech.getInstance().tts(english, "en", "xiaoyan", 40);
				String soundFile = SysParamUtil.getSoundPath() + "/sentence/" + sentence.getEnglishDigest() + ".mp3";
                soundFile = "/tmp/sentence/" + sentence.getEnglishDigest() + ".mp3"; //测试,待删除
				Utils.saveData2File(soundFile, sound);

				// 更细例句信息
				//sentence.setTheType(Sentence.TTS); // 测试，待恢复
				Global.getSentenceBO().updateEntity(sentence);

				log.info(String.format("成功对例句进行了TTS，例句ID[%d] mp3[%s]", sentenceId, soundFile));
			} catch (Exception e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
