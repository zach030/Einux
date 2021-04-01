package os;

import hardware.CPU;
import os.schedule.Detector;
import os.schedule.Schedule;
import ui.PlatForm;

public class Controller {
    public static Controller controller = new Controller();

    /**
     * @description: 开机
     * @author: zach
     **/
    public void Start() {
        Schedule.schedule.start();
    }

    /**
     * @description: 暂停
     * @author: zach
     **/
    public void Pause() {
        Schedule.schedule.pauseThread();
    }

    /**
     * @description: 继续
     * @author: zach
     **/
    public void Resume() {
        Schedule.schedule.resumeThread();
    }

    /**
     * @description: 结束
     * @author: zach
     **/
    public void Stop() {
        Schedule.schedule.interrupt();
        CPU.cpu.clock.interrupt();
        Detector.detector.StopDetector();
        PlatForm.platForm.refreshSystemLog("系统已全部运行结束...");
    }
}
