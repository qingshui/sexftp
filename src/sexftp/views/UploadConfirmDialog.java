package sexftp.views;

import java.util.Iterator;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.sexftp.core.Tosimpleable;
import org.sexftp.core.ftp.bean.FtpConf;
import org.sexftp.core.ftp.bean.FtpUploadConf;

import java.util.List;
import sexftp.uils.LangUtil;

public class UploadConfirmDialog extends TitleAreaDialog implements IFtpStreamMonitor {
	private String title;
	private String message;
	private List<?> okFtpUploadConfList;
	//private FtpConf ftpConf;
	Composite contentPane = null;

	private MessageConsole console = null;
	private MessageConsoleStream consoleStream = null;

	public UploadConfirmDialog(Shell parentShell) {
		super(parentShell);
	}

	public UploadConfirmDialog(Shell parentShell, String title, String message, List<?> okFtpUploadConfList,
			FtpConf ftpConf) {
		super(parentShell);
		this.title = LangUtil.langText(title);
		this.message = LangUtil.langText(message);
		this.okFtpUploadConfList = okFtpUploadConfList;
		//this.ftpConf = ftpConf;
	}

	public void create() {
		super.create();
		setTitle(this.title);
		setMessage(this.message);
	}

	protected Control createDialogArea(Composite parent) {
		Control c = super.createDialogArea(parent);
		createContentPane(parent);
		setTitleImage(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/Twitter_bird.ico").createImage());
		createLoginControls();
		return c;
	}

	private void createContentPane(Composite parent) {
		this.contentPane = new Composite(parent, 0);
		GridLayout layout = new GridLayout(1, false);

		this.contentPane.setLayout(layout);

		this.contentPane.setLayoutData(new GridData(1808));

		this.contentPane.setFont(parent.getFont());
	}

	private void createLoginControls() {
		org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(this.contentPane, 2816);

		list.setSize(800, 300);
		for (Iterator<?> localIterator = this.okFtpUploadConfList.iterator(); localIterator.hasNext();) {
			Object item = localIterator.next();
			list.add(((Tosimpleable) item).toSimpleString(60));
		}

		GridData gridData = new GridData(768);

		gridData.heightHint = 300;
		gridData.widthHint = 800;
		list.setLayoutData(gridData);
		Text te = new Text(this.contentPane, 2048);
		gridData = new GridData(768);

		gridData.heightHint = 35;
		gridData.widthHint = 800;
		te.setLayoutData(gridData);
		te.setVisible(false);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
	}

	public void printSimple(String info) {
		console(info);
	}

	public void printStreamString(FtpUploadConf ftpUploadConf, long uploadedSize, long totalSize, String info) {
	}

	protected void initConsole() {
		if (this.console != null) {
			return;
		}
		this.console = new MessageConsole("SexFtpConsole", null);

		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { this.console });

		this.consoleStream = this.console.newMessageStream();
	}

	protected void openConsole() {
		initConsole();

		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(this.console);
	}

	protected void console(String text) {
		this.consoleStream.println(text);
	}
}
