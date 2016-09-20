package com.fatty.guessthissong.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by 17255 on 2016/9/11.
 */

/*音乐播放类*/
public class MyPlayer {

    /*音效索引，用来指定播放哪个音效*/
    public static final int INDEX_SOUND_EFFECT_ENTER = 0;
    public static final int INDEX_SOUND_EFFECT_CANCEL = 1;
    public static final int INDEX_SOUND_EFFECT_COIN = 2;

    /*音效的文件名*/
    private static final String[] SONG_EFFECT_NAMES = {"enter.mp3", "cancel.mp3", "coin.mp3"};

    /*歌曲的MediaPlayer*/
    private static MediaPlayer mMusicMediaPlayer;

    /*音效的MediaPlayer集合，三个都在这儿了*/
    private static MediaPlayer[] mSoundEffectMediaPlayers = new MediaPlayer[SONG_EFFECT_NAMES.length];

    /*播放音效*/
    public static void playSoundEffect(Context context, int index) {
        if (mSoundEffectMediaPlayers[index] == null) {
            mSoundEffectMediaPlayers[index] = new MediaPlayer();

            AssetManager assetManager = context.getAssets();

            try {
                AssetFileDescriptor fileDescriptor = assetManager.openFd(SONG_EFFECT_NAMES[index]);
                mSoundEffectMediaPlayers[index].setDataSource(fileDescriptor.getFileDescriptor(),
                        fileDescriptor.getStartOffset(),
                        fileDescriptor.getLength());

                mSoundEffectMediaPlayers[index].prepare();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

         /*声音播放*/
        mSoundEffectMediaPlayers[index].start();


    }


    /*播放音乐*/
    public static void playMusic(Context context, String fileName) {
        if (mMusicMediaPlayer == null) {
            mMusicMediaPlayer = new MediaPlayer();
        }

        /*强制重置*/
        mMusicMediaPlayer.reset();

        AssetManager assetManager = context.getAssets();
        try {
            AssetFileDescriptor fileDescriptor = assetManager.openFd(fileName);
            mMusicMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());

            mMusicMediaPlayer.prepare();

            /*声音播放*/
            mMusicMediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*停止播放*/
    public static void stopMusic(Context context) {
        if (mMusicMediaPlayer != null) {
            mMusicMediaPlayer.stop();
        }
    }
}
