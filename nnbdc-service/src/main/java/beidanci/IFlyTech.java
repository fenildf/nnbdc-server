package beidanci;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import beidanci.util.Base64Coder;
import beidanci.util.MD5Utils;
import beidanci.util.Util;
import beidanci.util.Utils;

/**
 * 科大讯飞语音接口
 */
public class IFlyTech {
	private static final Logger log = LoggerFactory.getLogger(IFlyTech.class);

	private static final String appId = "5b948701";
	private static final String ttsApiKey = "c6419ecca808abf8fe56ccd7353a7fd7";
	private static final String speechRecognitionApiKey = "5a54bf1190046909f1364269e9e95690";

	private static final String ttsUrl = "http://api.xfyun.cn/v1/service/v1/tts";

	private static IFlyTech instance = new IFlyTech();

	private IFlyTech() {
	}

	public static IFlyTech getInstance() {
		return instance;
	}

	public byte[] tts(String text, String lang, String speaker, int speed) throws IOException {
		TtsParam ttsParam = new TtsParam();
		ttsParam.auf = "audio/L16;rate=16000";
		ttsParam.aue = "lame";
		ttsParam.voice_name = speaker;
		ttsParam.speed = String.valueOf(speed);
		ttsParam.volume = "50";
		ttsParam.pitch = "50";
		ttsParam.engine_type = lang.equals("zh") ? "intp65" : "intp65_en";
		ttsParam.text_type = "text";
		String xParam = Base64Coder.encodeString(Util.makeJson(ttsParam));

		URL url = new URL(ttsUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		// HTTP Request头部字段
		conn.setRequestMethod("POST");
		String currTime = String.valueOf(new Date().getTime() / 1000);
		conn.setRequestProperty("X-CurTime", currTime);
		conn.setRequestProperty("X-Param", xParam);
		conn.setRequestProperty("X-Appid", appId);
		conn.setRequestProperty("X-CheckSum", MD5Utils.md5(ttsApiKey + currTime + xParam));
		conn.setRequestProperty("X-Real-Ip", "127.0.0.1");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

		// HTTP Request Body (文本内容)
		conn.setDoOutput(true);
		String data = URLEncoder.encode("text", "UTF-8") + "=" + URLEncoder.encode(text, "UTF-8");
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();

		// 读取response body
		byte[] responseBody;
		try (InputStream in = conn.getInputStream()) {
			final int BUFF_LEN = 1024 * 1024;
			byte[] buff = new byte[BUFF_LEN];
			int offset = 0;
			int len = BUFF_LEN;
			int bytesRead = in.read(buff, offset, len);
			while (bytesRead != -1) {
				len -= bytesRead;
				offset += bytesRead;

				if (len == 0) {
					throw new RuntimeException("HTTP body必须小于1M");
				}

				bytesRead = in.read(buff, offset, len);
			}

			int dataLen = BUFF_LEN - len;
			responseBody = new byte[dataLen];
			System.arraycopy(buff, 0, responseBody, 0, dataLen);
		}

		if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			String contentType = conn.getHeaderField("Content-Type");
			if (!contentType.equals("audio/mpeg")) {
				throw new RuntimeException(String.format("Content-Type(%s)无效(期望值为autio/mpeg) body[%s]", contentType,
						new String(responseBody)));
			}
		} else {
			throw new RuntimeException(String.format("调用科大讯分TTS接口失败，status code[%s] body[%s]", conn.getResponseCode(),
					new String(responseBody)));
		}

		return responseBody;
	}

	private class TtsParam {
		String auf;
		String aue;
		String voice_name;
		String speed;
		String volume;
		String pitch;
		String engine_type;
		String text_type;

		public String getAuf() {
			return auf;
		}

		public String getAue() {
			return aue;
		}

		public String getVoice_name() {
			return voice_name;
		}

		public String getSpeed() {
			return speed;
		}

		public String getVolume() {
			return volume;
		}

		public String getPitch() {
			return pitch;
		}

		public String getEngine_type() {
			return engine_type;
		}

		public String getText_type() {
			return text_type;
		}
	}

	public static void main(String[] args) throws IOException {
		IFlyTech iFlyTech = new IFlyTech();
		String text = "Dallas police on Saturday named Amber Guyger, a four-year force veteran, as the off-duty white officer who shot and killed 26-year-old Botham Jean Thursday night.";
		byte[] sound = iFlyTech.tts(text, "en", "xiaoyan", 40);
		System.out.println(new String(sound));
		Utils.saveData2File("/tmp/a.mp3", sound);
	}
}
