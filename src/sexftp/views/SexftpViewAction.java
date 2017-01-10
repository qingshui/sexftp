package sexftp.views;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import sexftp.uils.LangUtil;
import sexftp.uils.LogUtil;

public class SexftpViewAction extends Action {
	public void setText(String text) {
		super.setText(LangUtil.langText(text));
		super.setToolTipText(LangUtil.langText(text));
	}

	public void setToolTipText(String toolTipText) {
	}

	public void runWithEvent(Event event) {
		try {
			super.runWithEvent(event);
		} catch (Throwable e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}
