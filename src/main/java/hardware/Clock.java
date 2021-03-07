package hardware;

//TODO 提供时钟中断
public class Clock extends Thread {
    public static Clock clock = new Clock();

    private boolean if_interrupt = false;        //是否发生中断的标志位
    private int time = -100;    //计时器的时间
    public static final int SYSTEM_INTERVAL = 100;

    public void run() {
        while (true) {
            try {
                sleep(SYSTEM_INTERVAL);        //睡眠一定时间
                if_interrupt = true;           //CPU内时间自增
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    synchronized public void systemTimeSelfAdd() {
        time += SYSTEM_INTERVAL;
    }

    synchronized public int getCurrentTime() {
        return time;
    }

    synchronized public boolean GetIfInterrupt() {
        return if_interrupt;
    }

    synchronized public void ResetIfInterrupt() {
        if_interrupt = false;
    }
}
