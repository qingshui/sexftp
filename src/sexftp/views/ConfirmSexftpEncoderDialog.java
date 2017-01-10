package sexftp.views;

import java.util.Map;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.sexftp.core.utils.StringUtil;
import sexftp.uils.LangUtil;

public class ConfirmSexftpEncoderDialog extends TitleAreaDialog {
	private String FileAssociations;
	private String FileNoAssociations;
	private String path;
	Composite contentPane = null;
	Map<String, String> FTP_MAP = null;

	Text FileAssociationsText = null;
	Text FileNoAssociationsText = null;

	public ConfirmSexftpEncoderDialog(Shell parentShell, String path, String fileAssociations,
			String fileNoAssociations) {
		super(parentShell);
		this.FileAssociations = fileAssociations;
		this.FileNoAssociations = fileNoAssociations;
		this.path = path;
	}

	protected Control createDialogArea(Composite parent) {
		setTitleImage(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/Orange_forum.ico").createImage());

		Composite composite = (Composite) super.createDialogArea(parent);

		setTitle(LangUtil.langText(
				"Chose File Associations in [" + StringUtil.simpString(this.path, 30) + "] Which is Text File."));

		setMessage(LangUtil.langText(
				"Only Anyasis And Handle These File Associations.\r\nMatch Ways:Match File Name,? = any character,* = any string."));

		createContentPane(composite);

		createLoginControls();

		return composite;
	}

	private void createContentPane(Composite parent) {
		this.contentPane = new Composite(parent, 0);

		GridLayout layout = new GridLayout(3, false);

		layout.marginHeight = 20;

		layout.marginWidth = 70;

		layout.verticalSpacing = 10;

		layout.horizontalSpacing = 10;

		this.contentPane.setLayout(layout);

		this.contentPane.setLayoutData(new GridData(1808));

		this.contentPane.setFont(parent.getFont());
	}

	private void createLoginControls() {
		this.FileAssociationsText = createText("Text File Associations",
				"Only Anyasis And Handle These File Associations\r\nMatch File Name:? = any character,* = any string");
		this.FileAssociationsText.setText(this.FileAssociations);

		this.FileNoAssociationsText = createText("Other File Associations",
				"Other Unknown Type File Associations Show You For reference\r\n,If Need Anyasis And Handle These File Associations\r\nAdd Them To {Text File Associations}.");
		this.FileNoAssociationsText.setText(this.FileNoAssociations);
		this.FileNoAssociationsText.setEditable(false);
	}

	private Text createText(String label, String tips) {
		Label user = new Label(this.contentPane, 0);

		GridData layoutData = new GridData(128);

		user.setLayoutData(layoutData);

		user.setText(LangUtil.langText(label) + ":");

		user.setToolTipText(LangUtil.langText(tips));

		Text userText = new Text(this.contentPane, 18946);

		layoutData = new GridData(768);

		layoutData.heightHint = 80;
		layoutData.widthHint = 300;

		layoutData.horizontalSpan = 2;

		userText.setLayoutData(layoutData);

		userText.setToolTipText(LangUtil.langText(tips));
		return userText;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText(LangUtil.langText("Confirm File Associations"));
	}

	protected void okPressed() {
		this.FileAssociations = this.FileAssociationsText.getText();
		this.FileNoAssociations = this.FileNoAssociationsText.getText();
		super.okPressed();
	}

	public String getFileAssociations() {
		return this.FileAssociations;
	}

	public void setFileAssociations(String fileAssociations) {
		this.FileAssociations = fileAssociations;
	}

	public String getFileNoAssociations() {
		return this.FileNoAssociations;
	}

	public void setFileNoAssociations(String fileNoAssociations) {
		this.FileNoAssociations = fileNoAssociations;
	}
}
