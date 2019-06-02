package com.example.banany_23;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;


public class AddServerActivity extends AppCompatActivity {

    public static Toolbar myToolbar;
    public EditText editTextHostName;
    public EditText editTextPassword;
    public EditText editTextPort;
    public EditText editTextRemoteDir;
    public EditText editTextServerName;
    public EditText editTextUsername;
    public String hostName;
    public String localDir;
    public String password;
    int port;
    public String remoteDir;
    public String serverName;
    public String username;

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(C0322R.menu.create_server_action_bar, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == 16908322){
            finish();
            return true;
        } else if (item.getItemId() == C0322R.id.saveServer){
            saveServerHandler();
            return true;
        } else if (item.getItemId() != C0322R.id.testConnection){
            return false;
        } else {
            testConnectionHandler();
            return true;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0322R.layout.activity_add_server);
        this.editTextServerName = (EditText) findViewById(C0322R.id.editTextServerName);
        this.editTextHostName = (EditText) findViewById(C0322R.id.editTextHostName);
        this.editTextPort = (EditText) findViewById(C0322R.id.editTextPort);
        this.editTextUsername = (EditText) findViewById(C0322R.id.editTextUsername);
        this.editTextPassword = (EditText) findViewById(C0322R.id.editTextPassword);
        this.editTextRemoteDir = (EditText) findViewById(C0322R.id.editTextRemoteDir);
        myToolbar = (Toolbar) findViewById(C0322R.id.mainToolbar);
        myToolbar.setTitle((CharSequence) "Add new Server");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void saveServerHandler(){
        this.serverName = this.editTextServerName.getText().toString().trim();
        if (this.serverName.isEmpty()){
            Toast.makeText(this, "Please enter server name", 0).show();
        }
        else if (new FtpDatabaseHelper(this).getAllServers().contains(this.serverName)){
            Toast.makeText(this, "Server name already exists!", 0).show();
        }
        else {
            this.hostName = this.editTextHostName.getText().toString().trim();
            if (this.editTextPort.getText().toString().trim().isEmpty()){
                this.port = 21;
            } else {
                this.port = Integer.parseInt(this.editTextPort.getText().toString().trim());
            }
            this.username = this.editTextUsername.getText().toString().trim();
            this.password = this.editTextPassword.getText().toString().trim();
            this.remoteDir = this.editTextRemoteDir.getText().toString().trim();
            new TestConnection().execute(new Object[0]);
        }
    }

    public void addToDatabase(){
        FtpDatabaseHelper helper = new FtpDatabaseHelper(this);
        if (this.remoteDir.isEmpty()){
            this.remoteDir = TestConnection.REMOTE_DIR_STATIC;
        }
        helper.insertServer("BANANY", "192.168.1.165", 2221, "banany", "banany", "/storage/emulated/0/", "/");
        finish();
    }

    public void testConnectionHandler(){
        String serverName = this.editTextServerName.getText().toString().trim();
        if (serverName.isEmpty()){
            Toast.makeText(this, "Please enter a server name", 0).show();
        } else if (new FtpDatabaseHelper(this).getAllServers().contains(serverName)){
            Toast.makeText(this, "Server name already exists!", 0).show();
        } else {
            String hostName = this.editTextHostName.getText().toString().trim();
            if (!this.editTextPort.getText().toString().trim().isEmpty()){
                int parseInt = Integer.parseInt(this.editTextPort.getText().toString().trim());
            }
            String username = this.editTextUsername.getText().toString().trim();
            String password = this.editTextPassword.getText().toString().trim();
            String remoteDir = this.editTextRemoteDir.getText().toString().trim();
            new TestConnection().execute(new Object[0]);
        }
    }
}
