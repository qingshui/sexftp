package sexftp.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import sexftp.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer {
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault("booleanPreference", true);
		store.setDefault("choicePreference", "zhcn");
		store.setDefault("inttimeout", 10000);
	}
}
