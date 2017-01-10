package sexftp.editors.inner;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.editors.text.TextEditor;
import sexftp.editors.viewlis.IDoSaveListener;

public class SfTextEditor extends TextEditor {
	private IDoSaveListener doSaveListener;

	protected void initializeEditor() {
		super.initializeEditor();
	}

	public void dispose() {
		if (this.doSaveListener != null) {
			this.doSaveListener.dispose();
		}
		super.dispose();
	}

	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		if (this.doSaveListener == null)
			return;
		this.doSaveListener.dosave();
	}

	public IDoSaveListener getDoSaveListener() {
		return this.doSaveListener;
	}

	public void setDoSaveListener(IDoSaveListener doSaveListener) {
		this.doSaveListener = doSaveListener;
	}
}
