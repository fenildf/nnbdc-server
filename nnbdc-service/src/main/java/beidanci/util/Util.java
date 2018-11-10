package beidanci.util;

import java.io.*;
import java.net.*;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jcraft.jsch.ChannelSftp;

import beidanci.Global;
import beidanci.SessionData;
import beidanci.bo.SysParamBO;
import beidanci.exception.EmptySpellException;
import beidanci.exception.InvalidMeaningFormatException;
import beidanci.exception.ParseException;
import beidanci.po.*;
import beidanci.security.UserDetailsImpl;
import beidanci.store.WordStore;
import beidanci.vo.*;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class Util {
	private static Logger log = LoggerFactory.getLogger(Util.class);

	/**
	 * 把数据对象直接格式化为JSON字符串发送出去
	 *
	 * @param data
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public static String sendJson(Object data, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		String json = makeJson(data);

		out.print(json);
		return json;
	}

	public static String makeJson(Object data) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		StringWriter sw = new StringWriter();
		JsonGenerator gen = new JsonFactory().createJsonGenerator(sw);
		mapper.writeValue(gen, data);
		gen.close();
		String json = sw.toString();
		// System.out.println(json);
		return json;
	}

	public static void appendToFile(String content, File file, String encoding) throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), encoding));
			writer.write(content);
		} finally {
			if (writer != null)

				writer.close();
		}
	}

	public static String deleteLastChar(String str) {
		return str.substring(0, str.length() - 1);
	}

	private static myX509TrustManager xtm;

	private static myHostnameVerifier hnv;

	static {
		xtm = new myX509TrustManager();
		hnv = new myHostnameVerifier();

		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("SSLv3"); // 或SSL/TLS
			X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };
			sslContext.init(null, xtmArray, new java.security.SecureRandom());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		if (sslContext != null) {
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		}
		HttpsURLConnection.setDefaultHostnameVerifier(hnv);
	}

	private static class myX509TrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

	private static class myHostnameVerifier implements HostnameVerifier {

		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	public static String getHtml(String urlString, String srcCharSet, String dstCharSet, boolean printResponse)
			throws UnsupportedEncodingException, IOException {
		log.info("Getting page:" + urlString);

		StringBuffer html = new StringBuffer();
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestProperty("User-Agent",
				"compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET4.0C; .NET4.0E");
		InputStreamReader isr = new InputStreamReader(conn.getInputStream(), srcCharSet);
		BufferedReader br = new BufferedReader(isr);
		String temp;
		while ((temp = br.readLine()) != null) {
			html.append(temp).append("\n");
		}
		br.close();
		isr.close();

		String response = new String(html.toString().getBytes(dstCharSet));
		if (printResponse) {
			log.info("response: " + response);
		}
		return response;
	}

	public static String getXmlTagContent(String tag, String xml, int start) {
		final String startTag1 = "<" + tag + ">";
		final String startTag2 = "<" + tag + " ";
		final String endTag = "</" + tag + ">";

		int startCount = 0;
		int tagStart = -1;

		while (true) {
			int start1 = xml.indexOf(startTag1, start);
			int start2 = xml.indexOf(startTag2, start);
			int end = xml.indexOf(endTag, start);
			if (start1 == -1 || start2 == -1) {
				start = Math.max(start1, start2);
			} else {
				start = Math.min(start1, start2);
			}

			if (tagStart == -1) {
				tagStart = start;
			}

			if (start == -1 && end == -1) {
				return null;
			}

			if (start == -1 || end == -1) {
				startCount += start > end ? 1 : -1;
				start = start == -1 ? end + endTag.length() : start + startTag1.length();
			} else {
				startCount += start < end ? 1 : -1;
				start = start < end ? start + startTag1.length() : end + endTag.length();
			}

			if (startCount == 0) {
				String rawContent = xml.substring(tagStart, end + endTag.length());

				// Get ride of the tag itself.
				start = rawContent.indexOf(">");
				end = rawContent.lastIndexOf(endTag);
				return rawContent.substring(start + 1, end);
			}

		}
	}

	/**
	 * 从一段混杂了xml标记的文本中取出所有的xml标记
	 *
	 * @param content
	 * @return
	 */
	public static String deleteAllXmlTag(String content) {

		return content.replaceAll("<{1}[^<>]*>{1}", "");

	}

	/**
	 * 不用正则表达式的字符串替换
	 *
	 * @param aInput
	 * @param aOldPattern
	 * @param aNewPattern
	 * @return
	 */
	public static String replaceOld(final String aInput, final String aOldPattern, final String aNewPattern) {
		if (aOldPattern.equals("")) {
			throw new IllegalArgumentException("Old pattern must have content.");
		}
		final StringBuffer result = new StringBuffer();
		// startIdx and idxOld delimit various chunks of aInput; these
		// chunks always end where aOldPattern begins
		int startIdx = 0;
		int idxOld = 0;
		while ((idxOld = aInput.indexOf(aOldPattern, startIdx)) >= 0) {
			// grab a part of aInput which does not include aOldPattern
			result.append(aInput.substring(startIdx, idxOld));
			// add aNewPattern to take place of aOldPattern
			result.append(aNewPattern);
			// reset the startIdx to just after the current match, to see
			// if there are any further matches
			startIdx = idxOld + aOldPattern.length();
		}
		// the final chunk will go to the end of aInput
		result.append(aInput.substring(startIdx));
		return result.toString();
	}

	public static boolean isEnglishChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	public static boolean isNumber(String str) {
		if (str.length() == 0) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			char cTemp = str.charAt(i);
			if (cTemp < '0' || cTemp > '9')
				return false;
		}
		return true;
	}

	public static boolean isStringEnglishWord(String str) {
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (!(c >= 'A' && c <= 'Z') && !(c >= 'a' && c <= 'z') && (c != ' ') && (c != '-')) {
				return false;
			}
		}
		return true;
	}

	public static String loadStringFromFile(File file, String encoding) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
			StringBuilder builder = new StringBuilder();
			char[] chars = new char[4096];
			int length = 0;

			while (0 < (length = reader.read(chars))) {

				builder.append(chars, 0, length);

			}
			return builder.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public static void println(String msg) {
		System.out.println(msg);
	}

	public static String replaceDoubleSpace(String str) {
		while (str.indexOf("  ") != -1) {
			str = str.replaceAll("  ", " ");
		}
		return str;
	}

	public static void saveToFile(String content, File file, String encoding) throws IOException {

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), encoding));
			writer.write(content);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	public static SessionData getSessionData(HttpServletRequest request) {
		SessionData sessionData = (SessionData) request.getSession().getAttribute(SessionData.SESSION_DATA);
		return sessionData;
	}

	@SuppressWarnings("deprecation")
	public static boolean isSameDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		} else {
			return (date1.getMonth() == date2.getMonth() && date1.getYear() == date2.getYear()
					&& date1.getDate() == date2.getDate());
		}
	}

	public static String getSoundBaseUrl() {
		return String.format("http://%s:%d/sound/", SysParamUtil.getHostName(), SysParamUtil.getHostPort());
	}

	/**
	 * 设置页面不被浏览器缓存(立即过期)
	 *
	 * @param response
	 */
	public static void setPageNoCache(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-store");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);

	}

	public static void downloadFile(URL url, File saveToFile) throws IOException {
		// 下载网络文件
		int byteread = 0;

		URLConnection conn = url.openConnection();
		conn.setReadTimeout(5000);
		InputStream inStream = conn.getInputStream();
		File tempFile = new File(saveToFile.getAbsoluteFile() + ".temp");
		if (tempFile.exists()) {
			if (!tempFile.delete()) {
				throw new RuntimeException(String.format("删除临时文件[%s]失败!", tempFile.getAbsoluteFile()));
			}
		}

		FileOutputStream fs = new FileOutputStream(tempFile);
		byte[] buffer = new byte[1024 * 8];

		try {
			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}

			// 下载完成
			fs.close();
			FileUtils.copyFile(tempFile, saveToFile);
			tempFile.delete();
		} catch (SocketTimeoutException e) {
			log.warn("Read 超时, 再次尝试下载");
			fs.close();
			tempFile.delete();
			downloadFile(url, saveToFile);
		}

	}

	/**
	 * 为指定的文件创建临时文件
	 *
	 * @param forFile
	 * @return
	 */
	public static File createTempFile(File forFile) {
		File tempFile = new File(forFile.getPath() + "_temp");
		if (tempFile.exists()) {
			if (!tempFile.delete()) {
				throw new RuntimeException("File can not be deleted: " + tempFile.getPath());
			}
		}
		return tempFile;
	}

	/**
	 * 把临时文件重命名为指定文件F
	 *
	 * @param tempFile
	 */
	public static void renameTempFile(File tempFile, File toFile) {
		System.gc();
		if (!toFile.delete()) {
			throw new RuntimeException("File can not be deleted: " + toFile.getPath());
		}
		if (!tempFile.renameTo(toFile)) {
			throw new RuntimeException(
					String.format("File[%s] can not be renamed to [%s]", tempFile.getPath(), toFile.getPath()));
		}
	}

	/**
	 * 某些文件命中含有单词拼写（如单词的声音文件，例句声音文件），所以需要对单词的一些特殊字符做处理
	 *
	 * @param spell
	 * @return
	 */

	/**
	 * 判断一个字符串是一个合法的单词（或句子）
	 *
	 * @param str
	 */
	public static boolean isValidWord(String str) {
		return str.matches("[a-zA-Z0-9?'!;=\\s\\-\\,\\.\\(\\)]*");
	}

	public static boolean isValidUserName(String userName) {
		return userName.matches("^[A-Za-z0-9_-]+$");
	}

	public static boolean isReservedUserName(String userName) {
		return userName.toLowerCase().startsWith("guest") || userName.equalsIgnoreCase("all")
				|| userName.equalsIgnoreCase("sys");
	}

	/**
	 * 对指定文件的每一行进行规格化，如消除多余空格
	 *
	 * @throws IOException
	 */
	public static void uniformFile(File file) throws IOException {
		// 创建临时文件，用于保存新版本的单词书
		File tempFile = Util.createTempFile(file);
		RandomAccessFile raf = new RandomAccessFile(tempFile, "rw");
		raf.seek(0);

		// 经原单词书的每一行复制到临时单词书，在此过程中进行规格化
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		String wordStr = reader.readLine();
		while (wordStr != null) {
			String uniformedStr = Utils.uniformString(wordStr);
			if (!uniformedStr.equalsIgnoreCase("")) {
				raf.write(Utils.uniformString(wordStr).getBytes("UTF-8"));
				raf.write("\n".getBytes("UTF-8"));
			}
			wordStr = reader.readLine();
		}
		reader.close();
		raf.close();

		// 文件改名，替换原始文件
		Util.renameTempFile(tempFile, file);
	}

	/**
	 * MD5的算法在RFC1321 中定义<br/>
	 * 在RFC 1321中，给出了Test suite用来检验你的实现是否正确：<br/>
	 * MD5 ("") = d41d8cd98f00b204e9800998ecf8427e<br/>
	 * MD5 ("a") = 0cc175b9c0f1b6a831c399e269772661<br/>
	 * MD5 ("abc") = 900150983cd24fb0d6963f7d28e17f72<br/>
	 * MD5 ("message digest") = f96b697d7cb7938d525a2f31aaf161d0<br/>
	 * MD5 ("abcdefghijklmnopqrstuvwxyz") = c3fcd3d76192e4007dfb496cca67e13b<br/>
	 * <br/>
	 *
	 * @author haogj<br/>
	 *         <br/>
	 *         传入参数：一个字节数组<br/>
	 *         传出参数：字节数组的 MD5 结果字符串<br/>
	 */
	public static String getMD5(byte[] source) {
		String s = null;
		char hexDigits[] = { // 用来将字节转换成 16 进制表示的字符
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			md.update(source);
			byte tmp[] = md.digest(); // MD5 的计算结果是一个 128 位的长整数，
			// 用字节表示就是 16 个字节
			char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
			// 所以表示成 16 进制需要 32 个字符
			int k = 0; // 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节
				// 转换成 16 进制字符的转换
				byte byte0 = tmp[i]; // 取第 i 个字节
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
				// >>>
				// 为逻辑右移，将符号位一起右移
				str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
			}
			s = new String(str); // 换后的结果转换为字符串

		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	public static void sendSimpleEmail(String toEmail, String toName, String subject, String content)
			throws EmailException {
		SimpleEmail email = new SimpleEmail();
		email.setHostName("smtp.live.com");
		email.setSmtpPort(587);
		email.setAuthentication("nnbdc@hotmail.com", "Badhorse201418");
		email.setTLS(true);
		email.setDebug(true);
		email.setCharset("UTF-8");
		email.addTo(toEmail, toName);
		email.setFrom("nnbdc@hotmail.com", "牛牛背单词");
		email.setSubject(subject);
		email.setMsg(content);
		email.send();
	}

	public static boolean isUserAgentMobile(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		if (userAgent == null) {
			return false;
		}

		String[] mobileKeyWords = { "Android", "iPhone", "iPod", "iPad", "Windows Phone", "MQQBrowser" };

		if (userAgent.indexOf("Windows NT") != -1 && userAgent.indexOf("compatible; MSIE 9.0;") == -1) {
			return false;
		}

		if (userAgent.indexOf("Macintosh") != -1) {
			return false;
		}

		for (String mobileKeyWord : mobileKeyWords) {
			if (userAgent.indexOf(mobileKeyWord) != -1) {
				return true;
			}
		}

		return false;
	}

	public static String getNickNameOfUser(User user) {
		if (user == null) {
			return "";
		}
		String nickName = user.getUserName();

		if (!isStringEmpty(user.getNickName())) {
			nickName = user.getNickName();
		}

		return nickName;
	}

	public static String getNickNameOfUser(UserVo user) {
		if (user == null) {
			return "";
		}
		String nickName = user.getUserName();

		if (!isStringEmpty(user.getNickName())) {
			nickName = user.getNickName();
		}

		return nickName;
	}

	public static boolean isStringEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static String array2Str(String[] array) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]);
			if (i < array.length - 1) {
				sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	@SuppressWarnings("deprecation")
	public static <T> String sendBooleanResponse(boolean boolValue, String msg, T data, HttpServletResponse response)
			throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		try {
			Result<T> ajaxResult = new Result<T>(boolValue, msg, data);

			String json = makeJson(ajaxResult);

			out.println(json);
			return json;
		} finally {
			out.close();
		}
	}

	public static void sendAjaxResult(Result<? extends Object> result, HttpServletResponse response)
			throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		try {
			out.println((JSONSerializer.toJSON(result)).toString());
		} finally {
			out.close();
		}
	}

	public static boolean isValidEmail(String email) {
		return email.matches("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
	}

	public static String getUserAgent(HttpServletRequest request) {
		return request.getHeader("User-Agent");
	}

	/**
	 * 设置多个JSP页面都需要的属性，如: User/IsUsingMobile
	 */
	public static void setCommonAttributesForShowingJSP(HttpServletRequest request) {
		SessionData sessionData = Util.getSessionData(request);
		request.setAttribute(SessionData.SESSION_DATA, sessionData);

		User user = getLoggedInUser();
		if (user != null) {
			request.setAttribute("user", user);
		}

		request.setAttribute("nickName", Util.getNickNameOfUser(user));
		request.setAttribute("isUsingMobile", Util.isUserAgentMobile(request));
		request.setAttribute("isChatRoomOpen", SysParamUtil.isChatRoomOpen());
		request.setAttribute("isSuperUser", user != null && user.getIsSuper());
		request.setAttribute("userAgent", request.getHeader("User-Agent"));
		request.setAttribute("httpUrl", SysParamUtil.getHttpUrl());
		request.setAttribute("soundBaseUrl", Util.getSoundBaseUrl());
		request.setAttribute("eyeMode", getEyeMode(request));
		request.setAttribute("sessionId", request.getSession().getId());

		String context = request.getContextPath();
		String domain = makeDomain(request);
		String basePath = request.getScheme() + "://" + domain + "/service" + context + "/";
		request.setAttribute("basePath", basePath);
		request.setAttribute("imgBaseUrl", SysParamUtil.getImageBaseUrl());
	}

	public static String makeDomain(HttpServletRequest request) {
		String domain = String.format("%s%s", request.getServerName(),
				SysParamUtil.getHostPort() == 80 ? "" : (":" + String.valueOf(SysParamUtil.getHostPort())));
		return domain;
	}

	public static User genUser(String userName, String password, String nickName, String email, User invitedBy) {
		User user = new User();
		user.setUserName(userName.toLowerCase());
		user.setPassword(password);
		user.setNickName(EmojiFilter.filterEmoji(nickName));
		user.setEmail(email);
		SysParamBO sysParamDAO = Global.getSysParamBO();
		SysParam sysParam = sysParamDAO.findById("DefaultWordsPerDay");
		user.setWordsPerDay(Integer.valueOf(sysParam.getParamValue()));
		user.setCreateTime(new Timestamp(new Date().getTime()));
		user.setLearnedDays(0);
		user.setLearningFinished(false);
		user.setLastLearningPosition(-1);
		user.setLastLearningMode(-1);
		user.setMasteredWordsCount(0);
		user.setCowDung(20); // 注册送牛粪
		user.setThrowDiceChance(0);
		user.setInvitedBy(invitedBy);
		user.setInviteAwardTaken(false);
		user.setIsSuper(false);
		user.setIsAdmin(false);
		user.setDakaDayCount(0);
		user.setIsRawWordBookPrivileged(false);
		user.setAutoPlaySentence(false);
		user.setAutoPlayWord(true);
		user.setShowAnswersDirectly(true);
		user.setContinuousDakaDayCount(0);
		user.setMaxContinuousDakaDayCount(0);
		user.setDakaScore(0);
		user.setGameScore(0);
		user.setIsInputor(false);
		return user;
	}

	public static Map<String, String> parseUrlParams(String paramStr) {
		Map<String, String> params = new HashMap<String, String>();
		String[] parts = paramStr.split("&");
		for (String param : parts) {
			if (param.length() > 0) {
				String[] nameAndValue = param.split("=");
				params.put(nameAndValue[0], nameAndValue[1]);
			}
		}
		return params;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseJsonToMap(String response) {

		JSONObject jsonObject = JSONObject.fromObject(response);
		Map<String, Object> map = jsonObject;
		return map;

	}

	/**
	 * 从ip的字符串形式得到字节数组形式
	 *
	 * @param ip
	 *            字符串形式的ip
	 * @return 字节数组形式的ip
	 */
	public static byte[] getIpByteArrayFromString(String ip) {
		byte[] ret = new byte[4];
		StringTokenizer st = new StringTokenizer(ip, ".");

		try {
			ret[0] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
			ret[1] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
			ret[2] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
			ret[3] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
		} catch (Exception e) {
			log.error("无法解析的地址：" + ip);
		}

		return ret;
	}

	/**
	 * @param ip
	 *            ip的字节数组形式
	 * @return 字符串形式的ip
	 */
	public static String getIpStringFromBytes(byte[] ip) {
		StringBuilder sb = new StringBuilder();
		sb.delete(0, sb.length());
		sb.append(ip[0] & 0xFF);
		sb.append('.');
		sb.append(ip[1] & 0xFF);
		sb.append('.');
		sb.append(ip[2] & 0xFF);
		sb.append('.');
		sb.append(ip[3] & 0xFF);
		return sb.toString();
	}

	/**
	 * 根据某种编码方式将字节数组转换成字符串
	 *
	 * @param b
	 *            字节数组
	 * @param offset
	 *            要转换的起始位置
	 * @param len
	 *            要转换的长度
	 * @param encoding
	 *            编码方式
	 * @return 如果encoding不支持，返回一个缺省编码的字符串
	 */
	public static String getString(byte[] b, int offset, int len, String encoding) {
		try {
			return new String(b, offset, len, encoding);
		} catch (UnsupportedEncodingException e) {
			return new String(b, offset, len);
		}
	}

	public static void uploadFile(String srcFile, String destFile) throws Exception {
		Map<String, String> sftpDetails = new HashMap<String, String>();
		// 设置主机ip，端口，用户名，密码
		sftpDetails.put(SFTPConstants.SFTP_REQ_HOST, "116.255.247.39");
		sftpDetails.put(SFTPConstants.SFTP_REQ_USERNAME, "root");
		sftpDetails.put(SFTPConstants.SFTP_REQ_PASSWORD, "y4v6y5");
		sftpDetails.put(SFTPConstants.SFTP_REQ_PORT, "22000");

		SFTPChannel channel = new SFTPChannel();
		ChannelSftp chSftp = channel.getChannel(sftpDetails, 60000);

		chSftp.put(srcFile, destFile, ChannelSftp.OVERWRITE);

		chSftp.quit();
		channel.closeChannel();
	}

	public static ModelAndView createGeneralErrorView(HttpServletRequest request, String errMsg) {
		request.setAttribute("errMsg", errMsg);
		Util.setCommonAttributesForShowingJSP(request);
		return new ModelAndView("generalError");
	}

	/**
	 * 从google下载英文句子的发音文件, 并保存在指定文件中，如果文件不存在则自动创建
	 *
	 * @param sentence
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public static void getSoundOfSentence(String sentence, File destFile) throws IOException {

		if (sentence.length() > 100) {
			return;
		}

		HttpClient httpClient = new HttpClient();
		String url = String.format("http://translate.google.cn/translate_tts?tl=en&q=%s", URLEncoder.encode(sentence));
		HttpMethod method = new GetMethod(url);
		log.info(String.format("从google下载TTS数据, url:%s", url));
		httpClient.executeMethod(method);

		if (method.getStatusCode() == 200) {
			int bytesum = 0;
			int byteread = 0;
			byte[] buffer = new byte[1204];
			InputStream is = method.getResponseBodyAsStream();
			FileOutputStream fs = new FileOutputStream(destFile);
			try {
				while ((byteread = is.read(buffer)) != -1) {
					bytesum += byteread;
					fs.write(buffer, 0, byteread);
				}
			} finally {
				fs.close();
			}
			log.info(String.format("从google下载TTS数据[%d]bytes", bytesum));
		} else {
			log.info(String.format("从google下载TTS数据失败：%s, 句子长度[%s]", method.getStatusLine(), sentence.length()));
		}

		method.releaseConnection();
	}

	/**
	 * 计算句子的摘要信息，以便区分句子
	 *
	 * @param sentence
	 * @return
	 */
	public static String makeSentenceDigest(String sentence) {
		return MD5Utils.md5(sentence);
	}

	public static WordVo makeFakeWord()
			throws IOException, ParseException, InvalidMeaningFormatException, EmptySpellException {
		WordVo word = new WordVo("fake");

		List<MeaningItemVo> meaningItems = new ArrayList<MeaningItemVo>();
		MeaningItemVo meaningItem = new MeaningItemVo("adj", "假的");
		meaningItems.add(meaningItem);
		word.setMeaningItems(meaningItems);

		List<SynonymsItem> synonymsItems = new ArrayList<SynonymsItem>();
		SynonymsItem item = new SynonymsItem();
		item.setMeaning("n. 脸；表面；面子；面容；外观；威信");
		List<String> words = new ArrayList<String>();
		words.add("surface");
		words.add("outside");
		words.add("garment");
		words.add("look");
		item.setWords(words);
		synonymsItems.add(item);

		item = new SynonymsItem();
		item.setMeaning("vi. 向；朝");
		words = new ArrayList<String>();
		words.add("open to");
		item.setWords(words);
		synonymsItems.add(item);

		item = new SynonymsItem();
		item.setMeaning("vt. 面对；面向；承认；抹盖");
		words = new ArrayList<String>();
		words.add("accept");
		words.add("agree");
		words.add("recognize");
		words.add("look");
		words.add("grant");
		item.setWords(words);
		synonymsItems.add(item);

		word.setSynonymsItems(synonymsItems);

		WordVo word2 = WordStore.getInstance().getWordBySpell("face");
		// word2.setGroupInfo(null);
		// word2.setSynonymsItems(synonymsItems);

		word.setSynonymsItems(word2.getSynonymsItems());

		return word;
	}

	private static volatile long lastMsgID = 0;

	private static String generateMsgID() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + String.valueOf(++lastMsgID);
	}

	/**
	 * 转换用户提交的内容，因为其中可能含有攻击脚本。
	 *
	 * @param content
	 * @return
	 */
	public static String purifyContent(String content) {
		return content.replaceAll("'", "’").replaceAll("\"", "”").replaceAll("&", "§").replace("script", "ｓｃｒｉｐｔ");
	}

	/**
	 * 获取当前的登录用户(可能是管理员、供应商或顾客), 如果未登录，返回null<br>
	 * 注：返回的用户对象是PO，是从数据库中读取的，因而与数据库信息一致。
	 *
	 * @return
	 * @throws Exception
	 */
	public static User getLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			Object principal = authentication.getPrincipal();
			return getUserFromPrincipal(principal);
		}
		return null;
	}

	/**
	 * 获取当前的登录用户(可能是管理员、供应商或顾客), 如果未登录，返回null<br>
	 * 注：返回的用户对象是VO，是从缓存中读取的，因而可能与数据库信息不一致。
	 */
	public static UserVo getLoggedInUserVO() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof UserDetailsImpl) {
				return ((UserDetailsImpl) principal).getUserVo();
			}
		}
		return null;
	}

	public static User getUserFromPrincipal(Object principal) {
		if (principal instanceof UserDetailsImpl) {
			UserDetailsImpl userDetails = (UserDetailsImpl) principal;
			User user = Global.getUserBO().getByUserName(userDetails.getUsername());

			return user;
		}
		return null;
	}

	/**
	 * 根据是否含有x-requested-with头来确定请求是否是AJAX请求。<br>
	 * 由于/login.do可能是跨域请求，而浏览器对跨域请求有特殊限制，无法为请求附加x-requested-with头，所以/login.
	 * do直接认为是AJAX请求
	 *
	 * @param request
	 * @return
	 */
	public static boolean isAjaxRequest(HttpServletRequest request) {
		String requestedWith = request.getHeader("x-requested-with");
		return "XMLHttpRequest".equalsIgnoreCase(requestedWith) || request.getRequestURI().contains("/login.do");
	}

	/**
	 * 获取一个单词的所有可能变体（如-ing, -ed）
	 *
	 * @return
	 */
	public static List<String> getVariantsOfWord(String word) {
		List<String> variants = new ArrayList<String>();

		// 自身
		variants.add(word);

		// -s -es
		if (word.endsWith("s") || word.endsWith("ch")) {
			variants.add(word + "es");
		} else {
			variants.add(word + "s");
		}

		// -ing -ed
		if (word.endsWith("e")) {
			variants.add(word.substring(0, word.length() - 1) + "ing");
			variants.add(word + "d");
		} else {
			variants.add(word + "ed");
		}

		return variants;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(Arrays.toString(getVariantsOfWord("teach").toArray(new String[0])));
	}

	public static boolean getEyeMode(HttpServletRequest request) {
		Boolean eyeMode = (Boolean) request.getSession().getAttribute("eyeMode");
		eyeMode = eyeMode == null ? false : eyeMode;
		return eyeMode;
	}

	/**
	 * 获取用户学习中的(并且是被用户选中的)单词书 (单词书可能尚未学完就被用户删掉，这些单词书也是学习中的，但未被选中)
	 *
	 * @param user
	 * @return
	 */
	public static List<LearningDict> getSelectedLearningDicts(User user) {
		List<LearningDict> learningDicts = user.getLearningDicts();
		for (Iterator<LearningDict> i = learningDicts.iterator(); i.hasNext();) {

			LearningDict leaningDict = i.next();
			SelectedDictId id = new SelectedDictId(user.getId(), leaningDict.getDict().getId());
			SelectedDict selectedDict = Global.getSelectedDictBO().findById(id);
			if (selectedDict == null) {
				i.remove();
			} else {
				leaningDict.setIsPrivileged(selectedDict.getIsPrivileged());
			}
		}
		return learningDicts;
	}

	public static boolean isAllDictsFinished(List<LearningDict> learningDicts) {
		boolean allDictsFinished = true;
		for (LearningDict dict : learningDicts) {
			boolean isLearningFinished = (dict.getCurrentWordOrder() == null ? -1 : dict.getCurrentWordOrder()) >= dict
					.getDict().getWordCount();
			if (!isLearningFinished) {
				allDictsFinished = false;
				break;
			}
		}
		return allDictsFinished;
	}

	public static boolean needSelectDictBeforeStudy(User user) {
		List<LearningDict> learningDicts = getSelectedLearningDicts(user);
		boolean allDictsFinished = isAllDictsFinished(learningDicts);
		return allDictsFinished;
	}

	/**
	 * 生成指定范围内的随机整数
	 *
	 * @param min
	 * @param max
	 * @return
	 */
	public static int genRandomNumber(int min, int max) {
		Random random = new Random();
		return random.nextInt(max) % (max - min + 1) + min;
	}

	/**
	 * 根据Http Session ID获取此Session上登录的用户
	 *
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static User getUserBySessionId(String jsessionid) throws Exception {
		User user = null;
		SessionRegistry sessionRegistry = Global.getSessionRegistry();
		SessionInformation sessionInfo = sessionRegistry.getSessionInformation(jsessionid);
		if (sessionInfo != null && !sessionInfo.isExpired()) {
			Object principal = sessionInfo.getPrincipal();
			if (principal != null) {
				user = Util.getUserFromPrincipal(principal);
			}
		}
		return user;
	}

	/**
	 * 获取客户端IP
	 *
	 * @param request
	 * @return
	 */
	public static String getClientIP(HttpServletRequest request) {
		String remoteAddr = request.getHeader("X-Forwarded-For"); // X-Forwarded-For是nginx配置文件中定义的，保存了客户端实际IP地址
		if (remoteAddr == null) {
			remoteAddr = request.getRemoteAddr();
		} else {
			remoteAddr = remoteAddr.split(",")[0]; // 在有多个nginx的情况下，X-Forwarded-For的值是客户端到服务端路径中每个主机的IP地址（以逗号分隔），其中第一个是客户端的IP地址
		}
		return remoteAddr;
	}

	public static Date removeTimePart(Date date) throws java.text.ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String s = sdf.format(date);
		return sdf.parse(s);
	}
}