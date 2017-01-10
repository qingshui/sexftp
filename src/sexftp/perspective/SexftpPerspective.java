package sexftp.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class SexftpPerspective implements IPerspectiveFactory {
	public void createInitialLayout(IPageLayout pageLayout) {
		pageLayout.addView("sexftp.views.MainView", 1, 0.2F, "org.eclipse.ui.editorss");
	}
}