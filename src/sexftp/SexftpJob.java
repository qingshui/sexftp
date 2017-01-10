package sexftp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.sexftp.core.ftp.FtpPools;
import sexftp.uils.LogUtil;

public abstract class SexftpJob extends Job implements Runnable {
	private SrcViewable secview;
	private IProgressMonitor monitor;
	private Thread curThread;
	private boolean fineshed = false;

	public SexftpJob(String name, SrcViewable secview) {
		super("Sexftp Job - " + name);
		this.secview = secview;
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			if (this.monitor == null)
				this.monitor = monitor;
			if (this.curThread == null) {
				this.curThread = Thread.currentThread();
				new Thread(this).start();
			}

			IStatus srun = srun(new SexftpProgressMonitor(monitor));

			return srun;
		} catch (Throwable e) {
			this.secview.handleException(e);
			return Status.CANCEL_STATUS;
		} finally {
			this.fineshed = true;
		}
	}

	public void run() {
		for (int i = 0; i < 50000; ++i)
			try {
				Thread.sleep(1000L);
				if (this.fineshed) {
					return;
				}

				if ((this.monitor != null) && (this.monitor.isCanceled())) {
					Thread.sleep(5000L);
					if (this.fineshed)
						return;
					new FtpPools(null, null).disconnectAll();
					LogUtil.info("Canceled Time Out,Disconnect All.");
				}
			} catch (InterruptedException e) {
				LogUtil.error(e.getMessage(), e);
			}
	}

	protected abstract IStatus srun(IProgressMonitor paramIProgressMonitor) throws Exception;

	public class SexftpProgressMonitor implements IProgressMonitor {
		IProgressMonitor monitor = null;

		private long nexoptime = 0L;

		public SexftpProgressMonitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		public void beginTask(String arg0, int arg1) {
			this.monitor.beginTask(arg0, arg1);
		}

		public void done() {
			this.monitor.done();
		}

		public void internalWorked(double arg0) {
			this.monitor.internalWorked(arg0);
		}

		public boolean isCanceled() {
			return this.monitor.isCanceled();
		}

		public void setCanceled(boolean arg0) {
			this.monitor.setCanceled(arg0);
		}

		public void setTaskName(String arg0) {
			this.monitor.setTaskName(arg0);
		}

		public void subTask(String arg0) {
			long currentTimeMillis = System.currentTimeMillis();
			if (currentTimeMillis <= this.nexoptime)
				return;
			this.monitor.subTask(arg0);
			this.nexoptime = (currentTimeMillis + 500L);
		}

		public void worked(int arg0) {
			this.monitor.worked(arg0);
		}
	}
}