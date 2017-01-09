package sexftp.views.savelisteners;

import java.io.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.sexftp.core.ftp.FileMd5;
import org.sexftp.core.ftp.FtpPools;
import org.sexftp.core.ftp.XFtp;
import org.sexftp.core.ftp.bean.FtpUploadConf;
import org.sexftp.core.utils.FileUtil;
import org.sexftp.core.utils.StringUtil;
import sexftp.editors.viewlis.IDoSaveListener;
import sexftp.views.AbstractSexftpView;
import sexftp.views.IFtpStreamMonitor;

public class ServerInnnerEditSaveListener implements IDoSaveListener {
	private IFile ifile;
	private String serverDirPath;
	private String serverFileName;
	private AbstractSexftpView srcview;
	private FtpPools ftppool;
	private String md5 = "";
	
	public ServerInnnerEditSaveListener(IFile ifile, String serverDirPath, String serverFileName,
			AbstractSexftpView srcview, FtpPools ftppool) {
		this.ifile = ifile;
		this.serverDirPath = serverDirPath;
		this.serverFileName = serverFileName;
		this.srcview = srcview;
		this.ftppool = ftppool;
		this.md5 = FileMd5.getMD5(ifile.getLocation().toFile());
	}

	public synchronized void dispose() {
		File file = this.ifile.getLocation().toFile();
		if (!FileMd5.getMD5(file).equals(this.md5)) {
			oksave();
		}
		deleteTmpFolder(file.getParentFile().getParentFile());
	}

	public synchronized void dosave() {
		oksave();
	}

	private void deleteTmpFolder(File folder) {
		for (File subfile : folder.listFiles()) {
			Long l = new Long(subfile.getName());
			if (System.currentTimeMillis() - l.longValue() <= 18000000L)
				continue;
			for (File subsubfile : subfile.listFiles()) {
				subsubfile.delete();
			}
			subfile.delete();
		}
	}

	private void oksave() {
		if (!this.srcview.showQuestion("Upload Current Edit File [" + this.serverFileName + "] To Server?")) {
			return;
		}
		Job job = new Job("uploading") {
			protected IStatus run(IProgressMonitor mon) {
				final IProgressMonitor monitor = mon;
				monitor.beginTask("uploading..", -1);
				XFtp ftp = ServerInnnerEditSaveListener.this.ftppool.getFtp();
				synchronized (ftp) {
					ServerInnnerEditSaveListener.this.ftppool.getConnectedFtp();
					ftp.cd(ServerInnnerEditSaveListener.this.serverDirPath);

					String uploadFile = ServerInnnerEditSaveListener.this.ifile.getLocation().toFile().getParent() + "/"
							+ ServerInnnerEditSaveListener.this.serverFileName;
					if (!ServerInnnerEditSaveListener.this.ifile.getLocation().toFile().getName()
							.equals(ServerInnnerEditSaveListener.this.serverFileName)) {
						FileUtil.copyFile(
								ServerInnnerEditSaveListener.this.ifile.getLocation().toFile().getAbsolutePath(),
								uploadFile);
					}

					ftp.upload(uploadFile, new IFtpStreamMonitor() {
						public void printStreamString(FtpUploadConf ftpUploadConf, long uploadedSize, long totalSize,
								String info) {
							monitor.subTask("uploaded:" + StringUtil.getHumanSize(uploadedSize));
						}

						public void printSimple(String info) {
							ServerInnnerEditSaveListener.this.srcview.console(info);
						}
					});
				}
				ServerInnnerEditSaveListener.this.md5 = FileMd5
						.getMD5(ServerInnnerEditSaveListener.this.ifile.getLocation().toFile());

				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}
}