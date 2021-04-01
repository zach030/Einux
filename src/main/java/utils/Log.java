package utils;

import hardware.CPU;
import org.apache.log4j.Logger;
import ui.PlatForm;

// 系统日志
public class Log {
    public static Log log = new Log();
    public static Logger logger = Logger.getLogger(Log.class);

    public static void Info(String period, String msg) {
        String data = "[" + period + "]" + "[Time :" + CPU.cpu.clock.getCurrentTime() + "]" + "-------" + msg + "\n";
        logger.info(data);
        PlatForm.platForm.refreshSystemLog(data);
    }

    public static void Debug(String period, String msg) {
        String data = "[" + period + "]" + "[Time :" + CPU.cpu.clock.getCurrentTime() + "]" + "-------" + msg + "\n";
        logger.debug(data);
        PlatForm.platForm.refreshSystemLog(data);
    }

    public static void Error(String period, String msg) {
        String data = "[" + period + "]" + "[Time :" + CPU.cpu.clock.getCurrentTime() + "]" + "-------" + msg + "\n";
        logger.error(data);
        PlatForm.platForm.refreshSystemLog(data);
    }
}
