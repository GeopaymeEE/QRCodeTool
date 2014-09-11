package polly.java.util;

public class LogUtil {
	public static void log(Object from, Object... msges) {
		if (from == null || msges == null || msges.length == 0) {
			return;
		}
		String fromStr = (from instanceof String) ? from.toString() : from.getClass().getName();
		System.out.println("--[" + fromStr + "]--");
		for (int i = 0; i < msges.length; i++) {
			System.out.println(msges[i]);
		}
		System.out.println("--[END]--");
		System.out.println();
	}
}
