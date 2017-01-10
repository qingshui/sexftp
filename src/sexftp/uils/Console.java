package sexftp.uils;

import java.util.Hashtable;
import java.util.Map;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Console {
	private String name;
	private String iconName;
	private MessageConsole console = null;
	private MessageConsoleStream consoleStream = null;

	private static Map<String, Console> cash = new Hashtable<String, Console>();

	public static Console createConsole(String name, String icon) {
		Console console2 = (Console) cash.get(name);
		if (console2 == null) {
			Console con = new Console(name, icon);
			con.init();
			console2 = con;
			cash.put(name, console2);
		}
		return console2;
	}

	private Console(String name, String iconName) {
		this.name = name;
		this.iconName = iconName;
	}

	private void init() {
		if (this.console != null) {
			return;
		}
		this.console = new MessageConsole(this.name,
				AbstractUIPlugin.imageDescriptorFromPlugin("sexftp", "/icons/" + this.iconName));

		ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { this.console });

		this.consoleStream = this.console.newMessageStream();
	}

	public void openConsole() {
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(this.console);
	}

	public void console(String text) {
		this.consoleStream.println(text);

		LogUtil.info(text);
	}

	public MessageConsoleStream getConsoleStream() {
		return this.consoleStream;
	}

	public void setConsoleStream(MessageConsoleStream consoleStream) {
		this.consoleStream = consoleStream;
	}
}
