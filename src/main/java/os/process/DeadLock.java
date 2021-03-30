package os.process;

import hardware.CPU;
import utils.Log;
import utils.SysConst;

import java.util.ArrayList;
import java.util.Arrays;

public class DeadLock {
    public static DeadLock deadLock = new DeadLock();
    public static Banker banker;

    DeadLock() {
        banker = new Banker();
    }

    /*资源类型0,1,2 资源数目：1,1,5 */
    public enum ResourceType {
        KEYBOARD, SCREEN, OTHER
    }

    public static final int PROCESS_NUM = 15;
    public static final int RESOURCE_NUM = 3;

    class Banker {
        Banker() {
            initMaxMatrix();
            initAllocationMatrix();
            initNeedMatrix();
            initSafeSeries();
        }

        // 初始化最大需求矩阵，假设都需求全部资源
        void initMaxMatrix() {
            //todo 改成随机数
            for (int p = 0; p < PROCESS_NUM; p++) {
                Max[p][0] = 1;
                Max[p][1] = 1;
                Max[p][2] = 5;
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
            System.arraycopy(Available, 0, Work, 0, RESOURCE_NUM);
        }

        // 初始化标志位向量
        void initFinishVector() {
            for (int i = 0; i < PROCESS_NUM; i++) {
                Finish[i] = false;
            }
            //todo 因为无0号进程
            Finish[0] = true;
        }

        // 加入rest集合
        void initRestSet() {
            for (int i = 0; i < PROCESS_NUM; i++) {
                Rest[i] = i;
            }
            //todo 因为无0号进程
            Rest[0] = -1;
        }

        void initSafeSeries() {
            Arrays.fill(safeSeries, 0);
        }

        int[] Available = new int[]{1, 1, 5};                         // 各资源当前可申请数目
        int[][] Max = new int[PROCESS_NUM][RESOURCE_NUM];        // 最大需求矩阵
        int[][] Allocation = new int[PROCESS_NUM][RESOURCE_NUM]; // 已分配矩阵
        int[][] Need = new int[PROCESS_NUM][RESOURCE_NUM];       // 需求矩阵
        boolean[] Finish = new boolean[PROCESS_NUM]; //它表示系统是否有足够的资源分配给进程，使之运行完成
        int[] safeSeries = new int[PROCESS_NUM];
        int[][] Request = new int[PROCESS_NUM][RESOURCE_NUM];    // 资源请求矩阵
        int[] Work = new int[RESOURCE_NUM];    // 系统可提供给进程继续运行所需的各类资源数目
        int[] Rest = new int[PROCESS_NUM];

        void display() {
            System.out.println("当前系统各类资源剩余: ");
            for (int i = 0; i < RESOURCE_NUM; i++) {
                System.out.print(String.format("%d\t", Available[i]));
            }
            System.out.println("\n");
            System.out.println("PID\t\tMAX\t\tAllocation\tNeed\n");
            for (int i = 0; i < PROCESS_NUM; i++) {
                System.out.print(String.format("P%d\t\t", i));
                for (int j = 0; j < RESOURCE_NUM; j++) {
                    System.out.print(String.format("%d ", Max[i][j]));
                }
                System.out.print("\t");
                for (int j = 0; j < RESOURCE_NUM; j++) {
                    System.out.print(String.format("%d ", Allocation[i][j]));
                }
                System.out.print("\t");
                for (int j = 0; j < RESOURCE_NUM; j++) {
                    System.out.print(String.format("%d ", Need[i][j]));
                }
                System.out.println("\n");
            }
            System.out.println("\n");
        }
    }

    private void makeApplyRequest(PCB pcb, int resource, int num) {
        Log.Info("申请资源", String.format("进程：%d,申请资源:%d, %d个", pcb.getID(), resource, num));
        banker.Request[pcb.getID()][resource] = num;
    }

