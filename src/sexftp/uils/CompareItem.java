package sexftp.uils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

public class CompareItem implements IStreamContentAccessor, ITypedElement, IModificationDate {
	private String contents;
	private String name;
	private long time;

	public CompareItem(String name, String contents, long time) {
		this.name = name;
		this.contents = contents;
		this.time = time;
	}

	public CompareItem(String name, String contents) {
		this.name = name;
		this.contents = contents;
		this.time = new Date().getTime();
	}

	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(this.contents.getBytes());
	}

	public Image getImage() {
		return null;
	}

	public long getModificationDate() {
		return this.time;
	}

	public String getName() {
		return this.name;
	}

	public String getString() {
		return this.contents;
	}

	public String getType() {
		return "txt";
	}

}
