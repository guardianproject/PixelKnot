package info.guardianproject.pixelknot;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class PixelKnotContentProvider extends ContentProvider {

    private static final String[] DEFAULT_COLUMNS = {
            OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
    };

    public static final Uri CONTENT_URI = Uri.parse("content://info.guardianproject.pixelknot/");

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        StegoEncryptionJob job = getJobFromUri(uri);
        if (job == null)
            return null;

        if (projection == null) {
            projection = DEFAULT_COLUMNS;
        }
        ArrayList<String> cols = new ArrayList<>();
        ArrayList<Object> values = new ArrayList<>();
        int i = 0;
        for (String col : projection) {
            if (OpenableColumns.DISPLAY_NAME.equals(col)) {
                cols.add(col);
                values.add(job.getOutputImageName());
            } else if (OpenableColumns.SIZE.equals(col)) {
                cols.add(col);
                values.add(job.getOutputLength());
            }
        }
        final MatrixCursor cursor = new MatrixCursor(cols.toArray(new String[0]), 1);
        cursor.addRow(values);
        return cursor;
    }

    private StegoEncryptionJob getJobFromUri(Uri uri) {
        String jobId = uri.getLastPathSegment();
        if (TextUtils.isEmpty(jobId))
            return null;

        return (StegoEncryptionJob) App.getInstance().getJobById(jobId);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return "image/jpeg";
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        StegoEncryptionJob job = getJobFromUri(uri);
        if (job == null)
            return null;
        try {
            return ParcelFileDescriptor.open(job.getOutputFile(), ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (IOException e) {
            throw new FileNotFoundException("Failed to open " + uri.toString());
        }
    }
}
