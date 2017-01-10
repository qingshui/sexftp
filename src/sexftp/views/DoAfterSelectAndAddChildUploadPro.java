package sexftp.views;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract interface DoAfterSelectAndAddChildUploadPro {
	public abstract void doafter(Object[] paramArrayOfObject, IProgressMonitor paramIProgressMonitor) throws Exception;
}