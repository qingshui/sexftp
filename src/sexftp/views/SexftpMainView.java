package sexftp.views;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.desy.xbean.XbeanUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.sexftp.core.exceptions.AbortException;
import org.sexftp.core.exceptions.BizException;
import org.sexftp.core.ftp.FileMd5;
import org.sexftp.core.ftp.FtpPools;
import org.sexftp.core.ftp.FtpUtil;
import org.sexftp.core.ftp.bean.FtpConf;
import org.sexftp.core.ftp.bean.FtpDownloadPro;
import org.sexftp.core.ftp.bean.FtpFile;
import org.sexftp.core.ftp.bean.FtpUploadConf;
import org.sexftp.core.ftp.bean.FtpUploadPro;
import org.sexftp.core.utils.FileUtil;
import org.sexftp.core.utils.SearchCallback;
import org.sexftp.core.utils.StringUtil;
import org.sexftp.core.utils.TreeViewUtil;
import sexftp.SexftpJob;
import sexftp.SexftpRun;
import sexftp.editors.XMLEditor;
import sexftp.editors.viewlis.IDoSaveListener;
import sexftp.uils.LangUtil;
import sexftp.uils.PluginUtil;

public class SexftpMainView extends AbstractSexftpView {
	private Integer ignorNoPrjFiles = null;

	protected void actionLocation_actionPerformed() throws Exception {
		AbstractSexftpView.TreeParent[] selectFtpUploadConfNodes = getSelectFtpUploadConfNodes();
		AbstractSexftpView.TreeParent[] allFtpUploadConfNodes = getAllFtpUploadConfNodes();
		AbstractSexftpView.TreeObject[] selectNodes = getSelectNodes(false);
		Object[] selectionObjects = getSelectionObjects();
		String path = null;
		if (selectionObjects.length == 1) {
			path = getDefaultPathToLocation(selectionObjects[0]);
		}
		final String input = input("Location To",
				"Location To File Or Folder In Sexftp Viewer\r\nFull Path:Precise Location\r\nFile Name:Only Find In Expanded Tree Node",
				(path != null) ? path : "");

		AbstractSexftpView.TreeParent selectObj = null;
		for (AbstractSexftpView.TreeParent n : selectFtpUploadConfNodes) {
			FtpUploadConf fup = (FtpUploadConf) n.getO();
			if (!input.startsWith(fup.getServerPath()))
				continue;
			selectObj = n;
			break;
		}

		for (int i = 0; i < allFtpUploadConfNodes.length; ++i) {
			if ((allFtpUploadConfNodes[i] != selectObj) || (input.replace('\\', '/').indexOf("/") < 0))
				continue;
			directTo(input, Integer.valueOf(i));
			return;
		}

		if (input.replace('\\', '/').indexOf("/") >= 0) {
			directTo(input, null);
			return;
		}

		if (selectNodes.length != 1)
			return;
		AbstractSexftpView.TreeObject to = TreeViewUtil.serchTreeData(selectNodes[0], new SearchCallback() {
			public TreeViewUtil.ThisYourFind isThisYourFind(AbstractSexftpView.TreeObject o) {
				String path = SexftpMainView.this.getDefaultPathToLocation(o.getO());
				if ((path != null) && (input.equals(new File(path).getName()))) {
					return new TreeViewUtil.ThisYourFind(true, false);
				}
				return new TreeViewUtil.ThisYourFind(false, true);
			}
		});
		if (to == null)
			return;
		TreePath tpath = TreeViewUtil.changeTreePath(to);
		TreeSelection t = new TreeSelection(tpath);
		this.viewer.setSelection(t);
	}

	protected String getDefaultPathToLocation(Object selectO) {
		if (selectO instanceof FtpUploadConf) {
			return ((FtpUploadConf) selectO).getClientPath();
		}
		return null;
	}

