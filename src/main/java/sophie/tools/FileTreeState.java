package sophie.tools;

import java.io.File;
import java.io.Serializable;

import javax.swing.JOptionPane;

import sophie.util.SafeObjectFile;

class FileTreeState implements Serializable {
	private static final long serialVersionUID = -1091869594850842669L;
	private static final String STATE_FILE = "FileTree.state";
	private transient static File stateBase;
    String baseDirectory; 
    String textTreeFile;
    boolean unicodeTreeDrawing;
    boolean unicodeIcon;
    boolean directorySuffix;
    int leadingSpacing;
    int trailingSpacing;
    int iconSpacing;
    int nameSpacing;
    boolean htmlEnvelope;
    boolean preEnvelope;

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(String local) {
        this.baseDirectory = local;
    }

    public String getTextTreeFile() {
		return (textTreeFile != null)? textTreeFile: "";
	}

	public void setTextTreeFile(String textTreeFile) {
		this.textTreeFile = textTreeFile;
	}

	public boolean isUnicodeTreeDrawing() {
		return unicodeTreeDrawing;
	}

	public void setUnicodeTreeDrawing(boolean unicodeTreeDrawing) {
		this.unicodeTreeDrawing = unicodeTreeDrawing;
	}

	public boolean isUnicodeIcon() {
		return unicodeIcon;
	}

	public void setUnicodeIcon(boolean unicodeIcon) {
		this.unicodeIcon = unicodeIcon;
	}

	public boolean isDirectorySuffix() {
		return directorySuffix;
	}

	public void setDirectorySuffix(boolean directorySuffix) {
		this.directorySuffix = directorySuffix;
	}

	public int getLeadingSpacing() {
		return leadingSpacing;
	}
	public void setLeadingSpacing(int trunkSpacing) {
		this.leadingSpacing = trunkSpacing;
	}

	public int getTrailingSpacing() {
		return trailingSpacing;
	}

	public void setTrailingSpacing(int branchSpacing) {
		this.trailingSpacing = branchSpacing;
	}

	public int getIconSpacing() {
		return iconSpacing;
	}

	public void setIconSpacing(int iconSpacing) {
		this.iconSpacing = iconSpacing;
	}

	public int getNameSpacing() {
		return nameSpacing;
	}

	public void setNameSpacing(int nameSpacing) {
		this.nameSpacing = nameSpacing;
	}

	public boolean isHtmlEnvelope() {
		return htmlEnvelope;
	}

	public void setHtmlEnvelope(boolean htmlEnvelope) {
		this.htmlEnvelope = htmlEnvelope;
	}

	public boolean isPreEnvelope() {
		return preEnvelope;
	}

	public void setPreEnvelope(boolean preEnvelope) {
		this.preEnvelope = preEnvelope;
	}

	private static File getStateBase() {
        if(stateBase == null) {
            stateBase = new File(System.getProperty("user.home").replace('\\', '/') + "/AppData/Local/sophie/tools");
            if(!stateBase.exists()) {
            	stateBase.mkdirs();
            }
        }
        return stateBase;
    }

    void store() {
        try {
            SafeObjectFile.store(getStateBase(), STATE_FILE, this);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(null, "Can't store the state.", FileTree.TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }
    
    static FileTreeState load() {
        Object state = SafeObjectFile.load(getStateBase(), STATE_FILE);
        if(!(state instanceof FileTreeState)) {
            JOptionPane.showMessageDialog(null, "Can't load the state.  The default will be assumed.", FileTree.TITLE,  JOptionPane.WARNING_MESSAGE);
        	state = new FileTreeState();
        }
        return (FileTreeState)state;
    }
    
	FileTreeState() {
	    baseDirectory = ""; 
	    textTreeFile = "";
	    unicodeTreeDrawing = true;
	}

}
