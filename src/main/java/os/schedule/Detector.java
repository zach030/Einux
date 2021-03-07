package os.schedule;

import hardware.CPU;
import os.job.JobManage;
import utils.Log;

public class Detector {
    public static Detector detector = new Detector();
    public static final int HIGH_DETECTOR_INTERVAL = 3000;
    public static final int MID_DETECTOR_INTERVAL = 2000;

    public void StartDetector() {
        //new MidDetector().start();
        new JobDetector().start();
    }

    class MidDetector extends Thread {
        public void run() {
            while (true) {
                try {
                    sleep(MID_DETECTOR_INTERVAL);
                    Log.Info("定期开启中级调度", "正在检测是否可进行中级调度");
                    Schedule.needMediumLevelScheduling = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class JobDetector extends Thread {
        public void run() {
            while (true) {
                try {
                    sleep(HIGH_DETECTOR_INTERVAL);
                    int now = CPU.cpu.clock.getCurrentTime();
                    Log.Info("后备作业检测", "正在进行后备队列检测");
                    Schedule.needHighLevelScheduling = needHighLevelSchedule(now);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        boolean needHighLevelSchedule(int now) {
            return JobManage.jm.backJCBS.size() != 0 && JobManage.jm.backJCBS.get(0).getJobInTime() * 1000 <= now;
        }
    }
}
