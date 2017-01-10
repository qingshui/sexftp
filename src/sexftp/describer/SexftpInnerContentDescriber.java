package sexftp.describer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;

public class SexftpInnerContentDescriber implements ITextContentDescriber {
	public int describe(InputStream ins, IContentDescription arg) throws IOException {
		return 0;
	}

	public int describe(Reader arg0, IContentDescription arg1) throws IOException {
		return 0;
	}

	public QualifiedName[] getSupportedOptions() {
		return null;
	}
}
