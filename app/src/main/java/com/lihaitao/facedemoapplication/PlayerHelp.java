package com.lihaitao.facedemoapplication;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

public class PlayerHelp {

    private static final String TAG = "player";

    private static volatile PlayerHelp instance;

    private PlayerHelp() {

    }

    public static PlayerHelp getInstance() {
        if (instance == null) {
            synchronized (PlayerHelp.class) {
                if (instance == null) {
                    instance = new PlayerHelp();
                }
            }
        }
        return instance;
    }

    private SimpleExoPlayer player;
    private DefaultDataSourceFactory dataSourceFactory;
    private Handler mainHandler;
    private IPlayerStateListener stateListener;

    public void initPlayer(Context context, PlayerView playerView, String url, IPlayerStateListener stateListener) {
        this.stateListener = stateListener;
        if (player == null) {
            createPlayer(context);
        }
        playerView.setPlayer(player);
        player.prepare(buildMediaSource(context, url));
    }

    public void start() {
        if (player != null) {
            if (player.getPlaybackState() == Player.STATE_ENDED) {
                player.setPlayWhenReady(false);
                player.seekTo(0);
                player.setPlayWhenReady(true);
            } else if (!player.getPlayWhenReady()) {
                player.setPlayWhenReady(true);
            }
        }
    }

    private long position = 0;

    public void restart() {
        if (player != null) {
            player.seekTo(position);
        }
    }

    public void pause() {
        if (player != null && player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
            position = player.getCurrentPosition();
        }
    }

    public void stop() {
        if (player != null && player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
        }
    }

    public void release() {
        if (player != null) {
            stop();
            player.release();
        }
    }

    public void distroy() {
        release();
        if (player != null) {
            player = null;
        }
        if (dataSourceFactory != null) {
            dataSourceFactory = null;
        }
        if (mainHandler != null) {
            mainHandler = null;
        }
        if (stateListener != null) {
            stateListener = null;
        }
    }

    private void createPlayer(Context context) {
        // 创建带宽
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // 创建轨道选择工厂
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        // 创建轨道选择实例
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        // 创建播放器实例
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {
                super.onLoadingChanged(isLoading);
                Log.e(TAG, "---onLoadingChanged---" + isLoading);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                super.onPlayerStateChanged(playWhenReady, playbackState);
                Log.e(TAG, "---onPlayerStateChanged---" + "playWhenReady---" + playWhenReady + ",playbackState---" + playbackState);
                if (stateListener != null) {
                    stateListener.onPlayerStateChanged(playWhenReady, playbackState);
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.e(TAG, "---onPlayerError---" + error.getClass());
                super.onPlayerError(error);
                if (stateListener != null) {
                    stateListener.onError();
                }
            }
        });

        dataSourceFactory = new DefaultDataSourceFactory(context, com.google.android.exoplayer2.util.Util.getUserAgent(context, getApplicationName(context)), null);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private MediaSource buildMediaSource(Context context, String url) {
        // 创建加载数据的工厂
        Uri uri = Uri.parse(url);
        int type = getUrlType(uri.toString());
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory(TAG, defaultBandwidthMeter);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, dataSourceFactory, new DefaultSsChunkSource.Factory(new DefaultDataSourceFactory(context, defaultBandwidthMeter, factory)), mainHandler, null);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, dataSourceFactory, new DefaultDashChunkSource.Factory(new DefaultDataSourceFactory(context, defaultBandwidthMeter, factory)), mainHandler, null);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, dataSourceFactory, mainHandler, null);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, dataSourceFactory, new DefaultExtractorsFactory(), mainHandler, null);
            default: {
                return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            }
        }
    }

    private int getUrlType(String url) {
        if (url.contains(".mpd")) {
            return C.TYPE_DASH;
        } else if (url.contains(".ism") || url.contains(".isml")) {
            return C.TYPE_SS;
        } else if (url.contains(".m3u8")) {
            return C.TYPE_HLS;
        } else {
            return C.TYPE_OTHER;
        }
    }

    private String getApplicationName(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        return (String) packageManager.getApplicationLabel(applicationInfo);
    }

    public interface IPlayerStateListener {

        void onPlayerStateChanged(boolean playWhenReady, int playbackState);

        void onError();

    }

}
