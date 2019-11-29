package com.hon.fantasy.subfragments;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.hon.fantasy.MusicPlayer;
import com.hon.fantasy.R;
import com.hon.fantasy.activities.BaseActivity;
import com.hon.fantasy.listeners.MusicStateListener;
import com.hon.fantasy.utils.FantasyUtils;
import com.hon.fantasy.utils.Helpers;
import com.hon.fantasy.utils.ImageUtils;
import com.hon.fantasy.utils.NavigationUtils;
import com.hon.fantasy.utils.PreferencesUtility;
import com.hon.fantasy.utils.SlideTrackSwitcher;
import com.hon.fantasy.widget.PlayPauseButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.steamcrafted.materialiconlib.MaterialIconView;

/**
 * Created by Frank on 2018/3/4.
 * E-mail:frank_hon@foxmail.com
 */

public class QuickControlsFragment extends Fragment implements MusicStateListener {


    public static View topContainer;
    private ProgressBar mProgress;
    private SeekBar mSeekBar;
    private int overflowcounter = 0;
    private PlayPauseButton mPlayPause, mPlayPauseExpanded;
    private TextView mTitle, mTitleExpanded;
    private TextView mArtist, mArtistExpanded;
    private ImageView mAlbumArt, mBlurredArt;
    private View rootView;
    private View playPauseWrapper, playPauseWrapperExpanded;
    private MaterialIconView previous, next;
    private boolean duetoplaypause = false;
    private boolean fragmentPaused = false;

    public Runnable mUpdateProgress = new Runnable() {

        @Override
        public void run() {

            long position = MusicPlayer.getInstance().position();
            mProgress.setProgress((int) position);
            mSeekBar.setProgress((int) position);

            overflowcounter--;
            if (MusicPlayer.getInstance().isPlaying()) {
                int delay = (int) (1500 - (position % 1000));
                if (overflowcounter < 0 && !fragmentPaused) {
                    overflowcounter++;
                    mProgress.postDelayed(mUpdateProgress, delay);
                }
            } else mProgress.removeCallbacks(this);

        }
    };

