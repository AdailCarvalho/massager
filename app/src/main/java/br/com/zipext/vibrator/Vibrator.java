package br.com.zipext.vibrator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.api.GoogleApiClient;

import pl.droidsonroids.gif.GifImageButton;

public class Vibrator extends AppCompatActivity {

    private static final String TAG = Vibrator.class.getSimpleName();

    private static final Integer MAX_SEEK_INTENSITY = 60000;

    private SeekBar intensityBar;

    private SeekBar pauseBar;

    private GifImageButton gifButton;

    private android.os.Vibrator osVibrator;

    boolean isVibrating = false;

    private long[] buttonPattern = {0, 100, 0};

    private long[] seekPattern = {0 ,100 ,0};

    private InterstitialAd vibrateInteserctial;

    private IntentFilter vibrateIntent;

    private AdView vibrateAdView;

    private AdRequest adRequest;

    private GoogleApiClient client;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibrator);

        this.initializeComponents();

        gifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isVibrating) {
                    osVibrator.vibrate(buttonPattern, 0);
                    gifButton.setBackgroundResource(R.drawable.animation);
                    isVibrating = true;
                } else {
                    osVibrator.cancel();
                    gifButton.setBackgroundResource(R.drawable.paused);
                    isVibrating = false;
                    if (vibrateInteserctial.isLoaded()) {
                        vibrateInteserctial.show();
                    }
                }
            }
        });

        intensityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long intensitySec = (long) intensityBar.getProgress();
                seekPattern[1] = intensitySec;
                TextView selectedIntensity = (TextView) findViewById(R.id.text_intensity);
                selectedIntensity.setText(getString(R.string.text_intensity_set)
                        .replace("{seconds}", String.valueOf(intensitySec / 1000) + " sec"));
                if (isVibrating) {
                    osVibrator.cancel();
                    osVibrator.vibrate(seekPattern, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        pauseBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long pauseSec = (long) pauseBar.getProgress();
                seekPattern[2] = pauseSec;
                TextView selectedInterval= (TextView) findViewById(R.id.text_interval);
                selectedInterval.setText(getString(R.string.text_interval_set)
                        .replace("{seconds}", String.valueOf(pauseSec / 1000) + " sec"));
                if (isVibrating) {
                    osVibrator.cancel();
                    osVibrator.vibrate(seekPattern, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        registerReceiver(vibrateReceiver, vibrateIntent);
    }

    public void initializeComponents () {
        vibrateIntent = new IntentFilter(Intent.ACTION_SCREEN_OFF);

        vibrateAdView = (AdView) findViewById(R.id.ad_banner);

        adRequest = new AdRequest.Builder().build();
        vibrateAdView.loadAd(adRequest);

        vibrateInteserctial = new InterstitialAd(this);
        vibrateInteserctial.setAdUnitId(getString(R.string.intersectial_uid));
        vibrateInteserctial.loadAd(adRequest);

        intensityBar = (SeekBar) findViewById(R.id.seek_intensity);
        intensityBar.setMax(MAX_SEEK_INTENSITY);
        pauseBar = (SeekBar) findViewById(R.id.seek_interval);
        pauseBar.setMax(MAX_SEEK_INTENSITY);

        gifButton = (GifImageButton) findViewById(R.id.gif_vibrate);

        osVibrator = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vibrator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_rate) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_rate)
                    .replace("{marketId}", getPackageName()))));
            return true;
        } else if (id == R.id.action_exit) {
            try {
                vibrateInteserctial.show();
                this.finishAffinity();
            } catch (Throwable e1) {
                Log.d(TAG, e1.getMessage());
            }
            return true;
        } else if (id == R.id.action_share) {
            startActivity(new Intent(Intent.ACTION_VIEW).setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, getString(R.string.link_share)
                            .replace("{packageName}", getPackageName()))
                    .setType("text/plain"));
            return true;
        } else if (id == R.id.action_more){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_more))));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public BroadcastReceiver vibrateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && isVibrating == true) {
                osVibrator.vibrate(buttonPattern, 0);
            }
        }
    };
}
