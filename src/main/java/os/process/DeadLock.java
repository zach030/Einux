package os.process;

import utils.Log;

public class DeadLock {
    public static DeadLock deadLock = new DeadLock();
    public static Banker banker;

    /*资源类型0,1,2*/
    enum ResourceType {
        KEYBOARD, SCREEN, PRINT
    }

    public static final int PROCESS_NUM = 15;
    public static final int RESOURCE_CATEGORY = 3;

    class Banker {
        Banker() {
            initMaxMatrix();
            initAllocationMatrix();
            initNeedMatrix();
        }

        // 初始化最大需求矩阵，假设都需求全部资源
        void initMaxMatrix() {
            for (int p = 0; p < PROCESS_NUM; p++) {
                Max[p][0] = 1;
                Max[p][1] = 1;
                Max[p][2] = 2;
            }
        }

        // 初始化已分配矩阵，假设都没分配资源
        void initAllocationMatrix() {
            for (int i = 0; i < PROCESS_NUM; i++) {
                Allocation[i][0] = 0;
                Allocation[i][1] = 0;
                Allocation[i][2] = 0;
            }
        }

        // 初始化需求矩阵
        void initNeedMatrix() {
            for (int i = 0; i < PROCESS_NUM; i++) {
                Need[i][0] = Max[i][0] - Allocation[i][0];
                Need[i][1] = Max[i][1] - Allocation[i][1];
                Need[i][2] = Max[i][2] - Allocation[i][2];
            }
        }

        // 初始化工作向量
        void initWorkVector() {
            System.arraycopy(Available, 0, Work, 0, RESOURCE_CATEGORY);
        }

        // 初始化标志位向量
        void initFinishVector() {
            for (int i = 0; i < PROCESS_NUM; i++) {
                Finish[i] = false;
            }
        }

        // 加入rest集合
        void initRestSet() {
            for (int i = 0; i < PROCESS_NUM; i++) {
                Rest[i] = i;
            }
        }

        int[] Available = new int[]{1, 1, 2};                         // 各资源当前可申请数目
        int[][] Max = new int[PROCESS_NUM][RESOURCE_CATEGORY];        // 最大需求矩阵
        int[][] Allocation = new int[PROCESS_NUM][RESOURCE_CATEGORY]; // 已分配矩阵
        int[][] Need = new int[PROCESS_NUM][RESOURCE_CATEGORY];       // 需求矩阵

        int[][] Request = new int[PROCESS_NUM][RESOURCE_CATEGORY];    // 资源请求矩阵
        int[] Work = new int[RESOURCE_CATEGORY];    // 系统可提供给进程继续运行所需的各类资源数目
        boolean[] Finish = new boolean[PROCESS_NUM]; //它表示系统是否有足够的资源分配给进程，使之运行完成
        int[] Rest = new int[PROCESS_NUM];
        int[] Mutex = new int[]{1, 1, 2};

        private void P(int resource) {
            this.Mutex[resource]--;
        }

        private void V(int resource) {
            this.Mutex[resource]++;
        }
    }

    public void makeApplyRequest(PCB pcb, int resource, int num) {
        banker.Request[pcb.getID()][resource] = num;
    }

    public void applyResource(PCB pcb, int resource, int num) {
        //todo 银行家算法实现
        //0 比较request与need，如果超过need，则出错，需求资源数已超过最大申请
        int requestNum = banker.Request[pcb.getID()][resource];
        int needNum = banker.Need[pcb.getID()][resource];
        if (requestNum > needNum) {
            Log.Error("死锁检测", String.format("当前进程:%d，对于资源:%d，的申请量:%d，已超过其所需值:%d", pcb.getID(), resource, requestNum, needNum));
            return;
        }
        //1 比较request与available，如果大于，则需要阻塞
        int availableNum = banker.Available[resource];
        if (requestNum > availableNum) {
            Log.Error("死锁检测", String.format("当前进程:%d，对于资源:%d，的申请量:%d，已超过系统可用值:%d", pcb.getID(), resource, requestNum, availableNum));
            //todo 阻塞进程
        }
        int[][] allotCopy = banker.Allocation;
        int[] availableCopy = banker.Available;
        int[][] needCopy = banker.Need;
        int[][] requestCopy = banker.Request;
        //2 尝试将资源分配给进程 Available[j] = Available[j] - Requesti[j];
        //　　　　              Allocation[i,j] = Allocation[i,j] + Requesti[j];
        //　　　　              Need[i,j] = Need[i,j] - Requesti[j];
        //安全性算法
        if (attemptAllot(pcb.getID(), resource, availableCopy, requestCopy, allotCopy, needCopy)) {
            // 通过
            banker.Allocation = allotCopy;
            banker.Available = availableCopy;
            banker.Need = needCopy;
            banker.Request = requestCopy;
        }
        //todo 不通过分配
        // 进程等待分配
    }

    // 试探性分配
    public boolean attemptAllot(int pid, int rid, int[] available, int[][] request, int[][] allot, int[][] need) {
        allot[pid][rid] += request[pid][rid];
        available[rid] -= request[pid][rid];
        need[pid][rid] -= request[pid][rid];
        // 安全检测
        banker.initWorkVector();
        banker.initFinishVector();
        banker.initRestSet();
        banker.Finish[pid] = true;
        boolean find = false;
        for (int i = 0; i < PROCESS_NUM; i++) {
            int p = banker.Rest[i];
            if (need[p][rid] <= banker.Work[p]) {
                // 释放pi占用的资源
                banker.Work[rid] += allot[p][rid];
                banker.Rest[p] = -1;
                find = true;
            }
        }
        if(find){
            banker.Finish[pid]=false;
            //todo 停止算法
        }
        for (int i = 0; i < banker.Rest.length; i++) {
            if (banker.Rest[i] != -1) {
                return false;
            }
        }
        return true;
    }

    public void Run() {
        new Thread(new Detect()).start();
    }

    public class Detect extends Thread {
        public void run() {
            //todo 资源的死锁检测
        }
    }
}
