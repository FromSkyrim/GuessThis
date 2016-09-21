package com.fatty.guessthissong;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fatty.guessthissong.util.Util;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;

public class CourseClear extends Activity {

    private ImageButton mBtnGoToMarket;
    private ImageButton mBtnCourseClearShareToWX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_clear);

        mBtnGoToMarket = (ImageButton) findViewById(R.id.btn_go_to_market);
        mBtnGoToMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Util.saveData(CourseClear.this, -1, MainActivity.coinReward(50));
                Util.goToMarket(CourseClear.this, "com.fatty.guessthissong");
            }
        });

        mBtnCourseClearShareToWX = (ImageButton) findViewById(R.id.btn_share_to_wx_course_clear);
        mBtnCourseClearShareToWX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.saveData(CourseClear.this, -1, MainActivity.coinReward(50));
                //初始化WXImageObject和WXMediaMessage对象
                Bitmap bmp = generateScreenShot();
                WXImageObject imgObj = new WXImageObject(bmp);
                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = imgObj;

                //构造一个Req
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = String.valueOf(System.currentTimeMillis());
                //transaction字段用于唯一标识一个请求

                req.message = msg;
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                MainActivity.api.sendReq(req);
            }
        });

    }

    private Bitmap generateScreenShot() {
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        return view.getDrawingCache();
    }
}
