package sexftp;

public abstract interface SrcViewable {
	public abstract void handleException(Throwable paramThrowable);

	public abstract boolean showQuestion(String paramString);
}