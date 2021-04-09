package sexftp.views;

import com.lowagie.text.html.HtmlEncoder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.sexftp.core.exceptions.AbortException;
import org.sexftp.core.exceptions.BizException;
import org.sexftp.core.exceptions.SRuntimeException;
import org.sexftp.core.ftp.Consoleable;
import org.sexftp.core.ftp.bean.FtpConf;
import org.sexftp.core.ftp.bean.FtpUploadConf;
import org.sexftp.core.ftp.bean.FtpUploadPro;
import org.sexftp.core.utils.StringUtil;
import sexftp.SrcViewable;
import sexftp.uils.Console;
import sexftp.uils.LangUtil;
import sexftp.uils.LogUtil;
import sexftp.uils.PluginUtil;

@SuppressWarnings("deprecation")
public class AbstractSexftpView extends ViewPart implements Consoleable, SrcViewable {
	public static final String ID = "sexftp.views.MainView";
	public static String workspacePath = "";

	public static String workspaceWkPath = "";
	protected TreeViewer viewer;
	//private DrillDownAdapter drillDownAdapter;
	protected Action actionApplySexFtpConf;
	protected Action actionEditSexFtpConf;
	protected Action actionDeleteSexFtpConf;
	protected Action actionFormat;
	protected Action actionPrepareUpload;
	protected Action actionLocationTo;
	protected Action actionPrepareServUpload;
	protected Action actionUpload;
	protected Action actionDownload;
	protected Action actionEdit;
	protected Action actionLocalEdit;
	protected Action actionCompare;
	protected Action actionDisconnect;
	protected Action actionDirectSServer;
	protected Action actionDirectSLocal;
	protected Action actionRefreshSexftp;
	protected Action actionRefreshFile;
	private Action doubleClickAction;
	protected Action actionCopy;
	protected Action actionCopyQualifiedName;
	protected Action actionCopyCientPath;
	protected Action actionCopyServerPath;
	protected Action actionExplorer;
	protected Action actionCollapseAll;
	protected Action actionExpandAll;
	protected TreeParent invisibleRoot;
	protected Map<String, String> customizedImgMap = new Hashtable<String, String>();

	IWorkbenchPage activePage = null;

	private Console console = null;

	public void createPartControl(Composite parent) {
		this.viewer = new TreeViewer(parent, 770);
		//this.drillDownAdapter = new DrillDownAdapter(this.viewer);
		this.viewer.setContentProvider(new ViewContentProvider());
		this.viewer.setLabelProvider(new ViewLabelProvider());
		this.viewer.setSorter(new NameSorter());
		this.viewer.setInput(getViewSite());

		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.viewer.getControl(), "sexftp.viewer");

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		hookSelectionChange();
		hookTreeListener();
		contributeToActionBars();

		this.viewer.expandToLevel(2);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				AbstractSexftpView.this.menuAboutToShow_event(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(this.viewer.getControl());
		this.viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, this.viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	protected void actionEnableHandle() {
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.actionRefreshSexftp);
		manager.add(this.actionDirectSServer);
		manager.add(this.actionDirectSLocal);
	}

	public void fillLocalToolBar(IToolBarManager manager) {
		actionEnableHandle();
		new Action("Refresh And Reload Sexftp Config Files(&w)") {
		};
		manager.add(this.actionRefreshSexftp);
		manager.add(this.actionRefreshFile);
		manager.add(new Separator());
		manager.add(this.actionDirectSServer);
		manager.add(this.actionDirectSLocal);
		manager.add(this.actionLocationTo);
		manager.add(new Separator());
		manager.add(this.actionApplySexFtpConf);
		manager.add(this.actionEditSexFtpConf);
		manager.add(this.actionDeleteSexFtpConf);
		manager.add(new Separator());
		manager.add(this.actionFormat);
		manager.add(this.actionPrepareUpload);
		manager.add(this.actionPrepareServUpload);
		manager.add(new Separator());
		manager.add(this.actionUpload);
		manager.add(this.actionDownload);
		manager.add(this.actionDisconnect);
		manager.add(new Separator());
		manager.add(this.actionExplorer);
	}

	public Shell getShell() {
		return (Shell) PluginUtil.runAsDisplayThread(new PluginUtil.RunAsDisplayThread() {
			public Object run() throws Exception {
				return AbstractSexftpView.this.viewer.getControl().getShell();
			}
		});
	}

