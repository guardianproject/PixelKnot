package info.guardianproject.pixelknot;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class FileManager {

    private static final boolean LOGGING = false;
    private static final String LOGTAG = "FileManager";

    private final File mDirectory; // Directory where files are saved

    public FileManager(Context context) {
        mDirectory = context.getCacheDir();
        cleanupFiles();
    }

    public File createFileForJob(String jobId) throws IOException {
        File tempFile = File.createTempFile("pixelknot", ".jpg", mDirectory);
        if (LOGGING)
            Log.d(LOGTAG, "created file: " + tempFile.getAbsolutePath());
        return tempFile;
    }

    private void cleanupFiles() {
        for (File file : mDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("pixelknot");
            }
        })) {
            file.delete();
        }
    }
}
