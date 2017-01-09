package sexftp.popup.actions;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import sexftp.mainbar.actions.SexftpMainAction;
import sexftp.uils.LangUtil;

public class SexftpSubmenuAction extends SexftpMainAction {
	//private Shell shell;
	static Map<String, String> idMapLable = new HashMap<String, String>();

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		//this.shell = targetPart.getSite().getShell();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		String id = action.getId();

		String lable = (String) idMapLable.get(id);
		if (lable == null) {
			synchronized (idMapLable) {
				lable = action.getText();
				if ((lable != null) && (lable.length() > 0))
					idMapLable.put(id, lable);
			}
		}
		if ((lable != null) && (lable.length() > 0)) {
			String viewText = LangUtil.langText(lable);
			action.setText(viewText);
		}
		super.selectionChanged(action, selection);
	}
}
