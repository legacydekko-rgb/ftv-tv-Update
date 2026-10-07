package com.example.ftvtv;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * ============================================================================
 * FILE: app/src/main/java/com/example/ftvtv/PlayerActivity.java
 * ============================================================================
 * OPTIONAL in-app player.
 *
 * You only need this file if you set PREFER_BUILTIN_PLAYER = true in
 * WebViewActivity. The default flow hands the video URL to VLC / MX Player,
 * which is far more reliable because those apps ship their own codecs and
 * handle MKV/AC3/DTS audio properly.
 *
 * This activity uses the platform VideoView, which supports MP4/H.264/WebM
 * natively and streams over HTTP. For MKV with exotic codecs you should either
 * use an external player, or swap VideoView for ExoPlayer (see the README).
 *
 * >>> NOTE: this Activity uses NO androidx imports, so it compiles in AIDE
 *     without any support-library setup. That is why MediaController is used
 *     instead of ExoPlayer / Media3.
 * ============================================================================
 */
public class PlayerActivity extends Activity {

    public static final String EXTRA_VIDEO_URL = "extra_video_url";
    public static final String EXTRA_TITLE = "extra_title";

    private VideoView videoView;
    private ProgressBar buffering;
    private TextView titleView;
    private View errorView;

    private int resumePosition = 0;
    private String videoUrl = "";
    private String title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Keep the TV awake during playback.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        videoView = (VideoView) findViewById(R.id.video_view);
        buffering = (ProgressBar) findViewById(R.id.player_buffering);
        titleView = (TextView) findViewById(R.id.player_title);
        errorView = findViewById(R.id.player_error);

        if (getIntent() != null) {
            videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
            title = getIntent().getStringExtra(EXTRA_TITLE);
        }
        if (videoUrl == null || videoUrl.length() == 0) {
            Toast.makeText(this, "No video URL supplied", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        titleView.setText(title != null && title.length() > 0 ? title : videoUrl);

        // MediaController gives you play/pause/seek with the TV remote
        // (LEFT/RIGHT seek, OK toggles the controls).
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);

        videoView.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(android.media.MediaPlayer mp) {
                buffering.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
                if (resumePosition > 0) {
                    videoView.seekTo(resumePosition);
                }
                videoView.start();
            }
        });

        videoView.setOnErrorListener(new android.media.MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(android.media.MediaPlayer mp, int what, int extra) {
                buffering.setVisibility(View.GONE);
                showPlayerError();
                return true; // we handled it
            }
        });

        videoView.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(android.media.MediaPlayer mp) {
                finish();
            }
        });

        findViewById(R.id.player_open_external).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hand the stream to VLC / MX Player instead.
                MainActivity.openVideoInExternalPlayer(PlayerActivity.this, videoUrl, title);
                finish();
            }
        });

        startPlayback();
    }

    private void startPlayback() {
        try {
            buffering.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(videoUrl));
            videoView.requestFocus();
        } catch (Exception e) {
            showPlayerError();
        }
    }

    private void showPlayerError() {
        errorView.setVisibility(View.VISIBLE);
        Toast.makeText(this,
                "This file's codec is not supported by the built-in player.\n" +
                        "Tap \"Open in external player\" (VLC handles everything).",
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            resumePosition = videoView.getCurrentPosition();
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null && resumePosition > 0 && !videoView.isPlaying()) {
            videoView.seekTo(resumePosition);
            videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        if (videoView != null) {
            videoView.stopPlayback();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // Stop playback cleanly before leaving.
        if (videoView != null && videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        super.onBackPressed();
    }
}
