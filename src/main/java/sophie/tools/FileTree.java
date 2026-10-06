package sophie.tools;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import org.apache.commons.text.StringEscapeUtils;

import sophie.ui.CancelledByTheUserException;
import sophie.util.IconList;
import sophie.widget.ErrorMessageDialog;
import sophie.widget.JFileTextField;

public class FileTree extends JPanel {
	private static final String VERSION = "1.2.0";
	static final String TITLE = FileTree.class.getSimpleName() + " " + VERSION;
	PrintWriter report;
	FileTreeState fileTreeState;
	JFileTextField baseDirectoryTextField;
	JCheckBox showRootNameCheckBox;
	JCheckBox boldCheckBox;

	JTree treeView;
	JTextArea reportTextArea;
	JPopupMenu menu;
	DefaultMutableTreeNode menuNode;
	String remoteFileName;
	String baseFileName;
	File baseFile;
	boolean showRootName;
	boolean bold;
	JFrame fileTreeTextTreeFrame;

	static class FileTreeLeaf implements Comparable<FileTreeLeaf> {

		private Collator collator = Collator.getInstance();
		private String name;
		private Date lastModified;
		private FileTreeNode parent;
		private boolean bold;
		private String color;

		FileTreeLeaf(String name, Date lastModified, FileTreeNode parent) {
			this.name = name;
			this.lastModified = lastModified;
			this.parent = parent;
		}

		public int compareTo(FileTreeLeaf other) {
			return collator.compare(name, other.name);
		}

		public boolean isBold() {
			return bold;
		}

		public void setBold(boolean bold) {
			this.bold = bold;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("<html>");
			if(bold) {
				sb.append("<b>");
			}
			if(color != null) {
				sb.append("<font color=#");
				sb.append(color);
				sb.append(">");
			}
			sb.append(StringEscapeUtils.escapeHtml4(name));
			if(color != null) {
				sb.append("</font>");
			}
			if(bold) {
				sb.append("</b>");
			}
			sb.append("</html>");
			return sb.toString();
		}

		public FileTreeNode getRoot() {
			return (parent == null)? (FileTreeNode)this: getParent().getRoot();
		}

		public String getPath() {
			if(parent == null) {
				return "";
			} else {
				String parentPath = getParent().getPath();
				if(parentPath.equals(""))
					return name;
				else
					return parentPath + "/" + name;
			}
		}


		public FileTreeNode getParent() {
			return parent;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		void delete(File file) {
			file.delete();
		}
	}

	static class FileTreeNode extends FileTreeLeaf {
		TreeSet<FileTreeNode> nodes;
		TreeSet<FileTreeLeaf> leaves;

		FileTreeNode(String name, Date lastModified, FileTreeNode parent) {
			super(name, lastModified, parent);
			nodes = new TreeSet<FileTreeNode>();
			leaves = new TreeSet<FileTreeLeaf>();
		}

		void add(FileTreeNode fileTreeNode) {
			nodes.add(fileTreeNode);
		}
		void add(FileTreeLeaf fileTreeLeaf) {
			leaves.add(fileTreeLeaf);
		}


		int getLeafCount() {
			int value = 0;
			for(FileTreeNode node: nodes) {
				value += node.getLeafCount();
			}
			value += leaves.size();
			return value;
		}

		void trimNoleafNodes() {
			ArrayList<FileTreeNode> set = new ArrayList<FileTreeNode>();
			for(FileTreeNode node: nodes) {
				node.trimNoleafNodes();
				if(node.getLeafCount() == 0) {
					set.add(node);
				}
			}
			for(FileTreeNode e: set) {
				nodes.remove(e);
			}
		}

		DefaultMutableTreeNode makeDefaultMutableTreeNode() {
			DefaultMutableTreeNode thisTreeNode = new JTree.DynamicUtilTreeNode(this, new Object[0]);
			for(FileTreeNode node: nodes) {
				thisTreeNode.add(node.makeDefaultMutableTreeNode());
			}
			for(FileTreeLeaf node: leaves) {
				thisTreeNode.add(new DefaultMutableTreeNode(node, false));
			}
			return thisTreeNode;
		}

		public TreeSet<FileTreeNode> getNodes() {
			return nodes;
		}

		public TreeSet<FileTreeLeaf> getLeaves() {
			return leaves;
		}
		void delete(File file) {
			for(FileTreeNode node: nodes) {
				node.delete(new File(file, node.getName()));
			}
			for(FileTreeLeaf node: leaves) {
				node.delete(new File(file, node.getName()));
			}
			if(file.list().length == 0) {
				file.delete();
			}
		}
	}