    private final View.OnClickListener mPlayPauseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            duetoplaypause = true;
            if (!mPlayPause.isPlayed()) {
                mPlayPause.setPlayed(true);
                mPlayPause.startAnimation();
            } else {
                mPlayPause.setPlayed(false);
                mPlayPause.startAnimation();
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.getInstance().playOrPause();
                }
            }, 200);

        }
    };

    private final View.OnClickListener mPlayPauseExpandedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            duetoplaypause = true;
            if (!mPlayPauseExpanded.isPlayed()) {
                mPlayPauseExpanded.setPlayed(true);
                mPlayPauseExpanded.startAnimation();
            } else {
                mPlayPauseExpanded.setPlayed(false);
                mPlayPauseExpanded.startAnimation();
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.getInstance().playOrPause();
                }
            }, 200);

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false);
        this.rootView = rootView;

        mPlayPause = (PlayPauseButton) rootView.findViewById(R.id.play_pause);
        mPlayPauseExpanded = (PlayPauseButton) rootView.findViewById(R.id.playpause);
        playPauseWrapper = rootView.findViewById(R.id.play_pause_wrapper);
        playPauseWrapperExpanded = rootView.findViewById(R.id.playpausewrapper);
        playPauseWrapper.setOnClickListener(mPlayPauseListener);
        playPauseWrapperExpanded.setOnClickListener(mPlayPauseExpandedListener);
        mProgress = (ProgressBar) rootView.findViewById(R.id.song_progress_normal);
        mSeekBar = (SeekBar) rootView.findViewById(R.id.song_progress);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        mArtist = (TextView) rootView.findViewById(R.id.artist);
        mTitleExpanded = (TextView) rootView.findViewById(R.id.song_title);
        mArtistExpanded = (TextView) rootView.findViewById(R.id.song_artist);
        mAlbumArt = (ImageView) rootView.findViewById(R.id.album_art_nowplayingcard);
        mBlurredArt = (ImageView) rootView.findViewById(R.id.blurredAlbumart);
        next = (MaterialIconView) rootView.findViewById(R.id.next);
        previous = (MaterialIconView) rootView.findViewById(R.id.previous);
        topContainer = rootView.findViewById(R.id.topContainer);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mProgress.getLayoutParams();
        mProgress.measure(0, 0);
        layoutParams.setMargins(0, -(mProgress.getMeasuredHeight() / 2), 0, 0);
        mProgress.setLayoutParams(layoutParams);

        mPlayPause.setColor(Config.accentColor(getActivity(), Helpers.getATEKey(getActivity())));
        mPlayPauseExpanded.setColor(Color.WHITE);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    MusicPlayer.getInstance().seek((long) i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.getInstance().next();
                    }
                }, 200);

            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.getInstance().previous(getActivity(), false);
                    }
                }, 200);

            }
        });


        ((BaseActivity) getActivity()).setMusicStateListenerListener(this);

        if (PreferencesUtility.getInstance(getActivity()).isGesturesEnabled()) {
            new SlideTrackSwitcher() {
                @Override
                public void onClick() {
                    NavigationUtils.navigateToNowplaying(getActivity(), false);
                }
            }.attach(rootView.findViewById(R.id.root_view));
        }


        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentPaused = true;
    }

    public void updateNowplayingCard() {
        mTitle.setText(MusicPlayer.getInstance().getTrackName());
        mArtist.setText(MusicPlayer.getInstance().getArtistName());
        mTitleExpanded.setText(MusicPlayer.getInstance().getTrackName());
        mArtistExpanded.setText(MusicPlayer.getInstance().getArtistName());
        if (!duetoplaypause) {
            ImageLoader.getInstance().displayImage(FantasyUtils.getAlbumArtUri(MusicPlayer.getInstance().getCurrentAlbumId()).toString(), mAlbumArt,
                    new DisplayImageOptions.Builder().cacheInMemory(true)
                            .showImageOnFail(R.drawable.ic_empty_music2)
                            .resetViewBeforeLoading(true)
                            .build(), new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            Bitmap failedBitmap = ImageLoader.getInstance().loadImageSync("drawable://" + R.drawable.ic_empty_music2);
                            if (getActivity() != null)
                                new setBlurredAlbumArt().execute(failedBitmap);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            if (getActivity() != null)
                                new setBlurredAlbumArt().execute(loadedImage);

                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
        }
        duetoplaypause = false;
        mProgress.setMax((int) MusicPlayer.getInstance().duration());
        mSeekBar.setMax((int) MusicPlayer.getInstance().duration());
        mProgress.postDelayed(mUpdateProgress, 10);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        topContainer = rootView.findViewById(R.id.topContainer);
        fragmentPaused = false;
        if (mProgress != null)
            mProgress.postDelayed(mUpdateProgress, 10);

    }

    public void updateState() {
        if (MusicPlayer.getInstance().isPlaying()) {
            if (!mPlayPause.isPlayed()) {
                mPlayPause.setPlayed(true);
                mPlayPause.startAnimation();
            }
            if (!mPlayPauseExpanded.isPlayed()) {
                mPlayPauseExpanded.setPlayed(true);
                mPlayPauseExpanded.startAnimation();
            }
        } else {
            if (mPlayPause.isPlayed()) {
                mPlayPause.setPlayed(false);
                mPlayPause.startAnimation();
            }
            if (mPlayPauseExpanded.isPlayed()) {
                mPlayPauseExpanded.setPlayed(false);
                mPlayPauseExpanded.startAnimation();
            }
        }
    }

    public void restartLoader() {

    }

    public void onPlaylistChanged() {

    }

    public void onMetaChanged() {
        updateNowplayingCard();
        updateState();
    }

    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], getActivity(), 6);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                if (mBlurredArt.getDrawable() != null) {
                    final TransitionDrawable td =
                            new TransitionDrawable(new Drawable[]{
                                    mBlurredArt.getDrawable(),
                                    result
                            });
                    mBlurredArt.setImageDrawable(td);
                    td.startTransition(400);

                } else {
                    mBlurredArt.setImageDrawable(result);
                }
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }
}

