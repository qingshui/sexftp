package sexftp.editors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager {
	protected Map<RGB,Color> fColorTable = new HashMap<RGB,Color>(10);

	public void dispose() {
		@SuppressWarnings("rawtypes")
		Iterator e = this.fColorTable.values().iterator();
		while (e.hasNext())
			((Color) e.next()).dispose();
	}

	public Color getColor(RGB rgb) {
		Color color = (Color) this.fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			this.fColorTable.put(rgb, color);
		}
		return color;
	}
}