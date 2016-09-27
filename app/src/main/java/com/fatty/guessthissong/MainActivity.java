package com.fatty.guessthissong;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fatty.guessthissong.data.Const;
import com.fatty.guessthissong.model.AlertDialogButtonClickListener;
import com.fatty.guessthissong.model.Song;
import com.fatty.guessthissong.model.WordButton;
import com.fatty.guessthissong.model.WordButtonClickListener;
import com.fatty.guessthissong.myUi.MyGridView;
import com.fatty.guessthissong.util.MyPlayer;
import com.fatty.guessthissong.util.Util;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements WordButtonClickListener {


    public static final int STATUS_ANSWER_CORRECT = 1;
    public static final int STATUS_ANSWER_INCORRECT = 2;
    public static final int STATUS_ANSWER_INCOMPLETE = 3;

    public static final int ID_DIALOG_DELETE_WORD = 1;
    public static final int ID_DIALOG_TIP_WORD = 2;
    public static final int ID_DIALOG_NOT_ENOUGHT_COINS = 3;

    private int mCurrentSongDuration;

    private Animation mPanAnim;
    private LinearInterpolator mPanLin;

    private Animation mBarInAnim;
    private LinearInterpolator mBarInLin;

    private Animation mBarOutAnim;
    private LinearInterpolator mBarOutLin;

    private ImageButton mBtnPlayStart;

    private ImageView mViewPan;

    private ImageView mViewPanBar;

    private boolean isRunning = false;

    private ArrayList<WordButton> mAllWords;

    private ArrayList<WordButton> mBtnSelectedWords;

    private MyGridView mMyGridView;


    /*注册微信分享功能*/
    private static final String APP_ID = "wxbee94cb77ee27244";

    /*IWXAPI是第三方app和微信通信的openapi接口*/
    public static IWXAPI api;

    /*初始化过关界面*/
    private LinearLayout passView;

    /*现在拥有的金币数量*/
    private static int mCurrentCoins = Const.TOTAL_COINS;

    /*这个是容纳被选字的linearlayout容器*/
    private LinearLayout mViewWordsContainer;

    /*当前关的歌曲*/
    private Song mCurrentSong;

    /*当前关的索引,用来从保存歌曲信息的二维数组中取出当前关歌曲的数据*/
    private static int mCurrentStageIndex = -1;

    /*答题错误时,需要文字闪烁几次*/
    private final static int SET_SPARK_TIMES = 6;

    /*主界面中显示当前金币数量的TextView*/
    private static TextView mTextViewCurrentConis;

    /*过关界面中显示当前是第几关的TextView*/
    private TextView mTextViewCurrentLevelNumber;

    /*过关界面中显示当前关是什么歌曲的TextView*/
    private TextView mTextViewCurrentClearSongName;

    private int checkAnswerResult;

    /*初始化过关界面中的下一关按钮*/
    private ImageButton mBtnNextLevel;

    /*初始化过关界面中的分享到微信按钮*/
    private ImageButton mBtnShareToWX;

    /*在猜歌主界面中显示当前是第几关的TextView*/
    private TextView mMainActivityTextViewCurrentLevelNumber;

    private ImageButton mBtnPreviousStage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*读取已保存的数据*/
        int[] data = Util.loadData(MainActivity.this);
        mCurrentStageIndex = data[0];
        mCurrentCoins = data[1];

        /*初始化唱片盘和唱片针*/
        mViewPan = (ImageView) findViewById(R.id.imageView1);
        mViewPanBar = (ImageView) findViewById(R.id.imageView2);

        regToWx();

        /*初始化显示你拥有多少金币的那个控件*/
        mTextViewCurrentConis = (TextView) findViewById(R.id.txt_bar_coins);
        mTextViewCurrentConis.setText(mCurrentCoins + "");

        /*初始化下面的选文字布局*/
        mMyGridView = (MyGridView) findViewById(R.id.gridView);

        mBtnPreviousStage = (ImageButton) findViewById(R.id.btn_bar_back);


        /*这个activity实现了监听器,调用MyGridView中的方法,把自己作为监听器传过去*/
        mMyGridView.registerWordButtonClickListener(this);

        /*初始化动画*/
        mPanAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);

        mPanLin = new LinearInterpolator();
        mPanAnim.setInterpolator(mPanLin);

        mBarInAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_45);
        mBarInAnim.setFillAfter(true);
        mBarInLin = new LinearInterpolator();
        mBarInAnim.setInterpolator(mBarInLin);

        mBarOutAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_d_45);
        mBarOutAnim.setFillAfter(true);
        mBarOutLin = new LinearInterpolator();
        mBarOutAnim.setInterpolator(mBarOutLin);

        /*初始化容纳被选择文字的linear layout容器*/
        mViewWordsContainer = (LinearLayout) findViewById(R.id.word_select_container);

        /*设置动画监听器
        * 唱片盘转动动画的监听器*/
        mPanAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                MyPlayer.playMusic(MainActivity.this, mCurrentSong.getSongFileName(), true);

                isRunning = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                MyPlayer.stopMusic(MainActivity.this);
                mViewPanBar.startAnimation(mBarOutAnim);
                isRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        /*唱片播杆移入动画的监听器*/
        mBarInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPan.startAnimation(mPanAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        /*唱片播杆移出动画的监听器*/
        mBarOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        /*初始化播放按钮*/
        mBtnPlayStart = (ImageButton) findViewById(R.id.btn_play_start);


        mBtnPlayStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    handlePlayButton();
                }
            }
        });


        initCurrentStageData();

        handleButtonTipRightAnswer();

        handleButtonDeleteWrongAnswer();



        /*设置topbar中的返回按钮*/
        mBtnPreviousStage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentStageIndex >= 2) {
                    mCurrentStageIndex = mCurrentStageIndex - 2;
                } else {
                    mCurrentStageIndex = -1;
                }

                initCurrentStageData();
            }
        });
    }

    private void handlePlayButton() {
        if (mViewPanBar != null) {
            mViewPanBar.startAnimation(mBarInAnim);


        }
        /*onCreate结束*/
    }

    @Override
    protected void onPause() {
        mViewPan.clearAnimation();
        mViewPanBar.clearAnimation();
        super.onPause();
    }

    private Song loadStageSongInfo(int stageIndex) {
        Song song = new Song();

        String[] stage = Const.SONG_INFO[stageIndex];

        song.setSongFileName(stage[Const.INDEX_FILE_NAME]);
        song.setSongName(stage[Const.INDEX_SONG_NAME]);

        return song;
    }

    protected void initCurrentStageData() {
        /*读取当前关的歌曲信息*/
        mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);


        /*设置旋转动画的播放长度*/
        mCurrentSongDuration = MyPlayer.playMusic(MainActivity.this, mCurrentSong.getSongFileName(), false);
        Log.d("MainActivity", "现在歌曲的播放长度是" + mCurrentSongDuration);

        mPanAnim.setDuration(mCurrentSongDuration);
        /*初始化已选择的文字框*/
        mBtnSelectedWords = initSelectedWords();

        mViewWordsContainer.removeAllViews();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(140, 140);

        for (int i = 0; i < mBtnSelectedWords.size(); i++) {
            mViewWordsContainer.addView(mBtnSelectedWords.get(i).mViewButton, params);
        }

        mMainActivityTextViewCurrentLevelNumber = (TextView) findViewById(R.id.textView_level_num);
        String str = (mCurrentStageIndex + 1) + "";
        mMainActivityTextViewCurrentLevelNumber.setText(str);


        /*获得数据*/
        mAllWords = initAllWord();
        /*更新数据*/
        mMyGridView.updateData(mAllWords);

        /*一进入关卡就播放歌曲,每次进入新关卡都自动播放歌曲*/
        handlePlayButton();
    }


    /*初始化待选文字框*/
    private ArrayList<WordButton> initAllWord() {
        ArrayList<WordButton> data = new ArrayList<>();

        /*获得所有待选文字*/
        String[] words = Util.generateWords(mCurrentSong);

        for (int i = 0; i < MyGridView.COUNT; i++) {
            WordButton button = new WordButton();

            button.mWordString = words[i];

            data.add(button);
        }
        return data;
    }

    /*初始化已选择文字的选择框*/
    private ArrayList<WordButton> initSelectedWords() {
        ArrayList<WordButton> data = new ArrayList<>();

        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
            View view = Util.getView(MainActivity.this, R.layout.grid_view_item);

            final WordButton button = new WordButton();
            button.mViewButton = (Button) view.findViewById(R.id.item_btn);
            button.mViewButton.setTextColor(Color.WHITE);
            button.mViewButton.setText("");
            button.mIsVisiable = false;
            button.mViewButton.setBackgroundResource(R.drawable.game_wordblank);
            button.mViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clearTheAnswer(button);
                }
            });

            data.add(button);
        }
        return data;
    }


    /*待选框24个文字框的点击事件*/
    @Override
    public void onWordButtonClick(WordButton wordButton) {
        setSelectWordToSelectedZone(wordButton);

        /*获取答案状态码*/
        checkAnswerResult = checkAnswer();

        /*分别写答案正确,答案错误和答案缺失后的逻辑*/
        if (checkAnswerResult == STATUS_ANSWER_CORRECT) {
            /*答案正确,出现过关界面*/
            handlePassEvent();


        } else if (checkAnswerResult == STATUS_ANSWER_INCORRECT) {

            /*闪烁文字以提示用户 答案错了*/
            sparkTheWords();

        } else if (checkAnswerResult == STATUS_ANSWER_INCOMPLETE) {

            /*重新设置答案回到白色*/
            for (int i = 0; i < mBtnSelectedWords.size(); i++) {
                mBtnSelectedWords.get(i).mViewButton.setTextColor(Color.WHITE);

            }
        }
    }

    /*处理过关界面的逻辑*/
    private void handlePassEvent() {
        /*过一关奖励三个金币*/
        coinReward(Const.COIN_REWARD_FOR_EACH_LEVEL);

        /*展示过关界面*/
        passView = (LinearLayout) findViewById(R.id.pass_view);
        passView.setVisibility(View.VISIBLE);

        /*播放金币音效*/
        MyPlayer.playSoundEffect(MainActivity.this, MyPlayer.INDEX_SOUND_EFFECT_COIN);

        /*停止动画*/
        mViewPan.clearAnimation();

        /*在过关界面中显示当前是第几关*/
        mTextViewCurrentLevelNumber = (TextView) findViewById(R.id.textView_current_stage);
        String str = (mCurrentStageIndex + 1) + "";
        mTextViewCurrentLevelNumber.setText(str);

        /*在过关界面中显示当前通关的歌曲名称*/
        mTextViewCurrentClearSongName = (TextView) findViewById(R.id.textView_current_song_name);
        mTextViewCurrentClearSongName.setText(mCurrentSong.getSongName());

        /*保存游戏*/
        Util.saveData(MainActivity.this, mCurrentStageIndex, mCurrentCoins);

        /*在过关界面点击下一关按钮后的逻辑*/
        mBtnNextLevel = (ImageButton) findViewById(R.id.btn_next_level);
        mBtnNextLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clearCourse()) {
                    int value = mCurrentCoins;

                    /*如果全部通关，则展示最终通关界面*/
                    Util.startActivity(MainActivity.this, CourseClear.class, value);
                    Util.saveData(MainActivity.this, -1, mCurrentCoins);


                } else {
                    /*如果只是正常过关，就*/
                    passView.setVisibility(View.GONE);
                    initCurrentStageData();
                }
            }
        });

        mBtnShareToWX = (ImageButton) findViewById(R.id.btn_share);
        mBtnShareToWX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBtnShareToWX.setVisibility(View.INVISIBLE);
                mBtnNextLevel.setVisibility(View.INVISIBLE);
                wechatShare();
                mBtnShareToWX.setVisibility(View.VISIBLE);
                mBtnNextLevel.setVisibility(View.VISIBLE);
            }
        });

    }

    /*检查是否全部通关*/
    private boolean clearCourse() {
        return (mCurrentStageIndex == (Const.SONG_INFO.length - 1));
    }


    /*设置待选框的24个文字框在被点击的时候移到上面的已选框那里*/
    private void setSelectWordToSelectedZone(WordButton wordButton) {
        for (int i = 0; i < mBtnSelectedWords.size(); i++) {
            if (mBtnSelectedWords.get(i).mWordString.length() == 0) {

                /*设置已选择文字框被选择后的文字内容,索引和可见性*/
                mBtnSelectedWords.get(i).mViewButton.setText(wordButton.mWordString);
                mBtnSelectedWords.get(i).mIndex = wordButton.mIndex;
                mBtnSelectedWords.get(i).mWordString = wordButton.mWordString;
                mBtnSelectedWords.get(i).mIsVisiable = true;

                /*设置下面待选框的文字 在选择后设为不可见*/
                setButtonVisibility(wordButton, View.INVISIBLE);
                break;
            }
        }
    }


    /*设置下面待选框的文字 在选择后设为不可见的具体方法*/
    private void setButtonVisibility(WordButton wordButton, int visibility) {
        wordButton.mViewButton.setVisibility(visibility);
        wordButton.mIsVisiable = (visibility == View.VISIBLE);
    }


    /*设置清除已选框上面已经选择的字*/
    private void clearTheAnswer(WordButton wordButton) {
        wordButton.mViewButton.setText("");
        wordButton.mWordString = "";
        wordButton.mIsVisiable = false;

        /*设置从已选框上面又返回下来待选框的文字可见*/
        setButtonVisibility(mAllWords.get(wordButton.mIndex), View.VISIBLE);
    }


    /*检查答案是正确,错误,还是缺失并返回状态码*/
    private int checkAnswer() {

        /*检查答案是否缺失*/
        for (int i = 0; i < mBtnSelectedWords.size(); i++) {
            if (mBtnSelectedWords.get(i).mWordString.length() == 0) {
                return STATUS_ANSWER_INCOMPLETE;
            }
        }

        StringBuffer sb = new StringBuffer();
        /*检查答案是否正确*/
        for (int i = 0; i < mBtnSelectedWords.size(); i++) {

            sb.append(mBtnSelectedWords.get(i).mWordString);
        }

        return (sb.toString().equals(mCurrentSong.getSongName())) ?
                STATUS_ANSWER_CORRECT : STATUS_ANSWER_INCORRECT;
    }


    /*使选择文字错误的时候,文字闪烁*/
    private void sparkTheWords() {
        /*设置定时器*/
        TimerTask timerTask = new TimerTask() {
            /*控制文字现在闪到红色还是闪到白色*/
            boolean mChange = false;
            int mTotalSparkTime = 0;

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (++mTotalSparkTime > SET_SPARK_TIMES) {
                            return;
                        }

                        /*闪烁的逻辑,交替显示红色和白色*/
                        for (int i = 0; i < mBtnSelectedWords.size(); i++) {
                            mBtnSelectedWords.get(i).mViewButton.setTextColor(
                                    mChange ? Color.RED : Color.WHITE);
                        }

                        mChange = !mChange;

                    }
                });
            }
        };

        Timer timer = new Timer();
        timer.schedule(timerTask, 1, 150);
    }


    /*处理金币的消耗或增加
    * 返回值为true就表示金币可以成功进行增加或减少
    * 返回值为false就表示金币不够数量,减少失败*/
    private boolean handleCoinsChange(int num) {
        if ((mCurrentCoins + num) >= 0) {
            mCurrentCoins += num;

            String str = mCurrentCoins + "";
            mTextViewCurrentConis.setText(str);
            return true;

        } else {

            return false;
        }
    }


    /*设置提示按钮:去除一个错误文字 的点击事件*/
    private void handleButtonDeleteWrongAnswer() {
        ImageButton button = (ImageButton) findViewById(R.id.btn_help_delete_wrong_answer);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*先显示对话框，确认是否真的需要提示一个正确文字*/
                showConfirmDialog(ID_DIALOG_DELETE_WORD);


            }
        });
    }


    /*设置提示按钮:提示一个正确文字 的点击事件*/
    private void handleButtonTipRightAnswer() {

        ImageButton button = (ImageButton) findViewById(R.id.btn_help_tips_right_answer);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*先显示对话框，确认是否真的需要提示一个正确文字*/
                showConfirmDialog(ID_DIALOG_TIP_WORD);


            }
        });
    }


    /*随机挑选出错误的答案中的一个并返回*/
    private WordButton helpDeleteWrongAnswer() {
        Random random = new Random();

        /*随机选择一个答案字块*/
        while (true) {
            int index = random.nextInt(MyGridView.COUNT);

            WordButton wordButton;
            wordButton = mAllWords.get(index);

            boolean contain = false;
            for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
                if (wordButton.mWordString.equals("" + mCurrentSong.getNameCharaters()[i])) {
                    contain = true;
                    break;
                }
            }
            /*如果这个答案字块是正在显示的而且它不是正确答案,就返回这个字块*/
            if (wordButton.mIsVisiable && !contain) {
                return wordButton;
            }
        }
    }

    private WordButton helpTipsRightAnswer(int index) {
        WordButton wordButton;

        for (int i = 0; i < MyGridView.COUNT; i++) {
            wordButton = mAllWords.get(i);

            if (wordButton.mWordString.equals((mCurrentSong.getNameCharaters()[index]) + "")) {
                return wordButton;
            }
        }

        return null;
    }


    private AlertDialogButtonClickListener dialogDeleteWrongAnswerButtonClickListener
            = new AlertDialogButtonClickListener() {
        @Override
        public void onClick() {
            /*实现删除一个错误答案的对话框的点击按钮逻辑*/
            if (handleCoinsChange(-getResources().getInteger(R.integer.help_delete_wrong_answer))) {
                    /*金币数量足够,减少相应的金币数量并去除一个错误答案*/
                setButtonVisibility(helpDeleteWrongAnswer(), View.INVISIBLE);
            } else {
                 /*金币数量不够*/
                showConfirmDialog(ID_DIALOG_NOT_ENOUGHT_COINS);
            }

        }
    };


    private AlertDialogButtonClickListener dialogTipRightAnswerButtonClickListener
            = new AlertDialogButtonClickListener() {
        @Override
        public void onClick() {
            /*实现提示一个正确答案的对话框的点击按钮逻辑*/


            if (handleCoinsChange(-getResources().getInteger(R.integer.help_tips_right_answer))) {

                    /*金币数量足够，减少相应的金币数量并提示一个正确答案*/
                for (int i = 0; i < mBtnSelectedWords.size(); i++) {
                    if (mBtnSelectedWords.get(i).mWordString.length() == 0) {
                        onWordButtonClick(helpTipsRightAnswer(i));
                        break;
                    } else {
                        if (checkAnswerResult == STATUS_ANSWER_INCORRECT) {
                            sparkTheWords();
                            handleCoinsChange(getResources().getInteger(R.integer.help_tips_right_answer));
                            Toast.makeText(MainActivity.this, "请先清除答案框内的错误答案", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            } else {
                /*金币数量不够*/
                showConfirmDialog(ID_DIALOG_NOT_ENOUGHT_COINS);
            }

        }
    };


    private AlertDialogButtonClickListener dialogNotEnoughCoinsButtonClickListener
            = new AlertDialogButtonClickListener() {
        @Override
        public void onClick() {
            /*实现提示金币不足的对话框的点击按钮逻辑*/
        }
    };


    /*显示对话框*/
    private void showConfirmDialog(int id) {

        int a = getResources().getInteger(R.integer.help_delete_wrong_answer);
        int b = getResources().getInteger(R.integer.help_tips_right_answer);

        switch (id) {
            case ID_DIALOG_DELETE_WORD:
                Util.showDialog(MainActivity.this,
                        "确定扣除" + a + "个金币删除一个错误答案吗",
                        dialogDeleteWrongAnswerButtonClickListener);
                break;

            case ID_DIALOG_TIP_WORD:
                Util.showDialog(MainActivity.this,
                        "确定扣除" + b + "个金币提示一个正确答案吗",
                        dialogTipRightAnswerButtonClickListener);
                break;

            case ID_DIALOG_NOT_ENOUGHT_COINS:
                Util.showDialog(MainActivity.this,
                        "金币不足，请到商店补充。",
                        dialogNotEnoughCoinsButtonClickListener);
                break;
        }
    }

    private Bitmap generateScreenShot() {
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        return view.getDrawingCache();
    }

    private void regToWx() {
        api = WXAPIFactory.createWXAPI(this, APP_ID, true);

        api.registerApp(APP_ID);
    }


    /*分享图片到微信*/
    private void wechatShare() {
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
        api.sendReq(req);


    }

    public static int coinReward(int num) {
        mCurrentCoins += num;
        mTextViewCurrentConis.setText(mCurrentCoins + "");
        return mCurrentCoins;
    }


}

