package sophie.io;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.Math;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import sophie.widget.JFileTextField;

public class JTextAreaWriter extends java.io.Writer {
    javax.swing.JTextArea textArea;
    boolean askForMakingLogWriter;
    Document document;
    String logFileName;
    Writer logWriter = null;
    boolean shutdownHook = false;
    int textAreaMaxCapacity = 100 * 1024;
    JPopupMenu menu;

    @SuppressWarnings("serial")
	static class LogFileDialog extends JDialog {
    	static final String TITLE = "Message Logging";
    	int optionType;
    	LogFilePanel logFilePanel;
    	String logFileName;

    	static class LogFilePanel extends JPanel {
    		JFileTextField logFileNameTextField;
    		JCheckBox appendCheckBox;
    		
    		LogFilePanel(String logFileName) {
    			setLayout(new GridBagLayout());
    	        GridBagConstraints gbc = new GridBagConstraints();
    			gbc.insets = new Insets(2, 2, 2, 2);
    	        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
    	        JButton logFileButton = new JButton("Log File");
    	        add(logFileButton, gbc);
    	        gbc.gridx++; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
    	        logFileNameTextField = new JFileTextField(logFileName, 40);
    	        add(logFileNameTextField, gbc);
    	        //
    	        gbc.gridy++;
    	        gbc.gridx = 1;
    	        Box box = Box.createHorizontalBox();
    	        appendCheckBox = new JCheckBox("Append");
    	        box.add(appendCheckBox);
    	        box.add(Box.createHorizontalGlue());
    	        JButton clearButton = new JButton("Clear");
    	        box.add(clearButton);
    	        add(box, gbc);
    	        logFileButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						browseLogFile();
					}
				});
    	        clearButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						logFileNameTextField.setText("");
					}
				});
    	        logFileNameTextField.getDocument().addDocumentListener(
    	    			new DocumentListener() {
    	    				public void insertUpdate(DocumentEvent e) {
    	    					logFileChanged();
    	    				}
    	    				public void removeUpdate(DocumentEvent e) {
    	    					logFileChanged();
    	    				}
    	    				public void changedUpdate(DocumentEvent e) {
    	    					logFileChanged();
    	    				}
    	        });
    	        logFileChanged();
    		}
    		
    		void browseLogFile() {
    	        JFileChooser chooser = new JFileChooser(getLogFileName());
    	        chooser.setDialogTitle("Choose a message log file");
    	        int returnVal = chooser.showSaveDialog(SwingUtilities.getWindowAncestor(this));
    	        if(returnVal == JFileChooser.APPROVE_OPTION) {
    	        	String fileName = chooser.getSelectedFile().getPath().replace('\\', '/');
    	        	logFileNameTextField.setText(fileName);
    	        }
    		}
    		
    		void logFileChanged() {
    			String fileName = logFileNameTextField.getText().trim();
    			if(fileName.length() != 0) {
    				File file = new File(fileName);
    				if(file.exists() && file.isFile()) {
    					appendCheckBox.setSelected(true);
    				}
    			}
    		}

			public String getLogFileName() {
				return logFileNameTextField.getText().trim();
			}
    		
			boolean getAppend() {
				return appendCheckBox.isSelected();
			}
    	}
    	
    	private LogFileDialog(Window owner, String logFileName) {
    		super(owner, TITLE, Dialog.ModalityType.APPLICATION_MODAL);
            Box panel = new Box(BoxLayout.Y_AXIS);
    		logFilePanel = new LogFilePanel(logFileName);
    		panel.add(logFilePanel);
            panel.add(Box.createVerticalStrut(10));
            getContentPane().add(panel);
            JPanel buttonBox = new JPanel();
            JButton okButton = new JButton("OK");
            buttonBox.add(okButton);
            buttonBox.add(Box.createHorizontalStrut(20));
            JButton cancelButton = new JButton("Cancel");
            buttonBox.add(cancelButton);
            getContentPane().add(buttonBox, BorderLayout.SOUTH);
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed();
                }
            });
            
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelPressed();
                }
            });
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    cancelPressed();
                }
            });
            pack();
    	}
    	
    	void okPressed() {
        	try {
    			logFileName = logFilePanel.getLogFileName();
    	        optionType = JOptionPane.OK_OPTION;
    	        dispose();
        	} catch(Exception ex) {
        		JOptionPane.showMessageDialog(this, ex, TITLE, JOptionPane.ERROR_MESSAGE);
        	}
        }

        void cancelPressed() {
            optionType = JOptionPane.CANCEL_OPTION;
            dispose();
        }

        public int getOptionType() {
            return optionType;
        }

        public String getLogFileName() {
            return logFileName;
        }

        public boolean getAppend() {
        	return logFilePanel.getAppend();
        }
        
        public void setVisible(boolean visible) {
    		optionType = JOptionPane.CANCEL_OPTION;
    		super.setVisible(visible);
    	}
    }
    
    public JTextAreaWriter(javax.swing.JTextArea textArea, boolean askForMakingLogWriter) {
        this.textArea = textArea;
        this.askForMakingLogWriter = askForMakingLogWriter;
        document = textArea.getDocument();
        textArea.addMouseListener(new MouseAdapter() {
           	public void mouseClicked(MouseEvent e) {
            		if(e.getButton() == 3) {
           			JTextAreaWriter.this.mouseClicked(e);
           		}
        	}
 		});
        JPopupMenu menu = new JPopupMenu();
        JMenuItem fileItem = new JMenuItem("Message log");
        fileItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageLog();
			}
		});
        menu.add(fileItem);
        this.menu = menu;
    }
    
    public JTextAreaWriter(javax.swing.JTextArea textArea) {
    	this(textArea, false);
    }

    public void mouseClicked(MouseEvent e) {
    	menu.show(textArea, e.getX(), e.getY());
    }
    
    public void messageLog() {
		LogFileDialog dialog = new LogFileDialog(SwingUtilities.getWindowAncestor(textArea), (logFileName != null)? logFileName: "");
        dialog.setLocationRelativeTo(textArea);
        dialog.setVisible(true);
        if(dialog.getOptionType() == JOptionPane.OK_OPTION) {
        	try {
				close();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
            logWriter = null;
        	logFileName = dialog.getLogFileName();
        	if(logFileName.length() != 0) {
        		try {
					logWriter = new BufferedWriter(new FileWriter(logFileName, dialog.getAppend()));
					if(!shutdownHook) {
				        Runtime.getRuntime().addShutdownHook(new Thread() {
					        public void run() {
					        	try {
									close();
								} catch (IOException e) {
									// Ignore the exception occurred in shutdown process.
								}
				            }
				        });
						shutdownHook = true;
					}
				} catch (IOException e) {
					JOptionPane.showConfirmDialog(textArea, e.toString(), LogFileDialog.TITLE, JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
				}        		
        	}
        }
    }
    
    public void reset() {
        textArea.setText("");
    }
    
    public void write(char[] cbuf, int off, int len) throws IOException {
        textArea.append(new String(cbuf, off, len));
        if(logWriter != null) {
            logWriter.write(cbuf, off, len);
        }
        try {
            trim();
            textArea.setCaretPosition(document.getLength());
        } catch(javax.swing.text.BadLocationException e) {
            e.printStackTrace(System.err);
        }
    }

    public void flush() throws IOException {
        if(logWriter != null) {
            logWriter.flush();
        }
    }

    public void close() throws IOException {
        if(logWriter != null) {
            logWriter.close();
        }
    }
    
    private void trim() throws javax.swing.text.BadLocationException, IOException {
        if(document.getLength() > textAreaMaxCapacity) {
            int chunkLength = Math.min(textAreaMaxCapacity, 1024);
            String text = document.getText(0, chunkLength);
            int lastIndex = text.lastIndexOf('\n');
            int length = (lastIndex < 0)? chunkLength: lastIndex + 1;
            document.remove(0, length);
            flush();
        }
    }
}