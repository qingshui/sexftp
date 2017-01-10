package sexftp;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.sexftp.core.utils.FileUtil;

public class PathEditorInput implements IPathEditorInput, IPersistableElement {
	private IPath path;

	public PathEditorInput(IPath path) {
		this.path = path;
	}

	public IPath getPath() {
		return this.path;
	}

	public boolean exists() {
		return this.path.toFile().exists();
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return this.path.toString();
	}

	public IPersistableElement getPersistable() {
		return this;
	}

	public String getToolTipText() {
		return this.path.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		return null;
	}

	public boolean equals(Object obj) {
		if ((obj instanceof PathEditorInput) && (this.path != null)) {
			return this.path.equals(((PathEditorInput) obj).getPath());
		}
		return super.equals(obj);
	}

	public String getFactoryId() {
		return null;
	}

	public void saveState(IMemento arg0) {
	}

	public static void main(String[] args) throws Exception {
		String str = "你乱勃l";
		byte[] b = str.getBytes("utf-8");
		FileUtil.writeByte2File("d:/out.ff", b);
		String ns = new String(b, "gbk");
		System.out.println(ns);
	}
}
