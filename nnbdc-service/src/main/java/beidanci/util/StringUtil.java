/**
 *
 */
package beidanci.util;

import java.util.Date;

/**
 * @author Yongrui Wang
 */
public class StringUtil {

	public static boolean isValuableStr(String string) {
		boolean result = false;

		if (null != string) {
			result = !string.trim().isEmpty();
		}

		return result;
	}

	/**
	 * 以字符串格式输出当前日期 日期格式：yyyy-MM-dd HH:mm:ss
	 *
	 * @return
	 */
	public static Date getStringDate() {
		Date date = new Date();
		// 规定日期的输出格式
		// SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// //对日期进行格式化
		// String str=sdf.format(date);
		return date;
	}

}
