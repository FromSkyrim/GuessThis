package com.fatty.guessthissong.model;

/**
 * Created by 17255 on 2016/8/29.
 */
public class Song {

    /*歌曲名称*/
    private String songName;

    /*歌曲文件名称*/
    private String songFileName;

    /*歌曲名称长度*/
    private int nameLength;

    public char[] getNameCharaters() {
        return songName.toCharArray();
    }

    public void setSongName(String songName) {
        this.songName = songName;

        this.nameLength = songName.length();
    }

    public String getSongName() {
        return songName;
    }

    public void setSongFileName(String songFileName) {
        this.songFileName = songFileName;
    }

    public String getSongFileName() {
        return songFileName;
    }

    public int getNameLength() {
        return nameLength;
    }
}
