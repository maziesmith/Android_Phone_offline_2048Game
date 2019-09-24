package com.example.my2048game;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView tvScore;
    private static MainActivity mainActivity = null;
    private int score = 0;
    public static final String SP_KEY_BEST_SCORE = "bestScore";
    private TextView tvBestScore;

    public MainActivity() {
        mainActivity = this;
    }
    private AnimLayer animLayer = null;
    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvScore = (TextView) findViewById(R.id.tvScore);
        tvBestScore = (TextView) findViewById(R.id.tvBestScore);
        animLayer = (AnimLayer) findViewById(R.id.animLayer);
    }

    public void cleanScore() {
        score = 0;
        showScore();
    }

    public void addScore(int s) {
        score += s;
        showScore();

        int maxScore = Math.max(score, getBestScore());
        saveBestScore(maxScore);
        showBestScore(maxScore);
    }

    private void showScore() {
        tvScore.setText(score + "");
    }

    public void start(View view) {
        GameView.getGameView().startGame();
    }
    public AnimLayer getAnimLayer() {
        return animLayer;
    }
    //之前的分数
    public void saveBestScore(int s){
        SharedPreferences.Editor e = getPreferences(MODE_PRIVATE).edit();
        e.putInt(SP_KEY_BEST_SCORE, s);
        e.commit();
    }

    public int getBestScore(){
        return getPreferences(MODE_PRIVATE).getInt(SP_KEY_BEST_SCORE, 0);
    }

    public void showBestScore(int s){
        tvBestScore.setText(s+"");
    }
}
