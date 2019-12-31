package com.sdmp.audioclient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import sdmp.project5.services.musicplayer.MusicPlayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

   Button startService;
   Button stopService;
   ListView listview;
   TextView displayMsg;
   Boolean isBound=false;
   Boolean needBinding=false;
   private MusicPlayer musicPlayer;
   ArrayList<String> arraylist=new ArrayList<>();
   MyCustomAdapter customadapter;
    private Boolean flag = false;
    private static final String TOAST_INTENT_FILTER =
            "edu.uic.cs478.s19.kaboom.showToast";
    private Receiver1 mReceiver1 ;
    private IntentFilter mFilter1 ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.landscape);
        }
        else{
            setContentView(R.layout.activity_main);
        }
        startService=findViewById(R.id.startService);
        stopService=findViewById(R.id.stopService);
        displayMsg=findViewById(R.id.displayMsg);
        listview=findViewById(R.id.songslistview);
        listview.setVisibility(View.INVISIBLE);
        displayMsg.setText("Please Select a track from below options");
        displayMsg.setVisibility(View.INVISIBLE);
        stopService.setEnabled(false);
        mReceiver1 = new Receiver1() ;
        mFilter1 = new IntentFilter(TOAST_INTENT_FILTER) ;
        registerReceiver(mReceiver1, mFilter1) ;

        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService();
            }
        });

        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("PlayBack Will be Stopped ")
                        .setMessage("Press OK to continue.Else Press Cancel").setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                stop();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        });

    }

    public void startService() {
        if(!needBinding){
            Intent intent = new Intent(MusicPlayer.class.getName());
            ResolveInfo info = getPackageManager().resolveService(intent, 0);
            intent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
            startForegroundService(intent);
            resetList();
            displayMsg.setVisibility(View.VISIBLE);
            listview.setVisibility(View.VISIBLE);
            stopService.setEnabled(true);
            startService.setEnabled(false);
            bind();
        }
        else {
            bind();
            startService.setEnabled(false);
        }
    }

    public void stop() {
        try{
            listview.setVisibility(View.INVISIBLE);
            displayMsg.setVisibility(View.INVISIBLE);
            stopService.setEnabled(false);
            if(flag) {
                musicPlayer.stop();
            }
            if(isBound) {
                getApplicationContext().unbindService(this.connection);
                isBound = false;
            }
            musicPlayer.release();
            flag=false;
            startService.setEnabled(true);
            needBinding=false;
        }
        catch (RemoteException e){
            e.printStackTrace();
        }

    }

    public void bind(){
        if (!isBound) {
            boolean bind = false;
            Intent intent = new Intent(MusicPlayer.class.getName());
            ResolveInfo info = getPackageManager().resolveService(intent, 0);
            intent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
            bind = getApplicationContext().bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
            if(bind){
                isBound=true;
            }
        }
    }


    public void stopService(){
            try{
                Toast.makeText(MainActivity.this, "Service Unbounded ,Press Start button to Re-Bind", Toast.LENGTH_SHORT).show();
                if(flag) {
                    musicPlayer.stop();
                }
                if(isBound) {

                    getApplicationContext().unbindService(this.connection);
                    isBound = false;
                }
                flag=false;
                startService.setEnabled(true);
                needBinding=true;
            }
            catch (RemoteException e){
                e.printStackTrace();
            }
    }

    public void resetList(){
        arraylist.clear();
        arraylist.add("Track 1");
        arraylist.add("Track 2");
        arraylist.add("Track 3");
        arraylist.add("Track 4");
        arraylist.add("Track 5");
        customadapter = new MyCustomAdapter(this, R.layout.songslist, arraylist);
        listview.setAdapter(customadapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver1) ;
        if(!startService.isEnabled()) {
            try{
                if(flag) {
                    musicPlayer.stop();
                }
                musicPlayer.release();
                getApplicationContext().unbindService(this.connection);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }


    }


    private final ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iservice) {

            musicPlayer = MusicPlayer.Stub.asInterface(iservice);

            isBound = true;

        }

        public void onServiceDisconnected(ComponentName className) {

            musicPlayer = null;

            isBound = false;

        }
    };

    public class MyCustomAdapter extends BaseAdapter {

        private Context context;
        private int layout;
        private ArrayList<String> arrayList;


        public MyCustomAdapter(Context context, int layout, ArrayList<String> arrayList) {
            this.context = context;
            this.layout = layout;
            this.arrayList = arrayList;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public class ViewHolder {
            public TextView txtName;
            public ImageView ivPlay, ivStop,ivResume;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewholder;
            if(convertView == null){
                viewholder=new ViewHolder();
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(layout,parent, false);
                viewholder.txtName = (TextView) convertView.findViewById(R.id.txtName);
                viewholder.ivPlay = (ImageView) convertView.findViewById(R.id.ivPlay);
                viewholder.ivStop = (ImageView) convertView.findViewById(R.id.ivStop);
                viewholder.ivResume = (ImageView) convertView.findViewById(R.id.ivResume);
                viewholder.ivResume.setVisibility(View.INVISIBLE);
                viewholder.ivStop.setVisibility(View.INVISIBLE);
                convertView.setTag(viewholder);
            } else {
                viewholder=(ViewHolder)convertView.getTag();
            }
            viewholder.txtName.setText(arrayList.get(position));

            viewholder.ivPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        if(flag && isBound){
                            musicPlayer.stop();
                            flag=false;

                        }
                        if(flag!=true && isBound) {
                            musicPlayer.play(position);
                            flag = true;
                            viewholder.ivResume.setVisibility(View.VISIBLE);
                            viewholder.ivStop.setVisibility(View.VISIBLE);

                        }
                        else{
                            Toast.makeText(context, "Service Not Bound,Click Start Button to Bind", Toast.LENGTH_SHORT).show();
                        }

                    }
                    catch (RemoteException e){
                        e.printStackTrace();
                    }

                }
            });

            viewholder.ivResume.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        if(flag) {
                            musicPlayer.pause(position);
                            flag=false;
                        } else if(!flag&& isBound) {
                            musicPlayer.resume(position);
                            flag = true;

                        }
                        else {
                            Toast.makeText(context, "Service Not Bound,Click Start Button to Bind", Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (RemoteException e){
                        e.printStackTrace();
                    }

                }
            });

            viewholder.ivStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try{
                        if(flag) {
                            musicPlayer.stop();
                            flag = false;
                            stopService();
                        }
                        else if(isBound &&!flag){
                            Toast.makeText(context, "Music is already stopped", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(context, "Service not Bound,Click Start Button to Bind", Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (RemoteException e){
                        e.printStackTrace();
                    }

                }
            });

            return convertView;
        }
    }


    public class Receiver1 extends BroadcastReceiver {
        int songStopped;
        @Override
        public void onReceive(Context context, Intent intent) {

            songStopped=intent.getIntExtra("STOPPED",0);
            if(songStopped==1){
                stopService();
            }

        }
    }


}