	protected void actionApplySexFtpConf_actionPerformed() throws Exception {
		List<String> pathList = new ArrayList<String>();
		String projectName = okPopActionApplySexFtpConf(pathList);
		if (projectName == null) {
			projectName = "SexftpConfig";
		}

		NewFtpConfDialog n = new NewFtpConfDialog(this.viewer.getTree().getShell(),
				projectName + new Random().nextInt(100) + ".xml");
		int r = n.open();
		if (r != 0) {
			return;
		}
		FtpConf ftpConf = n.getFtpconf();
		String confName = ftpConf.getName();
		if (confName == null) {
			throw new BizException("Config File Name Error!");
		}
		if (new File(workspacePath + confName).exists()) {
			throw new BizException("Config File [" + confName + "] Exists!");
		}

		showMessage(
				"We'll Generate Sexftp Upload Unit XML Config File .\r\nYou May Need Fulfill The XML Config File\r\nAfter Save The XML File,\r\nThe Sexftp Upload Unit Config Will Show In \r\n[Sexftp Local View] and [Sexftp Server View]\r\nAnd Then You Can Use It.\r\nIf You Don't Save,The Sexftp Upload Unit Config File Will Be Deleted.");

		String projectPath = new File(workspacePath + "/client-temp/").getAbsolutePath();
		if (!projectName.equals("SexftpConfig")) {
			IProject iproject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

			projectPath = iproject.getFile("/a.txt").getLocation().toFile().getParent();
		}

		String confPath = "/.settings/.sexuftp10/" + confName + ".sfUTF-8";
		IProject wkproject = PluginUtil.getOneOpenedProject();
		IFile file = wkproject.getFile(confPath);
		final File confFile = file.getLocation().toFile();

		if (!confFile.getParentFile().exists()) {
			confFile.getParentFile().mkdirs();
		}

		if (pathList.size() == 0) {
			pathList = Arrays.asList(new String[] { new File(projectPath + "/webroot/").getAbsolutePath() });
		}

		ftpConf.setName(confName);
		List<FtpUploadConf> ftpUploadConfList = new ArrayList<FtpUploadConf>();
		for (String path : pathList) {
			FtpUploadConf ftpUploadConf = new FtpUploadConf();
			ftpUploadConf.setClientPath(path);
			ftpUploadConf.setServerPath(path.replace(projectPath, "").replaceAll("\\\\", "/") + "/");
			ftpUploadConf.setFileMd5("");
			ftpUploadConf.setExcludes(new String[] { "*.svn*", "*/WEB-INF/classes/*.properties" });
			ftpUploadConf.setIncludes(new String[] { "*?.*" });
			ftpUploadConfList.add(ftpUploadConf);
		}
		ftpConf.setFtpUploadConfList(ftpUploadConfList);
		String bean2xml = XbeanUtil.bean2xml(ftpConf, null);
		bean2xml = StringUtil.replaceAll(bean2xml, "<fileMd5></fileMd5>", "");

		bean2xml = StringUtil.replaceAll(bean2xml, "</serverType>",
				"</serverType><!-- "
						+ LangUtil.langText(
								new StringBuilder("Support ").append(FtpPools.FTP_MAP.keySet().toString()).toString())
						+ " -->");
		bean2xml = StringUtil.replaceAll(bean2xml, "</clientPath>",
				"</clientPath><!-- " + LangUtil.langText("Client Root Folder Or File Path") + " -->");
		bean2xml = StringUtil.replaceAll(bean2xml, "</serverPath>",
				"</serverPath><!-- " + LangUtil.langText("Server Root Folder Path") + " -->");
		bean2xml = StringUtil.replaceAll(bean2xml, "</excludes>",
				"</excludes><!-- " + LangUtil.langText("Exclude Path,? = any character,* = any string") + " -->");
		bean2xml = StringUtil.replaceAll(bean2xml, "</includes>",
				"</includes><!-- " + LangUtil.langText("Include Path,? = any character,* = any string") + " -->");
		bean2xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<!-- you may need use <![CDATA[your string]]> for special char  -->\r\n"
				+ bean2xml;
		FileUtil.writeByte2File(confFile.getAbsolutePath(), StringUtil.getBytes(bean2xml, "utf-8"));

		IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
		String id2 = editorRegistry.findEditor("sexftp.editors.XMLEditor").getId();
		file.refreshLocal(1, null);
		IDE.getContentType(file).setDefaultCharset("UTF-8");
		IEditorPart openEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.openEditor(new FileEditorInput(file), id2);
		final XMLEditor xmlEditor = (XMLEditor) openEditor;
		xmlEditor.setDoSaveListener(new IDoSaveListener() {
			private String oldFile = null;

			public void dosave() {
				try {
					String xmlconf = FileUtil.getTextFromFile(confFile.getAbsolutePath(), "utf-8");
					FtpConf conf = (FtpConf) XbeanUtil.xml2Bean(FtpConf.class, xmlconf);
					SexftpMainView.this.checkSexFtpConfigFils(conf);
					
					if ( this.oldFile != null ) {
						if ((!this.oldFile.equals(conf.getName()))
								&& (new File(SexftpMainView.workspacePath + conf.getName()).exists())) {
							throw new BizException("[" + conf.getName() + "] Exists!");
						}
					}
					
					FileUtil.copyFile(confFile.getAbsolutePath(), SexftpMainView.workspacePath + conf.getName());
					if (this.oldFile == null) {
						this.oldFile = conf.getName();
					}

					String [] idlst = new String[] {"sexftp.views.MainView", "sexftp.views.ServerView",
							"sexftp.views.SexftpSyncView"};
					for (String revid :  idlst) {
						SexftpMainView mv = (SexftpMainView) PluginUtil.getActivePage().findView(revid);
						if (mv != null) {
							mv.refreshTreeViewData();
							mv.getViewer().refresh();
							mv.getViewer().expandToLevel(2);
						}

					}

					Set<String> newPathList = new HashSet<String>();
					for (FtpUploadConf ftpUpConf : conf.getFtpUploadConfList()) {
						String clientPath = new File(ftpUpConf.getClientPath()).getAbsolutePath();
						newPathList.add(clientPath);
					}
					SexftpMainView.this.inner_formatnew(conf.getName(), false, newPathList, this.oldFile.length() > 0,
							false);
				} catch (Exception e) {
					SexftpMainView.this.handleException(e);
				}
			}

			public void dispose() {
				confFile.delete();
				if (xmlEditor.editorCounts() != 1)
					return;
				confFile.getParentFile().delete();
			}
		});
		xmlEditor.doSave(null);
	}

	protected void actionExpandAll_actionPerformed() throws Exception {
		ISelection selection = this.viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		this.viewer.expandToLevel(obj, 2005);
		super.actionExpandAll_actionPerformed();
	}

	protected void actionCollapseAll_actionPerformed() throws Exception {
		ISelection selection = this.viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		this.viewer.collapseToLevel(obj, 1);
		super.actionCollapseAll_actionPerformed();
	}

	private void checkSexFtpConfigFils(FtpConf conf) {
		if (conf.getFtpUploadConfList() != null) {
			for (FtpUploadConf ftpUploadConf : conf.getFtpUploadConfList()) {
				if (ftpUploadConf.getClientPath() == null) {
					throw new BizException("Client Path Is Empty!");
				}
				File file = new File(ftpUploadConf.getClientPath());
				if (!file.exists()) {
					file.mkdirs();
				}
				if (ftpUploadConf.getServerPath() == null) {
					throw new BizException("Server Path Is Empty!");
				}
				if (ftpUploadConf.getServerPath().endsWith("/"))
					continue;
				throw new BizException("[" + ftpUploadConf.getServerPath() + "] Must End With '/'");
			}

		}

		if ((conf.getName() != null) && (conf.getName().endsWith(".xml")))
			return;
		throw new BizException("Name [" + conf.getName() + "] Invalid! (ex:Sample.xml)");
	}

