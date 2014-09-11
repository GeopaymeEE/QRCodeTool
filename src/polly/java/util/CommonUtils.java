package polly.java.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {

	/**
	 * 格式化文件大小
	 * @param size 文件的字节数
	 * @return
	 */
	public static String formatFileSize(long size) {
		String sizeStr = null;
		DecimalFormat format = new DecimalFormat("#.00");
		if (size < 1024) {
			sizeStr = size + "B";
		} else if (size < 1048576) {
			sizeStr = format.format((double) size / 1024) + "K";
		} else if (size < 1073741824) {
			sizeStr = format.format((double) size / 1048576) + "M";
		} else {
			sizeStr = format.format((double) size / 1073741824) + "G";
		}
		return sizeStr;
	}
	
	/**
	 * 格式化日期
	 * @param pattern 比如：yyyy年MM月dd日 H时m分s秒
	 * @param time 距离基准时间的毫秒值
	 * @return
	 */
	public static String formatDate(String pattern, long time) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(new Date(time));
	}
}
