package org.liverliu.neteasemusichacker;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    private void writeFile(File file, String content) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file.toString());
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button activateButton = (Button) findViewById(R.id.button_activate);
        Button restoreButton = (Button) findViewById(R.id.button_restore);
        final EditText serverIp = (EditText) findViewById(R.id.editText2);
        serverIp.setTextColor(Color.RED);
        File dir = new File("/data/data/org.liverliu.neteasemusichacker/");
        if(!dir.exists()) {
            dir.mkdir();
        }
        final File file = new File("/data/data/org.liverliu.neteasemusichacker/ip");
        if(file.exists()) {
            try{
                InputStream instream = new FileInputStream("/data/data/org.liverliu.neteasemusichacker/ip");
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                while (( line = buffreader.readLine()) != null) {
                    serverIp.setText(line);
                }
                buffreader.close();
                inputreader.close();
                instream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            serverIp.setText("45.78.6.233");
            writeFile(file, "45.78.6.233");
        }

        activateButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Process p;
                try {
                    // Preform su to get root privledges
                    p = Runtime.getRuntime().exec("su");

                    // Attempt to write a file to a root-only
                    DataOutputStream os = new DataOutputStream(p.getOutputStream());

                    os.writeBytes("mount -o remount,rw /system\n");
                    os.writeBytes("echo \"127.0.0.1 localhost\" >/system/etc/hosts\n");
                    os.writeBytes("echo \"" + serverIp.getText() + " music.163.com\" >>/system/etc/hosts\n");
                    os.writeBytes("mount -o remount,ro /system\n");
                    // Close the terminal
                    os.writeBytes("exit\n");
                    os.flush();
                    try {
                        p.waitFor();
                        if (p.exitValue() != 255) {
                            writeFile(file, serverIp.getText().toString());
                            // TODO Code to run on success
                            toastMessage("启用成功");
                        } else {
                            // TODO Code to run on unsuccessful
                            toastMessage("未root！");
                        }
                    } catch (InterruptedException e) {
                        // TODO Code to run in interrupted exception
                        toastMessage("未root！");
                    }
                } catch (IOException e) {
                    // TODO Code to run in input/output exception
                    toastMessage("未root！");
                }

            }

        });

        restoreButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Process p;
                try {
                    // Preform su to get root privledges
                    p = Runtime.getRuntime().exec("su");

                    // Attempt to write a file to a root-only
                    DataOutputStream os = new DataOutputStream(p.getOutputStream());
                    os.writeBytes("mount -o remount,rw /system\n");
                    os.writeBytes("echo \"127.0.0.1 localhost\" >/system/etc/hosts\n");
                    os.writeBytes("mount -o remount,ro /system\n");
                    // Close the terminal
                    os.writeBytes("exit\n");
                    os.flush();
                    try {
                        p.waitFor();
                        if (p.exitValue() != 255) {
                            // TODO Code to run on success
                            toastMessage("禁用成功");
                        } else {
                            // TODO Code to run on unsuccessful
                            toastMessage("未root！");
                        }
                    } catch (InterruptedException e) {
                        // TODO Code to run in interrupted exception
                        toastMessage("未root！");
                    }
                } catch (IOException e) {
                    // TODO Code to run in input/output exception
                    toastMessage("未root！");
                }

            }


        });

    }

    public void toastMessage(String messageText) {
        Toast.makeText(getApplicationContext(), messageText,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
