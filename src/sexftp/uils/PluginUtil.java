package sexftp.uils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.sexftp.core.exceptions.SRuntimeException;
import org.sexftp.core.utils.FileUtil;
import sexftp.Activator;
import sexftp.editors.inner.SexftpCompareEditor;
import sexftp.views.SexftpLocalView;
import sexftp.views.SexftpServerView;
import sexftp.views.SexftpSyncView;

public class PluginUtil {
	public static IWorkbenchPage getActivePage() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return null;
		IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null)
			return null;
		return activeWorkbenchWindow.getActivePage();
	}

	public static IProject[] getAllOpenedProjects() {
		List<IProject> proejctList = new ArrayList<IProject>();
		IProject[] iprojects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject iProject : iprojects) {
			if ((!iProject.isOpen()) || (iProject.getName().startsWith(".sexftp")))
				continue;
			proejctList.add(iProject);
		}

		return (IProject[]) proejctList.toArray(new IProject[0]);
	}

	public static IProject[] getAllProjects() {
		List<IProject> proejctList = new ArrayList<IProject>();
		IProject[] iprojects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject iProject : iprojects) {
			if (iProject.getName().startsWith(".sexftp"))
				continue;
			proejctList.add(iProject);
		}

		return (IProject[]) proejctList.toArray(new IProject[0]);
	}

	public static String getProjectRealPath(IProject project) {
		return project.getFile("/a.txt").getLocation().toFile().getParent();
	}

	public static IProject getOneOpenedProject() {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(".sexftpwkproject");
			if (!project.exists()) {
				project.create(null);
			}
			if (!project.isOpen())
				project.open(null);
			return project;
		} catch (Exception e) {
			throw new SRuntimeException(e);
		}
	}

	public static IFile createSexftpIFileFromPath(String filePath) {
		IFile file = null;
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(".sexftpwkproject");
			if (!project.exists()) {
				project.create(null);
			}
			if (!project.isOpen())
				project.open(null);
			file = project.getFile(filePath);
			File f = file.getLocation().toFile();
			if (!f.getParentFile().exists()) {
				f.getParentFile();
			}
			file.refreshLocal(1, null);
		} catch (Exception e) {
			throw new SRuntimeException(e);
		}
		return file;
	}

	public static IFile rename(IFile ifile, String newfilename) {
		File file = ifile.getLocation().toFile();
		File newfile = new File(file.getParent() + "/" + newfilename);

		FileUtil.copyFile(file.getAbsolutePath(), newfile.getAbsolutePath());
		String oldfilepath = ifile.getFullPath().toString();
		oldfilepath = oldfilepath.substring(0, oldfilepath.lastIndexOf("/"));
		oldfilepath = oldfilepath.substring(oldfilepath.indexOf("/", 1));
		IFile newifile = ifile.getProject().getFile(oldfilepath + "/" + newfilename);
		newifile.getLocation().toFile();
		try {
			ifile.getProject().getFile(oldfilepath + "/").refreshLocal(1, null);
			newifile.refreshLocal(1, null);
		} catch (CoreException e) {
			throw new SRuntimeException(e);
		}
		return newifile;
	}

	public static void openCompareEditor(final IWorkbenchPage actPage, final String leftContents, final String rightContents,
			final String descContent) {
		final CompareEditorInput input = new CompareEditorInput(new CompareConfiguration()) {
			protected Object prepareInput(IProgressMonitor arg0)
					throws InvocationTargetException, InterruptedException {
				CompareItem left = new CompareItem("Left Title", leftContents);
				CompareItem right = new CompareItem("Right Title", rightContents);
				DiffNode diffNode = new DiffNode(left, right);
				diffNode.getName();
				return diffNode;
			}
		};
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					SexftpCompareEditor c = (SexftpCompareEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().openEditor(input, "sexftp.editors.inner.SexftpCompareEditor");
					c.setContentDescription(descContent);
				} catch (PartInitException e) {
					throw new SRuntimeException(e);
				}
			}
		});
	}

	public static SexftpLocalView findLocalView(final IWorkbenchPage actPage) throws PartInitException {
		return (SexftpLocalView) runAsDisplayThread(new RunAsDisplayThread() {
			public Object run() throws Exception {
				SexftpLocalView find = (SexftpLocalView) actPage.findView("sexftp.views.MainView");
				if (find == null) {
					actPage.showView("sexftp.views.MainView");

					find = (SexftpLocalView) actPage.findView("sexftp.views.MainView");
				}

				return find;
			}
		});
	}

	public static SexftpLocalView findAndShowLocalView(final IWorkbenchPage actPage) throws PartInitException {
		return (SexftpLocalView) runAsDisplayThread(new RunAsDisplayThread() {
			public Object run() throws Exception {
				actPage.showView("sexftp.views.MainView");

				return (SexftpLocalView) actPage.findView("sexftp.views.MainView");
			}
		});
	}

	public static SexftpSyncView findAndShowSyncView(final IWorkbenchPage actPage) throws PartInitException {
		actPage.showView("sexftp.views.SexftpSyncView");

		return (SexftpSyncView) actPage.findView("sexftp.views.SexftpSyncView");
	}

	public static boolean isSyncViewShowed(final IWorkbenchPage actPage) throws PartInitException {
		return ((Boolean) runAsDisplayThread(new RunAsDisplayThread() {
			public Object run() throws Exception {
				if (actPage.findView("sexftp.views.SexftpSyncView") != null)
					return Boolean.valueOf(true);
				return Boolean.valueOf(false);
			}
		})).booleanValue();
	}

	public static Object runAsDisplayThread(final RunAsDisplayThread yourun) {
		Runnable runnable = new Runnable() {
			private Object q;
			private Throwable e;

			public void run() {
				try {
					this.q = yourun.run();
				} catch (Throwable e) {
					this.e = e;
				}
			}

			@SuppressWarnings("unused")
			public Object getObj() {
				return this.q;
			}

			@SuppressWarnings("unused")
			public Throwable getE() {
				return this.e;
			}
		};
		Display.getDefault().syncExec(runnable);
		try {
			Throwable e = (Throwable) runnable.getClass().getMethod("getE", new Class[0]).invoke(runnable,
					new Object[0]);
			if (e != null) {
				if (e instanceof RuntimeException) {
					throw ((RuntimeException) e);
				}
				throw new SRuntimeException(e);
			}
			return runnable.getClass().getMethod("getObj", new Class[0]).invoke(runnable, new Object[0]);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new SRuntimeException(e);
		}
	}

	public static SexftpServerView findServerView(final IWorkbenchPage actPage) throws PartInitException {
		return (SexftpServerView) runAsDisplayThread(new RunAsDisplayThread() {
			public Object run() throws Exception {
				SexftpServerView find = (SexftpServerView) actPage.findView("sexftp.views.ServerView");
				if (find == null) {
					actPage.showView("sexftp.views.ServerView");
					find = (SexftpServerView) actPage.findView("sexftp.views.ServerView");
				}
				return find;
			}
		});
	}

	public static SexftpServerView findAndShowServerView(final IWorkbenchPage actPage) throws PartInitException {
		return (SexftpServerView) runAsDisplayThread(new RunAsDisplayThread() {
			public Object run() throws Exception {
				actPage.showView("sexftp.views.ServerView");

				return (SexftpServerView) actPage.findView("sexftp.views.ServerView");
			}
		});
	}

	public static String getLanguage() {
		return getPreferenceStore("choicePreference");
	}

	public static Boolean overwriteTips() {
		String over = getPreferenceStore("booleanPreference");
		return Boolean.valueOf((over != null) ? Boolean.valueOf(over).booleanValue() : true);
	}

	public static int getServerTimeout() {
		String over = getPreferenceStore("inttimeout");
		return (over != null) ? Integer.parseInt(over) : 10000;
	}

	public static String getPreferenceStore(String key) {
		if (Activator.getDefault() == null)
			return null;
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (store == null)
			return null;
		return store.getString(key);
	}

	public static abstract interface RunAsDisplayThread {
		public abstract Object run() throws Exception;
	}
}
