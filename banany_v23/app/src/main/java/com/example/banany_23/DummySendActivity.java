package com.example.banany_23;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DummySendActivity extends Activity{

    public ListView listView;
    public ArrayList<String> uploadList = new ArrayList();

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(C0322R.layout.pick_server);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String path;
        if("android.intent.action.SEND".equals(action) && type != null){
            Uri uri = (Uri) intent.getParcelableArrayExtra("android.intent.extra.STREAM");
            if (uri == null){
                Toast.makeText(this, "myBANANY could not handle the share..Please choose a different appliaction", 1).show();
                finish();
                return;
            }
            path = new File(uri.getPath()).getPath();
            Log.d("banany", "Got path: " + path);
            this.uploadList.add(path);
        } else if ("android.intent.action.SEND_MULTIPLE".equals(action) && type != null){
            Iterator it = intent.getParcelableArrayListExtra("android.intent.extra.STREAM").iterator();
            while (it.hasNext()){
                Uri i = (Uri) it.next();
                if(i == null){
                    Toast.makeText(this, "myBANANY could not handle the share..Please choose a different appliaction", 1).show();
                    finish();
                    return;
                }
                path = new File(i.getPath()).getPath();
                Log.d("banany", "Got Path: " + path);
                this.uploadList.add(path);
            }
        }
        else if ("android.intent.action.SEND_MULTIPLE".equals(action) && type != null){
            Iterator it = intent.getParcelableArrayListExtra("android.intent.extra.STREAM").iterator();
            while (it.hasNext()){
                Uri i = (Uri) it.next();
                if (i == null){
                    Toast.makeText(this, "myBanany could not handle the share...Please choose a different application", 1).show();
                    finish();
                    return;
                }
                path = new File (i.getPath()).getPath();
                Log.d("banany", "Got path: " + path);
                this.uploadList.add(path);
            }
        }
        final ArrayList<String> list = new FtpDatabaseHelper(this).getAllServers();
        ArrayAdapter adapter = new ArrayAdapter(this, C0322R.layout.pick_server_listitem, list);
        this.listView = (ListView) findViewById(C0322R.id.listViewPickServer);
        this.listView.setAdapter(adapter);
        this.listView.setOnItemClickListener(new OnItemClickListener(){
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                Cursor cursor = new FtpDatabaseHelper(DummySendActivity.this).getServerDetails((String) list.get(position));
                cursor.moveToFirst();
                Intent intent = new Intent (DummySendActivity.this, UploadServiceHandler.class);
                intent.putExtra("IP", cursor.getString(cursor.getColumnIndex(FtpDatabaseHelper.COLUMN_HOST_NAME)));
                intent.putExtra("USERNAME", cursor.getString(cursor.getColumnIndex(FtpDatabaseHelper.COLUMN_USERNAME)));
                intent.putExtra("PASSWORD", cursor.getString(cursor.getColumnIndex(FtpDatabaseHelper.COLUMN_PASSWORD)));
                intent.putExtra("PORT", cursor.getInt(cursor.getColumnIndex(FtpDatabaseHelper.COLUMN_PORT)));
                intent.putExtra("REMOTE_SAVE_PATH", cursor.getString(cursor.getColumnIndex(FtpDatabaseHelper.COLUMN_REMOTE_DIR)));
                intent.putExtra("UPLOAD_LIST", DummySendActivity.this.uploadList);
                DummySendActivity.this.startService(intent);
                DummySendActivity.this.finish();
            }
        });
    }
}
