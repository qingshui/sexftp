package sexftp.views;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.sexftp.core.exceptions.AbortException;
import org.sexftp.core.exceptions.BizException;
import org.sexftp.core.exceptions.SRuntimeException;
import org.sexftp.core.utils.Cpdetector;
import org.sexftp.core.utils.StringUtil;
import sexftp.SrcViewable;
import sexftp.uils.Console;
import sexftp.uils.LangUtil;
import sexftp.uils.LogUtil;
import sexftp.uils.PluginUtil;

@SuppressWarnings("deprecation")
public class AbstractSexftpEncodView extends ViewPart implements SrcViewable {
	public static final String ID = "sexftp.views.SexftpEncodView";
	private static final String[] SUPORT_CHARSET = Cpdetector.SUPORT_CHARSET;
	protected TreeViewer viewer;
	// private DrillDownAdapter drillDownAdapter;
	protected Action action1;
	protected Action applyChanges;
	private Action filedillAction;
	protected Action doubleClickAction;
	protected Action actionCopy;
	protected Action actionCopyQualifiedName;
	protected ParentCorCod invisibleRoot;
	protected List<ParentCorCod> corcodeList = new ArrayList<ParentCorCod>();

	private IWorkbenchPage actPage = null;

	public void createPartControl(Composite parent) {
		this.viewer = new TreeViewer(parent, 66306);
		// this.drillDownAdapter = new DrillDownAdapter(this.viewer);
		this.viewer.setContentProvider(new ViewContentProvider());
		this.viewer.setLabelProvider(new ViewLabelProvider());
		this.viewer.setSorter(new NameSorter());
		this.viewer.setInput(getViewSite());

		Tree tree = this.viewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		TableLayout tLayout = new TableLayout();
		tree.setLayout(tLayout);

		tLayout.addColumnData(new ColumnWeightData(50));
		new TreeColumn(tree, 0).setText(LangUtil.langText("Folder"));
		tLayout.addColumnData(new ColumnWeightData(15));
		new TreeColumn(tree, 0).setText(LangUtil.langText("File association"));
		tLayout.addColumnData(new ColumnWeightData(15));
		new TreeColumn(tree, 0).setText(LangUtil.langText("File Encoding"));
		tLayout.addColumnData(new ColumnWeightData(15));
		new TreeColumn(tree, 0).setText(LangUtil.langText("File New Encoding"));
		tLayout.addColumnData(new ColumnWeightData(40));
		new TreeColumn(tree, 0).setText(LangUtil.langText("Description"));
		tLayout.addColumnData(new ColumnWeightData(50));
		new TreeColumn(tree, 0).setText(LangUtil.langText("Samples"));

		TreeViewerEditor.create(this.viewer, new ColumnViewerEditorActivationStrategy(this.viewer), 1);
		CellEditor[] cellEditors = new CellEditor[5];

		ComboBoxCellEditor ce = new ComboBoxCellEditor(tree, SUPORT_CHARSET);
		cellEditors[2] = ce;
		cellEditors[3] = ce;
		ce.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				e.getOldValue();
			}
		});
		this.viewer.setCellEditors(cellEditors);
		this.viewer.setColumnProperties(new String[] { "00", "01", "02", "03", "04", "07" });
		this.viewer.setCellModifier(new ICellModifier() {
			public void modify(Object arg0, String p, Object val) {
				try {
					TreeItem ti = (TreeItem) arg0;
					AbstractSexftpEncodView.CorCod c = (AbstractSexftpEncodView.CorCod) ti.getData();
					if (p.equals("01")) {
						c.setEndexten((String) val);
						ti.setText(1, (String) val);
					}
					if (p.equals("02")) {
						CCombo cc = (CCombo) AbstractSexftpEncodView.this.viewer.getCellEditors()[2].getControl();
						"test".getBytes(cc.getText());
						AbstractSexftpEncodView.this.oldEncodeChanged(c, c.getOldfileencode(), cc.getText());

						c.setOldfileencode(cc.getText());

						ti.setText(2, cc.getText());
						if (!c.getOldfileencode().equalsIgnoreCase(c.getFileencode())) {
							c.setChengeDes(String.format("Change: [%s -> %s]",
									new Object[] { c.getOldfileencode(), c.getFileencode() }));
							c.setDescIcon("run_exc.gif");
							ti.setText(4, c.getChengeDes());
						} else if ((ti.getText(4) != null) && (ti.getText(4).indexOf(" -> ") >= 0)) {
							c.setChengeDes("");
							ti.setText(4, "");
							c.setDescIcon("");
						}
					}
					if (p.equals("03")) {
						CCombo cc = (CCombo) AbstractSexftpEncodView.this.viewer.getCellEditors()[2].getControl();
						"test".getBytes(cc.getText());
						c.setFileencode(cc.getText());
						ti.setText(3, cc.getText());
						if (!c.getOldfileencode().equalsIgnoreCase(c.getFileencode())) {
							c.setChengeDes(String.format("Change: [%s -> %s]",
									new Object[] { c.getOldfileencode(), c.getFileencode() }));
							c.setDescIcon("run_exc.gif");
							ti.setText(4, c.getChengeDes());
						} else if ((ti.getText(4) != null) && (ti.getText(4).indexOf(" -> ") >= 0)) {
							c.setChengeDes("");

							ti.setText(4, "");
							c.setDescIcon("");
						}
					}
				} catch (UnsupportedEncodingException localUnsupportedEncodingException) {
				}
				AbstractSexftpEncodView.this.viewer.refresh(true);
			}

			public Object getValue(Object arg0, String p) {
				AbstractSexftpEncodView.CorCod c = (AbstractSexftpEncodView.CorCod) arg0;

				if (p.equals("01")) {
					return (c);
				}
				if (p.equals("02")) {
					CCombo cc = (CCombo) AbstractSexftpEncodView.this.viewer.getCellEditors()[2].getControl();
					for (int i = 0; i < cc.getItemCount(); ++i) {
						String item = cc.getItem(i);
						if (item.equals(c.getOldfileencode())) {
							return Integer.valueOf(i);
						}
					}
					return Integer.valueOf(0);
				}
				if (p.equals("03")) {
					CCombo cc = (CCombo) AbstractSexftpEncodView.this.viewer.getCellEditors()[3].getControl();
					for (int i = 0; i < cc.getItemCount(); ++i) {
						String item = cc.getItem(i);
						if (item.equals(c.getFileencode())) {
							return Integer.valueOf(i);
						}
					}
					return Integer.valueOf(0);
				}

				return p;
			}

			public boolean canModify(Object arg0, String arg1) {
				AbstractSexftpEncodView.CorCod c = (AbstractSexftpEncodView.CorCod) arg0;
				if (c.getFileencode().indexOf("ASCII") >= 0) {
					return false;
				}

				return c instanceof AbstractSexftpEncodView.ParentCorCod;
			}
		});
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.viewer.getControl(), "sexftp.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	protected void oldEncodeChanged(CorCod c, String encode, String newencode) {
	}

	public void refreshData(final List<ParentCorCod> corcodeList) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				AbstractSexftpEncodView.this.corcodeList = corcodeList;
				AbstractSexftpEncodView.this.invisibleRoot = new AbstractSexftpEncodView.ParentCorCod("");
				for (AbstractSexftpEncodView.ParentCorCod p : corcodeList) {
					AbstractSexftpEncodView.this.invisibleRoot.addChild(p);
				}

				AbstractSexftpEncodView.this.viewer.refresh();
			}
		});
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				AbstractSexftpEncodView.this.fillContextMenu(manager);
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

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(this.filedillAction);
		manager.add(new Separator());
		manager.add(this.applyChanges);
		manager.add(new Separator());
		manager.add(this.actionCopy);
		manager.add(this.actionCopyQualifiedName);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(this.filedillAction);
		manager.add(new Separator());
		manager.add(this.applyChanges);
		manager.add(new Separator());
		manager.add(this.actionCopy);
		manager.add(this.actionCopyQualifiedName);

		manager.add(new Separator("additions"));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(this.filedillAction);
		manager.add(new Separator());
		manager.add(this.applyChanges);
		manager.add(new Separator());
		manager.add(this.actionCopy);
		manager.add(this.actionCopyQualifiedName);
	}

	private void makeActions() {
		this.actionCopy = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpEncodView.this.actionCopy_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpEncodView.this.handleException(e);
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
					AbstractSexftpEncodView.this.actionCopyQualifiedName_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpEncodView.this.handleException(e);
				}
			}
		};
		this.actionCopyQualifiedName.setText("Copy Qualified Name");
		this.actionCopyQualifiedName.setToolTipText("Copy Qualified Name");
		this.actionCopyQualifiedName
				.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_TOOL_COPY"));

		this.filedillAction = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpEncodView.this.filedillAction_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpEncodView.this.handleException(e);
				}
			}
		};
		this.filedillAction.setText("Chose Your Folder (&V)");
		this.filedillAction.setToolTipText("Chose Your Folder");
		this.filedillAction
				.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_OBJ_FOLDER"));

		this.applyChanges = new SexftpViewAction() {
			public void run() {
				try {
					AbstractSexftpEncodView.this.applyChanges_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpEncodView.this.handleException(e);
				}
			}
		};
		this.applyChanges.setText("Apply Changes(&A)");
		this.applyChanges.setToolTipText("Apply Changes");
		this.applyChanges
				.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/run_exc.gif"));
		this.doubleClickAction = new Action() {
			public void run() {
				try {
					AbstractSexftpEncodView.this.doubleClick_actionPerformed();
				} catch (Exception e) {
					AbstractSexftpEncodView.this.handleException(e);
				}
			}
		};
	}

	protected void doubleClick_actionPerformed() throws Exception {
	}

	protected void applyChanges_actionPerformed() throws Exception {
	}

	protected void filedillAction_actionPerformed() throws Exception {
	}

	protected void actionCopyQualifiedName_actionPerformed() throws Exception {
	}

	protected void actionCopy_actionPerformed() throws Exception {
	}

	private void hookDoubleClickAction() {
		this.viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				AbstractSexftpEncodView.this.doubleClickAction.run();
			}
		});
	}

	protected void showMessage(final String message) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(AbstractSexftpEncodView.this.viewer.getControl().getShell(),
						LangUtil.langText("Sexftp Charset Encoder"), LangUtil.langText(message));
			}
		});
	}

	public void setFocus() {
		this.viewer.getControl().setFocus();
	}

	public void console(final String str) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				Console.createConsole("SexFtpConsole", "Twitter bird.ico").console(str);
			}
		});
	}

	protected IWorkbenchPage getActPage() {
		if (this.actPage == null) {
			IWorkbenchPage activePage = PluginUtil.getActivePage();
			this.actPage = activePage;
		}
		return this.actPage;
	}

	public boolean showQuestion(final String message) {
		Runnable runnable = new Runnable() {
			private boolean isok = false;

			public void run() {
				isok = MessageDialog.openQuestion(AbstractSexftpEncodView.this.viewer.getControl().getShell(),
						LangUtil.langText("Sexftp Charset Encoder"), LangUtil.langText(message));
			}

			@SuppressWarnings("unused")
			public boolean isOk() {
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

	public void handleException(final Throwable e) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (e instanceof BizException) {
					AbstractSexftpEncodView.this.showMessage(e.getMessage());
				} else {
					if (e instanceof AbortException) {
						return;
					}

					AbstractSexftpEncodView.this.console(StringUtil.readExceptionDetailInfo(e));
					LogUtil.error(e.getMessage(), e);
				}
			}
		});
	}

	public class CorCod implements IAdaptable, Comparable<CorCod> {
		private String name;
		private AbstractSexftpEncodView.ParentCorCod parent;
		private String parentFolder = "";
		private String endexten = "*.jsp";
		private String oldfileencode = "gbk";
		private String fileencode = "gbk";
		private String eclipseeditorencode = "utf-8";
		private String chengeDes = "";
		private String samples = "";
		private String fileType = "";
		private String descIcon = "";

		public String getEndexten() {
			return this.endexten;
		}

		public void setEndexten(String endexten) {
			this.endexten = endexten;
		}

		public String getFileencode() {
			return this.fileencode;
		}

		public void setFileencode(String fileencode) {
			this.fileencode = fileencode;
		}

		public String getOldfileencode() {
			return this.oldfileencode;
		}

		public void setOldfileencode(String oldfileencode) {
			this.oldfileencode = oldfileencode;
		}

		public String getEclipseeditorencode() {
			return this.eclipseeditorencode;
		}

		public void setEclipseeditorencode(String eclipseeditorencode) {
			this.eclipseeditorencode = eclipseeditorencode;
		}

		public String getChengeDes() {
			return this.chengeDes;
		}

		public void setChengeDes(String chengeDes) {
			this.chengeDes = chengeDes;
		}

		public String getParentFolder() {
			return this.parentFolder;
		}

		public void setParentFolder(String parentFolder) {
			this.parentFolder = parentFolder;
		}

		public String getFileType() {
			return this.fileType;
		}

		public void setFileType(String fileType) {
			this.fileType = fileType;
		}

		public String getFileencodeText() {
			return getParent().getFileencode();
		}

		public String getEndextenText() {
			return new File(getEndexten()).getName();
		}

		public String getOldfileencodeText() {
			return getParent().getOldfileencode();
		}

		public String getParentFolderText() {
			return new File(getEndexten()).getParent();
		}

		public String getSamples() {
			return this.samples;
		}

		public void setSamples(String samples) {
			this.samples = samples;
		}

		public String getDescIcon() {
			return this.descIcon;
		}

		public void setDescIcon(String descIcon) {
			this.descIcon = descIcon;
		}

		public int hashCode() {
			int result = 1;
			result = 31 * result + getOuterType().hashCode();
			result = 31 * result + ((this.chengeDes == null) ? 0 : this.chengeDes.hashCode());
			result = 31 * result + ((this.eclipseeditorencode == null) ? 0 : this.eclipseeditorencode.hashCode());
			result = 31 * result + ((this.endexten == null) ? 0 : this.endexten.hashCode());
			result = 31 * result + ((this.fileType == null) ? 0 : this.fileType.hashCode());
			result = 31 * result + ((this.fileencode == null) ? 0 : this.fileencode.hashCode());
			result = 31 * result + ((this.oldfileencode == null) ? 0 : this.oldfileencode.hashCode());
			result = 31 * result + ((this.parentFolder == null) ? 0 : this.parentFolder.hashCode());
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (super.getClass() != obj.getClass())
				return false;
			CorCod other = (CorCod) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (this.chengeDes == null)
				if (other.chengeDes != null)
					return false;
				else if (!this.chengeDes.equals(other.chengeDes))
					return false;
			if (this.eclipseeditorencode == null)
				if (other.eclipseeditorencode != null)
					return false;
				else if (!this.eclipseeditorencode.equals(other.eclipseeditorencode))
					return false;
			if (this.endexten == null)
				if (other.endexten != null)
					return false;
				else if (!this.endexten.equals(other.endexten))
					return false;
			if (this.fileType == null)
				if (other.fileType != null)
					return false;
				else if (!this.fileType.equals(other.fileType))
					return false;
			if (this.fileencode == null)
				if (other.fileencode != null)
					return false;
				else if (!this.fileencode.equals(other.fileencode))
					return false;
			if (this.oldfileencode == null)
				if (other.oldfileencode != null)
					return false;
				else if (!this.oldfileencode.equals(other.oldfileencode))
					return false;
			if (this.parentFolder == null)
				if (other.parentFolder != null)
					return false;
				else if (!this.parentFolder.equals(other.parentFolder))
					return false;
			return true;
		}

		public int compareTo(CorCod o) {
			return getEndexten().compareTo(o.getEndexten());
		}

		private AbstractSexftpEncodView getOuterType() {
			return AbstractSexftpEncodView.this;
		}

		public String toSimpleString(int maxlen) {
			return String.format("%s", new Object[] { this.endexten });
		}

		public CorCod(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public void setParent(AbstractSexftpEncodView.ParentCorCod parent) {
			this.parent = parent;
		}

		public AbstractSexftpEncodView.ParentCorCod getParent() {
			return this.parent;
		}

		public String toString() {
			return String.format("%s\t%s\t%s\t%s\t%s\t%s", new Object[] { this.parentFolder, this.endexten,
					this.oldfileencode, this.fileencode, this.chengeDes, this.samples });
		}

		public <T> T getAdapter(Class<T> arg0) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class NameSorter extends ViewerSorter {
		NameSorter() {
		}
	}

	public class ParentCorCod extends AbstractSexftpEncodView.CorCod {
		private List<AbstractSexftpEncodView.CorCod> children;

		public ParentCorCod(String name) {
			super(name);
			this.children = new ArrayList<CorCod>();
		}

		public void addChild(AbstractSexftpEncodView.CorCod child) {
			this.children.add(child);
			child.setParent(this);
		}

		public void removeChild(AbstractSexftpEncodView.CorCod child) {
			this.children.remove(child);
			child.setParent(null);
		}

		public AbstractSexftpEncodView.CorCod[] getChildren() {
			return (AbstractSexftpEncodView.CorCod[]) this.children
					.toArray(new AbstractSexftpEncodView.CorCod[this.children.size()]);
		}

		public boolean hasChildren() {
			return this.children.size() > 0;
		}

		public String getFileencodeText() {
			return getFileencode();
		}

		public String getEndextenText() {
			return getEndexten();
		}

		public String getOldfileencodeText() {
			return getOldfileencode();
		}

		public String getParentFolderText() {
			return getParentFolder();
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
			if (parent.equals(AbstractSexftpEncodView.this.getViewSite())) {
				if (AbstractSexftpEncodView.this.invisibleRoot == null)
					initialize();
				return getChildren(AbstractSexftpEncodView.this.invisibleRoot);
			}
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof AbstractSexftpEncodView.CorCod) {
				return ((AbstractSexftpEncodView.CorCod) child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof AbstractSexftpEncodView.ParentCorCod) {
				return ((AbstractSexftpEncodView.ParentCorCod) parent).getChildren();
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof AbstractSexftpEncodView.ParentCorCod)
				return ((AbstractSexftpEncodView.ParentCorCod) parent).hasChildren();
			return false;
		}

		private void initialize() {
			AbstractSexftpEncodView.this.invisibleRoot = new AbstractSexftpEncodView.ParentCorCod("Root");
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		ViewLabelProvider() {
		}

		public String getText(Object obj) {
			return obj.toString();
		}

		public Image getImage(Object obj) {
			String imageKey = "IMG_OBJ_ELEMENTS";
			if (obj instanceof AbstractSexftpEncodView.ParentCorCod)
				imageKey = "IMG_OBJ_FOLDER";
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}

		public Image getColumnImage(Object obj, int index) {
			String sexFtpIcon = null;
			if (obj instanceof AbstractSexftpEncodView.ParentCorCod) {
				if (index == 0) {
					sexFtpIcon = "Orange forum.ico";
				}

			} else if (index == 0) {
				sexFtpIcon = "Technorati.ico";
			}

			if (index == 4) {
				sexFtpIcon = ((AbstractSexftpEncodView.CorCod) obj).getDescIcon();
			}
			if ((sexFtpIcon != null) && (sexFtpIcon.length() > 0))
				return AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/" + sexFtpIcon).createImage();
			return null;
		}

		public String getColumnText(Object obj, int index) {
			AbstractSexftpEncodView.CorCod c = (AbstractSexftpEncodView.CorCod) obj;
			if (index == 0)
				return getText(c.getParentFolderText());
			if (index == 1)
				return getText(c.getEndextenText());
			if (index == 2) {
				return getText(c.getOldfileencodeText());
			}
			if (index == 3) {
				return getText(c.getFileencodeText());
			}
			if (index == 4)
				return getText(LangUtil.langText(c.getChengeDes()));
			if (index == 5)
				return getText(c.getSamples());
			return getText(obj);
		}
	}
}