package com.NoaoN.voiceRecorderWithNotes.helper_classes;

import java.io.File;

/**
 * Singleton class. Created to share recordings directory location between the activities.
 * RecordingActivity creates the instance, and PlayRecWithNotes uses same instance to get
 * recordings' directory.
 */
public class DirectorySingleton {
    private static DirectorySingleton ds = null;
    private File directory = null;

    /**
     * Get instance of DirectorySingleton.
     * @return instance of DirectorySingleton.
     */
    public static DirectorySingleton getInstance(){
        if(ds == null){
            ds = new DirectorySingleton();
        }
        return ds;
    }

    /**
     * Constructor.
     */
    private DirectorySingleton(){
    }

    /**
     * Get directory.
     * @return directory.
     */
    public File getDirectory(){
        return this.directory;
    }

    /**
     * Set recordings directory.
     * @param directory - directory to save recordings to.
     */
    public void setDirectory(File directory){
        this.directory = directory;
    }

}
