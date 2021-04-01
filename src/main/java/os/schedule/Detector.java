package os.schedule;

import hardware.CPU;
import os.job.JobManage;
import os.process.DeadLock;
import os.process.ProcessManager;
import utils.Log;

public class Detector {
    public static Detector detector = new Detector();
    public static final int HIGH_DETECTOR_INTERVAL = 2000;
    public static final int MID_DETECTOR_INTERVAL = 2000;
    MidDetector midDetector = new MidDetector();
    JobDetector jobDetector = new JobDetector();
    WakeDetector wakeDetector = new WakeDetector();

    public void StartDetector() {
        Log.Info("检测线程", "正在开启高级、中级调度检测线程...");
        midDetector.start();
        jobDetector.start();
        wakeDetector.start();
    }

    public void StopDetector() {
        midDetector.interrupt();
        jobDetector.interrupt();
    }

    /**
     * @description: 如果当前运行进程为空，就绪队列为空，进程被阻塞了，这个线程用来将被阻塞的线程唤醒，加入就绪队列
     * @author: zach
     **/
    static class WakeDetector extends Thread {
        public void run() {
            while (true){
                try {
                    sleep(5000);
                    if (ProcessManager.pm.queueManager.isReadyQueueEmpty() && ProcessManager.pm.queueManager.isPCBBlockByResource()) {
                        DeadLock.deadLock.releaseSystemResource();
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    static class MidDetector extends Thread {
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

    static class JobDetector extends Thread {
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
