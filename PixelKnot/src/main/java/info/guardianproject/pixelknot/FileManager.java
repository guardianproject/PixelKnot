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
        File tempFile = new File(mDirectory, "pixelknot_" + jobId + ".jpg");
        if (!tempFile.exists())
            tempFile.createNewFile();
        if (LOGGING)
            Log.d(LOGTAG, "created file: " + tempFile.getAbsolutePath());
        return tempFile;
    }

    // Take ownership of the temporary file
    public File moveInputFileToJob(File file, String jobId) {
        File newFile = new File(mDirectory, "pixelknot_i_" + jobId + ".jpg");
        file.renameTo(newFile);
        return newFile;
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