	protected void actionEditSexFtpConf_actionPerformed() {
		ISelection selection = this.viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		AbstractSexftpView.TreeParent p = (AbstractSexftpView.TreeParent) obj;
		final String path = workspacePath + p.getName().split(" \\- ")[0];

		IProject iproject = PluginUtil.getOneOpenedProject();
		if (iproject == null)
			throw new BizException("We must have one Projects to work!");
		String projectPath = PluginUtil.getProjectRealPath(iproject);

		String confPathInProjectAb = "/.settings/.sexuftp10/.editconf/" + new File(path).getName() + ".sfUTF-8";
		final String confPathInProject = projectPath + confPathInProjectAb;

		File confPathFile = new File(confPathInProject);
		if (!confPathFile.getParentFile().exists())
			confPathFile.getParentFile().mkdirs();
		FileUtil.copyFile(path, confPathInProject);

		IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
		String id2 = editorRegistry.findEditor("sexftp.editors.XMLEditor").getId();
		try {
			IFile file = iproject.getFile(confPathInProjectAb);
			file.refreshLocal(1, null);
			IDE.getContentType(file).setDefaultCharset("UTF-8");
			IEditorPart openEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new FileEditorInput(file), id2);
			final XMLEditor xmlEditor = (XMLEditor) openEditor;

			xmlEditor.setDoSaveListener(new IDoSaveListener() {
				public void dosave() {
					try {
						String xmlconf = FileUtil.getTextFromFile(confPathInProject, "utf-8");
						FtpConf conf = (FtpConf) XbeanUtil.xml2Bean(FtpConf.class, xmlconf);

						SexftpMainView.this.checkSexFtpConfigFils(conf);

						File oldFile = new File(path);
						if (oldFile.getName().equals(conf.getName())) {
							FileUtil.copyFile(confPathInProject, path);
						} else {
							if (new File(oldFile.getParent() + "/" + conf.getName()).exists()) {
								throw new BizException("[" + conf.getName() + "] Exists!");
							}
							FileUtil.copyFile(confPathInProject, oldFile.getParent() + "/" + conf.getName());
							oldFile.delete();
						}

						for (String revid : new String[] { "sexftp.views.MainView", "sexftp.views.ServerView",
								"sexftp.views.SexftpSyncView" }) {
							SexftpMainView mv = (SexftpMainView) PluginUtil.getActivePage().findView(revid);
							if (mv != null) {
								mv.refreshTreeViewData();
								mv.getViewer().refresh();
								mv.getViewer().expandToLevel(2);
							}

						}

						Set<String> newPathList = new HashSet<String>();

						for (FtpUploadConf ftpUpConf : conf.getFtpUploadConfList()) {
							String clientPath = new File(ftpUpConf.getClientPath()).getAbsolutePath();
							newPathList.add(clientPath);
						}

						SexftpMainView.this.inner_formatnew(conf.getName(), false, newPathList, true, false);
					} catch (Exception e) {
						SexftpMainView.this.handleException(e);
					}
				}

				public void dispose() {
					new File(confPathInProject).delete();
					if (xmlEditor.editorCounts() != 1)
						return;
					new File(confPathInProject).getParentFile().delete();
					new File(confPathInProject).getParentFile().getParentFile().delete();
				}

			});
		} catch (Exception e) {
			handleException(e);
		}
	}

	protected void actionDeleteSexFtpConf_actionPerformed() throws Exception {
		ISelection selection = this.viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		AbstractSexftpView.TreeParent p = (AbstractSexftpView.TreeParent) obj;

		if (!showQuestion("Sure To Delete The Sexftp Config File [" + p.getName().split(" \\- ")[0] + "] ?")) {
			return;
		}

		String path = workspacePath + p.getName().split(" \\- ")[0];
		new File(path).delete();
		actionRefreshSexftp_actionPerformed();
	}

	protected void actionExplorer_actionPerformed() throws Exception {
		ISelection selection = this.viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		AbstractSexftpView.TreeObject p = (AbstractSexftpView.TreeObject) obj;
		String path = "";
		if (p.getO() instanceof IFile) {
			path = ((IFile) p.getO()).getLocation().toFile().getAbsolutePath();
		} else if (p.getO() instanceof IProject) {
			path = ((IProject) p.getO()).getFile("/a.txt").getLocation().toFile().getParent();
		} else if (p.getO() instanceof FtpUploadConf) {
			path = ((FtpUploadConf) p.getO()).getClientPath();
		} else if (p.getO() instanceof FtpUploadPro) {
			path = ((FtpUploadPro) p.getO()).getFtpUploadConf().getClientPath();
		} else if (p.getO() instanceof FtpDownloadPro) {
			path = ((FtpDownloadPro) p.getO()).getFtpUploadConf().getClientPath();
		}
		if (path.length() <= 0)
			return;
		File pathfile = new File(path);
		if (!pathfile.exists())
			return;
		if (pathfile.isFile()) {
			path = pathfile.getParent();
		} else {
			path = pathfile.getAbsolutePath();
		}

		Runtime.getRuntime().exec("explorer " + path);
	}

	protected void actionCopyQualifiedName_actionPerformed() throws Exception {
		Object[] objes = getSelectionObjects();
		StringBuffer sb = new StringBuffer();
		for (Object o : objes) {
			sb.append(o);
			sb.append("\r\n");
		}
		Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Object tText = new StringSelection(sb.toString().trim());
		systemClipboard.setContents((Transferable) tText, null);
	}

	protected void actionCopy_actionPerformed() throws Exception {
		ISelection selection = this.viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		AbstractSexftpView.TreeObject p = (AbstractSexftpView.TreeObject) obj;
		Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable tText = new StringSelection(p.getName());
		systemClipboard.setContents(tText, null);
	}

	protected void actionFormat_actionPerformed() throws Exception {
		if (!showQuestion("After Format,New Modify Checking Will Based On this Result!Sure?"))
			return;
		Object[] selectOs = getSelectionObjects();
		FtpConf[] selectFtpconfs = getFtpConfsSelected();
		if (selectFtpconfs.length > 1) {
			throw new AbortException();
		}

		String confName = selectFtpconfs[0].getName();
		Set<String> newPathList = new HashSet<String>();
		boolean allformat = false;
		for (Object select : selectOs) {
			if (select instanceof FtpConf) {
				for (FtpUploadConf ftpUpConf : ((FtpConf) select).getFtpUploadConfList()) {
					String clientPath = new File(ftpUpConf.getClientPath()).getAbsolutePath();
					newPathList.add(clientPath);
				}
				allformat = true;
				break;
			}
			if (select instanceof FtpUploadConf) {
				newPathList.add(((FtpUploadConf) select).getClientPath());
			} else if (select instanceof FtpUploadPro) {
				newPathList.add(((FtpUploadPro) select).getFtpUploadConf().getClientPath());
			} else {
				if (!(select instanceof FtpDownloadPro))
					continue;
				newPathList.add(downPro2UpPro((FtpDownloadPro) select).getFtpUploadConf().getClientPath());
			}
		}

		newPathList = FileUtil.unionUpFilePath(newPathList);
		if (newPathList.size() == 0) {
			throw new BizException("No Files Format!");
		}
		inner_formatnew(confName, true, newPathList, false, allformat);
	}

	/*private void actionFormat_innerPerofrmed(String conffilename) throws Exception {
		actionFormat_innerPerofrmed(conffilename, false, null);
	}*/

	public void actionFormat_innerPerofrmed(final String conffilename, final boolean needAnswer,
			final List<FtpUploadConf> ftpUploadConfList) throws Exception {
		final String path = workspacePath + conffilename;

		Job job = new SexftpJob("Format", this) {
			protected IStatus srun(IProgressMonitor monitor) throws Exception {
				monitor.beginTask("Formating...", -1);
				if (ftpUploadConfList != null) {
					FtpUtil.formaterSel(SexftpMainView.workspaceWkPath, path, ftpUploadConfList);
				} else {
					FtpUtil.formater(SexftpMainView.workspaceWkPath, path);
				}
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (needAnswer)
							SexftpMainView.this.showMessage("Format Success!");
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	public void inner_formatnew(final String conffilename, final boolean needAnswer, final Set<String> newPathLIst, final boolean skipOld,
			final boolean allFormat) throws Exception {
		final File lastModifyFile = new File(workspaceWkPath + conffilename + "/lastModMap.d");

		Job job = new SexftpJob("Format Local File Upload Point", this) {
			private int formatCounts;

			@SuppressWarnings("unchecked")
			protected IStatus srun(IProgressMonitor monitor) throws Exception {
				Set<String> oldPathSet = new HashSet<String>();
				Map<String, String> lastModMap = null;
				String oldPath;
				int oretur;
				if (!lastModifyFile.exists()) {
					lastModifyFile.getParentFile().mkdirs();
					lastModMap = new HashMap<String, String>();
				} else {
					lastModMap = FtpUtil.readLastModMap(lastModifyFile.getParent());
					if ((skipOld) && (!allFormat)) {
						oldPathSet = FileUtil.unionUpFilePath(lastModMap.keySet());
						Set<String> needCheckFormatPath = new HashSet<String>();
						StringBuffer needCheckFormatPathStr = new StringBuffer();
						File newfile;
						for (String newpath : newPathLIst) {
							newfile = new File(newpath);
							if (newfile.exists()) {
								newpath = newfile.getAbsolutePath();
								for (Iterator<String> localIterator2 = oldPathSet.iterator(); localIterator2.hasNext();) {
									oldPath = (String) localIterator2.next();

									oldPath = new File(oldPath).getAbsolutePath();
									if (newpath.startsWith(oldPath)) {
										continue;
									}
									IProject[] allProjects = PluginUtil.getAllProjects();
									boolean isInPrj = false;
									for (IProject iproject : allProjects) {
										String prjRealPath = PluginUtil.getProjectRealPath(iproject);

										if (!newpath.startsWith(prjRealPath))
											continue;
										isInPrj = true;
										break;
									}

									if (isInPrj)
										continue;
									if (!needCheckFormatPath.contains(newpath)) {
										needCheckFormatPathStr.append(newpath);
										needCheckFormatPathStr.append("\r\n");
									}
									needCheckFormatPath.add(newpath);
								}

							}
						}

						if (needCheckFormatPath.size() > 0) {
							oretur = 0;
							if (SexftpMainView.this.ignorNoPrjFiles == null) {
								MessageDialogWithToggle showQuestion = SexftpMainView.this.showQuestionInDTread(
										"We'll Format Local File Upload Point Of New Folders Or Files For Local New Modified Files Check Later On,\r\nBut It's Not The Folders Or Files Of Any Projects As Follows:\r\n["
												+ needCheckFormatPathStr.toString().trim() + "]\r\n"
												+ "Sure Format Upload Point Of Folders Or Files Above?",
										"Remember Me In Current Session.");
								if (showQuestion.getToggleState()) {
									SexftpMainView.this.ignorNoPrjFiles = Integer.valueOf(showQuestion.getReturnCode());
								}
								oretur = showQuestion.getReturnCode();
							} else {
								oretur = SexftpMainView.this.ignorNoPrjFiles.intValue();
							}
							if (oretur == 1) {
								return Status.CANCEL_STATUS;
							}
							if (oretur != 2) {
								for (String noFormat : (List<String>) needCheckFormatPath) {
									newPathLIst.remove(noFormat);
								}

							}

						}

					}

				}

				if (allFormat) {
					lastModMap = new HashMap<String, String>();
				}
				List<File> list = new ArrayList<File>();
				for (String newpath : newPathLIst) {
					list.addAll(FileUtil.searchFile(new File(newpath), monitor));
				}

				monitor.beginTask("Format Local File Upload Point...", list.size());

				for (File file : (List<File>) list) {
					if (monitor.isCanceled())
						throw new AbortException("User Canceled.");
					int i = 1;
					for (String oldPath2 : oldPathSet) {
						if (!file.getAbsolutePath().startsWith(oldPath2))
							continue;
						i = 0;
						break;
					}

					if (i != 0) {
						monitor.subTask("Formating " + file.getAbsolutePath());
						SexftpMainView.this.console("Formated Local File Upload Point Of " + file.getAbsolutePath());

						lastModMap.put(file.getAbsolutePath(), FileMd5.getMD5(file, monitor));
						monitor.subTask("waiting..");
						this.formatCounts += 1;
					}
					monitor.worked(1);
				}

				FtpUtil.writeLastModMap(lastModifyFile.getParent(), lastModMap);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (needAnswer)
							SexftpMainView.this.showMessage(
									"Format Local File Upload Point Success,[" + formatCounts + "] Files Formated!");
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	protected void actionPrepareUpload_actionPerformed() throws Exception {
		final Set<FtpConf> ftpconfSet = new HashSet<FtpConf>();
		HashMap<FtpUploadConf, FtpConf> uploadMapConf = new HashMap<FtpUploadConf, FtpConf>();
		ISelection selection = this.viewer.getSelection();
		Iterator<?> it = ((IStructuredSelection) selection).iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (!(o instanceof AbstractSexftpView.TreeParent))
				continue;
			AbstractSexftpView.TreeParent p = (AbstractSexftpView.TreeParent) o;
			if (p.getO() instanceof FtpConf) {
				ftpconfSet.add((FtpConf) p.getO());
			} else {
				if (!(p.getO() instanceof FtpUploadConf))
					continue;
				AbstractSexftpView.TreeParent et = p.getParent();
				ftpconfSet.add((FtpConf) et.getO());
				uploadMapConf.put((FtpUploadConf) p.getO(), (FtpConf) et.getO());
			}
		}
		FtpConf[] arrayOfFtpConf = (FtpConf[]) ftpconfSet.toArray(new FtpConf[0]);
		for (FtpConf ftpconf : arrayOfFtpConf) {
			String lastModify = workspaceWkPath + ftpconf.getName() + "/lastModMap.d";
			if (new File(lastModify).exists())
				continue;
			showMessage("No Formated Files,Need Format First,And You Can Get Modified Files Next Time!");
			actionFormat_innerPerofrmed(ftpconf.getName(), true, null);

			return;
		}

		final Iterator<?> itagain = ((IStructuredSelection) selection).iterator();
		Job job = new SexftpJob("PrepareModified", this) {
			protected IStatus srun(IProgressMonitor monitor) throws Exception {
				monitor.beginTask("Prepare Modified File For Upload,doing ...", -1);

				final List<TreeObject> needExpandTree = new ArrayList<TreeObject>();
				while (itagain.hasNext()) {
					Object o = itagain.next();
					if (o instanceof AbstractSexftpView.TreeParent) {
						AbstractSexftpView.TreeParent p = (AbstractSexftpView.TreeParent) o;
						if (p.getO() instanceof FtpConf) {
							String path = SexftpMainView.workspacePath + p.getName().split(" \\- ")[0];
							AbstractSexftpView.TreeObject[] children = p.getChildren();
							for (AbstractSexftpView.TreeObject ftpUploadTree : children) {
								((AbstractSexftpView.TreeParent) ftpUploadTree).removeAll();
								List<FtpUploadConf> canFtpUploadConfList = FtpUtil.anyaCanUploadFiles(
										SexftpMainView.workspaceWkPath, path, (FtpConf) p.getO(),
										(FtpUploadConf) ftpUploadTree.getO());
								for (FtpUploadConf ftpUploadConf : canFtpUploadConfList) {
									if (!StringUtil.fileStyleEIMatch(ftpUploadConf.getClientPath(),
											((FtpUploadConf) ftpUploadTree.getO()).getExcludes(),
											((FtpUploadConf) ftpUploadTree.getO()).getIncludes()))
										continue;
									AbstractSexftpView.TreeObject treeObject = new AbstractSexftpView.TreeObject(
											ftpUploadConf.toSimpleString(60),
											new FtpUploadPro(ftpUploadConf, (FtpConf) p.getO()));
									((AbstractSexftpView.TreeParent) ftpUploadTree).addChild(treeObject);
								}

								needExpandTree.add(ftpUploadTree);
							}
						} else if (p.getO() instanceof FtpUploadConf) {
							p.removeAll();
							AbstractSexftpView.TreeParent et = p.getParent();
							if (!ftpconfSet.contains(p.getO())) {
								String path = SexftpMainView.workspacePath + et.getName().split(" \\- ")[0];
								List<FtpUploadConf> canFtpUploadConfList = FtpUtil.anyaCanUploadFiles(
										SexftpMainView.workspaceWkPath, path, (FtpConf) et.getO(),
										(FtpUploadConf) p.getO());
								for (FtpUploadConf ftpUploadConf : canFtpUploadConfList) {
									if (!StringUtil.fileStyleEIMatch(ftpUploadConf.getClientPath(),
											((FtpUploadConf) p.getO()).getExcludes(),
											((FtpUploadConf) p.getO()).getIncludes()))
										continue;
									AbstractSexftpView.TreeObject treeObject = new AbstractSexftpView.TreeObject(
											ftpUploadConf.toSimpleString(60),
											new FtpUploadPro(ftpUploadConf, (FtpConf) et.getO()));
									p.addChild(treeObject);
								}
							}

							needExpandTree.add(p);
						}

					}

					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							SexftpMainView.this.viewer.setContentProvider(new AbstractSexftpView.ViewContentProvider());
							for (AbstractSexftpView.TreeObject need : needExpandTree) {
								SexftpMainView.this.viewer.expandToLevel(need, 1);
							}
						}

					});
				}

				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	protected void actionRefreshFile_actionPerformed() throws Exception {
		AbstractSexftpView.TreeObject[] selectNodes = getSelectNodes(false);
		if ((selectNodes.length != 1) || (!(selectNodes[0] instanceof AbstractSexftpView.TreeParent)))
			return;
		AbstractSexftpView.TreeParent parent = (AbstractSexftpView.TreeParent) selectNodes[0];
		parent.removeAll();
		treeExpanded_actionPerformed(new TreeExpansionEvent(this.viewer, parent));
	}

	protected void actionRefreshSexftp_actionPerformed() {
		refreshTreeViewData();
		this.viewer.setContentProvider(new AbstractSexftpView.ViewContentProvider());
		this.viewer.expandToLevel(2);
	}

	protected void refreshPendingTree(final AbstractSexftpView.TreeParent parent, final SexftpRun run) {
		final AbstractSexftpView.TreeObject pendchild = new AbstractSexftpView.TreeObject("Pending", "");
		parent.addChild(pendchild);
		refreshTreeView(parent);

		Job job = new SexftpJob("Pending Process", this) {
			protected IStatus srun(IProgressMonitor monitor) throws Exception {
				try {
					monitor.beginTask("Pending Process", -1);
					run.setMonitor(monitor);
					run.run();
				} catch (Throwable e) {
					SexftpMainView.this.handleException(e);
				}
				parent.removeChild(pendchild);

				if (parent.getChildren().length > 0) {
					AbstractSexftpView.TreeObject selectTo = null;
					Object returnObject = run.getReturnObject();
					if ((returnObject != null) && (returnObject instanceof AbstractSexftpView.TreeObject)) {
						selectTo = (AbstractSexftpView.TreeObject) returnObject;
					}

					SexftpMainView.this.refreshTreeView(parent, selectTo);
				} else {
					SexftpMainView.this.backRefreshTreeView(parent);
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(false);
		job.schedule();
	}

	protected void backRefreshTreeView(final AbstractSexftpView.TreeParent collapseElem) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				SexftpMainView.this.viewer.setContentProvider(new AbstractSexftpView.ViewContentProvider());
				if (collapseElem == null)
					return;
				SexftpMainView.this.viewer.collapseToLevel(collapseElem, 1);
			}
		});
	}

	protected void refreshTreeView(final AbstractSexftpView.TreeParent explandElem) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				SexftpMainView.this.viewer.refresh(explandElem);
				if (explandElem == null)
					return;
				SexftpMainView.this.viewer.expandToLevel(explandElem, 1);
			}
		});
	}

	protected void refreshTreeViewx(final AbstractSexftpView.TreeParent refreshParent) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				SexftpMainView.this.viewer.refresh(refreshParent);
			}
		});
	}

	protected void refreshTreeView(final AbstractSexftpView.TreeParent refreshElem,
			final AbstractSexftpView.TreeObject expandElem) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				SexftpMainView.this.viewer.refresh(refreshElem);
				if (expandElem != null) {
					TreePath tpath = TreeViewUtil.changeTreePath(expandElem);
					TreeSelection t = new TreeSelection(tpath);
					SexftpMainView.this.viewer.setSelection(t);
				} else {
					if (refreshElem == null)
						return;
					SexftpMainView.this.viewer.expandToLevel(refreshElem, 1);
				}
			}
		});
	}

	public void refreshTreeViewData() {
		List<FtpConf> allConf = FtpUtil.getAllConf(workspacePath);
		AbstractSexftpView.TreeParent root = new AbstractSexftpView.TreeParent("Sexftp Start", null);
		AbstractSexftpView.TreeParent to1;
		Object localObject;
		FtpUploadConf ftpUpConf;
		for (FtpConf ftpConf : allConf) {
			String conf = String.format("%s - %s:%s@%s",
					new Object[] { ftpConf.getName(), ftpConf.getHost(), ftpConf.getPort(), ftpConf.getUsername() });
			to1 = new AbstractSexftpView.TreeParent(conf, ftpConf);
			if (ftpConf.getFtpUploadConfList() != null)
				for (localObject = ftpConf.getFtpUploadConfList().iterator(); ((Iterator<?>) localObject).hasNext();) {
					ftpUpConf = (FtpUploadConf) ((Iterator<?>) localObject).next();

					if (this instanceof SexftpServerView) {
						to1.addChild(new AbstractSexftpView.TreeParent(ftpUpConf.getServerPath(), ftpUpConf));
					} else {
						to1.addChild(new AbstractSexftpView.TreeParent(ftpUpConf.toSimpleString(), ftpUpConf));
					}
				}

			root.addChild(to1);
		}

		if (isShowProjectView()) {
			IProject[] projects = PluginUtil.getAllOpenedProjects();
			AbstractSexftpView.TreeParent projectsap = new AbstractSexftpView.TreeParent("Projects View",
					"Projects Applies");
			root.addChild(projectsap);
			for (IProject iProject : projects) {
				AbstractSexftpView.TreeParent p = new AbstractSexftpView.TreeParent(iProject.getName(), iProject);
				projectsap.addChild(p);
			}

		}

		this.invisibleRoot = new AbstractSexftpView.TreeParent("", null);
		this.invisibleRoot.addChild(root);
	}

	protected boolean isShowProjectView() {
		return true;
	}

	public FtpUploadPro downPro2UpPro(FtpDownloadPro dpro) {
		FtpUploadConf ftpUploadConf = dpro.getFtpUploadConf();
		ftpUploadConf = (FtpUploadConf) StringUtil.deepClone(ftpUploadConf);
		String clientPath = dpro.getFtpUploadConf().getClientPath() + "/"
				+ new File(dpro.getFtpUploadConf().getServerPath()).getName();
		ftpUploadConf.setClientPath(new File(clientPath).getAbsolutePath());
		String serverPath = ftpUploadConf.getServerPath().substring(0,
				ftpUploadConf.getServerPath().lastIndexOf("/") + 1);
		ftpUploadConf.setServerPath(serverPath);
		FtpUploadPro upro = new FtpUploadPro(ftpUploadConf, dpro.getFtpConf());
		return upro;
	}

	public FtpDownloadPro uPro2DownPro(FtpUploadPro upro) {
		File file = new File(upro.getFtpUploadConf().getClientPath());

		String filename = file.getName();
		FtpFile ftpfile = new FtpFile(filename, false, 0L, null);
		FtpUploadConf fconf = (FtpUploadConf) StringUtil.deepClone(upro.getFtpUploadConf());
		if (file.isFile()) {
			fconf.setServerPath(fconf.getServerPath() + filename);
			fconf.setClientPath(file.getParent());
		}
		FtpDownloadPro dpro = new FtpDownloadPro(fconf, upro.getFtpConf(), ftpfile);
		return dpro;
	}

	private void addChildOfProjectDir(AbstractSexftpView.TreeParent p, IProgressMonitor monitor) {
		IFile ifile = (IFile) p.getO();
		File[] subfiles = ifile.getLocation().toFile().listFiles();
		if (subfiles == null) {
			return;
		}
		for (File file : subfiles) {
			if (file.isHidden()) {
				continue;
			}
			if (!file.isDirectory())
				continue;
			if (monitor.isCanceled()) {
				throw new AbortException();
			}
			monitor.subTask("scanning " + file.getAbsolutePath());
			AbstractSexftpView.TreeParent child = new AbstractSexftpView.TreeParent(file.getName(),
					ifile.getProject().getWorkspace().getRoot()
							.getFile(new Path(ifile.getFullPath() + "/" + file.getName())));
			p.addChild(child);
			addChildOfProjectDir(child, monitor);
		}
	}

	protected void doubleClickAction_actionPerformed() throws Exception {
		ISelection selection = this.viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj instanceof AbstractSexftpView.TreeParent)
			if ((!this.viewer.getExpandedState(obj))
					|| (((AbstractSexftpView.TreeParent) obj).getChildren().length == 0)) {
				treeExpanded_actionPerformed(new TreeExpansionEvent(this.viewer, obj));
				this.viewer.expandToLevel(obj, 1);
			} else {
				this.viewer.collapseToLevel(obj, 1);
			}
	}

	protected void treeExpanded_actionPerformed(TreeExpansionEvent e) throws Exception {
		super.treeExpanded_actionPerformed(e);
		Object elem = e.getElement();
		if (!(elem instanceof AbstractSexftpView.TreeParent))
			return;
		expandProjects((AbstractSexftpView.TreeParent) elem);
	}

	private void expandProjects(final AbstractSexftpView.TreeParent p) {
		if (!(p.getO() instanceof IProject))
			return;
		
		p.removeAll();
		
		refreshPendingTree(p, new SexftpRun(this) {
			public void srun() throws Exception {
				IProject project = (IProject) p.getO();
				File[] subfiles = project.getFile("/a.txt").getLocation().toFile().getParentFile().listFiles();
				if (subfiles == null)
					return;
				for (File file : subfiles) {
					if (!file.isDirectory())
						continue;
					AbstractSexftpView.TreeParent child = new AbstractSexftpView.TreeParent(file.getName(),
							project.getFile("/" + file.getName()));
					p.addChild(child);
					SexftpMainView.this.addChildOfProjectDir(child, getMonitor());
				}
			}
		});
	}

	protected void actionEnableHandle() {
		this.actionRefreshSexftp.setEnabled(true);
		this.actionRefreshFile.setEnabled(false);
		this.actionPrepareUpload.setEnabled(false);
		this.actionApplySexFtpConf.setEnabled(false);
		this.actionFormat.setEnabled(false);
		this.actionEditSexFtpConf.setEnabled(false);
		this.actionPrepareServUpload.setEnabled(false);
		this.actionUpload.setEnabled(false);
		this.actionDeleteSexFtpConf.setEnabled(false);
		this.actionEdit.setEnabled(false);
		this.actionLocalEdit.setEnabled(false);
		this.actionDownload.setEnabled(false);
		this.actionApplySexFtpConf.setEnabled(true);
		this.actionCompare.setEnabled(false);

		if (okPopActionEditSexFtpConf()) {
			this.actionEditSexFtpConf.setEnabled(true);
			this.actionDeleteSexFtpConf.setEnabled(true);
		}
		FtpConf[] ftpConfsSelected = getFtpConfsSelected();
		if ((okPopActionPrepareUpload()) && (canEnableUpload()) && (ftpConfsSelected.length == 1)) {
			this.actionPrepareServUpload.setEnabled(true);
			this.actionPrepareUpload.setEnabled(true);
		}

		if (ftpConfsSelected.length == 1) {
			this.actionCompare.setEnabled(true);
			this.actionFormat.setEnabled(true);
		}
		if ((okPopActionUpload()) && (ftpConfsSelected.length == 1)) {
			this.actionUpload.setEnabled(true);
		}

		if ((okPopActionDownload()) && (ftpConfsSelected.length == 1)) {
			this.actionDownload.setEnabled(true);
		}

		if (getSelectionObjects().length != 1)
			return;
		Object o = getSelectionObjects()[0];
		if (!(o instanceof FtpUploadPro) && !(o instanceof FtpDownloadPro) && !(o instanceof FtpUploadConf))
			return;
		this.actionRefreshFile.setEnabled(true);
	}

	public FtpConf[] getFtpConfsSelected() {
		Set<FtpConf> set = new HashSet<FtpConf>();
		ISelection selection = this.viewer.getSelection();
		Iterator<?> it = ((IStructuredSelection) selection).iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (!(o instanceof AbstractSexftpView.TreeObject))
				continue;
			AbstractSexftpView.TreeObject to = (AbstractSexftpView.TreeObject) o;
			for (int i = 0; (i < 50) && (to != null); ++i) {
				if (to.getO() instanceof FtpConf) {
					set.add((FtpConf) to.getO());
					break;
				}
				to = to.getParent();
			}
		}

		return (FtpConf[]) set.toArray(new FtpConf[0]);
	}

	protected boolean canEnableUpload() {
		return false;
	}

	protected void actionDisconnect_actionPerformed() throws Exception {
		FtpPools pool = new FtpPools(null, this);
		pool.disconnectAll();
	}

	protected void menuAboutToShow_event(IMenuManager manager) {
		actionEnableHandle();
		manager.add(this.actionRefreshSexftp);
		manager.add(this.actionRefreshFile);
		manager.add(new Separator("additions"));
		manager.add(this.actionApplySexFtpConf);
		manager.add(this.actionEditSexFtpConf);
		manager.add(this.actionDeleteSexFtpConf);
		manager.add(new Separator("additions"));
		manager.add(this.actionFormat);
		manager.add(this.actionPrepareUpload);
		manager.add(this.actionPrepareServUpload);

		manager.add(new Separator("additions"));
		manager.add(this.actionUpload);
		manager.add(this.actionDownload);
		manager.add(this.actionEdit);
		manager.add(this.actionLocalEdit);
		manager.add(this.actionCompare);
		manager.add(this.actionDisconnect);
		manager.add(new Separator("additions"));
		manager.add(this.actionDirectSLocal);
		manager.add(this.actionDirectSServer);
		manager.add(this.actionLocationTo);
		manager.add(new Separator("additions"));
		manager.add(this.actionCopy);
		manager.add(this.actionCopyQualifiedName);
		manager.add(this.actionCopyCientPath);
		manager.add(this.actionCopyServerPath);
		manager.add(this.actionExplorer);
		manager.add(new Separator("additions"));
		manager.add(this.actionExpandAll);
		manager.add(this.actionCollapseAll);

		List<?> hiddenActions = getHiddenActions();
		for (IContributionItem menuItem : manager.getItems()) {
			if (!(menuItem instanceof ActionContributionItem))
				continue;
			ActionContributionItem a = (ActionContributionItem) menuItem;
			if (!hiddenActions.contains(a.getAction()))
				continue;
			manager.remove(menuItem);
		}

		super.menuAboutToShow_event(manager);
	}

	public void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		List<?> hiddenActions = getHiddenActions();
		for (IContributionItem menuItem : manager.getItems()) {
			if (!(menuItem instanceof ActionContributionItem))
				continue;
			ActionContributionItem a = (ActionContributionItem) menuItem;
			if (!hiddenActions.contains(a.getAction()))
				continue;
			manager.remove(menuItem);
		}
	}

	@SuppressWarnings("rawtypes")
	public List<?> getHiddenActions() {
		return new ArrayList();
	}

	protected boolean okPopActionPrepareUpload() {
		Object[] os = getSelectionObjects();
		if (os.length == 0)
			return false;
		for (Object o : os) {
			if (!(o instanceof FtpConf) && !(o instanceof FtpUploadConf) && !(o instanceof FtpUploadPro)) {
				return false;
			}
		}
		return true;
	}

	protected boolean okPopActionUpload() {
		return false;
	}

	protected boolean okPopActionDownload() {
		return false;
	}

	private boolean okPopActionEditSexFtpConf() {
		Object[] os = getSelectionObjects();

		return (os.length == 1) && (os[0] instanceof FtpConf);
	}

	private String okPopActionApplySexFtpConf(List<String> pathList) {
		Set<String> projectNameSet = new HashSet<String>();
		String projectName = null;
		Object[] objes = getSelectionObjects();
		for (Object o : objes) {
			if (o instanceof IFile) {
				IFile ifile = (IFile) o;
				projectName = ifile.getProject().getName();
				projectNameSet.add(projectName);
				pathList.add(ifile.getLocation().toFile().getAbsolutePath());
			} else if (o instanceof IProject) {
				IProject iproject = (IProject) o;
				projectName = iproject.getName();
				projectNameSet.add(projectName);
				pathList.add(iproject.getFile("/a.txt").getLocation().toFile().getParent());
			} else {
				return null;
			}
		}

		if (projectNameSet.size() == 1)
			return projectName;

		return null;
	}

	protected void actionCopyCientPath_actionPerformed() throws Exception {
		Object[] objes = getSelectionObjects();
		StringBuffer sb = new StringBuffer();
		for (Object o : objes) {
			String serverPath = null;
			String clientPath = null;
			if (o instanceof FtpUploadConf) {
				serverPath = ((FtpUploadConf) o).getServerPath();
				clientPath = ((FtpUploadConf) o).getClientPath();
			} else if (o instanceof FtpUploadPro) {
				serverPath = ((FtpUploadPro) o).getFtpUploadConf().getServerPath();
				clientPath = ((FtpUploadPro) o).getFtpUploadConf().getClientPath();
			} else if (o instanceof FtpDownloadPro) {
				serverPath = ((FtpDownloadPro) o).getFtpUploadConf().getServerPath();
				clientPath = ((FtpDownloadPro) o).getFtpUploadConf().getClientPath();
				if (!serverPath.endsWith("/")) {
					clientPath = clientPath + "/" + new File(serverPath).getName();
					clientPath = new File(clientPath).getAbsolutePath();
				}
			}
			File client = new File(clientPath);
			if ((client.isFile()) && (serverPath.endsWith("/"))) {
				serverPath = serverPath + client.getName();
			}
			sb.append(clientPath);
			sb.append("\r\n");
		}
		Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Object tText = new StringSelection(sb.toString().trim());
		systemClipboard.setContents((Transferable) tText, null);
	}

	protected void actionCopyServerPath_actionPerformed() throws Exception {
		Object[] objes = getSelectionObjects();
		StringBuffer sb = new StringBuffer();
		for (Object o : objes) {
			String serverPath = null;
			String clientPath = null;
			if (o instanceof FtpUploadConf) {
				serverPath = ((FtpUploadConf) o).getServerPath();
				clientPath = ((FtpUploadConf) o).getClientPath();
			} else if (o instanceof FtpUploadPro) {
				serverPath = ((FtpUploadPro) o).getFtpUploadConf().getServerPath();
				clientPath = ((FtpUploadPro) o).getFtpUploadConf().getClientPath();
			} else if (o instanceof FtpDownloadPro) {
				serverPath = ((FtpDownloadPro) o).getFtpUploadConf().getServerPath();
				clientPath = ((FtpDownloadPro) o).getFtpUploadConf().getClientPath();
				if (!serverPath.endsWith("/")) {
					clientPath = clientPath + "/" + new File(serverPath).getName();
					clientPath = new File(clientPath).getAbsolutePath();
				}
			}
			File client = new File(clientPath);
			if ((client.isFile()) && (serverPath.endsWith("/"))) {
				serverPath = serverPath + client.getName();
			}
			sb.append(serverPath);
			sb.append("\r\n");
		}
		Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Object tText = new StringSelection(sb.toString().trim());
		systemClipboard.setContents((Transferable) tText, null);
	}

	protected Object[] getSelectionObjects() {
		return getSelectionObjects(false);
	}

	protected Object[] getSelectionObjects(boolean includChild) {
		Set<Object> r = new HashSet<Object>();
		ISelection selection = this.viewer.getSelection();
		Iterator<?> it = ((IStructuredSelection) selection).iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof AbstractSexftpView.TreeParent) {
				AbstractSexftpView.TreeParent p = (AbstractSexftpView.TreeParent) o;
				r.add(p.getO());
				if (includChild)
					r.addAll(Arrays.asList(getChildObjects(p, null)));
			} else if (o instanceof AbstractSexftpView.TreeObject) {
				AbstractSexftpView.TreeObject p = (AbstractSexftpView.TreeObject) o;
				r.add(p.getO());
			} else {
				r.add("Unknown");
			}
		}
		return r.toArray();
	}

	protected Object[] getChildObjects(AbstractSexftpView.TreeParent p, IProgressMonitor monitor) {
		if ((monitor != null) && (monitor.isCanceled())) {
			throw new AbortException();
		}
		List<Object> r = new ArrayList<Object>();
		AbstractSexftpView.TreeObject[] children = p.getChildren();
		for (AbstractSexftpView.TreeObject child : children) {
			r.add(child.getO());
			if (!(child instanceof AbstractSexftpView.TreeParent))
				continue;
			if (monitor != null)
				monitor.subTask("Prepareing data in Node " + child.toString());
			r.addAll(Arrays.asList(getChildObjects((AbstractSexftpView.TreeParent) child, monitor)));
		}

		return r.toArray();
	}

	private AbstractSexftpView.TreeObject[] getChildNode(AbstractSexftpView.TreeParent p) {
		List<TreeObject> r = new ArrayList<TreeObject>();
		AbstractSexftpView.TreeObject[] children = p.getChildren();
		for (AbstractSexftpView.TreeObject child : children) {
			r.add(child);
			if (!(child instanceof AbstractSexftpView.TreeParent))
				continue;
			r.addAll(Arrays.asList(getChildNode((AbstractSexftpView.TreeParent) child)));
		}

		return (AbstractSexftpView.TreeObject[]) r.toArray(new AbstractSexftpView.TreeObject[0]);
	}

	public AbstractSexftpView.TreeObject[] getUpNodes(AbstractSexftpView.TreeObject[] nodes) {
		Set<TreeObject> set = new LinkedHashSet<TreeObject>(Arrays.asList(nodes));
		for (AbstractSexftpView.TreeObject nod : nodes) {
			AbstractSexftpView.TreeObject c = nod;
			for (int i = 0; (i < 50) && (c != null) && (c != nod); c = c.getParent()) {
				if (set.contains(c)) {
					set.remove(nod);
					break;
				}
				++i;
			}

		}

		return (AbstractSexftpView.TreeObject[]) set.toArray(new AbstractSexftpView.TreeObject[0]);
	}

	public AbstractSexftpView.TreeObject[] getSelectNodes(boolean includeChild) {
		List<TreeObject> r = new ArrayList<TreeObject>();
		ISelection selection = this.viewer.getSelection();
		Iterator<?> it = ((IStructuredSelection) selection).iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof AbstractSexftpView.TreeParent) {
				AbstractSexftpView.TreeParent p = (AbstractSexftpView.TreeParent) o;
				r.add(p);
				if (includeChild)
					r.addAll(Arrays.asList(getChildNode(p)));
			} else {
				r.add((AbstractSexftpView.TreeObject) o);
			}
		}
		return (AbstractSexftpView.TreeObject[]) r.toArray(new AbstractSexftpView.TreeObject[0]);
	}

	public AbstractSexftpView.TreeParent[] getAllFtpConfNodes() {
		List<TreeParent> list = new ArrayList<TreeParent>();
		for (AbstractSexftpView.TreeObject ftpstart : this.invisibleRoot.getChildren()) {
			for (AbstractSexftpView.TreeObject ftpConfNode : ((AbstractSexftpView.TreeParent) ftpstart).getChildren()) {
				AbstractSexftpView.TreeParent ftpConfNodeIn = (AbstractSexftpView.TreeParent) ftpConfNode;
				list.add(ftpConfNodeIn);
			}
		}

		return (AbstractSexftpView.TreeParent[]) list.toArray(new AbstractSexftpView.TreeParent[0]);
	}

	public AbstractSexftpView.TreeParent[] getAllFtpUploadConfNodes() {
		List<TreeParent> list = new ArrayList<TreeParent>();
		for (AbstractSexftpView.TreeObject ftpstart : this.invisibleRoot.getChildren()) {
			for (AbstractSexftpView.TreeObject ftpConfNode : ((AbstractSexftpView.TreeParent) ftpstart).getChildren()) {
				AbstractSexftpView.TreeParent ftpConfNodeIn = (AbstractSexftpView.TreeParent) ftpConfNode;
				for (AbstractSexftpView.TreeObject ftpUploadConfNode : ftpConfNodeIn.getChildren()) {
					list.add((AbstractSexftpView.TreeParent) ftpUploadConfNode);
				}
			}
		}
		return (AbstractSexftpView.TreeParent[]) list.toArray(new AbstractSexftpView.TreeParent[0]);
	}

	public AbstractSexftpView.TreeParent[] getSelectFtpUploadConfNodes() {
		Set<TreeParent> nodset = new HashSet<TreeParent>();
		Object[] selectFtpConfNodes = getSelectNodes(true);
		for (Object object : selectFtpConfNodes) {
			AbstractSexftpView.TreeObject to = (AbstractSexftpView.TreeObject) object;
			for (int i = 0; (i < 10) && (to != null); ++i) {
				if (to.getO() instanceof FtpUploadConf) {
					nodset.add((AbstractSexftpView.TreeParent) to);
					break;
				}
				to = to.getParent();
			}
		}
		return (AbstractSexftpView.TreeParent[]) nodset.toArray(new AbstractSexftpView.TreeParent[0]);
	}

	public AbstractSexftpView.TreeObject[] getSelectFtpconfNodes() {
		Set<TreeObject> nodset = new HashSet<TreeObject>();
		Object[] selectFtpConfNodes = getSelectNodes(false);
		for (Object object : selectFtpConfNodes) {
			AbstractSexftpView.TreeObject to = (AbstractSexftpView.TreeObject) object;
			for (int i = 0; (i < 10) && (to != null); ++i) {
				if (to.getO() instanceof FtpConf) {
					nodset.add(to);
					break;
				}
				to = to.getParent();
			}
		}
		return (AbstractSexftpView.TreeObject[]) nodset.toArray(new AbstractSexftpView.TreeObject[0]);
	}

	public void refreshSelectNode(boolean includeChild) {
		Object[] os = getSelectNodes(includeChild);
		for (Object object : os)
			this.viewer.refresh(object, true);
	}
}