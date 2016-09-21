package com.fatty.guessthissong.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fatty.guessthissong.MainActivity;
import com.fatty.guessthissong.R;

import com.fatty.guessthissong.data.Const;
import com.fatty.guessthissong.model.AlertDialogButtonClickListener;
import com.fatty.guessthissong.model.Song;
import com.fatty.guessthissong.model.WordButton;
import com.fatty.guessthissong.myUi.MyGridView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * Created by 17255 on 2016/8/24.
 */
public class Util {

    public static AlertDialog mAlertDialog;

    public static View getView(Context context, int layoutID) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(layoutID, null);

    }

    /*生成随机的文字,包含歌名*/
    public static String[] generateWords(Song currentSong) {
        String[] words = new String[MyGridView.COUNT];
        Random random = new Random();

        /*把歌名存进去*/
        for (int i = 0; i < currentSong.getNameLength(); i++) {
            words[i] = currentSong.getNameCharaters()[i] + "";
        }
        
        /*把随机字存进去*/
        for (int i = currentSong.getNameLength(); i < MyGridView.COUNT; i++) {
            words[i] = getRandomChar() + "";
        }

        /*打乱文字*/
        for (int i = MyGridView.COUNT - 1; i >= 0; i--) {
            int index = random.nextInt(i + 1);

            String buf = words[index];
            words[index] = words[i];
            words[i] = buf;
        }

        return words;
    }


    /*获取随机汉字*/
    public static char getRandomChar() {
        String str = "";
        int highPos;
        int lowPos;

        Random random = new Random();

        highPos = (176 + Math.abs(random.nextInt(39)));
        lowPos = (161 + Math.abs(random.nextInt(93)));

        byte[] b = new byte[2];
        b[0] = (Integer.valueOf(highPos)).byteValue();
        b[1] = (Integer.valueOf(lowPos)).byteValue();

        try {
            str = new String(b, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return str.charAt(0);
    }

    /*启动另一个activity*/
    public static void startActivity(Context context, Class destiny, int value) {
        Intent intent = new Intent(context, destiny);
        intent.putExtra("data", value);
        context.startActivity(intent);

        /*关闭当前的activity*/
        ((Activity) context).finish();
    }


    /*显示对话框*/
    public static void showDialog(final Context context,
                                  String message,
                                  final AlertDialogButtonClickListener listener) {

        View dialogView = getView(context, R.layout.dialog_view);

        AlertDialog.Builder builder
                = new AlertDialog.Builder(context, R.style.Theme_Transparent);

        /*初始化对话框的ok按钮*/
        ImageButton mAlertDialogOk
                = (ImageButton) dialogView.findViewById(R.id.imageButton_alertDialog_ok);

        /*初始化对话框的cancel按钮*/
        ImageButton mAlertDialogCancel
                = (ImageButton) dialogView.findViewById(R.id.imageButton_alertDialog_cancel);

        /*初始化对话框的问题*/
        TextView mAlertDialogQuestion
                = (TextView) dialogView.findViewById(R.id.textView_alertDialog_question);

        /*设置对话框文字的问题*/
        mAlertDialogQuestion.setText(message);


        /*设置对话框ok按钮的点击事件*/
        mAlertDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlertDialog.cancel();
                listener.onClick();
                MyPlayer.playSoundEffect(context, MyPlayer.INDEX_SOUND_EFFECT_ENTER);
            }
        });

        /*设置对话框cancel按钮的点击事件*/
        mAlertDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlertDialog.cancel();
                MyPlayer.playSoundEffect(context, MyPlayer.INDEX_SOUND_EFFECT_CANCEL);
            }
        });

        /*为dialog设置view*/
        builder.setView(dialogView);
        mAlertDialog = builder.create();

        /*显示对话框*/
        mAlertDialog.show();

    }

    /*保存游戏数据*/
    public static void saveData(Context context, int stageNum, int coinCount) {
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = context.openFileOutput(Const.SAVE_DATA_FILE_NAME, Context.MODE_PRIVATE);
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

            dataOutputStream.writeInt(stageNum);
            dataOutputStream.writeInt(coinCount);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*读取游戏数据*/
    public static int[] loadData(Context context) {
        FileInputStream fileInputStream = null;
        /*一维数组，第一个数据是关卡的序号，第二个是金币的数量，初始化的时候先填入初始值*/
        int[] data = {-1, Const.TOTAL_COINS};

        try {
            fileInputStream = context.openFileInput(Const.SAVE_DATA_FILE_NAME);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            /*从文件中读取数据*/
            data[0] = dataInputStream.readInt();
            data[1] = dataInputStream.readInt();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }


    public static void goToMarket(Context context, String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }


}
