package com.example.my2048game;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class GameView extends GridLayout {
    private static GameView gameView = null;
    private CardFrame[][] cardFrames = new CardFrame[Config.LINES][Config.LINES];
    private List<Point> emptyPoints = new ArrayList<>(); //使用Point类型是为了存储x,y的值，也可以自己创建一个类，只要能存储x,y的值就Ok，而且是需要存储多个
    private boolean merge = false;

    public static GameView getGameView() {
        return gameView;
    }

    public GameView(Context context) {
        super(context);
        initGameView();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGameView();
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGameView();
    }

    //初始化
    @SuppressLint("WrongConstant")
    private void initGameView() {
        this.gameView = this;
        setOrientation(LinearLayout.VERTICAL);
//        setColumnCount(Config.LINES);//设置列数
        setBackgroundResource(R.color.gameBackground);//setBackgroundColor无法使用colors.xml的 R.color.xxxx 参数为一个颜色值，其目的是设置一个view的背景颜色
        addCards(GetCardWidth());
        setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY, offsetX, offsetY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN://按下事件
                        //记录按下的位置
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP://离开事件
                        //记录离开的位置与按下位置的偏移
                        offsetX = event.getX() - startX;
                        offsetY = event.getY() - startY;
                        //判断手势滑动方向
                        //Math.abs()是取绝对值，用x和y的进行判断，防止比如：用户往左下滑动如何区分
                        if (Math.abs(offsetX) >= Math.abs(offsetY)) {
                            if (offsetX < -5) {
                                //往左，因为会有误差所以给一个数值，而不是offsetX<0
                                swipeLeft();
                            } else if (offsetX > 5) {
                                swipeRight();
                            }
                        } else {
                            if (offsetY < -5) {
                                swipeTop();
                            } else if (offsetY > 5) {
                                swipeBottom();
                            }
                        }
                        if (merge) {
                            addRandomNum();
                            checkComplete();
                        }
                        break;
                }
                return true;
            }
        });
        //onSizeChange这个方法是在构造函数执行之后才会执行的,所以无法添加到布局中,所以想要添加到布局中要在构造里使用，
    }

    //添加Card
    public void addCards(int cardWidth) {
        CardFrame cardFrame;
        LinearLayout line;
        LinearLayout.LayoutParams lineLp;
        for (int y = 0; y < Config.LINES; y++) {
            line = new LinearLayout(getContext());
            lineLp = new LinearLayout.LayoutParams(-1, cardWidth);
            addView(line, lineLp);
            for (int x = 0; x < Config.LINES; x++) {
                cardFrame = new CardFrame(getContext());
                line.addView(cardFrame, cardWidth, cardWidth);
                cardFrames[x][y] = cardFrame;
            }
        }
    }

    //根据手机屏幕大小不同动态改变，为了适应不同屏幕大小的手机
    //在清单文件中设置android:screenOrientation="portrait"屏幕为直立
    //获取屏幕的宽和高
    private int GetCardWidth() {
        //屏幕信息的对象
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        //获取屏幕信息
        //设置每一个card的宽和高,因为card是正方形，求最小值，为了判断屏幕的宽或高那个小
        int cardWidth = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        //一行有四个卡片，每个卡片占屏幕的四分之一
        return (cardWidth - 10) / Config.LINES;

    }

    //onSizeChange这个方法是在构造函数执行之后才会执行的,所以无法添加到布局中,所以想要添加到布局中要在构造里使用，在这里启动startGame，因为MainActivity.getMainActivity().cleanScore();需要获得控件，而在构造里无法获取，因为还没执行完，控件不存在。
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        startGame();
    }

    //开始游戏
    public void startGame() {
        MainActivity mainActivity = MainActivity.getMainActivity();
        mainActivity.cleanScore();
        mainActivity.showBestScore(mainActivity.getBestScore());
        for (int y = 0; y < Config.LINES; y++) {
            for (int x = 0; x < Config.LINES; x++) {
                cardFrames[x][y].setNum(0);//所有值都需要清零
            }
        }
        addRandomNum();
        addRandomNum();
    }

    //添加随机数
    private void addRandomNum() {
        emptyPoints.clear();
        for (int y = 0; y < Config.LINES; y++) {
            for (int x = 0; x < Config.LINES; x++) {
                if (cardFrames[x][y].getNum() <= 0) {
                    //使用Point类型是为了存储x,y的值，也可以自己创建一个类，只要能存储x,y的值就Ok，而且是需要存储多个
                    emptyPoints.add(new Point(x, y));
                }
            }
        }
        if (emptyPoints.size()>0) {

            Point p = emptyPoints.remove((int)(Math.random()*emptyPoints.size()));//这个是随机移除的类
            cardFrames[p.x][p.y].setNum(Math.random() > 0.1 ? 2 : 4);//4和2的出现比例为1:9

            MainActivity.getMainActivity().getAnimLayer().createScaleTo1(cardFrames[p.x][p.y]);
        }


    }

    //结束
    private void checkComplete() {
        boolean complete = true;
        ALL:
        for (int y = 0; y < Config.LINES; y++) {
            for (int x = 0; x < Config.LINES; x++) {
                if ((cardFrames[x][y].getNum() == 0) ||
                        (x > 0 && cardFrames[x][y].equals(cardFrames[x - 1][y])) ||
                        (x < Config.LINES-1 && cardFrames[x][y].equals(cardFrames[x + 1][y])) ||
                        (y > 0 && cardFrames[x][y].equals(cardFrames[x][y - 1])) ||
                        (y < Config.LINES-1 && cardFrames[x][y].equals(cardFrames[x][y + 1]))) {
                    complete = false;
                    break ALL;
                }
            }
        }
        if (complete) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Hello！你好")
                    .setMessage("Game Over!")
                    .setPositiveButton("重新开始", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startGame();
                        }
                    })
                    .show();
        }
    }

    private void swipeLeft() {
        merge = false;
        for (int y = 0; y < Config.LINES; y++) {
            for (int x = 0; x < Config.LINES; x++) {
                for (int x1 = x + 1; x1 < Config.LINES; x1++) {
                    if (cardFrames[x1][y].getNum() > 0) {
                        if (cardFrames[x][y].getNum() <= 0) {
                            MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardFrames[x1][y], cardFrames[x][y],x1, x, y, y);
                            cardFrames[x][y].setNum(cardFrames[x1][y].getNum());
                            cardFrames[x1][y].setNum(0);
                            x--;//如果不x--不在遍历一遍会出现这种情况：  空  2  空  2 -->结果： 2  2  空  空
                            merge = true;
                        } else if (cardFrames[x][y].getNum() == cardFrames[x1][y].getNum()) {//如果不判断，则会隔着数进行相加，例如：2  4  2  空,结果：4  4  空  空
                            MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardFrames[x1][y], cardFrames[x][y],x1, x, y, y);
                            cardFrames[x][y].setNum(cardFrames[x][y].getNum() * 2);
                            cardFrames[x1][y].setNum(0);
                            MainActivity.getMainActivity().addScore(cardFrames[x][y].getNum());
                            merge = true;
                        }
                        break;//已经找到了，并且放到了对应的位置并且x--需要重新遍历，所以跳出当前循环
                    }
                }
            }
        }

    }

    private void swipeRight() {
        merge = false;
        for (int y = 0; y < Config.LINES; y++) {
            for (int x = Config.LINES-1; x >= 0; x--) {
                for (int x1 = x - 1; x1 >= 0; x1--) {
                    if (cardFrames[x1][y].getNum() > 0) {
                        if (cardFrames[x][y].getNum() <= 0) {
                            MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardFrames[x1][y], cardFrames[x][y],x1, x, y, y);
                            cardFrames[x][y].setNum(cardFrames[x1][y].getNum());
                            cardFrames[x1][y].setNum(0);
                            x++;
                            merge = true;
                        } else if (cardFrames[x][y].getNum() == cardFrames[x1][y].getNum()) {
                            MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardFrames[x1][y], cardFrames[x][y],x1, x, y, y);
                            cardFrames[x][y].setNum(cardFrames[x][y].getNum() * 2);
                            cardFrames[x1][y].setNum(0);
                            MainActivity.getMainActivity().addScore(cardFrames[x][y].getNum());
                            merge = true;
                        }
                        break;
                    }
                }
            }
        }
    }

    private void swipeTop() {
        merge = false;
        for (int x = 0; x < Config.LINES; x++) {
            for (int y = 0; y < Config.LINES; y++) {
                for (int y1 = y + 1; y1 < Config.LINES; y1++) {
                    if (cardFrames[x][y1].getNum() > 0) {
                        if (cardFrames[x][y].getNum() <= 0) {
                            MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardFrames[x][y1],cardFrames[x][y], x, x, y1, y);
                            cardFrames[x][y].setNum(cardFrames[x][y1].getNum());
                            cardFrames[x][y1].setNum(0);
                            y--;//如果不x--不在遍历一遍会出现这种情况：  空  2  空  2 -->结果： 2  2  空  空
                            merge = true;
                        } else if (cardFrames[x][y].getNum() == cardFrames[x][y1].getNum()) {
                            MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardFrames[x][y1],cardFrames[x][y], x, x, y1, y);
                            cardFrames[x][y].setNum(cardFrames[x][y].getNum() * 2);
                            cardFrames[x][y1].setNum(0);
                            MainActivity.getMainActivity().addScore(cardFrames[x][y].getNum());
                            merge = true;
                        }
                        break;
                    }
                }
            }
        }
    }

    private void swipeBottom() {
        merge = false;
        for (int x = 0; x < Config.LINES; x++) {
            for (int y = Config.LINES-1; y >= 0; y--) {
                for (int y1 = y - 1; y1 >= 0; y1--) {
                    if (cardFrames[x][y1].getNum() > 0) {
                        if (cardFrames[x][y].getNum() <= 0) {
                            MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardFrames[x][y1],cardFrames[x][y], x, x, y1, y);
                            cardFrames[x][y].setNum(cardFrames[x][y1].getNum());
                            cardFrames[x][y1].setNum(0);
                            y++;
                            merge = true;
                        } else if (cardFrames[x][y].getNum() == cardFrames[x][y1].getNum()) {
                            MainActivity.getMainActivity().getAnimLayer().createMoveAnim(cardFrames[x][y1],cardFrames[x][y], x, x, y1, y);
                            cardFrames[x][y].setNum(cardFrames[x][y].getNum() * 2);
                            cardFrames[x][y1].setNum(0);
                            MainActivity.getMainActivity().addScore(cardFrames[x][y].getNum());
                            merge = true;
                        }
                        break;
                    }
                }
            }
        }
    }
}
