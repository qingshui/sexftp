package sexftp.views;

import com.lowagie.text.html.HtmlEncoder;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.desy.xbean.XbeanUtil;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.sexftp.core.bean.FileZilla;
import org.sexftp.core.bean.FileZillaServer;
import org.sexftp.core.ftp.FtpPools;
import org.sexftp.core.ftp.bean.FtpConf;
import org.sexftp.core.utils.FileUtil;
import sexftp.uils.LangUtil;
import sexftp.uils.LogUtil;

public class NewFtpConfDialog extends TitleAreaDialog {
	private String defaultConfigName = null;

	Composite contentPane = null;
	Map<String, String> FTP_MAP = null;

	Text configFileNameText = null;
	Text serverHostText = null;
	Text serverPortText = null;
	Combo serverTypeCombo = null;
	Text passwordText = null;
	Text userText = null;
	Text clilentPathText = null;
	Text serverPathText = null;

	FtpConf ftpconf = new FtpConf();

	public NewFtpConfDialog(Shell parentShell, String defaultConfigName) {
		super(parentShell);
		this.defaultConfigName = defaultConfigName;
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		setTitleImage(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/Twitter_bird.ico").createImage());

		setTitle(LangUtil.langText("Generate Sexftp Upload Unit Config File"));

		setMessage(LangUtil.langText(
				"Help You Generate Sexftp Upload Unit Config File Based Xml.After Save The XML File,\r\nThe Sexftp Upload Unit Config Will Show In [Sexftp Local View] and [Sexftp Server View]"));

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
		final FtpConf[] outerFtpconfs = getOuterFtpconfs();
		if (outerFtpconfs.length > 0) {
			String[] outftpconfitem = new String[outerFtpconfs.length];
			for (int i = 0; i < outerFtpconfs.length; ++i) {
				outftpconfitem[i] = outerFtpconfs[i].toString();
			}
			final Combo impComb = createSelect("Import From", "", outftpconfitem);
			impComb.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					int sel = impComb.getSelectionIndex();
					if (sel >= 0) {
						FtpConf ftpconfsel = outerFtpconfs[sel];
						NewFtpConfDialog.this.serverHostText.setText(ftpconfsel.getHost());
						if (ftpconfsel.getPort() != null)
							NewFtpConfDialog.this.serverPortText.setText(ftpconfsel.getPort().toString());
						NewFtpConfDialog.this.userText.setText(ftpconfsel.getUsername());
						NewFtpConfDialog.this.passwordText.setText(ftpconfsel.getPassword());
						String serverType = ftpconfsel.getServerType();
						if (serverType == null)
							return;
						NewFtpConfDialog.this.serverTypeCombo
								.setText(serverType + " - " + (String) NewFtpConfDialog.this.FTP_MAP.get(serverType));
					} else {
						impComb.select(0);
					}
				}

			});
		}

		this.configFileNameText = createText("Config File Name", "");
		this.configFileNameText.setText(this.defaultConfigName);
		this.serverHostText = createText("Server Host", "");
		this.serverHostText.setText("localhost");
		this.serverPortText = createText("Server Port", "");
		this.serverPortText.setText("21");

		this.FTP_MAP = new LinkedHashMap<String, String>();

		this.FTP_MAP.put("ftp", LangUtil.langText("FTP Transfer Protocal"));
		this.FTP_MAP.put("ftps", LangUtil.langText("Implicit TLS/SSL FTP"));
		this.FTP_MAP.put("ftpes", LangUtil.langText("Explicit TLS/SSL FTP"));
		this.FTP_MAP.put("sftp", LangUtil.langText("SSH File Transfer Protocal"));
		this.FTP_MAP.put("file", LangUtil.langText("Local File Transfer Protocal"));

		String[] arrays = (String[]) FtpPools.FTP_MAP.keySet().toArray(new String[0]);
		for (int i = 0; i < arrays.length; ++i) {
			arrays[i] = (arrays[i].toUpperCase() + " - " + (String) this.FTP_MAP.get(arrays[i]));
		}
		this.serverTypeCombo = createSelect("Server Type", "", arrays);
		this.serverTypeCombo.select(0);
		this.serverTypeCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (NewFtpConfDialog.this.serverTypeCombo.getSelectionIndex() >= 0)
					return;
				NewFtpConfDialog.this.serverTypeCombo.select(0);
			}
		});
		this.userText = createText("Login User", "Login User Name");
		this.userText.setText("root");
		this.passwordText = createText("Login Password", "Login Password");
		this.passwordText.setText("root123");
	}

	private Combo createSelect(String label, String tips, String[] items) {
		Label user = new Label(this.contentPane, 0);

		GridData layoutData = new GridData(128);

		user.setLayoutData(layoutData);

		user.setText(LangUtil.langText(label) + ":");

		user.setToolTipText(LangUtil.langText(tips));

		Combo combo = new Combo(this.contentPane, 18432);
		combo.setItems(items);

		layoutData = new GridData(768);

		layoutData.horizontalSpan = 2;

		combo.setLayoutData(layoutData);

		combo.setToolTipText(tips);
		return combo;
	}

	private Text createText(String label, String tips) {
		Label user = new Label(this.contentPane, 0);

		GridData layoutData = new GridData(128);

		user.setLayoutData(layoutData);

		user.setText(LangUtil.langText(label) + ":");

		user.setToolTipText(LangUtil.langText(tips));

		Text userText = new Text(this.contentPane, 18432);

		layoutData = new GridData(768);

		layoutData.horizontalSpan = 2;

		userText.setLayoutData(layoutData);

		userText.setToolTipText(tips);
		return userText;
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText(LangUtil.langText("New Sexftp Upload Unit"));
	}

	protected void okPressed() {
		this.ftpconf.setName(HtmlEncoder.encode(this.configFileNameText.getText().trim()));
		this.ftpconf.setHost(HtmlEncoder.encode(this.serverHostText.getText().trim()));
		try {
			this.ftpconf.setPort(Integer.valueOf(Integer.parseInt(this.serverPortText.getText().trim())));
		} catch (NumberFormatException localNumberFormatException) {
			this.ftpconf.setPort(Integer.valueOf(21));
		}
		int sel = this.serverTypeCombo.getSelectionIndex();
		if (sel >= 0) {
			int index = this.serverTypeCombo.getText().toLowerCase().indexOf(" - ");
			this.ftpconf.setServerType(this.serverTypeCombo.getText().toLowerCase().substring(0, index));
		}
		this.ftpconf.setPassword(HtmlEncoder.encode(this.passwordText.getText().trim()));
		this.ftpconf.setUsername(HtmlEncoder.encode(this.userText.getText().trim()));
		super.okPressed();
	}

	public FtpConf getFtpconf() {
		return this.ftpconf;
	}

	public void setFtpconf(FtpConf ftpconf) {
		this.ftpconf = ftpconf;
	}

	public static FtpConf[] getOuterFtpconfs() {
		List<FtpConf> ftpconfs = new ArrayList<FtpConf>();
		String userHome = System.getProperty("user.home");
		if ((userHome != null) && (userHome.trim().length() > 0)) {
			for (String folderPath : new String[] { userHome }) {
				for (String subPath : new String[] { "\\AppData\\Roaming\\FileZilla\\sitemanager.xml",
						"\\application data\\FileZilla\\sitemanager.xml" }) {
					File f = new File(folderPath + subPath);
					if (!f.exists())
						continue;
					try {
						String xml = FileUtil.getTextFromFile(f.getAbsolutePath(), "utf-8");
						xml = org.sexftp.core.utils.StringUtil
								.split(org.sexftp.core.utils.StringUtil.split(xml, "<Servers>")[1], "</Servers>")[0];
						xml = "<FileZilla>" + xml + "</FileZilla>";
						FileZilla fiz = (FileZilla) XbeanUtil.xml2Bean(FileZilla.class, xml);
						for (FileZillaServer fizserver : fiz.getServer()) {
							FtpConf ftpconf = new FtpConf();
							ftpconf.setHost(fizserver.getHost());
							try {
								ftpconf.setPort(new Integer(fizserver.getPort()));
							} catch (NumberFormatException localNumberFormatException) {
							}
							ftpconf.setUsername(fizserver.getUser());
							ftpconf.setPassword(fizserver.getPass());
							try {
								ftpconf.setServerType(
										((String[]) FtpPools.FTP_MAP.keySet().toArray(new String[0]))[new Integer(
												fizserver.getProtocol()).intValue()]);
							} catch (Exception localException1) {
							}
							ftpconf.setName(LangUtil.langText("From FileZilla"));
							ftpconfs.add(ftpconf);
						}
					} catch (Exception e) {
						LogUtil.error("Import Other Config Error:" + e.getMessage(), e);
					}
				}
			}
		}

		return (FtpConf[]) ftpconfs.toArray(new FtpConf[0]);
	}
}