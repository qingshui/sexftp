package sexftp;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class SexftpRun implements Runnable {
	private SrcViewable secview;
	private Object returnObject;
	private IProgressMonitor monitor;

	public SexftpRun(SrcViewable secview) {
		this.secview = secview;
	}

	public SexftpRun(SrcViewable secview, IProgressMonitor monitor) {
		this.secview = secview;
		this.monitor = monitor;
	}

	public void setSecview(SrcViewable secview) {
		this.secview = secview;
	}

	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public void run() {
		try {
			srun();
		} catch (Exception e) {
			this.secview.handleException(e);
		} finally {
			try {
				sfinally();
			} catch (Exception e) {
				this.secview.handleException(e);
			}
		}
	}

	public IProgressMonitor getMonitor() {
		return this.monitor;
	}

	public abstract void srun() throws Exception;

	public final void srun(IProgressMonitor monitor) throws Exception {
	}

	protected void sfinally() throws Exception {
	}

	public Object getReturnObject() {
		return this.returnObject;
	}

	public void setReturnObject(Object returnObject) {
		this.returnObject = returnObject;
	}
}
