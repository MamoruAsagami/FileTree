package sophie.tools;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.text.StringEscapeUtils;

import sophie.io.FileUtilities;
import sophie.tools.FileTree.FileTreeLeaf;
import sophie.widget.ErrorMessageDialog;
import sophie.widget.JFileTextField;

public class FileTreeTextTree extends JPanel {
	private static final char LightUpAndRightChar = '└';       // Light Up and Right
	private static final char LightVerticalAndRightChar = '├'; // Light Vertical and Right
	private static final char LightHorizontalChar = '─';       // Light Horizontal
	private static final char LightVerticalChar = '│';         // Light Vertical
	private static final char EmSpace = '\u2003';              // EM SPACE
	private static final String FolderIcon = String.valueOf(Character.toChars(0x1F4C1));
	private static final String DocumentIcon = String.valueOf(Character.toChars(0x1F4C4));
	private JFileTextField textTreeFileTextField;
	private JRadioButton unicodeRadioButton;
	private JRadioButton asciiRadioButton;
	private JCheckBox unicodeIconCheckBox;
	private JCheckBox directorySuffixCheckBox;
	private JSpinner trunkSpacingField;
	private JSpinner branchSpacingField;
	private JSpinner iconSpacingField;
	private JSpinner nameSpacingField;
	private JCheckBox htmlEnvelopeCheckBox;
	private JCheckBox preEnvelopeCheckBox;
	private FileTree fileTree;
	private char UpAndRightChar;          // Light Up and Right
	private char VerticalAndRightChar;    // Light Vertical and Right
	private char HorizontalChar;          // Light Horizontal
	private char VerticalChar;            // Light Vertical
	private char SpaceChar;               // SPACE
	private String lastFile;

	public FileTreeTextTree(FileTree fileTree) {
		this.fileTree = fileTree;
		FileTreeState fileTreeState = fileTree.fileTreeState;
		setLayout(new BorderLayout());
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 4, 0, 4);
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0; gbc.gridy = 0;
		northPanel.add(Box.createVerticalStrut(4), gbc);
		gbc.gridx = 0; gbc.gridy++;
		JButton outputFileButton = new JButton("Text Tree File");
		northPanel.add(outputFileButton, gbc);
		//
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		textTreeFileTextField = new JFileTextField(40);
		textTreeFileTextField.setText(fileTreeState.getTextTreeFile());
		northPanel.add(textTreeFileTextField, gbc);
		 gbc.weightx = 0;
		JPanel groupPanel;
		ButtonGroup buttonGroup;
		gbc.fill = GridBagConstraints.NONE;
		//
		gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.EAST;
		northPanel.add(new JLabel("Tree graphics"), gbc);
		gbc.gridx++; gbc.anchor = GridBagConstraints.WEST;
		boolean unicodeTreeDrawing = fileTreeState.isUnicodeTreeDrawing();
		groupPanel = new JPanel();
		groupPanel.add(unicodeRadioButton = new JRadioButton("Unicode", unicodeTreeDrawing));
		groupPanel.add(asciiRadioButton = new JRadioButton("ASCII", !unicodeTreeDrawing));
		buttonGroup = new ButtonGroup();
		buttonGroup.add(unicodeRadioButton);
		buttonGroup.add(asciiRadioButton);
		northPanel.add(groupPanel, gbc);
		//
		gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.EAST;
		northPanel.add(new JLabel("File decoration"), gbc);
		gbc.gridx++; gbc.anchor = GridBagConstraints.WEST;
		groupPanel = new JPanel();
		groupPanel.add(unicodeIconCheckBox = new JCheckBox("Unicode Icon", fileTreeState.isUnicodeIcon()));
		groupPanel.add(directorySuffixCheckBox = new JCheckBox("Directory suffix(/)", fileTreeState.isDirectorySuffix()));
		northPanel.add(groupPanel, gbc);
		//
		gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.EAST;
		northPanel.add(new JLabel("Spacing"), gbc);
		gbc.gridx++; gbc.anchor = GridBagConstraints.WEST;
		groupPanel = new JPanel();
		groupPanel.add(new JLabel("Leading"));
		groupPanel.add(trunkSpacingField = new JSpinner(new SpinnerNumberModel(fileTreeState.getLeadingSpacing(), 0, 9, 1)));
		groupPanel.add(Box.createHorizontalStrut(10));
		groupPanel.add(new JLabel("Trailing"));
		groupPanel.add(branchSpacingField = new JSpinner(new SpinnerNumberModel(fileTreeState.getTrailingSpacing(), 0, 9, 1)));
		groupPanel.add(Box.createHorizontalStrut(10));
		groupPanel.add(new JLabel("Icon"));
		groupPanel.add(iconSpacingField = new JSpinner(new SpinnerNumberModel(fileTreeState.getIconSpacing(), 0, 5, 1)));
		groupPanel.add(Box.createHorizontalStrut(10));
		groupPanel.add(new JLabel("Name"));
		groupPanel.add(nameSpacingField = new JSpinner(new SpinnerNumberModel(fileTreeState.getNameSpacing(), 0, 5, 1)));
		northPanel.add(groupPanel, gbc);
		//
		gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.EAST;
		northPanel.add(new JLabel("Html Envelope"), gbc);
		gbc.gridx++; gbc.anchor = GridBagConstraints.WEST;
		groupPanel = new JPanel();
		groupPanel.add(htmlEnvelopeCheckBox = new JCheckBox("<html> ... </html>", fileTreeState.isHtmlEnvelope()));
		groupPanel.add(preEnvelopeCheckBox = new JCheckBox("<pre> ... </pre>", fileTreeState.isPreEnvelope()));
		htmlEnvelopeCheckBox.putClientProperty("html.disable", true);
		preEnvelopeCheckBox.putClientProperty("html.disable", true);
		northPanel.add(groupPanel, gbc);
		//
		gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
		JPanel buttons = new JPanel();
		JButton fileButton = new JButton("File");
		buttons.add(fileButton);
		buttons.add(Box.createHorizontalStrut(20));
		JButton closeButton = new JButton("Close");
		buttons.add(closeButton);
		northPanel.add(buttons, gbc);
		add(northPanel, BorderLayout.NORTH);
		gbc.gridx = 0; gbc.gridy++;
		northPanel.add(Box.createVerticalStrut(4), gbc);


		outputFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseTextTreeFile();
			}
		});

		fileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				file();
			}
		});
		
		closeButton.addActionListener((e) -> {
			SwingUtilities.getWindowAncestor(this).dispose();
		});
	}

	void browseTextTreeFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle("Choose Text Tree File");
		int returnVal = chooser.showSaveDialog(SwingUtilities.getWindowAncestor(this));
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().getPath().replace('\\', '/');
			textTreeFileTextField.setText(name);
		}
	}

	void writeTextTree(DefaultMutableTreeNode node, DefaultTreeModel model, JTree tree, PrintWriter out) {
		FileTreeState fileTreeState = fileTree.fileTreeState;
		TreeNode[] path = model.getPathToRoot(node);
		boolean[] hasMoreSiblings = new boolean[path.length - 1];
		for(int i = 0; i < path.length - 1 ; i++) {
			hasMoreSiblings[i] = ((DefaultMutableTreeNode)path[i]).getChildAfter(path[i + 1]) != null;
		}
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < hasMoreSiblings.length - 1 ; i++) {
			// middle node
			for(int count = fileTreeState.getLeadingSpacing(); count > 0; count--) {
				sb.append(' ');
			}
			sb.append(hasMoreSiblings[i]? VerticalChar: SpaceChar);
			for(int count = fileTreeState.getTrailingSpacing(); count > 0; count--) {
				sb.append(SpaceChar);
			}
		}
		if(hasMoreSiblings.length != 0) {
			// Last node
			for(int count = fileTreeState.getLeadingSpacing(); count > 0; count--) {
				sb.append(' ');
			}
			sb.append(hasMoreSiblings[hasMoreSiblings.length - 1]? VerticalAndRightChar:  UpAndRightChar);
			for(int count = fileTreeState.getTrailingSpacing(); count > 0; count--) {
				sb.append(HorizontalChar);
			}
		}
		boolean folder = node.getUserObject() instanceof FileTree.FileTreeNode;
		if(fileTreeState.isUnicodeIcon()) {
			for(int count = fileTreeState.getIconSpacing(); count > 0; count--) {
				sb.append(' ');
			}
			sb.append(folder? FolderIcon: DocumentIcon);
		}
		for(int count = fileTreeState.getNameSpacing(); count > 0; count--) {
			sb.append(' ');
		}
		var fileTreeLeaf = (FileTreeLeaf)node.getUserObject();
		boolean bold = fileTreeLeaf.isBold();
		String color = fileTreeLeaf.getColor();
		String style = null;
		if(bold && color != null) {
			style = "font-weight: bold; color: #" + color;
		} else if(bold) {
			style = "font-weight: bold";
		} else if(color != null) {
			style = "color: #" + color;
		} else {
			
		}
		String folderSuffix = (folder && fileTreeState.isDirectorySuffix())? "/" : "";
		if(style != null) {
			sb.append("<span style=\"" + style + "\">");
		}
		sb.append(StringEscapeUtils.escapeHtml4(fileTreeLeaf.getName() + folderSuffix));
		if(style != null) {
			sb.append("</span>");
		}
		out.println(sb.toString());

		int n = node.getChildCount();
		for(int i = 0; i < n; i++) {
			var child = (DefaultMutableTreeNode)node.getChildAt(i);
			if(tree.isExpanded(new TreePath(path))) {
				writeTextTree(child, model, tree, out);
			}
		}

	}
	void writeContents(PrintWriter out) {
		FileTreeState fileTreeState = fileTree.fileTreeState;
		if(fileTreeState.isUnicodeTreeDrawing()) {
			UpAndRightChar = LightUpAndRightChar;
			VerticalAndRightChar = LightVerticalAndRightChar;
			HorizontalChar = LightHorizontalChar;
			VerticalChar = LightVerticalChar;
			SpaceChar = EmSpace;
		} else {
			UpAndRightChar = '+';
			VerticalAndRightChar = '+';
			HorizontalChar = '-';
			VerticalChar = '|';
			SpaceChar = ' ';
		}
		DefaultTreeModel model = (DefaultTreeModel)fileTree.treeView.getModel();
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)model.getRoot();
		if(rootNode.getUserObject() instanceof FileTreeLeaf) {
			writeTextTree(rootNode, model, fileTree.treeView, out);
		}
	}

	void file() {
		try {
			long startTime = System.currentTimeMillis();
			FileTreeState fileTreeState = fileTree.fileTreeState;
			fileTreeState.setTextTreeFile(textTreeFileTextField.getText().trim());
			boolean unicodeTreeDrawing = unicodeRadioButton.isSelected();
			fileTreeState.setUnicodeTreeDrawing(unicodeTreeDrawing);
			fileTreeState.setUnicodeIcon(unicodeIconCheckBox.isSelected());
			fileTreeState.setDirectorySuffix(directorySuffixCheckBox.isSelected());
			fileTreeState.setLeadingSpacing((Integer)trunkSpacingField.getValue());
			fileTreeState.setTrailingSpacing((Integer)branchSpacingField.getValue());
			fileTreeState.setIconSpacing((Integer)iconSpacingField.getValue());
			fileTreeState.setNameSpacing((Integer)nameSpacingField.getValue());
			fileTreeState.setHtmlEnvelope(htmlEnvelopeCheckBox.isSelected());
			fileTreeState.setPreEnvelope(preEnvelopeCheckBox.isSelected());
			String textTreeFile = fileTreeState.getTextTreeFile();
			if(textTreeFile.equals(lastFile) || FileUtilities.canWrite(new File(textTreeFile), this, FileTree.TITLE)) {
				lastFile = textTreeFile;
				try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileTreeState.getTextTreeFile()))) {
					try(PrintWriter out = new PrintWriter(writer)) {
						if(fileTreeState.isHtmlEnvelope()) {
							out.println("<html>");
							out.println("<head>");
							out.println("<title>Text File Tree</title");
							out.println("</head>");
							out.println("<body>");
							if(fileTreeState.isPreEnvelope()) {
								out.println("<pre style=\"line-height:16px\">");
							} else {
								out.println("<div style=\"white-space: pre; line-height:19px\">");
							}
						} else if(fileTreeState.isPreEnvelope()) {
							out.println("<pre style=\"line-height:19px\">");
						} else {
							// Nothing to do
						}
						writeContents(out);
						if(fileTreeState.isHtmlEnvelope()) {
							if(fileTreeState.isPreEnvelope()) {
								out.println("</pre>");
							} else {
								out.println("</div>");
							}
							out.println("</body>");
							out.println("</html>");
						} else if(fileTreeState.isPreEnvelope()) {
							out.println("</pre>");
						} else {
							// Nothing to do
						}
					}
				}
			}
			long elapsedTime = System.currentTimeMillis() - startTime;
			long sleepTime = 800 - elapsedTime;
			if(sleepTime > 0) {
				try {
					// This following sleep will give the user the sense of button action.
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {}
			}
		} catch(Throwable t) {
			ErrorMessageDialog.showMessageDialog(this, t, FileTree.TITLE);
		}
	}
}
