package os.job;

import hardware.CPU;
import os.schedule.Schedule;

// 检查是否有新的作业，进行高级调度
public class JobDetector {
    public static JobDetector jobDetector = new JobDetector();

    public static final int DETECTOR_INTERVAL = 5000;

    public void StartJobDetect() {
        new Detector().start();
    }

    // 检查后备作业队列，当有作业的进入时间等于系统时间后，则进行高级调度

    class Detector extends Thread {
        public void run() {
            while (true) {
                try {
                    sleep(DETECTOR_INTERVAL);
                    int now = CPU.cpu.clock.getCurrentTime();
                    System.out.printf("[JOB DETECT]-----正在进行后备队列检测，当前系统时间:%d\n", now);
                    Schedule.needHighLevelScheduling = needHighLevelSchedule(now);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        boolean needHighLevelSchedule(int now) {
            return JobManage.jm.backJCBS.size() != 0 && JobManage.jm.backJCBS.get(0).getJobInTime() <= now;
        }
    }
}
