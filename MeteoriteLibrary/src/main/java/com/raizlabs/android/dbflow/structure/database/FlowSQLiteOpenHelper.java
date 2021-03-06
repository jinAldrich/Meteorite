package com.raizlabs.android.dbflow.structure.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;

import java.io.File;

/**
 * Description: Wraps around the {@link SQLiteOpenHelper} and provides extra features for use in this library.
 */
public class FlowSQLiteOpenHelper extends SQLiteOpenHelper implements OpenHelper {

    private DatabaseHelperDelegate databaseHelperDelegate;
    private AndroidDatabase androidDatabase;

    public FlowSQLiteOpenHelper(@NonNull DatabaseDefinition databaseDefinition,
                                @NonNull DatabaseHelperListener listener) {
        super(FlowManager.getContext(), databaseDefinition.isInMemory() ? null :FlowManager.getDatabaseDir()+ File.separator+databaseDefinition.getDatabaseFileName(), null, databaseDefinition.getDatabaseVersion());

        OpenHelper backupHelper = null;
        if (databaseDefinition.backupEnabled()) {
            // Temp database mirrors existing
            backupHelper = new BackupHelper(FlowManager.getContext(),
                DatabaseHelperDelegate.getTempDbFileName(databaseDefinition),
                databaseDefinition.getDatabaseVersion(), databaseDefinition);
        }

        databaseHelperDelegate = new DatabaseHelperDelegate(listener, databaseDefinition, backupHelper);
    }

    @Override
    public void performRestoreFromBackup() {
        databaseHelperDelegate.performRestoreFromBackup();
    }

    @Nullable
    @Override
    public DatabaseHelperDelegate getDelegate() {
        return databaseHelperDelegate;
    }

    @Override
    public boolean isDatabaseIntegrityOk() {
        return databaseHelperDelegate.isDatabaseIntegrityOk();
    }

    @Override
    public void backupDB() {
        databaseHelperDelegate.backupDB();
    }

    @NonNull
    @Override
    public DatabaseWrapper getDatabase() {
        if (androidDatabase == null || !androidDatabase.getDatabase().isOpen()) {
            androidDatabase = AndroidDatabase.from(getWritableDatabase());
        }
        return androidDatabase;
    }

    /**
     * Set a listener to listen for specific DB events and perform an action before we execute this classes
     * specific methods.
     *
     * @param listener
     */
    public void setDatabaseListener(@Nullable DatabaseHelperListener listener) {
        databaseHelperDelegate.setDatabaseHelperListener(listener);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        databaseHelperDelegate.onCreate(AndroidDatabase.from(db));
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        databaseHelperDelegate.onUpgrade(AndroidDatabase.from(db), oldVersion, newVersion);
    }

    @Override
    public void onOpen(@NonNull SQLiteDatabase db) {
        databaseHelperDelegate.onOpen(AndroidDatabase.from(db));
    }

    @Override
    public void onDowngrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        databaseHelperDelegate.onDowngrade(AndroidDatabase.from(db), oldVersion, newVersion);
    }

    @Override
    public void closeDB() {
        getDatabase();
        androidDatabase.getDatabase().close();
    }

    /**
     * Simple helper to manage backup.
     */
    private class BackupHelper extends SQLiteOpenHelper implements OpenHelper {

        private AndroidDatabase androidDatabase;
        private final BaseDatabaseHelper baseDatabaseHelper;

        public BackupHelper(Context context, String name, int version, DatabaseDefinition databaseDefinition) {
            super(context, name, null, version);
            this.baseDatabaseHelper = new BaseDatabaseHelper(databaseDefinition);
        }

        @NonNull
        @Override
        public DatabaseWrapper getDatabase() {
            if (androidDatabase == null) {
                androidDatabase = AndroidDatabase.from(getWritableDatabase());
            }
            return androidDatabase;
        }

        @Override
        public void performRestoreFromBackup() {
        }

        @Nullable
        @Override
        public DatabaseHelperDelegate getDelegate() {
            return null;
        }

        @Override
        public boolean isDatabaseIntegrityOk() {
            return false;
        }

        @Override
        public void backupDB() {
        }

        @Override
        public void setDatabaseListener(@Nullable DatabaseHelperListener helperListener) {
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            baseDatabaseHelper.onCreate(AndroidDatabase.from(db));
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            baseDatabaseHelper.onUpgrade(AndroidDatabase.from(db), oldVersion, newVersion);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            baseDatabaseHelper.onOpen(AndroidDatabase.from(db));
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            baseDatabaseHelper.onDowngrade(AndroidDatabase.from(db), oldVersion, newVersion);
        }

        @Override
        public void closeDB() {
        }
    }

}
