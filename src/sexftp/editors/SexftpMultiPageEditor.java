package sexftp.editors;

import java.io.StringWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

public class SexftpMultiPageEditor extends MultiPageEditorPart implements IResourceChangeListener {
	private TextEditor editor;
	private Font font;
	private StyledText text;

	public SexftpMultiPageEditor() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	void createPage0() {
		try {
			this.editor = new TextEditor();
			int index = addPage(this.editor, getEditorInput());
			setPageText(index, this.editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	void createPage1() {
		Composite composite = new Composite(getContainer(), 0);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 2;

		Button fontButton = new Button(composite, 0);
		GridData gd = new GridData(1);
		gd.horizontalSpan = 2;
		fontButton.setLayoutData(gd);
		fontButton.setText("Change Font...");

		fontButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				SexftpMultiPageEditor.this.setFont();
			}
		});
		int index = addPage(composite);
		setPageText(index, "Properties");
	}

	void createPage2() {
		Composite composite = new Composite(getContainer(), 0);

		int index = addPage(composite);
		setPageText(index, "EncodingConfig");
	}

	protected void createPages() {
		createPage0();
		createPage1();
		createPage2();
	}

	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}

	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}

	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (editorInput instanceof IFileEditorInput) {
			super.init(site, editorInput);
		} else {
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		}
	}

	public boolean isSaveAsAllowed() {
		return true;
	}

	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 2)
			sortWords();
	}

	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == 2) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = SexftpMultiPageEditor.this.getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; ++i){
						if (((FileEditorInput) SexftpMultiPageEditor.this.editor.getEditorInput()).getFile()
								.getProject().equals(event.getResource())) {
							IEditorPart editorPart = pages[i]
									.findEditor(SexftpMultiPageEditor.this.editor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

	void setFont() {
		FontDialog fontDialog = new FontDialog(getSite().getShell());
		fontDialog.setFontList(this.text.getFont().getFontData());
		FontData fontData = fontDialog.open();
		if (fontData != null) {
			if (this.font != null)
				this.font.dispose();
			this.font = new Font(this.text.getDisplay(), fontData);
			this.text.setFont(this.font);
		}
	}

	void sortWords() {
		String editorText = this.editor.getDocumentProvider().getDocument(this.editor.getEditorInput()).get();

		StringTokenizer tokenizer = new StringTokenizer(editorText, " \t\n\r\f!@#$%^&*()-_=+`~[]{};:'\",.<>/?|\\");
		ArrayList<String> editorWords = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			editorWords.add(tokenizer.nextToken());
		}

		Collections.sort(editorWords, Collator.getInstance());
		StringWriter displayText = new StringWriter();
		for (int i = 0; i < editorWords.size(); ++i) {
			displayText.write((String) editorWords.get(i));
			displayText.write(System.getProperty("line.separator"));
		}
		this.text.setText(displayText.toString());
	}
}
