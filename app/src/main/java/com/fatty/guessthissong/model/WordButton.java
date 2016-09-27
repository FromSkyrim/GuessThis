package com.fatty.guessthissong.model;

import android.widget.Button;

/**
 * Created by 17255 on 2016/8/22.
 */

/*猜歌的文字按钮*/
public class WordButton {

    /*序号*/
    public int mIndex;
    /*这个按钮是否可见,因为如果按上去了,就不可见了*/
    public boolean mIsVisiable;
    /*按钮上面的文字*/
    public String mWordString;

    /*按钮控件本身*/
    public Button mViewButton;

    public WordButton() {
        mIsVisiable = true;
        mWordString = "";
    }
}
