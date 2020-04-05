package org.fc.seqedit;

import java.io.File;
import java.io.FileFilter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FileTreeItem extends TreeItem<String> {
	public static Image folderCollapseImage = new Image(
			FileTreeItem.class.getResourceAsStream("graphics/folder.closed.png"));
	public static Image folderExpandImage = new Image(
			FileTreeItem.class.getResourceAsStream("graphics/folder.open.png"));
	public static Image fileImage = new Image(FileTreeItem.class.getResourceAsStream("graphics/file.generic.png"));
	private boolean isLeaf;
	private boolean isFirstTimeChildren = true;
	private boolean isFirstTimeLeaf = true;
	private final File file;
	FileFilter fileFilter=null;
	
	public File getFile() {
		return (this.file);
	}

	private final String absolutePath;

	public String getAbsolutePath() {
		return (this.absolutePath);
	}

	private final boolean isDirectory;

	public boolean isDirectory() {
		return (this.isDirectory);
	}

	@SuppressWarnings("unchecked")
	public FileTreeItem(File file, FileFilter f) {
		super(file.toString());
		this.file = file;
		this.absolutePath = file.getAbsolutePath();
		this.isDirectory = file.isDirectory();
		this.fileFilter=f;
		if (this.isDirectory) {
			ImageView iv=new ImageView(folderCollapseImage);
			iv.setFitWidth(16);
			iv.setFitHeight(16);
			this.setGraphic(iv);
			// add event handlers
			this.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler() {
				@Override
				public void handle(Event e) {
					FileTreeItem source = (FileTreeItem) e.getSource();
					if (!source.isExpanded()) {
						ImageView iv = (ImageView) source.getGraphic();
						iv.setImage(folderCollapseImage);
						iv.setFitWidth(16);
						iv.setFitHeight(16);
					}
				}
			});
			this.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler() {
				@Override
				public void handle(Event e) {
					FileTreeItem source = (FileTreeItem) e.getSource();
					if (source.isExpanded()) {
						ImageView iv = (ImageView) source.getGraphic();
						iv.setImage(folderExpandImage);
						iv.setFitWidth(16);
						iv.setFitHeight(16);
					}
				}
			});
		} else {
			ImageView iv=new ImageView(fileImage);
			iv.setFitWidth(8);
			iv.setFitHeight(8);
			this.setGraphic(iv);
		}
		// set the value (which is what is displayed in the tree)
		String fullPath = file.getAbsolutePath();
		if (!fullPath.endsWith(File.separator)) {
			String value = file.toString();
			int indexOf = value.lastIndexOf(File.separator);
			if (indexOf > 0) {
				this.setValue(value.substring(indexOf + 1));
			} else {
				this.setValue(value);
			}
		}
	}

	@Override
	public ObservableList<TreeItem<String>> getChildren() {
		if (isFirstTimeChildren) {
			isFirstTimeChildren = false;
			super.getChildren().setAll(buildChildren(this));
		}
		return (super.getChildren());
	}

	@Override
	public boolean isLeaf() {
		if (isFirstTimeLeaf) {
			isFirstTimeLeaf = false;
			isLeaf = this.file.isFile();
		}
		return (isLeaf);
	}

	private ObservableList<FileTreeItem> buildChildren(FileTreeItem treeItem) {
		File f = treeItem.getFile();
		if ((f != null) && (f.isDirectory())) {
			File[] files = f.listFiles(fileFilter);
			if (files != null) {
				ObservableList<FileTreeItem> children = FXCollections.observableArrayList();
				for (File childFile : files) {
					children.add(new FileTreeItem(childFile, fileFilter));
				}
				return (children);
			}
		}
		return FXCollections.emptyObservableList();
	}

}
