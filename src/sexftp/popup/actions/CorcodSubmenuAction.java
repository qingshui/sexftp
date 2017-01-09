package sexftp.popup.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
//import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;
import org.sexftp.core.ftp.bean.FtpUploadConf;
import org.sexftp.core.ftp.bean.FtpUploadPro;
import sexftp.mainbar.actions.SexftpMainAction;
import sexftp.uils.LogUtil;
import sexftp.uils.PluginUtil;
import sexftp.views.AbstractSexftpEncodView;
import sexftp.views.AbstractSexftpView;
import sexftp.views.SexftpEncodView;

public class CorcodSubmenuAction extends SexftpMainAction {
	//private Shell shell;
	private String truePath = null;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		//this.shell = targetPart.getSite().getShell();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.truePath = null;
		if (selection instanceof StructuredSelection) {
			StructuredSelection s = (StructuredSelection) selection;

			Object e = s.getFirstElement();
			if (e instanceof FileEditorInput) {
				FileEditorInput fi = (FileEditorInput) e;
				this.truePath = fi.getFile().getLocation().toFile().getAbsolutePath();
				action.setEnabled(true);
				return;
			}
		}
		super.selectionChanged(action, selection);

		if (this.path != null) {
			action.setEnabled(true);
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(this.path.toString() + "/a.txt"));
			this.truePath = file.getLocation().toFile().getParentFile().getAbsolutePath();
		} else {
			if (selection instanceof TreeSelection) {
				this.path = null;
				TreeSelection treeSelect = (TreeSelection) selection;
				Object elem = treeSelect.getFirstElement();
				if (elem instanceof AbstractSexftpView.TreeObject) {
					Object o = ((AbstractSexftpView.TreeObject) elem).getO();
					if (o instanceof FtpUploadConf) {
						this.truePath = ((FtpUploadConf) o).getClientPath();
					} else if (o instanceof FtpUploadPro) {
						this.truePath = ((FtpUploadPro) o).getFtpUploadConf().getClientPath();
					}
				} else if (elem instanceof AbstractSexftpEncodView.ParentCorCod) {
					AbstractSexftpEncodView.CorCod cc = (AbstractSexftpEncodView.CorCod) elem;
					this.truePath = cc.getParentFolder();
				} else if (elem instanceof AbstractSexftpEncodView.CorCod) {
					AbstractSexftpEncodView.CorCod cc = (AbstractSexftpEncodView.CorCod) elem;
					this.truePath = cc.getEndexten();
				}
			}
			if (this.truePath != null) {
				action.setEnabled(true);
			} else {
				action.setEnabled(false);
			}
		}
	}

	public void run(IAction action) {
		try {
			PluginUtil.getActivePage().showView("sexftp.views.SexftpEncodView");
			SexftpEncodView c = (SexftpEncodView) PluginUtil.getActivePage().findView("sexftp.views.SexftpEncodView");

			c.checkAndView(this.truePath);
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}
