package org.calyxos.providers.crossprofile;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.FileUtils;
import android.os.UserManager;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;

import com.android.internal.content.FileSystemProvider;

import java.io.File;
import java.io.FileNotFoundException;

public class CrossProfileProvider extends FileSystemProvider {
    private static final String TAG = "CrossProfileProvider";
    private static final String AUTHORITY = "org.calyxos.providers.crossprofile.documents";

    private static final String DOC_ID_ROOT = "crossprofile";
    private static final String ROOT_DIR = "/data/local/traces";

    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_FLAGS,
            Root.COLUMN_DOCUMENT_ID,
    };

    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE,
            Document.COLUMN_LAST_MODIFIED,
    };

    @Override
    public boolean onCreate() {
        super.onCreate(DEFAULT_DOCUMENT_PROJECTION);
        return true;
    }

    @Override
    public Cursor queryRoots(String[] projection) {
        if (!UserManager.get(getContext()).isManagedProfile()) {
            return null;
        }

        final MatrixCursor result = new MatrixCursor(projection == null ? DEFAULT_ROOT_PROJECTION
                : projection);
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, DOC_ID_ROOT);
        row.add(Root.COLUMN_DOCUMENT_ID, DOC_ID_ROOT);
        row.add(Root.COLUMN_TITLE, getContext().getString(
                R.string.root_crossprofile));
        row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE
                | Root.FLAG_LOCAL_ONLY
                | Root.FLAG_SUPPORTS_IS_CHILD);
        return result;
    }

    @Override
    protected Uri buildNotificationUri(String docId) {
        return DocumentsContract.buildChildDocumentsUri(AUTHORITY, docId);
    }

    @Override
    protected String getDocIdForFile(File file) {
        return DOC_ID_ROOT + ":" + file.getName();
    }

    @Override
    protected File getFileForDocId(String documentId, boolean visible)
            throws FileNotFoundException {
        if (DOC_ID_ROOT.equals(documentId)) {
            return new File(ROOT_DIR);
        } else {
            final int splitIndex = documentId.indexOf(':', 1);
            final String name = documentId.substring(splitIndex + 1);
            if (splitIndex == -1 || !DOC_ID_ROOT.equals(documentId.substring(0, splitIndex))) {
                throw new FileNotFoundException("Invalid document ID: " + documentId);
            }
            final File file = new File(ROOT_DIR, name);
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: " + documentId);
            }
            return file;
        }
    }
}
