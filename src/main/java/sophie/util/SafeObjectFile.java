package sophie.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import sophie.io.FileUtilities;

public class SafeObjectFile {
	private static String Delimiter = "_limit_";
	
	private SafeObjectFile() {
	}
	
	private static File[] getStateFiles(File baseDirectory, String baseName) {
        return new File[] {new File(baseDirectory, baseName + ".1"), new File(baseDirectory, baseName + ".2")};
    }
	
	public static Object loadObject(File file) {
		Object object = null;
    	try(ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
    		object = in.readObject();
    		String delimiter = (String)in.readObject();
    		if(!delimiter.equals(Delimiter)) {
    			object = null;
    		}
    	} catch(Exception e) {
    		//Logger.getInstance().log("Can't load the state: " + e);
    		object = null;
    	}
    	return object;
	}
	
	public static void storeObject(File file, Object object) throws FileNotFoundException, IOException {
		try(ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			out.writeObject(object);
			out.writeObject(Delimiter);
		}
	}
	
	public static String getDelimiter() {
		return Delimiter;
	}
	
	public static Object load(File baseDirectory, String baseName) {
		Object object = null;
    	File[] objectFiles = getStateFiles(baseDirectory, baseName);
    	int fileIndex;
    	if(objectFiles[0].exists() && objectFiles[1].exists()) {
    		if(objectFiles[0].lastModified() > objectFiles[1].lastModified()) {
    			fileIndex = 0;
    		} else {
    			fileIndex = 1;
    		}
    	} else if(objectFiles[0].exists()) {
    		fileIndex = 0;
    	} else if(objectFiles[1].exists()) {
    		fileIndex = 1;
    	} else {
    		fileIndex = -1;
    	}
    	if(fileIndex >= 0) {
    		object = loadObject(objectFiles[fileIndex]);
    		if(object != null) {
    			int otherIndex = (fileIndex + 1) & 1;
    			if(!objectFiles[otherIndex].exists() ||
    			   (objectFiles[otherIndex].length() != objectFiles[fileIndex].length()) || 
    			   (Math.abs(objectFiles[otherIndex].lastModified() - objectFiles[fileIndex].lastModified()) > 1000)) {
    				
    				try {
						FileUtilities.copyFile(objectFiles[fileIndex], objectFiles[otherIndex]);
					} catch (IOException e) {
						//Logger.getInstance().log("Can't duplicate state file: " + e);
					}
    			}
    		} else {
    			fileIndex = (fileIndex + 1) & 1;
    			object = loadObject(objectFiles[fileIndex]);
    			if(object != null) {
        			int otherIndex = (fileIndex + 1) & 1;
        			try {
        				FileUtilities.copyFile(objectFiles[fileIndex], objectFiles[otherIndex]);
        			} catch (IOException e) {
        				//Logger.getInstance().log("Can't duplicate state file: " + e);
        			}
    			}
    		}
    	}
    	return object;
    }

	public static Object load(File base) {
		return load(base.getParentFile(), base.getName());
	}
	
	public static void store(File baseDirectory, String baseName, Object object) throws IOException {
		File[] stateFiles = getStateFiles(baseDirectory, baseName);
		FileUtilities.makeParentDirectories(stateFiles[0]);
		for(File file: stateFiles) {
			storeObject(file, object);
		}
	}
	
	public static void store(File base, Object object) throws IOException {
		store(base.getParentFile(), base.getName(), object);
	}

	public static void delete(File baseDirectory, String baseName) {
		File[] stateFiles = getStateFiles(baseDirectory, baseName);
		FileUtilities.makeParentDirectories(stateFiles[0]);
		for(File file: stateFiles) {
			file.delete();
		}
	}
	
	public static long lastModified(File baseDirectory, String baseName) {
		long lastModified = -1;
    	File[] objectFiles = getStateFiles(baseDirectory, baseName);
    	int fileIndex;
    	if(objectFiles[0].exists() && objectFiles[1].exists()) {
    		if(objectFiles[0].lastModified() > objectFiles[1].lastModified()) {
    			fileIndex = 0;
    		} else {
    			fileIndex = 1;
    		}
    	} else if(objectFiles[0].exists()) {
    		fileIndex = 0;
    	} else if(objectFiles[1].exists()) {
    		fileIndex = 1;
    	} else {
    		fileIndex = -1;
    	}
    	if(fileIndex >= 0) {
    		lastModified = objectFiles[fileIndex].lastModified();
    		Object object = loadObject(objectFiles[fileIndex]);
    		if(object == null) {
    			fileIndex = (fileIndex + 1) & 1;
        		lastModified = objectFiles[fileIndex].lastModified();
    			object = loadObject(objectFiles[fileIndex]);
    			if(object == null) {
    				lastModified = -1;
    			}
    		}
    	}
    	return lastModified;
    }
}