    public void applyResource(PCB pcb, ResourceType resourceType, int num) {
        banker.display();
        int resource = resourceType.ordinal();
        this.makeApplyRequest(pcb, resource, num);
        //0 比较request与need，如果超过need，则出错，需求资源数已超过最大申请
        int requestNum = banker.Request[pcb.getID()][resource];
        int needNum = banker.Need[pcb.getID()][resource];
        if (requestNum > needNum) {
            //todo 直接return or 阻塞？？
            Log.Error("死锁检测", String.format("当前进程:%d，对于资源:%d，的申请量:%d，已超过其所需值:%d", pcb.getID(), resource, requestNum, needNum));
            return;
        }
        //1 比较request与available，如果大于，则需要阻塞
        int availableNum = banker.Available[resource];
        if (requestNum > availableNum) {
            ProcessManager.pm.processOperator.blockPCB(pcb, resource);
            return;
        }
        int[][] allotCopy = banker.Allocation;
        int[] availableCopy = banker.Available;
        int[][] needCopy = banker.Need;
        int[][] requestCopy = banker.Request;
        if (attemptAllot(pcb.getID(), resource, availableCopy, requestCopy, allotCopy, needCopy)) {
            // 通过
            banker.Allocation = allotCopy;
            banker.Available = availableCopy;
            banker.Need = needCopy;
            banker.Request = requestCopy;
            Log.Info("银行家算法尝试分配", "此次尝试分配后是安全序列，可以分配");
            banker.display();
            return;
        }
        //todo 不通过分配
        // 进程等待分配
        banker.display();
        Log.Error("银行家算法尝试分配", "此次尝试分配后不安全，放弃分配资源");
        ProcessManager.pm.processOperator.blockPCB(pcb, resource);
    }

    // 试探性分配
    public boolean attemptAllot(int pid, int rid, int[] available, int[][] request, int[][] allot, int[][] need) {
        Log.Info("银行家算法尝试分配", String.format("已尝试给进程:%d,分配资源%d, 数量:%d", pid, rid, request[pid][rid]));
        allot[pid][rid] += request[pid][rid];
        available[rid] -= request[pid][rid];
        need[pid][rid] -= request[pid][rid];
        // 安全检测
        banker.initWorkVector();
        banker.initFinishVector();
        banker.initRestSet();
        for (int i = 1; i < PROCESS_NUM; i++) {
            int p = banker.Rest[i];
            if (need[p][rid] <= banker.Work[rid]) {
                // 释放pi占用的资源
                banker.Finish[i] = true;
                banker.Work[rid] += allot[p][rid];
                banker.Rest[p] = -1;
            }
        }
        for (int i = 0; i < banker.Rest.length; i++) {
            if (banker.Rest[i] != -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * @description: 进程释放资源
     * @author: zach
     **/
    public void releaseResource(PCB pcb, ResourceType resourceType, int num) {
        banker.Available[resourceType.ordinal()] += num;
        banker.Allocation[pcb.getID()][resourceType.ordinal()] -= num;
    }

    // 释放pcb所占用资源
    public void releasePCBResource(PCB pcb) {
        // 从资源阻塞队列中移除被阻塞进程
        for (int i = 0; i < RESOURCE_NUM; i++) {
            if (ProcessManager.pm.queueManager.resourceBlockQueue.get(i).contains(pcb)) {
                ProcessManager.pm.queueManager.resourceBlockQueue.get(i).remove(pcb);
                banker.Request[pcb.getID()][i] = 0;
            }
        }
        // 释放此进程占用的资源
        for (int i = 0; i < RESOURCE_NUM; i++) {
            if (banker.Allocation[pcb.getID()][i] > 0) {
                int num = banker.Allocation[pcb.getID()][i];
                banker.Available[i] += num;
                banker.Allocation[pcb.getID()][i] = 0;
                Log.Info("死锁检测--释放资源", String.format("成功释放进程:%d，所占用的资源类型:%d，资源数目:%d", pcb.getID(), i, num));
                notifyPCB(i);
            }
        }
    }

    // 唤醒被资源类型为rid所阻塞的进程
    synchronized void notifyPCB(int rid) {
        ArrayList<PCB> blockQueue = ProcessManager.pm.queueManager.resourceBlockQueue.get(rid);
        for (int i = 0; i < blockQueue.size(); i++) {
            // 从阻塞队列中移除
            PCB p = blockQueue.get(0);
            if (ProcessManager.pm.queueManager.removeFromResourceBlockQueue(p, rid)) {
                // 加入就绪队列
                ProcessManager.pm.queueManager.joinReadQueue(p);
            }
        }
        blockQueue.clear();
    }
}
