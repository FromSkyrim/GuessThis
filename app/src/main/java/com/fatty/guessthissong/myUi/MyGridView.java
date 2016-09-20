package com.fatty.guessthissong.myUi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.fatty.guessthissong.R;
import com.fatty.guessthissong.model.WordButton;
import com.fatty.guessthissong.model.WordButtonClickListener;
import com.fatty.guessthissong.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 17255 on 2016/8/22.
 */
public class MyGridView extends GridView {

    public final static int COUNT = 24;

    private List<WordButton> mArrayList = new ArrayList<>();
    private MyGridAdapter mAdapter;
    private Context mContext;
    private WordButtonClickListener mWordButtonClickListener;

    private Animation mScaleAnimation;

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        /*新建一个自己的实体,并把自己这个GridView关联进去*/
        mAdapter = new MyGridAdapter();
        this.setAdapter(mAdapter);
    }

    public void updateData(ArrayList<WordButton> list) {
        mArrayList = list;

        setAdapter(mAdapter);
    }

    class MyGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return mArrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final WordButton holder;

            if (view == null) {
                view = Util.getView(mContext, R.layout.grid_view_item);


                /*加载动画*/
                mScaleAnimation = AnimationUtils.loadAnimation(mContext, R.anim.scale);

                /*设置动画延迟*/
                mScaleAnimation.setStartOffset(i * 40);

                holder = mArrayList.get(i);
                holder.mIndex = i;
                holder.mViewButton = (Button) view.findViewById(R.id.item_btn);

                /*设置这24个按钮的点击事件监听器*/
                holder.mViewButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mWordButtonClickListener.onWordButtonClick(holder);
                    }
                });

                view.setTag(holder);
            } else {
                holder = (WordButton) view.getTag();
            }
            holder.mViewButton.setText(holder.mWordString);

            view.startAnimation(mScaleAnimation);

            return view;
        }
    }

    public void registerWordButtonClickListener(WordButtonClickListener wordButtonClickListener) {
        mWordButtonClickListener = wordButtonClickListener;
    }
}
