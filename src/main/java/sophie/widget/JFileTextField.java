package sophie.widget;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

@SuppressWarnings("serial")
public class JFileTextField extends JTextField {

	static class JFileTextFieldTransferHandler extends TransferHandler {
		JFileTextField textField;

		JFileTextFieldTransferHandler(JFileTextField textField) {
			this.textField = textField;
		}
		@Override
		public int getSourceActions(JComponent c) {
			return COPY | MOVE;
		}
		@Override
		protected Transferable createTransferable(JComponent c) {
			return new StringSelection(textField.getSelectedText());
		}
		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			if (support.isDrop()) {
				boolean copySupported = (COPY & support.getSourceDropActions()) == TransferHandler.COPY;
				if (copySupported) {
					support.setDropAction(COPY);
					for(var flavor: support.getDataFlavors()) {
						if(flavor.equals(DataFlavor.javaFileListFlavor) || flavor.equals(DataFlavor.stringFlavor)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		@Override
		public boolean importData(TransferSupport support) {
			try {
				var transferable = support.getTransferable();
				for(var flavor: transferable.getTransferDataFlavors()) {
					if(flavor.equals(DataFlavor.javaFileListFlavor)) {
						@SuppressWarnings("unchecked")
						List<File> fileList = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
						if(fileList.size() == 1) {
							textField.setText(fileList.get(0).toString().replace('\\', '/'));
						}
						return true;
					} else if(flavor.equals(DataFlavor.stringFlavor)) {
						String fileList = (String)transferable.getTransferData(DataFlavor.stringFlavor);
						String[] files = fileList.trim().split("\r\n|\r|\n");
						if(files.length == 1) {
							String file = files[0].trim();
							if(file.length() > 0) {
								String text = file.replace('\\', '/');
								if(support.isDrop()) {
									textField.setText(text);
								} else {
									int selectionEnd = textField.getSelectionEnd();
									if(selectionEnd > 0) {
										textField.replaceSelection(text);
									} else {
										textField.getDocument().insertString(textField.getCaretPosition(), text, SimpleAttributeSet.EMPTY);
									}
								}
							}
						}
						return true;
					} else {
						// Nothing to do
					}
				}
				return false;
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
			return false;
		}

	}
	void setupDnD() {
		setDragEnabled(true);
		setTransferHandler(new JFileTextFieldTransferHandler(this));
	}

	/*
	void setupDnD() {
        DropTargetListener dropTargetListener = new DropTargetListener() {
            public void dragEnter(DropTargetDragEvent dtde) {
                DataFlavor[] flavors = dtde.getCurrentDataFlavors();
                int acceptableActions = 0;
                for(int i=0; i<flavors.length; i++) {
                    if(flavors[i].equals(DataFlavor.javaFileListFlavor)) {
                        acceptableActions |= DnDConstants.ACTION_REFERENCE;
                    } else if(flavors[i].equals(DataFlavor.stringFlavor)) {
                    	acceptableActions |= DnDConstants.ACTION_COPY;
                    } else {
                    	// Nothing to do
                    }
                }
                int newActions = dtde.getSourceActions() & acceptableActions;
                newActions |= DnDConstants.ACTION_COPY;
                if(dtde.getDropAction() != newActions) {
                    dtde.acceptDrag(newActions);
                }
            }
            public void dragOver(DropTargetDragEvent dtde) {
            }
            public void dropActionChanged(DropTargetDragEvent dtde) {
            }
            public void dragExit(DropTargetEvent dte) {
            }
            public void drop(DropTargetDropEvent dtde){
                boolean complete = false;
                DataFlavor[] flavors = dtde.getCurrentDataFlavors();
                for(int i=0; i<flavors.length; i++) {
                    if(flavors[i].equals(DataFlavor.javaFileListFlavor) && ((dtde.getSourceActions() & DnDConstants.ACTION_REFERENCE) != 0)) {
                        dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
                        try {
                        	@SuppressWarnings("unchecked")
                        	List<File> fileList = (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        	if(fileList.size() == 1) {
                        		JFileTextField.this.setText(fileList.get(0).toString().replace('\\', '/'));
                        	}
                            complete = true;
                        } catch(UnsupportedFlavorException ex) {
                            System.out.println(ex);
                        } catch(IOException ex) {
                            System.out.println(ex);
                        }
                    } else if(flavors[i].equals(DataFlavor.stringFlavor) && ((dtde.getSourceActions() & DnDConstants.ACTION_COPY) != 0)) {
                        dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
                        try {
                        	@SuppressWarnings("unchecked")
                        	String fileList = (String)dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
                        	String[] files = fileList.trim().split("\r\n|\r|\n");
                        	if(files.length == 1) {
                        		String file = files[0].trim();
                        		if(file.length() > 0) {
                        			JFileTextField.this.setText(file.replace('\\', '/'));
                        		}
                        	}
                            complete = true;
                        } catch(UnsupportedFlavorException ex) {
                            System.out.println(ex);
                        } catch(IOException ex) {
                            System.out.println(ex);
                        }
                    } else {

                    }
                }
                if(complete) {
                    dtde.dropComplete(true);
                } else {
                    dtde.rejectDrop();
                }
            }
        };
        new DropTarget(this, dropTargetListener);
	}
	 */

	public JFileTextField() {
		super();
		setupDnD();
	}

	public JFileTextField(Document doc, String text, int columns) {
		super(doc, text, columns);
		setupDnD();
	}

	public JFileTextField(int columns) {
		super(columns);
		setupDnD();
	}

	public JFileTextField(String text) {
		super(text);
		setupDnD();
	}

	public JFileTextField(String text, int columns) {
		super(text, columns);
		setupDnD();
	}
}