	public IWorkbenchPage getWorkbenchPage() {
		IWorkbenchPage activePage = PluginUtil.getActivePage();
		if (activePage != null) {
			this.activePage = activePage;
		}
		return this.activePage;
	}

	private void makeActions() {
		this.actionApplySexFtpConf = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionApplySexFtpConf_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionApplySexFtpConf.setText("New Sexftp Upload Unit");
		this.actionApplySexFtpConf.setToolTipText(
				"Generate Sexftp Config File,Using Your Chosed Folders And Files,Main Option Start Here!");
		this.actionApplySexFtpConf
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/gif-0108.gif"));

		this.actionEditSexFtpConf = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionEditSexFtpConf_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionEditSexFtpConf.setText("Edit Sexftp Upload Unit");
		this.actionEditSexFtpConf.setToolTipText("Edit Sexftp Config File");
		this.actionEditSexFtpConf
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/gif-0708.gif"));

		this.actionDeleteSexFtpConf = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionDeleteSexFtpConf_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionDeleteSexFtpConf.setText("Remove Sexftp Upload Unit");
		this.actionDeleteSexFtpConf.setToolTipText("Remove Sexftp Config File");
		this.actionDeleteSexFtpConf
				.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_TOOL_DELETE"));

		this.actionFormat = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionFormat_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionFormat.setText("Format Local File Upload Point(&F)");
		this.actionFormat
				.setToolTipText("Format Local File Upload Point,New Modify Checking Will Based On this Result!");
		this.actionFormat.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/xhrmon.gif"));

		this.actionPrepareUpload = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionPrepareUpload_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionPrepareUpload.setText("View Or Upload Local New Modified Files(&M)");
		this.actionPrepareUpload.setToolTipText(
				"After This Action,We'll Gevi You Which File Modified After Last Format or Upload Option,And Then You Can Chose Them To Upload!");
		this.actionPrepareUpload
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/compare_view.gif"));

		this.actionLocationTo = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionLocationTo_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionLocationTo.setText("Location To(&N)");

		this.actionLocationTo
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/filter_history.gif"));

		this.actionPrepareServUpload = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionPrepareServUpload_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionPrepareServUpload.setText("View Or Upload Files Witch Different From Server(&A)");
		this.actionPrepareServUpload.setToolTipText("View Or Upload Files Witch Different From Server!");
		this.actionPrepareServUpload
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/synch_synch.gif"));

		this.actionUpload = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionUpload_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionUpload.setText("Upload To Server(&U)");
		this.actionUpload.setToolTipText("After This Action,Upload Files You Chose To Ftp Server!");
		this.actionUpload
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/prev_nav.gif"));

		this.actionDownload = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionDownload_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionDownload.setText("Download From Server(&D)");
		this.actionDownload.setToolTipText("After This Action,Download Files You Chose From Ftp Server!");
		this.actionDownload
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/next_nav.gif"));

		this.actionEdit = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionEdit_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionEdit.setText("View Or Edit ServerSide File(&E)");
		this.actionEdit.setToolTipText("View Or Edit ServerSide File!");
		this.actionEdit
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/history_view.gif"));

		this.actionLocalEdit = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionLocalEdit_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionLocalEdit.setText("View Or Edit LocalSide File(&T)");
		this.actionLocalEdit.setToolTipText("View Or Edit LocalSide File!");
		this.actionLocalEdit
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/edit_action.gif"));

		this.actionDisconnect = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionDisconnect_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionDisconnect.setText("Disconnect All Connections(&Q)");
		this.actionDisconnect.setToolTipText("Disconnect All Connections!");
		this.actionDisconnect
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/disconnect_co.gif"));

		this.actionDirectSServer = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionDirectSServer_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionDirectSServer.setText("Open Server Viewer(&S)");
		this.actionDirectSServer.setToolTipText("Open Server Viewer!");
		this.actionDirectSServer
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/repository_rep.gif"));

		this.actionCompare = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionCompare_actionPerformed();
				} catch (Throwable e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionCompare.setText("Compare (&C)");
		this.actionCompare.setToolTipText("Compare");
		this.actionCompare
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/compare.gif"));

		this.actionDirectSLocal = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionDirectSLocal_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionDirectSLocal.setText("Location To Local Viewer(&L)");
		this.actionDirectSLocal.setToolTipText("Location To Local Viewer!");
		this.actionDirectSLocal
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/Follow_me.ico"));

		this.actionRefreshSexftp = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionRefreshSexftp_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionRefreshSexftp.setText("Refresh And Reload Sexftp Config Files(&w)");
		this.actionRefreshSexftp.setToolTipText("Refresh And Reload All Sexftp Configs");
		this.actionRefreshSexftp
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/refresh.gif"));

		this.actionRefreshFile = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionRefreshFile_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionRefreshFile.setText("Refresh Files And Folders(&R)");
		this.actionRefreshFile.setToolTipText("Refresh Files And Folders");
		this.actionRefreshFile
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/refresh_5.gif"));

		this.actionCopy = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionCopy_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionCopy.setText("Copy");
		this.actionCopy.setToolTipText("Copy Name");
		this.actionCopy
				.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_TOOL_COPY"));

		this.actionCopyQualifiedName = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionCopyQualifiedName_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionCopyQualifiedName.setText("Copy Qualified Name");
		this.actionCopyQualifiedName.setToolTipText("Copy Qualified Name");
		this.actionCopyQualifiedName
				.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_TOOL_COPY"));

		this.actionCopyCientPath = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionCopyCientPath_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionCopyCientPath.setText("Copy Cient File/Folder Path(&Z)");
		this.actionCopyCientPath.setToolTipText("Copy Cient File or Folder's Path");
		this.actionCopyCientPath
				.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_TOOL_COPY"));

		this.actionCopyServerPath = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionCopyServerPath_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionCopyServerPath.setText("Copy Server File/Folder Path(&S)");
		this.actionCopyServerPath.setToolTipText("Copy Server File/Folder Path");
		this.actionCopyServerPath
				.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_TOOL_COPY"));

		this.actionExplorer = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionExplorer_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionExplorer.setText("Open In Explorer");
		this.actionExplorer.setToolTipText("Open In Explorer");
		this.actionExplorer
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/explorer_open.gif"));

		this.actionCollapseAll = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionCollapseAll_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionCollapseAll.setText("Collapse All");
		this.actionCollapseAll.setToolTipText("Collapse All");
		this.actionCollapseAll
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/collapseall.gif"));

		this.actionExpandAll = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.actionExpandAll_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		this.actionExpandAll.setText("Expand All");
		this.actionExpandAll.setToolTipText("Expand All");
		this.actionExpandAll
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/expand_all.gif"));

		this.doubleClickAction = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpView.this.doubleClickAction_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		};
		actionPrepare();
	}

	protected void actionPrepare() {
	}

	protected void actionPaste_actionPerformed() throws Exception {
	}

	protected void actionLocation_actionPerformed() throws Exception {
	}

	protected void actionExplorer_actionPerformed() throws Exception {
	}

	protected void actionCopyQualifiedName_actionPerformed() throws Exception {
	}

	protected void actionCopy_actionPerformed() throws Exception {
	}

	protected void actionCopyCientPath_actionPerformed() throws Exception {
	}

	protected void actionCopyServerPath_actionPerformed() throws Exception {
	}

	protected void actionCompare_actionPerformed() throws Exception {
	}

	protected void actionApplySexFtpConf_actionPerformed() throws Exception {
	}

	protected void actionEditSexFtpConf_actionPerformed() throws Exception {
	}

	protected void actionDeleteSexFtpConf_actionPerformed() throws Exception {
	}

	protected void actionDisconnect_actionPerformed() throws Exception {
	}

	protected void actionFormat_actionPerformed() throws Exception {
	}

	protected void actionPrepareUpload_actionPerformed() throws Exception {
	}

	protected void actionPrepareServUpload_actionPerformed() throws Exception {
	}

	protected void actionLocationTo_actionPerformed() throws Exception {
		actionLocation_actionPerformed();
	}

	protected void actionUpload_actionPerformed() throws Exception {
	}

	protected void actionDownload_actionPerformed() throws Exception {
	}

	protected void actionEdit_actionPerformed() throws Exception {
	}

	protected void actionLocalEdit_actionPerformed() throws Exception {
	}

	protected void actionDirectSLocal_actionPerformed() throws Exception {
	}

	protected void actionDirectSServer_actionPerformed() throws Exception {
	}

	protected void actionRefreshSexftp_actionPerformed() throws Exception {
	}

	protected void actionRefreshFile_actionPerformed() throws Exception {
	}

	protected void doubleClickAction_actionPerformed() throws Exception {
	}

	protected void actionCollapseAll_actionPerformed() throws Exception {
	}

	protected void actionExpandAll_actionPerformed() throws Exception {
	}

	protected void treeExpanded_actionPerformed(TreeExpansionEvent e) throws Exception {
	}

	protected void treeCollapsed_actionPerformed(TreeExpansionEvent e) throws Exception {
	}

	protected void menuAboutToShow_event(IMenuManager manager) {
	}

	protected void refreshTreeViewData() {
	}

	private void hookDoubleClickAction() {
		this.viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				try {
					AbstractSexftpView.this.doubleClickAction.run();
				} catch (Exception e) {
					AbstractSexftpView.this.handleException(e);
				}
			}
		});
	}

	private void hookSelectionChange() {
		this.viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent arg0) {
				AbstractSexftpView.this.actionEnableHandle();
			}
		});
	}

	public void expandToObject(String filePath) {
		for (Object o : this.viewer.getExpandedElements()) {
			if (o instanceof TreeParent) {
				for (TreeObject to : ((TreeParent) o).getChildren()) {
					expandToObject(to, filePath);
				}
			}
		}
	}

	private void expandToObject(TreeObject to, String filePath) {
		if (to instanceof TreeParent) {
			if ((to.getO() instanceof FtpUploadConf)
					&& (filePath.startsWith(((FtpUploadConf) to.getO()).getClientPath()))) {
				this.viewer.expandToLevel(to.getParent(), 1);
				return;
			}

			for (TreeObject co : ((TreeParent) to).getChildren()) {
				expandToObject(co, filePath);
			}
		} else {
			// to instanceof TreeObject;
		}
	}

	private void hookTreeListener() {
		this.viewer.addTreeListener(new ITreeViewerListener() {
			public void treeExpanded(TreeExpansionEvent e) {
				try {
					AbstractSexftpView.this.treeExpanded_actionPerformed(e);
				} catch (Exception e1) {
					AbstractSexftpView.this.handleException(e1);
				}
			}

			public void treeCollapsed(TreeExpansionEvent e) {
				try {
					AbstractSexftpView.this.treeCollapsed_actionPerformed(e);
				} catch (Exception e1) {
					AbstractSexftpView.this.handleException(e1);
				}
			}
		});
		this.viewer.getTree().addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent arg0) {
			}

			public void keyPressed(KeyEvent e) {
				try {
					if (e.stateMask != 262144)
						return;
					if (e.keyCode == 118) {
						AbstractSexftpView.this.actionPaste_actionPerformed();
					} else {
						if (e.keyCode != 108) {
							return;
						}
						AbstractSexftpView.this.actionLocation_actionPerformed();
					}
				} catch (Throwable e1) {
					AbstractSexftpView.this.handleException(e1);
				}
			}
		});
	}

	protected void showMessage(final String message) {
		Runnable runnable = new Runnable() {
			public void run() {
				MessageDialog.openInformation(AbstractSexftpView.this.viewer.getControl().getShell(),
						LangUtil.langText("Sexftp Messsage"), LangUtil.langText(message));
			}
		};
		Display.getDefault().syncExec(runnable);
	}

	protected void showError(final String message) {
		Runnable runnable = new Runnable() {
			public void run() {
				MessageDialog.openError(AbstractSexftpView.this.viewer.getControl().getShell(),
						LangUtil.langText("Sexftp Error"), LangUtil.langText(message));
			}
		};
		Display.getDefault().syncExec(runnable);
	}

	protected void showError(final String message, final Throwable e) {
		Status s = new Status(4, "sexftp", e.getMessage()) {
			public IStatus[] getChildren() {
				String[] es = StringUtil.readExceptionDetailInfo(e).split("\n");
				List<Status> st = new ArrayList<Status>();
				for (String m : es) {
					if (m.trim().length() <= 0)
						continue;
					st.add(new Status(4, "sexftp", m.trim(), null));
				}

				return (IStatus[]) st.toArray(new IStatus[0]);
			}
		};
		ErrorDialog.openError(this.viewer.getControl().getShell(), "Sexftp Error", message, s);
	}

	public boolean showQuestion(final String message) {
		Runnable runnable = new Runnable() {
			private boolean isok = false;
			public void run() {
				this.isok = MessageDialog.openQuestion(AbstractSexftpView.this.viewer.getControl().getShell(),
						LangUtil.langText("Sexftp Question"), LangUtil.langText(message));
			}
			@SuppressWarnings("unused")
			public boolean isOk(){
				return this.isok;
			}
		};
		Display.getDefault().syncExec(runnable);
		try {
			return ((Boolean) runnable.getClass().getMethod("isOk", new Class[0]).invoke(runnable, new Object[0]))
					.booleanValue();
		} catch (Exception e) {
			throw new SRuntimeException(e);
		}
	}

	public MessageDialogWithToggle showQuestionInDTread(final String message, final String toggleMessage) {
		Runnable runnable = new Runnable() {
			private MessageDialogWithToggle msg = null;;
			public void run() {
				this.msg = AbstractSexftpView.this.showQuestion(message, toggleMessage);
			}
			@SuppressWarnings("unused")
			public MessageDialogWithToggle getS() {
				return this.msg;
			}
		};
		Display.getDefault().syncExec(runnable);
		try {
			return (MessageDialogWithToggle) runnable.getClass().getMethod("getS", new Class[0]).invoke(runnable,
					new Object[0]);
		} catch (Exception e) {
			throw new SRuntimeException(e);
		}
	}

	public MessageDialogWithToggle showQuestion(final String message, final String toggleMessage) {
		MessageDialogWithToggle t = MessageDialogWithToggle.openYesNoCancelQuestion(this.viewer.getControl().getShell(),
				"Sexftp Question", message, toggleMessage, false, new IPreferenceStore() {
					public void setValue(String arg0, boolean arg1) {
					}

					public void setValue(String arg0, String arg1) {
					}

					public void setValue(String arg0, long arg1) {
					}

					public void setValue(String arg0, int arg1) {
					}

					public void setValue(String arg0, float arg1) {
					}

					public void setValue(String arg0, double arg1) {
					}

					public void setToDefault(String arg0) {
					}

					public void setDefault(String arg0, boolean arg1) {
					}

					public void setDefault(String arg0, String arg1) {
					}

					public void setDefault(String arg0, long arg1) {
					}

					public void setDefault(String arg0, int arg1) {
					}

					public void setDefault(String arg0, float arg1) {
					}

					public void setDefault(String arg0, double arg1) {
					}

					public void removePropertyChangeListener(IPropertyChangeListener arg0) {
					}

					public void putValue(String arg0, String arg1) {
					}

					public boolean needsSaving() {
						return false;
					}

					public boolean isDefault(String arg0) {
						return false;
					}

					public String getString(String arg0) {
						return null;
					}

					public long getLong(String arg0) {
						return 0L;
					}

					public int getInt(String arg0) {
						return 0;
					}

					public float getFloat(String arg0) {
						return 0.0F;
					}

					public double getDouble(String arg0) {
						return 0.0D;
					}

					public String getDefaultString(String arg0) {
						return null;
					}

					public long getDefaultLong(String arg0) {
						return 0L;
					}

					public int getDefaultInt(String arg0) {
						return 0;
					}

					public float getDefaultFloat(String arg0) {
						return 0.0F;
					}

					public double getDefaultDouble(String arg0) {
						return 0.0D;
					}

					public boolean getDefaultBoolean(String arg0) {
						return false;
					}

					public boolean getBoolean(String arg0) {
						return false;
					}

					public void firePropertyChangeEvent(String arg0, Object arg1, Object arg2) {
					}

					public boolean contains(String arg0) {
						return false;
					}

					public void addPropertyChangeListener(IPropertyChangeListener arg0) {
					}
				}, "tquestion");
		return t;
	}

	protected String input(String title, String message, String inival) {
		title = "Sexftp - " + LangUtil.langText(title);
		message = LangUtil.langText(message);
		InputDialog input = new InputDialog(this.viewer.getControl().getShell(), title, message, inival, null);

		int r = input.open();
		if (r == 1) {
			throw new AbortException("Cancel Input");
		}
		return input.getValue();
	}

	protected void initConsole() {
		if (this.console != null)
			return;
		this.console = Console.createConsole("SexFtpConsole", "Twitter bird.ico");
		try {
			this.console.console(
					"Welcome to Sexftp - " + Platform.asLocalURL(Platform.getBundle("sexftp").getEntry("")).getFile());
		} catch (IOException e) {
			this.console.console("Get SexFtp Bundle Failed!" + e.toString());
		}
	}

	protected void openConsole() {
		initConsole();

		this.console.openConsole();
	}

	public void console(String text) {
		initConsole();

		this.console.console(text);
	}

	public void handleException(final Throwable e) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (e instanceof BizException) {
					AbstractSexftpView.this.showError(e.getMessage());
					LogUtil.error(e.getMessage(), e);
				} else if (e instanceof AbortException) {
					if (e.getMessage() == null)
						return;
					AbstractSexftpView.this.console(e.getMessage());
				} else {
					AbstractSexftpView.this.openConsole();
					AbstractSexftpView.this.console(e.getMessage());
					LogUtil.error(e.getMessage(), e);
					AbstractSexftpView.this.showError(e.getMessage(), e);
				}
			}
		});
	}

	public void directTo(final String expandPath, final Integer ftpUploadTreeNodesIndex) {
	}
	
	public void directToAction(final String expandClientPath, final String actionName) {
	}
	
	public TreeViewer getViewer() {
		return this.viewer;
	}

	public TreeParent getRoot() {
		return this.invisibleRoot;
	}

	public void setFocus() {
		this.viewer.getControl().setFocus();
	}

	public static void main(String[] args) {
		System.out.println(HtmlEncoder.encode("&"));
	}

	class NameSorter extends ViewerSorter {
		NameSorter() {
		}

		public int compare(Viewer viewer, Object e1, Object e2) {
			int c = 0;
			c = (e2 instanceof AbstractSexftpView.TreeParent) ? c + 1 : c - 1;
			c = (e1 instanceof AbstractSexftpView.TreeParent) ? c - 1 : c + 1;
			if (c != 0)
				return c;
			if ((e1 instanceof AbstractSexftpView.TreeParent)
					&& (((AbstractSexftpView.TreeParent) e1).getO() instanceof String)
					&& (((AbstractSexftpView.TreeParent) e1).getO().toString().startsWith("Projects"))) {
				return -1;
			}
			if ((e2 instanceof AbstractSexftpView.TreeParent)
					&& (((AbstractSexftpView.TreeParent) e2).getO() instanceof String)
					&& (((AbstractSexftpView.TreeParent) e2).getO().toString().startsWith("Projects"))) {
				return 1;
			}

			return super.compare(viewer, e1, e2);
		}
	}

	public class TreeObject implements IAdaptable {
		private String name;
		private AbstractSexftpView.TreeParent parent;
		private Object o;
		private boolean visible = true;

		public TreeObject(String name, Object o) {
			this.name = name;
			this.o = o;
		}

		public String getName() {
			return this.name;
		}

		public void setParent(AbstractSexftpView.TreeParent parent) {
			this.parent = parent;
		}

		public AbstractSexftpView.TreeParent getParent() {
			return this.parent;
		}

		public String toString() {
			return getName();
		}

		public Object getO() {
			return this.o;
		}

		public void setO(Object o) {
			this.o = o;
		}

		public boolean isVisible() {
			return this.visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		public <T> T getAdapter(Class<T> arg0) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public class TreeParent extends AbstractSexftpView.TreeObject {
		private Object o;
		private ArrayList<AbstractSexftpView.TreeObject> children;

		public TreeParent(String name, Object o) {
			super(name, o);
			this.o = o;
			this.children = new ArrayList<TreeObject>();
		}

		public void addChild(AbstractSexftpView.TreeObject child) {
			this.children.add(child);
			child.setParent(this);
		}

		public void removeChild(AbstractSexftpView.TreeObject child) {
			this.children.remove(child);
			child.setParent(null);
		}

		public void removeAll() {
			for (AbstractSexftpView.TreeObject child : this.children) {
				child.setParent(null);
			}
			this.children.clear();
		}

		public AbstractSexftpView.TreeObject[] getChildren() {
			return (AbstractSexftpView.TreeObject[]) this.children
					.toArray(new AbstractSexftpView.TreeObject[this.children.size()]);
		}

		public boolean hasChildren() {
			return this.children.size() > 0;
		}

		public Object getO() {
			return this.o;
		}

		public void setO(Object o) {
			this.o = o;
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		ViewContentProvider() {
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent.equals(AbstractSexftpView.this.getViewSite())) {
				if (AbstractSexftpView.this.invisibleRoot == null)
					initialize();
				return getChildren(AbstractSexftpView.this.invisibleRoot);
			}
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof AbstractSexftpView.TreeObject) {
				return ((AbstractSexftpView.TreeObject) child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof AbstractSexftpView.TreeParent) {
				AbstractSexftpView.TreeObject[] children = ((AbstractSexftpView.TreeParent) parent).getChildren();
				List<TreeObject> childList = new ArrayList<TreeObject>();
				for (AbstractSexftpView.TreeObject c : children) {
					if (!c.isVisible())
						continue;
					childList.add(c);
				}

				return childList.toArray(new AbstractSexftpView.TreeObject[0]);
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			return parent instanceof AbstractSexftpView.TreeParent;
		}

		protected void initialize() {
			try {
				String p = Platform.asLocalURL(Platform.getBundle("sexftp").getEntry("")).getFile();
				System.out.println(p);
			} catch (Exception localException) {
			}

			AbstractSexftpView.workspacePath = Platform.getInstanceLocation().getURL().getFile() + "/.sexftp10/";
			AbstractSexftpView.workspaceWkPath = AbstractSexftpView.workspacePath + "/.work/";
			if (!new File(AbstractSexftpView.workspaceWkPath).exists()) {
				new File(AbstractSexftpView.workspaceWkPath).mkdirs();
			}

			AbstractSexftpView.this.refreshTreeViewData();
		}
	}

	class ViewLabelProvider extends LabelProvider implements IFontProvider, IColorProvider {
		ViewLabelProvider() {
		}

		public String getText(Object obj) {
			return obj.toString();
		}

		public Font getFont(Object obj) {
			return null;
		}

		public Color getBackground(Object obj) {
			return null;
		}

		public Color getForeground(Object obj) {
			return null;
		}

		public Image getImage(Object obj) {
			String sexFtpIcon = null;
			String imageKey = "IMG_OBJ_ELEMENTS";
			if (AbstractSexftpView.this instanceof SexftpServerView) {
				sexFtpIcon = "javaassist_co.gif";
			}
			if (obj instanceof AbstractSexftpView.TreeParent) {
				imageKey = "IMG_OBJ_FOLDER";
				if (AbstractSexftpView.this instanceof SexftpServerView) {
					sexFtpIcon = "cprj_obj.gif";
				}
			}

			if (obj instanceof AbstractSexftpView.TreeObject) {
				AbstractSexftpView.TreeObject treeObj = (AbstractSexftpView.TreeObject) obj;
				if ((treeObj.getO() == null) && (treeObj.getName().equals("Sexftp Start"))) {
					sexFtpIcon = "Twitter_bird.ico";
				} else if (treeObj.getO() instanceof FtpConf) {
					sexFtpIcon = "Duckling.ico";
				} else if (treeObj.getO() instanceof FtpUploadConf) {
					sexFtpIcon = "Online_writing.ico";
					if (AbstractSexftpView.this instanceof SexftpServerView) {
						sexFtpIcon = "repository_rep.gif";
					} else if (AbstractSexftpView.this instanceof SexftpSyncView) {
						sexFtpIcon = "compare.gif";
					}

				} else if (treeObj.getO() instanceof IProject) {
					imageKey = "IMG_OBJ_PROJECT";
					sexFtpIcon = null;
				} else if (treeObj.getO() instanceof String) {
					if (treeObj.getO().toString().startsWith("Projects")) {
						sexFtpIcon = "Follow_me.ico";
					}
				} else if (treeObj.getO() instanceof FtpUploadPro) {
					String clientPath = ((FtpUploadPro) treeObj.getO()).getFtpUploadConf().getClientPath();
					if (AbstractSexftpView.this.customizedImgMap.containsKey(clientPath)) {
						sexFtpIcon = (String) AbstractSexftpView.this.customizedImgMap.get(clientPath);
					}
				}
			}

			if (sexFtpIcon != null) {
//				System.out.println(sexFtpIcon);
				return AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/" + sexFtpIcon).createImage();
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}
}
