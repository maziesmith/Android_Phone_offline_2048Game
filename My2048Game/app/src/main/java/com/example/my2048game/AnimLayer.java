package com.example.my2048game;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

//这个是实现动画的类，如果在Card里使用那么每个Card的状态也会改变
public class AnimLayer extends FrameLayout {
    private List<CardFrame> cards = new ArrayList<CardFrame>();

    public AnimLayer(Context context) {
        super(context);
        initLayer();
    }

    public AnimLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayer();
    }

    public AnimLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayer();
    }

    private void initLayer() {
    }

    //移动动画
    public void createMoveAnim(final CardFrame from, final CardFrame to, int fromX, int toX, int fromY, int toY) {
        final CardFrame cardFrame = getCard(from.getNum());
        LayoutParams lp = new LayoutParams(Config.CARD_WIDTH, Config.CARD_WIDTH);
        lp.leftMargin = fromX * Config.CARD_WIDTH;
        lp.topMargin = fromX * Config.CARD_WIDTH;
        cardFrame.setLayoutParams(lp);
        if (to.getNum() <= 0) {
            to.getLabel().setVisibility(View.INVISIBLE);
        }
        TranslateAnimation ta = new TranslateAnimation(0, Config.CARD_WIDTH * (toX - fromX), 0, Config.CARD_WIDTH * (toY - fromY));
        ta.setDuration(100);
        ta.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                to.getLabel().setVisibility(View.VISIBLE);
                recycleCard(cardFrame);
            }
        });
        cardFrame.startAnimation(ta);
    }
    //优化防止太多Card出现oom，而Card添加动画后在移除会出问题，这是一个bug，高版本的没有这个bug
    //这段代码要学会使用
    private CardFrame getCard(int num) {
        CardFrame cardFrame;
        if (cards.size() > 0) {
            cardFrame = cards.remove(0);
        } else {
            cardFrame = new CardFrame(getContext());
            addView(cardFrame);
        }
        cardFrame.setVisibility(View.VISIBLE);
        cardFrame.setNum(num);
        return cardFrame;
    }

    private void recycleCard(CardFrame cardFrame) {
        cardFrame.setVisibility(View.INVISIBLE);
        cardFrame.setAnimation(null);
        cards.add(cardFrame);
    }

    public void createScaleTo1(CardFrame target) {
        ScaleAnimation sa = new ScaleAnimation(
                0.1f,
                1,
                0.1f,
                1,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        sa.setDuration(100);
        target.setAnimation(null);
        target.getLabel().startAnimation(sa);
    }
}
