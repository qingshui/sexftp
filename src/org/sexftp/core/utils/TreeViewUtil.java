package org.sexftp.core.utils;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.TreePath;
import sexftp.views.AbstractSexftpView;

public class TreeViewUtil {
	public static void serchTreeDatas(AbstractSexftpView.TreeObject p, SearchCallback c,
			List<AbstractSexftpView.TreeObject> yourLis) {
		ThisYourFind thisYourFind = c.isThisYourFind(p);
		if (thisYourFind.isThisYourFind()) {
			yourLis.add(p);
		}

		if ((p instanceof AbstractSexftpView.TreeParent) && (thisYourFind.isFindChild())) {
			for (AbstractSexftpView.TreeObject to : ((AbstractSexftpView.TreeParent) p).getChildren()) {
				serchTreeDatas(to, c, yourLis);
			}
		}
	}

	public static AbstractSexftpView.TreeObject serchTreeData(AbstractSexftpView.TreeObject p, SearchCallback c) {
		List<AbstractSexftpView.TreeObject> yourLis = new ArrayList<AbstractSexftpView.TreeObject>();
		serchTreeDatas(p, c, yourLis);
		if (yourLis.size() > 0)
			return (AbstractSexftpView.TreeObject) yourLis.get(0);
		return null;
	}

	public static TreePath changeTreePath(AbstractSexftpView.TreeObject to) {
		List<AbstractSexftpView.TreeObject> tLlist = new ArrayList<AbstractSexftpView.TreeObject>();
		AbstractSexftpView.TreeObject c = to;
		for (int i = 0; i < 50; ++i) {
			tLlist.add(c);
			c = c.getParent();
			if (c == null)
				break;
		}
		Object[] tps = new Object[tLlist.size()];
		int i = tLlist.size() - 1;
		for (int j = 0; i >= 0; ++j) {
			tps[j] = tLlist.get(i);

			--i;
		}

		return new TreePath(tps);
	}

	public static class ThisYourFind {
		private boolean isThisYourFind;
		private boolean findChild;

		public ThisYourFind(boolean isThisYourFind, boolean findChild) {
			this.isThisYourFind = isThisYourFind;
			this.findChild = findChild;
		}

		public boolean isThisYourFind() {
			return this.isThisYourFind;
		}

		public void setThisYourFind(boolean isThisYourFind) {
			this.isThisYourFind = isThisYourFind;
		}

		public boolean isFindChild() {
			return this.findChild;
		}

		public void setFindChild(boolean findChild) {
			this.findChild = findChild;
		}
	}
}