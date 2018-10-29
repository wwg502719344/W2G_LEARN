package w2g_lock;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Lock;

/**
 * Created by W2G on 2018/3/29.
 * P3:释放节点
 * 调用doReleaseShared释放节点，该方法通过调用unparkSuccessor唤起首节点的下一个节点，unparkSuccessor方法通过循环找到
 * 距离首节点最近的，waitStatus<0的节点，被唤起的线程唤起后执行acquireQueued方法，如果传入节点的前驱节点不是首节点，则调用
 * shouldParkAfterFailedAcquire方法，清除所有当前节点前驱节点waitStatus>0的节点，直至成为首节点的下一个节点
 *
 * P0:节点属性
 *
 * ===========节点释放===========================
 * P1:独占式获取同步状态
 * P1-1:线程在等待队列中等待获取资源
 * P1-2:清除当前节点到head之间所有waitstatus>0的节点
 *
 * ===========节点释放===========================
 * P2:共享式同步状态的获取
 * P2-1:设置首节点，有剩余资源就继续唤醒后续资源
 *
 * ===========节点释放===========================
 * P3:释放节点
 * P3-1：如果当前节点存在就唤醒当前节点
 */
public class w2g_AQS {

    AbstractQueuedSynchronizer a=null;


    /**
     * P0:节点属性
     */
    /*
    static final Node SHARED = new Node();//表明当前节点是共享式节点
    static final Node EXCLUSIVE = null;//表明当前节点是独占式节点

    //取消
    static final int CANCELLED = 1;
    //等待触发
    static final int SIGNAL = -1;
    //等待条件
    static final int CONDITION = -2;
    //状态需要向后传播
    static final int PROPAGATE = -3;

    volatile int waitStatus;//节点状态,具体值为上面四个,该变量为volatile类型，表明是在子线程中可见的
    volatile Node prev;
    volatile Node next;
    volatile Thread thread;
    Node nextWaiter;
    */


    /**
     * P1:调用获取同步状态的方法
     * 调用tryAcquire尝试获取同步状态，如果没有获取到同步状态且acquireQueued方法返回true(该线程在被挂起时被中断过)
     * 则将当前线程进行中断
     * @param arg the acquire argument.  This value is conveyed to
     *        {@link #tryAcquire} but is otherwise uninterpreted and
     *        can represent anything you like.
     */
    /*
    public final void acquire(int arg) {
        if (!tryAcquire(arg)
        //P1-1:构造节点并加入到等待队列中去，如果当前线程在被挂起的时候被中断过，则该线程被唤醒的时候进行自我中断
        //acquireQueued方法返回true
        && acquireQueued(addWaiter(AbstractQueuedSynchronizer.Node.EXCLUSIVE), arg)){
                    //执行该行代码是因为在acquireQueued方法中调用parkAndCheckInterrupt()的时候，发现该线程被中断过...
                    //但是线程被挂起的时候是不会相应中断的，当线程被唤醒后，发现被中断过，此时将会对自己进行中断操作
                    selfInterrupt();
                }
    }
    */

    /**
     * P1-1:线程在等待队列中等待获取资源
     * 获取同步资源，该方法主要做了两件事
     * 1:前驱节点为首节点，且成功获取同步资源，则将node节点赋值给head
     * 2:如果前驱节点不是首节点或者没有成功获取同步资源，首先是将当前节点前面waitStatus>0的节点清除掉，并且挂起当前线程
     * 如果interrupted返回为true且当前线程还没有获取到同步资源，则将该线程进行中断操作
     */
    /*
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {  //一个自旋配合CAS设置变量
                final Node p = node.predecessor();  //获取当前节点的前驱节点
                //如果前驱节点是首节点且是当前获取同步状态的节点
                //只有前驱节点是首节点的，才会尝试去获取锁，这样才能保证只有队首元素获取锁
                if (p == head && tryAcquire(arg)) {
                    //设置当前节点为首节点，但是包装的线程是null，只是把当前节点设置为首节点
                    //这里的线程是空，为了后续的等待节点cas进去
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                //P1-2:如果自己可以休息了，就进入waiting状态，直到被unpark()
                //shouldParkAfterFailedAcquire(p, node)方法清除当前节点之前waitStatus>0的节点(已经标注被取消的节点)。。。
                //并返回false，返回false意味着当前节点无法被挂起，说明
                //parkAndCheckInterrupt方法的作用是挂起当前线程，此时从自旋中退出
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())//阻塞当前线程且没有比中断
                    interrupted = true; //如果等待过程中被中断过，哪怕只有那么一次，就将interrupted标记为true
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }*/

