import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;


/**
 *
 * This class has the ability to automatically restart a java process that enters a deadlock situation.
 * The deadlock is detected by an inner class called DeadLockDetector.
 * Any app that wants to inherit the DeadLockDetector mechanism should inherit it from the AppWrapper class.
 * The DeadLockDetector is a thread that polls the JMX of findDeadlockedThreads to examine if the
 * given app has reached a deadlock. When a deadlock occurrs, the detector exits brutally
 * by System.exit($CODE) with the proper error code. This error code is returned to the
 * AppWrapper main loop and in case it's the deadlock error code, the app will be executed again from scratch.
 * Note that you can use that JMX external to your program with Remote Management in case
 * you don't have the source or you want to do it on a live system.
 *
 */
public abstract class AppWrapper {
	public AppWrapper() {
		new DeadLockDetector().start();
	}

	public static class DeadLockDetector extends Thread {

		/**
		 * Entry point for the detector thread.
		 * Poll the JMX findDeadlockedThreads and exit in a deadlock situation.
		 */
		@Override
		public void run() {
			while(true) {
				ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
				long[] ids = threadMXBean.findDeadlockedThreads();
				if (ids != null) {
					ThreadInfo[] infos = threadMXBean.getThreadInfo(ids, true, true);
					for (ThreadInfo threadInfo : infos) {
						System.out.println(threadInfo);
					}

					System.exit(127);
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					//SUPRESS
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Process process = null;
		int exitcode = 0;
		try {
			while (true) {
				process = Runtime.getRuntime().exec("java -cp . DeadlockedApp");
				exitcode = process.waitFor();
				System.out.println("exit code: " + exitcode);
				if (exitcode == 0)
					break;
			}
		} catch (Exception e) {
			if (process != null) {
				process.destroy();
			}
		}
	}
}
