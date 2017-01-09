package sexftp.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import sexftp.Activator;

public class SexftpPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public SexftpPreferencePage() {
		super(1);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Sexftp Preference,Some Change Should Restart Eclipse Go into Effect.");
	}

	public void createFieldEditors() {
		addField(new BooleanFieldEditor("booleanPreference", "&Prompt Before Overwrites File", getFieldEditorParent()));

		addField(new RadioGroupFieldEditor("choicePreference", "Sexftp Language", 1,
				new String[][] { { "&English", "enus" }, { "&C简体中文", "zhcn" } }, getFieldEditorParent()));
		addField(new IntegerFieldEditor("inttimeout", "Server Timeout Milliseconds:", getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
	}
}
