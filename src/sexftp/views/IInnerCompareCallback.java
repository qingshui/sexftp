package sexftp.views;

import java.util.List;
import org.sexftp.core.ftp.bean.FtpUploadPro;

public abstract interface IInnerCompareCallback {
	public abstract void afterCompareEnd(final List<FtpUploadPro> paramList1, final List<FtpUploadPro> paramList2,
			final List<FtpUploadPro> paramList3);
}