    /**
     * P1-2:返回true表示当前线程被成功挂起
     */
    /*
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        //如果前驱节点是signal表示前驱结点可以被唤醒
        if (ws == Node.SIGNAL)
             // This node has already set status asking a release
             // to signal it, so it can safely park.
             return true;
          //如果前节点是已经被取消的节点
          if (ws > 0) {
             // Predecessor(前任，前辈) was cancelled. Skip over predecessors and
             // indicate(表明,指示) retry.
             //获取pred节点的前驱节点pred2，并将node节点的前驱节点指向pred2,当pred的状态是已取消节点的时候
                do {
                    node.prev = pred = pred.prev;
                } while (pred.waitStatus > 0);
                pred.next = node;
            } else {
                 // waitStatus must be 0 or PROPAGATE.  Indicate that we
                 // need a signal, but don't park yet.  Caller will need to
                 // retry to make sure it cannot acquire before parking.
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL); //设置该节点的状态为SIGNAL
        }
        return false;
    }

    */







    /**
     * P2:共享式同步状态的获取
     * @param arg
     */
    /*
    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0)  //尝试获取资源，Q：首节点在哪里设置
            doAcquireShared(arg);   //进入等待队列，直到被unpark()/interrupt()并成功获取到资源才返回。整个等待过程也是忽略中断的
    }

    /**
     * 获取同步状态失败进入等待队列并尝试获取同步状态
     * @param arg
     */
    /*
    private void doAcquireShared(int arg) {
        //将当前节点加入到等待队列中去并且设为共享模式
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();  //获取当前节点的前驱节点
                if (p == head) {    //如果前驱结点是首节点
                    int r = tryAcquireShared(arg);  //尝试获取资源
                    if (r >= 0) {   //获取资源成功
                        //设置node节点为首节点
                        setHeadAndPropagate(node, r);//源码解析p2-1
                        p.next = null; // help GC
                        if (interrupted)    //如果等待过程有打断此时补上
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                //如果自己可以休息了，就进入waiting状态，直到被unpark()
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())    //继续挂起
                    interrupted = true;
            }
        } finally {
            if (failed) //如果循环获取失败则取消排队
                cancelAcquire(node);
        }
    }
    */

    /**
     * P2-1:设置首节点，有剩余资源就继续唤醒后续资源
     *
     */
    /*private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; // Record old head for check below
        setHead(node);//设置node为头节点

        if (propagate > 0 || h == null || h.waitStatus < 0 ||
                (h = head) == null || h.waitStatus < 0) {   //如果可以继续传播
            Node s = node.next; //获取node节点的下一个节点
            if (s == null || s.isShared())  //如果s是空或者是共享模式
                doReleaseShared();  //P3
        }
    }*/

    /**
     * P3:释放节点
     * 调用tryReleaseShared修改state状态
     * 调用doReleaseShared释放锁并唤醒后继节点
     */
    /*
    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }
    */

    /**
     * P3-0:唤起后继节点，设置节点为可传播状态
     * 共享式同步节点的释放不仅需要释放当前节点，还需要唤起后继节点，该方法就是将后继节点唤起且设置相应的状态，
     *
     */
    /*private void doReleaseShared() {
        for (;;) {
            Node h = head;
            if (h != null && h != tail) {   //当前节点不为空且不为尾节点(表示不止一个节点)
                int ws = h.waitStatus;  //获取首节点的状态
                if (ws == Node.SIGNAL) {    //如果头节点可以唤起后继结点
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0)) //将h节点的状态更改为0，然后唤醒h节点
                        continue;            // 循环复查
                    unparkSuccessor(h); //P3-1:唤醒当前节点的下一个可用节点
                }
                //如果当前节点已经被唤起了且将waitStatus设置为可传播
                else if (ws == 0 &&
                        !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            if (h == head)                   // loop if head changed
                break;
        }
    }*/

    /**
     * P3-1：如果当前节点存在就唤醒当前节点
     * @param node the node:该node应该为首节点
     */
    /*
    private void unparkSuccessor(Node node) {

        int ws = node.waitStatus;//获取当前节点状态
        if (ws < 0) //如果当前节点的状态是待唤醒，则更改为唤起状态
            compareAndSetWaitStatus(node, ws, 0);

        //下面的方法主要是为了唤起子节点的线程，主要做了以下操作
        //1-首先是获取首节点的下一个节点s
        //2-如果s节点为空或者已经被标记为取消，则从尾节点开始循环往前，一直获取到离首节点最近的可被唤起的节点，然后指向s
        //3-如果s节点不为null，则将s节点唤起
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {  //如果首节点的下一个节点为null或者已经被取消了
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev) //从节点链表尾部开始循环
                if (t.waitStatus <= 0)  //如果节点状态为带唤醒状态，则唤醒该节点
                  //此处的s是首节点后最后一个waitStatus<0的，结合shouldParkAfterFailedAcquire方法(清理所有waitStatus>0)
                  //由此保证s节点就是首节点的后继节点
                  s = t;
        }
        if (s != null)
            LockSupport.unpark(s.thread);
    }
    */



    /**
     * P10
     * 共享式获取同步状态
     */
    /*
    //尝试获取同步状态
    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }

    //获取共享状态失败，进入等待队列，获取资源才返回
    private void doAcquireShared(int arg) {
        //构造共享节点加入队列
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
    */





}
