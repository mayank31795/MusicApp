package com.acadgild.musicapp.activities;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acadgild.musicapp.R;
import com.acadgild.musicapp.adapters.SongListAdapter;
import com.acadgild.musicapp.helper.Song;
import com.acadgild.musicapp.services.MusicService;
import com.bluelinelabs.logansquare.LoganSquare;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class ActivityDisplaySongs extends ActionBarActivity implements SalutDataCallback, View.OnClickListener, AdapterView.OnItemClickListener {

    private ListView mListSongs;
    private LinearLayout mLinearListImportedFiles;
    private RelativeLayout mRelativeBtnImport;
    private SongListAdapter mAdapterListFile;
    private String[] STAR = {"*"};
    private ArrayList<Song> mSongList;
    private MusicService serviceMusic;
    private Intent playIntent;
    Thread splashTread;
    public int percent = 10;
    public Button discoverBtn, registerBtn, dataBtn, hostingBtn;
    TextView tv1, tv2;
    public static final String TAG = "SalutTestApp";
    public SalutDataReceiver dataReceiver;
    public SalutServiceData serviceData;
    public Salut network;
    SalutDataCallback callback;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_display_songs);
        hostingBtn = (Button) findViewById(R.id.host);
        discoverBtn = (Button) findViewById(R.id.join);
        registerBtn = (Button) findViewById(R.id.register);
        dataBtn = (Button) findViewById(R.id.data);
        tv1 = (TextView) findViewById(R.id.textView1);
        //tv2 = (TextView) findViewById(R.id.textView2);

        hostingBtn.setOnClickListener(this);
        discoverBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        dataBtn.setOnClickListener(this);

        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled())
        wifiManager.setWifiEnabled(false);
        wifiManager.setWifiEnabled(true);

        dataReceiver = new SalutDataReceiver(this, this);
        serviceData = new SalutServiceData("testAwesomeService", 60606,
                "HOST");
        network = new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                // wiFiFailureDiag.show();
                // OR
                Toast.makeText(getApplicationContext(), "Sorry, but this device does not support WiFi Direct.", Toast.LENGTH_SHORT).show();
            }
        });
        init();
    }

    private void setupNetwork() {
        if (!network.isRunningAsHost) {
            network.startNetworkService(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    tv1.setText(tv1.getText() + "\n" + salutDevice.deviceName);
                    Toast.makeText(getApplicationContext(), salutDevice.readableName + " has connected!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            network.stopNetworkService(false);
        }
    }

    private void discoverServices() {
        if (!network.isRunningAsHost && !network.isDiscovering) {
            network.discoverNetworkServices(new SalutCallback() {
                @Override
                public void call() {
                    tv1.setText(tv1.getText() + "\n" + network.foundDevices.get(0).deviceName);
                    Toast.makeText(getApplicationContext(), "Device: " + network.foundDevices.get(0).instanceName + " found.", Toast.LENGTH_SHORT).show();
                }
            }, true);
        } else {
            network.stopServiceDiscovery(true);
        }
    }

    private void registerService() {
        //Toast.makeText(getApplicationContext(),"click", Toast.LENGTH_SHORT).show();
        network.registerWithHost(network.foundDevices.get(0), new SalutCallback() {
            @Override
            public void call() {
                Toast.makeText(getApplicationContext(), "We're now registered.", Toast.LENGTH_SHORT).show();
            }
        }, new SalutCallback() {
            @Override
            public void call() {
                Toast.makeText(getApplicationContext(), "We failed to register.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDataReceived(Object data) {
        //Data Is Received
        Message newMessage = null;
        Toast.makeText(getApplicationContext(), "Data Received", Toast.LENGTH_SHORT).show();
        try {
            //newSong = LoganSquare.parse((File) data,Test.class );
            newMessage = LoganSquare.parse((String) data, Message.class);
            tv1.setText(newMessage.description);
            /*Message myMessage = new Message();
            myMessage.player = "play";
            network.sendToHost(myMessage, new SalutCallback() {
                @Override
                public void call() {
                    Log.e(TAG, "Oh no! The data failed to send.");
                }
            });*/
            //File file = newMessage.file;
            //byte[] decoded = Base64.decode(String.valueOf(newMessage), 0);
            //tv2.setText(newMessage.description);
            //startActivity(new Intent(getApplicationContext(), Speaker.class).putExtra("file",mySongs.get(0)));
            //FileUtils.writeByteArrayToFile(newSong, decoded);
            //startActivity(new Intent(getApplicationContext(), Speaker.class).putExtra("file", newMessage.file));
        } catch (IOException ex) {
            Toast.makeText(getApplicationContext(), "Failed to parse network data.", Toast.LENGTH_SHORT).show();
        }
        Log.d("name", newMessage.description);
        mSongList = listAllSongs();
        mAdapterListFile.setSongsList(mSongList);
        serviceMusic.setSongList(mSongList);
        for (int i = 0; i < mSongList.size(); i++) {
            Log.d("name", newMessage.description);
            if (mSongList.get(i).getSongName().equals(newMessage.description)) {
                serviceMusic.setSelectedSong(i, MusicService.NOTIFICATION_ID);
                Log.d("play", "" + i);
                break;
            } else if (i == mSongList.size() - 1) {
                Toast.makeText(getApplicationContext(), "Song not Found", Toast.LENGTH_SHORT).show();
            }
        }
        //startActivity(new Intent(getApplicationContext(), Speaker.class).putExtra("file", newMessage.file));
        //File file = ((Message) data).getFile();
//        String desc = ((Message) data).getDescription();
        //      Log.d(TAG, "onDataReceived: " + desc);
    }

    private void sendData(String string, int position) {
        Toast.makeText(this, "Sending Data", Toast.LENGTH_SHORT).show();
        //tv2.setText(encoded);
        Message myMessage = new Message();
        myMessage.description = string.trim();
        //myMessage.player = Integer.toString(position);
        //startActivity(new Intent(getApplicationContext(), Speaker.class).putExtra("file",mySongs.get(0)));
        //newSong = mySongs.get(0);
        //myMessage.file = newSong;



       /* File file = new File("/mnt/sdcard/song.mp3");
        Test test = new Test(new File("/mnt/sdcard/newsong.mp3"));*/

        network.sendToAllDevices(myMessage, new SalutCallback() {
            @Override
            public void call() {
                Toast.makeText(getApplicationContext(), "Data failed to send", Toast.LENGTH_SHORT).show();
            }
        });
    }

   /* private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        anim.reset();
        mBtnImport.clearAnimation();
        mBtnImport.startAnimation(anim);
        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 2000) {
                        sleep(100);
                        waited += 100;
                    }
                } catch (InterruptedException e) {
                    // do nothing
                }

            }
        };
        splashTread.start();

    }*/

    private void init() {
        getActionBar();
        mLinearListImportedFiles = (LinearLayout) findViewById(R.id.linear_list_imported_files);
        mRelativeBtnImport = (RelativeLayout) findViewById(R.id.relative_btn_import);
        mListSongs = (ListView) findViewById(R.id.list_songs_actimport);

        mListSongs.setOnItemClickListener(this);

        mSongList = new ArrayList<Song>();
        mAdapterListFile = new SongListAdapter(ActivityDisplaySongs.this, mSongList);
        mListSongs.setAdapter(mAdapterListFile);
        //StartAnimations();
        //View view = findViewById(android.R.id.content);
        //onClick(view);
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.PlayerBinder binder = (MusicService.PlayerBinder) service;
            //get service
            serviceMusic = binder.getService();
            serviceMusic.setSongList(mSongList);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onClick(View v) {
        if (!Salut.isWiFiEnabled(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "Please enable WiFi first.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (v.getId() == R.id.host) {
            setupNetwork();
            hostingBtn.setVisibility(View.GONE);
            discoverBtn.setVisibility(View.GONE);
            dataBtn.setVisibility(View.VISIBLE);
            //registerBtn.setVisibility(View.GONE);
        } else if (v.getId() == R.id.join) {
            discoverServices();
            hostingBtn.setVisibility(View.GONE);
            discoverBtn.setVisibility(View.GONE);
            //dataBtn.setVisibility(View.VISIBLE);
            registerBtn.setVisibility(View.VISIBLE);
        } else if (v.getId() == R.id.register) {
            registerService();
        } else if (v.getId() == R.id.data) {
            mSongList = listAllSongs();
            mAdapterListFile.setSongsList(mSongList);
            mLinearListImportedFiles.setVisibility(View.VISIBLE);
            mRelativeBtnImport.setVisibility(View.GONE);
            serviceMusic.setSongList(mSongList);
        }
    }

    private ArrayList<Song> listAllSongs() { //Fetch path to all the files from internal & external storage n store it in songList
        Cursor cursor;
        ArrayList<Song> songList = new ArrayList<Song>();
        Uri allSongsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        if (isSdPresent()) {
            cursor = managedQuery(allSongsUri, STAR, selection, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Song song = new Song();

                        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        String[] res = data.split("\\.");
                        song.setSongName(res[0]);
                        //Log.d("test",res[0] );
                        song.setSongFullPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        song.setSongId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                        song.setSongFullPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        song.setSongAlbumName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                        song.setSongUri(ContentUris.withAppendedId(
                                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID))));
                        String duration = getDuration(Integer.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))));
                        song.setSongDuration(duration);

                        songList.add(song);
                    } while (cursor.moveToNext());
                    return songList;
                }
                cursor.close();
            }
        }
        return null;
    }

    //Check whether sdcard is present or not
    private static boolean isSdPresent() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    //Method to convert the millisecs to min & sec
    private static String getDuration(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(6);
        sb.append(minutes < 10 ? "0" + minutes : minutes);
        sb.append(":");
        sb.append(seconds < 10 ? "0" + seconds : seconds);
        //sb.append(" Secs");
        return sb.toString();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        sendData(mSongList.get(position).getSongName(), position);
        serviceMusic.setSelectedSong(position, MusicService.NOTIFICATION_ID);
        //serviceMusic.playPauseSong();
        /*int interval = getInterval(position);
        Timer t = new Timer();
//Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {

                                  @Override
                                  public void run() {
                                      serviceMusic.nextSong();
                                      //Called each time when 1000 milliseconds (1 second) (the period parameter)
                                  }

                              },
//Set how long before to start calling the TimerTask (in milliseconds)
                interval,
//Set the amount of time between each execution (in milliseconds)
                getInterval(position++));*/
    }

    public int getInterval(int position) {
        int interval;
        Song song = mSongList.get(position);
        String string = song.getSongDuration();
        String min = "" + string.charAt(0) + string.charAt(1);
        String sec = "" + string.charAt(3) + string.charAt(4);
        interval = (Integer.parseInt(min) * 60 + Integer.parseInt(sec)) * 1000 / percent;
        Log.d("time", String.valueOf(interval));
        return interval;
    }


    @Override
    protected void onStart() {
        super.onStart();
        //Start service
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onDestroy() {
        //Stop service
        stopService(playIntent);
        serviceMusic = null;
        super.onDestroy();
        if (network.isRunningAsHost)
            network.stopNetworkService(false);
        else  {
            network.unregisterClient(true);
            network.stopServiceDiscovery(true);
        }
    }
}
