package com.weng.weng;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;

/** 极简 FileProvider（不引 androidx）：供安装器读取下载好的更新 APK */
public class UpdateFileProvider extends ContentProvider {

    @Override
    public boolean onCreate() { return true; }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File dir = getContext().getExternalFilesDir(null);
        if (dir == null) dir = getContext().getFilesDir();
        File f = new File(dir, "update.apk");
        if (!f.exists() || TextUtils.isEmpty(mode)) throw new FileNotFoundException(f.getPath());
        return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public String getType(Uri uri) {
        return "application/vnd.android.package-archive";
    }

    @Override
    public Cursor query(Uri uri, String[] p, String s, String[] a, String o) { return null; }

    @Override
    public Uri insert(Uri uri, ContentValues v) { return null; }

    @Override
    public int delete(Uri uri, String s, String[] a) { return 0; }

    @Override
    public int update(Uri uri, ContentValues v, String s, String[] a) { return 0; }
}
