package com.javier.simplemvc.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Logger {
	private static String tag = "SimpleMVC";

	/**
	 * 是否开启LOG
	 */
	public static boolean sIsLoggerEnable = true;
	/**
	 * 日志对象
	 */
	private static Logger mLogger = null;
	/**
	 * 是否写文件
	 */
	private boolean writeFileFlag = false;
	/**
	 * 日志文件
	 */
	public static String logFile = "";
	/**
	 * 打印日志级别
	 */
	private static int logLevel = Log.VERBOSE;

	public static boolean DEBUG = false;

	public static void initLogger(String tag, String appname, String logfileName, boolean debug, boolean bWirteFile) {
		mLogger = new Logger(tag, appname, logfileName, debug, bWirteFile);
	}

	public static Logger getLogger() {
		if(mLogger == null) {
			mLogger = new Logger();
		}

		return mLogger;
	}

	private Logger() {
		init("SimpleMVC", "SimpleMVC.log");
	}

	private Logger(String tag, String appname, String logfileName, boolean debug, boolean bWirteFile) {
		Logger.tag = tag;

		writeFileFlag = bWirteFile;

		DEBUG = debug;

		if (DEBUG) {
			logLevel = Log.VERBOSE;
		} else {
			logLevel = Log.ERROR;
		}

		init(appname, logfileName);
	}

	private void init(String appname, String logfileName) {
		String status = Environment.getExternalStorageState();
		if (writeFileFlag && status.equals(Environment.MEDIA_MOUNTED)) {
			logFile = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/" + appname + "/" + logfileName;
			File file = new File(logFile);
			file.mkdirs();
			if (file.exists()) {
				file.delete();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();
		if (sts == null) {
			return null;
		}

		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}
			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			if (st.getClassName().equals(this.getClass().getName())) {
				continue;
			}
			return "[ " + Thread.currentThread().getId() + " - "
					+ st.getFileName() + ":" + st.getLineNumber() + " - "+ st.getMethodName() +" ]";
		}
		return null;
	}

	public void info(Object str) {
		if (logLevel <= Log.INFO) {
			String name = getFunctionName();

			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.i(tag, ls);
			if (writeFileFlag) {
				writeToFile("Info", tag, name + " - " + str);
			}
		}
	}

	public void i(Object str) {
		info(str);
	}

	public void verbose(Object str) {
		if (logLevel <= Log.VERBOSE) {
			String name = getFunctionName();

			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.v(tag, ls);
			if (writeFileFlag) {
				writeToFile("Verbose", tag, ls);
			}
		}
	}

	public void v(Object str) {
		verbose(str);
	}

	public void warn(Object str) {
		if (logLevel <= Log.WARN) {
			String name = getFunctionName();

			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.w(tag, ls);
			if (writeFileFlag) {
				writeToFile("Warn", tag, ls);
			}
		}
	}

	public void w(Object str) {
		warn(str);
	}

	public void error(Object str) {
		if (logLevel <= Log.ERROR) {
			String name = getFunctionName();

			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.e(tag, ls);
			if (writeFileFlag) {
				writeToFile("Error", tag, ls);
			}
		}
	}

	public void error(Exception ex) {
		if (logLevel <= Log.ERROR) {

			StringBuffer sb = new StringBuffer();
			String name = getFunctionName();

			StackTraceElement[] sts = ex.getStackTrace();

			if (name != null) {
				sb.append(name + " - " + ex + "\r\n");
			} else {
				sb.append(ex + "\r\n");
			}

			if (sts != null && sts.length > 0) {
				for (StackTraceElement st : sts) {
					if (st != null) {
						sb.append("[ " + st.getFileName() + ":"
								+ st.getLineNumber() + " ]\r\n");
					}
				}
			}
			Log.e(tag, sb.toString());
			if (writeFileFlag)
				writeToFile("Excep", tag, sb.toString());
		}
	}

	public void e(Object str) {
		error(str);
	}

	public void e(Exception ex) {
		error(ex);
	}

	public void debug(Object str) {
		if (logLevel <= Log.DEBUG) {
			String name = getFunctionName();
			String ls = (name == null ? str.toString() : (name + " - " + str));
			Log.d(tag, ls);
			if (writeFileFlag) {
				writeToFile("Debug", tag, ls);
			}
		}
	}

	public void d(Object str) {
		debug(str);
	}

	private void writeToFile(String level, String tag, String info) {
		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			return;
		}
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd   hh:mm:ss");
		String date = sDateFormat.format(new Date());
		String msg = date + "  " + level + "--" + tag + ":" + info;

		try {
			FileOutputStream output = new FileOutputStream(logFile);

			output.write(msg.toString().getBytes());
			output.write("\r\n".getBytes());
			output.flush();
			output.close();

			output = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void d(String log, Throwable tr) {
		if (sIsLoggerEnable) {
			d(log + "\n" + Log.getStackTraceString(tr));
		}
	}

	public void i(String log, Throwable tr) {
		if (sIsLoggerEnable) {
			i(log + "\n" + Log.getStackTraceString(tr));
		}
	}

	public void w(String log, Throwable tr) {
		if (sIsLoggerEnable) {
			w(log + "\n" + Log.getStackTraceString(tr));
		}
	}

	public void e(String log, Throwable tr) {
		if (sIsLoggerEnable) {
			e(log + "\n" + Log.getStackTraceString(tr));
		}
	}

	public void e(Throwable tr) {
		if (sIsLoggerEnable) {
			e(Log.getStackTraceString(tr));
		}
	}
}