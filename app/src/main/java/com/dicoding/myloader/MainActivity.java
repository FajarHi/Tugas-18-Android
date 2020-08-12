package com.dicoding.myloader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    public static final String TAG = "ContactApp";
    private ListView lvContact;
    private ProgressBar progressBar;
    private ContactAdapter adapter;
    private static final int CONTACT_LOAD_ID = 110;
    private static final int CONTACT_PHONE_ID = 120;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvContact= findViewById(R.id.lv_contact);
        progressBar= findViewById(R.id.progress_bar);
        lvContact.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        adapter = new ContactAdapter(MainActivity.this, null, true);
        lvContact.setAdapter(adapter);
        lvContact.setOnItemClickListener(this);
        getSupportLoaderManager().initLoader(CONTACT_LOAD_ID, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader mCursorLoader = null;
        if (id == CONTACT_LOAD_ID) {
            String[] projectionFields = new String[] {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_URI
            };
            mCursorLoader = new CursorLoader(MainActivity.this,
                    ContactsContract.Contacts.CONTENT_URI,
                    projectionFields,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1",
                    null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        }
        if (id == CONTACT_PHONE_ID) {
            String[] phoneProjectionFields = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER};
            mCursorLoader = new CursorLoader(MainActivity.this,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    phoneProjectionFields,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                            ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE + " AND " +
                            ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1",
                    new String[] {args.getString("id")},
                    null);
        }
        return mCursorLoader;

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "LoadFinished");
        if (loader.getId() == CONTACT_LOAD_ID) {
            if (data.getCount() > 0) {
                lvContact.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                adapter.swapCursor(data);
            }
        }
        if (loader.getId() == CONTACT_PHONE_ID) {
            String contactNumber = null;
            if (data.moveToFirst()) {
                contactNumber = data.getString(data.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contactNumber));
            startActivity(dialIntent);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == CONTACT_LOAD_ID) {
            lvContact.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            adapter.swapCursor(null);
            Log.d(TAG, "LoaderReset");
        }
    }
}
