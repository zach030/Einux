package os;

import os.schedule.Schedule;

public class Controller {
    public static Controller controller = new Controller();
    /**
        * @description: 开机
        * @author: zach
     **/
    public void Start(){
        Schedule.schedule.init();
        Schedule.schedule.start();
    }
    /**
        * @description: 暂停
        * @author: zach
     **/
    public void Pause(){
        Schedule.schedule.pauseThread();

    }
    /**
        * @description: 继续
        * @author: zach
     **/
    public void Resume(){
        Schedule.schedule.resumeThread();
    }
    /**
        * @description: 结束
        * @author: zach
     **/
    public void Stop(){
        Schedule.schedule.Stop();
    }
}
