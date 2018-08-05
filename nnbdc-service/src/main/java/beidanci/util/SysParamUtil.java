package beidanci.util;

import beidanci.Global;
import beidanci.bo.SysParamBO;

public class SysParamUtil {
	private static SysParamBO sysParamBO = Global.getSysParamBO();

	public static String getHostName() {
		return sysParamBO.findById("HostName").getParamValue();
	}

	public static boolean isChatRoomOpen() {
		return Boolean.parseBoolean(sysParamBO.findById("IsChatRoomOpen").getParamValue());
	}

	public static boolean isJplayerWarningEnabled() {
		return Boolean.parseBoolean(sysParamBO.findById("JplayerWarningEnabled").getParamValue());
	}

	public static boolean isJplayerErrorEnabled() {
		return Boolean.parseBoolean(sysParamBO.findById("JplayerErrorEnabled").getParamValue());
	}

	public static String getImageBaseUrl() {
		return sysParamBO.findById("imgBaseUrl").getParamValue();
	}

	public static String getImageBaseDir() {
		return sysParamBO.findById("imgBaseDir").getParamValue();
	}

	public static String getSocketServerAddr() {
		return sysParamBO.findById("SocketServerAddr").getParamValue();
	}

	public static int getSocketServerPort() {
		return Integer.parseInt(sysParamBO.findById("SocketServerPort").getParamValue());
	}

	public static int getHostPort() {
		return Integer.parseInt(sysParamBO.findById("HostPort").getParamValue());
	}

	public static int getDefaultWordsPerDay() {
		return Integer.parseInt(sysParamBO.findById("DefaultWordsPerDay").getParamValue());
	}

	public static int getAwardCowDungForShare() {
		return Integer.parseInt(sysParamBO.findById("AwardCowDungForShare").getParamValue());
	}

	public static int getFetchMsgInterval() {
		return Integer.parseInt(sysParamBO.findById("FetchMsgInterval").getParamValue());
	}

	public static String getHttpUrl() {
		return sysParamBO.findById("HttpUrl").getParamValue();
	}

	public static String getTempDirForUpload() {
		return sysParamBO.findById("TempDirForUpload").getParamValue();
	}

	public static String getSaveDirForUpload() {
		return sysParamBO.findById("SaveDirForUpload").getParamValue();
	}

	public static String getSoundPath() {
		return sysParamBO.findById("SoundPath").getParamValue();
	}

	public static float getHolidayCowDungRatio() {
		return Float.parseFloat(sysParamBO.findById("HolidayCowDungRatio").getParamValue());
	}

	public static String getHolidayCowDungDesc() {
		return sysParamBO.findById("HolidayCowDungDesc").getParamValue();
	}

	public static String getExportFileDir() {
		return sysParamBO.findById("exportFileDir").getParamValue();
	}

	public static String getExportFileUrl() {
		return sysParamBO.findById("exportFileUrl").getParamValue();
	}
}