	static DefaultMutableTreeNode makeDefaultMutableTreeNode(FileTreeNode tree) {
		return tree.makeDefaultMutableTreeNode();
	}

	void removeOthersRecursivly(DefaultMutableTreeNode node) {
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
		if(parent != null) {
			parent.removeAllChildren();
			parent.add(node);
			removeOthersRecursivly(parent);
		}	
	}

	public FileTree() throws IOException {
		setLayout(new BorderLayout());
		add(northPanel(), BorderLayout.NORTH);
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		add(splitPane);
		FileTreeNode emptyRootNode = new FileTreeNode("", new Date(), null);
		treeView = new JTree(emptyRootNode.makeDefaultMutableTreeNode());
		splitPane.setTopComponent(new JScrollPane(treeView));
		reportTextArea = new JTextArea(4, 40);        
		report = new PrintWriter(new sophie.io.JTextAreaWriter(reportTextArea));
		splitPane.setBottomComponent(new JScrollPane(reportTextArea));
		splitPane.setResizeWeight(1);
		menu = new JPopupMenu();
		JMenuItem item;
		//treeView.add(menu);
		menu.add(item=new JMenuItem("Rename"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileTreeLeaf leaf = (FileTreeLeaf)menuNode.getUserObject();
				boolean useConfirmDialog = true;
				if(useConfirmDialog) {
					JPanel panel = new JPanel();
					panel.add(new JLabel("New name"));
					JTextField nameField = new JTextField(20);
					nameField.setText(leaf.getName());
					panel.add(nameField);
					int reply = JOptionPane.showConfirmDialog(FileTree.this, panel, "Rename", JOptionPane.OK_CANCEL_OPTION);
					if(reply == JOptionPane.OK_OPTION) {
						leaf.setName(nameField.getText());
					}
				} else {
					String newName = JOptionPane.showInputDialog(FileTree.this, "New name", leaf.getName());
					if(newName != null) {
						leaf.setName(newName);
					}
				}
				treeView.clearSelection();
				treeView.updateUI();
			}
		});
		menu.add(item=new JMenuItem("Bold"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileTreeLeaf leaf = (FileTreeLeaf)menuNode.getUserObject();
				leaf.setBold(!leaf.isBold());
				treeView.clearSelection();
				treeView.updateUI();
			}
		});
		menu.add(item=new JMenuItem("Black"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileTreeLeaf leaf = (FileTreeLeaf)menuNode.getUserObject();
				leaf.setColor("000000");
				treeView.clearSelection();
				treeView.updateUI();
			}
		});
		menu.add(item=new JMenuItem("Red"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileTreeLeaf leaf = (FileTreeLeaf)menuNode.getUserObject();
				leaf.setColor("ff0000");
				treeView.clearSelection();
				treeView.updateUI();
			}
		});
		menu.add(item=new JMenuItem("Green"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileTreeLeaf leaf = (FileTreeLeaf)menuNode.getUserObject();
				leaf.setColor("00ff00");
				treeView.clearSelection();
				treeView.updateUI();
			}
		});
		menu.add(item=new JMenuItem("Blue"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileTreeLeaf leaf = (FileTreeLeaf)menuNode.getUserObject();
				leaf.setColor("0000ff");
				treeView.clearSelection();
				treeView.updateUI();
			}
		});
		menu.add(item=new JMenuItem("Move up"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(menuNode.getPreviousSibling() != null) {
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode)menuNode.getParent();
					int count = parent.getChildCount();
					DefaultMutableTreeNode[] siblings = new DefaultMutableTreeNode[count];
					for(int i = 0; i < siblings.length; i++) {
						siblings[i] = (DefaultMutableTreeNode)parent.getChildAt(i);
					}
					for(int i = 0; i < siblings.length; i++) {
						if(siblings[i] == menuNode) {
							DefaultMutableTreeNode temp = siblings[i];
							siblings[i] = siblings[i - 1];
							siblings[i - 1] = temp;
							break;
						}
					}
					for(int i = 0; i < siblings.length; i++) {
						parent.add(siblings[i]);
					}
				}
				treeView.clearSelection();
				treeView.updateUI();
			}
		});
		menu.add(item=new JMenuItem("Move down"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(menuNode.getNextSibling() != null) {
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode)menuNode.getParent();
					int count = parent.getChildCount();
					DefaultMutableTreeNode[] siblings = new DefaultMutableTreeNode[count];
					for(int i = 0; i < siblings.length; i++) {
						siblings[i] = (DefaultMutableTreeNode)parent.getChildAt(i);
					}
					for(int i = 0; i < siblings.length; i++) {
						if(siblings[i] == menuNode) {
							DefaultMutableTreeNode temp = siblings[i];
							siblings[i] = siblings[i + 1];
							siblings[i + 1] = temp;
							break;
						}
					}
					for(int i = 0; i < siblings.length; i++) {
						parent.add(siblings[i]);
					}
				}
				treeView.clearSelection();
				treeView.updateUI();

			}
		});
		menu.add(item=new JMenuItem("Remove"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				menuNode.removeFromParent();
				treeView.clearSelection();
				treeView.updateUI();

			}
		});
		menu.add(item=new JMenuItem("Remove others"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)menuNode.getParent();
				if(parent != null) {
					parent.removeAllChildren();
					parent.add(menuNode);
					removeOthersRecursivly(parent);
				}	
				treeView.clearSelection();
				treeView.updateUI();

			}
		});
		menu.add(item=new JMenuItem("Remove others recursivly"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeOthersRecursivly(menuNode);
				treeView.clearSelection();
				treeView.updateUI();

			}
		});
		treeView.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Point point = e.getPoint();
				if(e.getButton() == MouseEvent.BUTTON3) {
					TreePath path = treeView.getPathForLocation(point.x, point.y);
					if(path != null) {
						menuNode = (DefaultMutableTreeNode)path.getLastPathComponent();
						menu.show(treeView, point.x, point.y);
					}
				}
			}
		});
	}

	private JPanel northPanel() {        
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 4, 2, 4);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0; gbc.gridy = 0;
		JButton baseDirectoryButton = new JButton("Base Directory");
		panel.add(baseDirectoryButton, gbc);
		gbc.gridx++;
		gbc. weightx = 1.0;
		baseDirectoryTextField = new JFileTextField(30);
		panel.add(baseDirectoryTextField, gbc);
		gbc. weightx = 0;
		gbc.gridx++;
		JButton makeButton = new JButton("Make");
		panel.add(makeButton, gbc);
		gbc.gridx = 1; gbc.gridy++;
		gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
		JPanel buttonGroupPanel = new JPanel();
		JButton expandButton = new JButton("Expand");
		buttonGroupPanel.add(expandButton);
		JButton collapseButton = new JButton("Collapse");
		buttonGroupPanel.add(collapseButton);
		showRootNameCheckBox =  new JCheckBox("Show Root Name", true);
		buttonGroupPanel.add(showRootNameCheckBox);
		boldCheckBox = new JCheckBox("Bold");
		buttonGroupPanel.add(boldCheckBox);
		panel.add(buttonGroupPanel, gbc);
		gbc.gridx++; gbc.fill = GridBagConstraints.HORIZONTAL;
		JButton fileButton = new JButton("File");
		panel.add(fileButton, gbc);
		loadState();

		baseDirectoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseBaseDirectory();
			}
		});
		makeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				make();
			}
		});

		expandButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				expand();
			}
		});
		collapseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				collapse();
			}
		});
		fileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				file();
			}
		});
		return panel;
	}

	void browseBaseDirectory() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Choose Base directory");
		int returnVal = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			baseDirectoryTextField.setText(chooser.getSelectedFile().getPath().replace('\\', '/'));
		}
	}

	private void loadState() {
		fileTreeState = FileTreeState.load();
		baseDirectoryTextField.setText(fileTreeState.getBaseDirectory());

	}

	private void storeState() {
		fileTreeState.setBaseDirectory(baseDirectoryTextField.getText());
		fileTreeState.store();
	}

	void makeTree(File directory, FileTreeNode fileTreeNode, String path, boolean stay) throws java.io.IOException, java.security.GeneralSecurityException {
		String pathPrefix = (path.length() == 0)? path: path + "/";
		// tree are made in the order of as follows:
		// 1 - directories
		// 2 - files
		TreeSet<String> nameSet = new TreeSet<String>();
		if(directory != null) {
			for(String name: directory.list()) {
				nameSet.add(name);
			}
		}
		// 1 - directories
		for(String name: nameSet) {
			File subfile = null;
			if(directory != null)
				subfile = new File(directory, name);
			if(subfile.isDirectory()) {
				FileTreeNode subfileTreeNode = new FileTreeNode(name, new Date(subfile.lastModified()), fileTreeNode);
				subfileTreeNode.setBold(bold);
				fileTreeNode.add(subfileTreeNode);
				makeTree(subfile, subfileTreeNode,  pathPrefix + name, true);
			}
		}
		// 2 - files
		for(String name: nameSet) {
			File subfile = null;
			if(directory != null)
				subfile = new File(directory, name);
			if(subfile != null && subfile.exists()) {
				// both exists
				if(!subfile.isDirectory()) {
					FileTreeLeaf subfileTreeLeaf = new FileTreeLeaf(name, new Date(subfile.lastModified()), fileTreeNode);
					subfileTreeLeaf.setBold(bold);
					fileTreeNode.add(subfileTreeLeaf);
				}
			}
		}
	}

	private void showTree() throws java.io.IOException, CancelledByTheUserException, java.security.GeneralSecurityException, InterruptedException {
		baseFileName = baseDirectoryTextField.getText().trim();
		baseFile = new File(baseFileName);
		if(!baseFile.exists()) {
			throw new FileNotFoundException(baseFileName);                     
		}
		bold = boldCheckBox.isSelected();
		showRootName = showRootNameCheckBox.isSelected();
		FileTreeNode fileTreeNode = new FileTreeNode(showRootName? baseFile.getName(): "", new Date(baseFile.lastModified()), null);
		fileTreeNode.setBold(bold);
		makeTree(baseFile, fileTreeNode, "", false);
		//fileTreeNode.trimNoleafNodes();
		treeView.setModel(new DefaultTreeModel(fileTreeNode.makeDefaultMutableTreeNode()));
		DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
		selectionModel.setSelectionMode(DefaultTreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		treeView.setSelectionModel(selectionModel);
		treeView.setBorder(new EmptyBorder(4, 4, 4, 4));
		updateUI();
	}

	void make() {
		new Thread() {
			public void run() {
				try {
					reportTextArea.setText("");
					showTree();
				} catch(InterruptedException e) {
				} catch(Exception e) {
					if(!(e instanceof CancelledByTheUserException)) {
						JOptionPane.showMessageDialog(FileTree.this, e, TITLE, JOptionPane.ERROR_MESSAGE);
						e.printStackTrace(System.err);
					}
				} catch(Error e) {
					JOptionPane.showMessageDialog(FileTree.this, e, TITLE, JOptionPane.ERROR_MESSAGE);
					e.printStackTrace(System.err);
				}
			}
		}.start();
	}

	void expand() {
		if(treeView != null) {
			for (int i = 0; i < treeView.getRowCount(); i++) {
				treeView.expandRow(i);
			}
		}
	}

	void collapse() {
		if(treeView != null) {
			for (int i = 0; i < treeView.getRowCount(); i++) {
				treeView.collapseRow(i);
			}
		}
	}

	void file() {
		try {
			if(fileTreeTextTreeFrame == null) {
				FileTreeTextTree fileTreeTextTree = new FileTreeTextTree(this);
  				JFrame f = fileTreeTextTreeFrame = new JFrame(FileTree.class.getSimpleName() + "/" + "File" + " " + VERSION);
  				f.setIconImages(IconList.of(FileTree.class));
  				f.getContentPane().add(fileTreeTextTree, BorderLayout.CENTER);
  				f.pack();
  				f.setLocationRelativeTo(this);
  				f.addWindowListener(new WindowListener() {

  					@Override
  					public void windowOpened(WindowEvent e) {
  					}

  					@Override
  					public void windowClosing(WindowEvent e) {
  						f.dispose(); // windowClosed(WindowEvent e) will be called.
  					}

  					@Override
  					public void windowClosed(WindowEvent e) {
  						fileTreeTextTreeFrame = null;
  					}

  					@Override
  					public void windowIconified(WindowEvent e) {
  					}

  					@Override
  					public void windowDeiconified(WindowEvent e) {
  					}

  					@Override
  					public void windowActivated(WindowEvent e) {
  					}

  					@Override
  					public void windowDeactivated(WindowEvent e) {
  					}
  				});
  			}
  			fileTreeTextTreeFrame.setVisible(true);
		} catch(Throwable t) {
			ErrorMessageDialog.showMessageDialog(this, t, TITLE);
		}
	}

	public static void main(String args[]) {
		try {
			UIManager.put("OptionPane.cancelButtonText", "Cancel");
			JFrame frame = new JFrame(TITLE);
			frame.setIconImages(IconList.of(FileTree.class));
			final FileTree fileTree = new FileTree();
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					fileTree.storeState();
					System.exit(0);
				}
			});         
			frame.getContentPane().add(fileTree);
			frame.pack();
			frame.setVisible(true);
		} catch(Throwable t) {
			ErrorMessageDialog.showMessageDialog(null, t, TITLE);
		}
	}
}
