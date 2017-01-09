package org.sexftp.core.utils;

import sexftp.views.AbstractSexftpView.TreeObject;

public abstract interface SearchCallback {
	public abstract TreeViewUtil.ThisYourFind isThisYourFind(TreeObject paramTreeObject);
}