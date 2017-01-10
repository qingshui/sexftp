package sexftp.editors;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import sexftp.editors.viewlis.IDoSaveListener;
import sexftp.uils.LogUtil;
import sexftp.views.SexftpViewAction;

public class XMLEditor extends TextEditor {
	private static Set<XMLEditor> editSet = new HashSet<XMLEditor>(50);
	private IDoSaveListener doSaveListener;
	private ColorManager colorManager;
	protected Action actionEditSexFtpConf;

	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		menu.add(this.actionEditSexFtpConf);
		menu.add(new Separator("additions"));
		super.editorContextMenuAboutToShow(menu);
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		parent.getChildren();

		this.actionEditSexFtpConf = new SexftpViewAction() {
			public void run() {
				if (XMLEditor.this.doSaveListener == null)
					return;
				XMLEditor.this.doSaveListener.dosave();
			}
		};
		this.actionEditSexFtpConf.setText("Save Sexftp Upload Unit Config And Load In Sexftp Viewer(&S)");
		this.actionEditSexFtpConf
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/boxmodel_props.gif"));
	}

	public XMLEditor() {
		this.colorManager = new ColorManager();
		setSourceViewerConfiguration(new XMLConfiguration(this.colorManager));
		setDocumentProvider(new XMLDocumentProvider());
	}

	public void dispose() {
		this.colorManager.dispose();
		if (this.doSaveListener != null) {
			this.doSaveListener.dispose();
		}
		super.dispose();
		editSet.remove(this);
	}

	protected void initializeEditor() {
		super.initializeEditor();
		new Thread() {
			public void run() {
				try {
					sleep(2000L);
					Display.getDefault().asyncExec(new Runnable() {
						@SuppressWarnings("deprecation")
						public void run() {
							if (XMLEditor.this.doSaveListener != null)
								return;
							XMLEditor.this.setTitle("Dead Config File");
							XMLEditor.this.setTitleToolTip(
									"It's Dead Config File,Please Close Me And Chose [Edit Sexftp Config] Option Again!");
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
									.closeEditor(XMLEditor.this, true);
						}
					});
				} catch (Exception localException) {
				}
			}
		}.start();
		editSet.add(this);
	}

	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		if (this.doSaveListener != null)
			this.doSaveListener.dosave();
		LogUtil.info("Save XmlEditor,Now There is " + editorCounts() + " Editors");
	}

	public IDoSaveListener getDoSaveListener() {
		return this.doSaveListener;
	}

	public void setDoSaveListener(IDoSaveListener doSaveListener) {
		this.doSaveListener = doSaveListener;
	}

	public int editorCounts() {
		return editSet.size();
	}
}
