package com.example.pomodorotimer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final long START_TIME_IN_MILLIS_STUDY = 1500000;
    private static final long START_TIME_IN_MILLIS_BREAK = 300000;
    private static final String GET_STARTED = "Let's get started!";
    private static final String STUDYING = "   Studying...   ";
    private static final String ON_BREAK = "On break! Rest Up!";

    private TextView mStarsCount;
    private TextView mTextViewCountDown;
    private TextView mTextViewWhatTime;
    private Button mButtonStartPause;
    private Button mButtonReset;

    private CountDownTimer mCountDownTimer;

    private boolean mTimerRunning;
    private boolean studyTime;

    private int points;
    private long mTimeLeftInMillis;
    private long mEndTime;

    private MediaPlayer breakSound;
    private MediaPlayer studySound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStarsCount = findViewById(R.id.text_view_starsCount);
        mTextViewCountDown = findViewById(R.id.text_view_countdown);
        mTextViewWhatTime = findViewById(R.id.text_view_whatTime);

        mButtonStartPause = findViewById(R.id.button_start_pause);
        mButtonReset = findViewById(R.id.button_start_reset);

        studyTime = true;

        breakSound = MediaPlayer.create(this, R.raw.yay);
        studySound = MediaPlayer.create(this, R.raw.boo);

        mButtonStartPause.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (mTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        mButtonReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

    }

    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                if (studyTime) {
                    studyTime = false;
                    mTimeLeftInMillis = START_TIME_IN_MILLIS_BREAK;
                    breakSound.start();
                    updateButtons();
                    updateCountDownText();
                    startTimer();
                } else {
                    mTimerRunning = false;
                    points++;
                    studySound.start();
                    resetTimer();
                }
//                mTimerRunning = false;
//                updateButtons();
//                resetTimer();
//                startTimer();
            }
        }.start();

        mTimerRunning = true;
        updateButtons();
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        updateButtons();
    }

    private void resetTimer() {
//        if (studyTime) {
//            mTimeLeftInMillis = START_TIME_IN_MILLIS_BREAK;
//            studyTime = false;
//        } else {
//            mTimeLeftInMillis = START_TIME_IN_MILLIS_STUDY;
//            studyTime = true;
//        }
        mTimeLeftInMillis = START_TIME_IN_MILLIS_STUDY;
        studyTime = true;
        updateCountDownText();
        updateButtons();

    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        mTextViewCountDown.setText(timeLeftFormatted);

        if (mTimerRunning) {
            if (studyTime) {
                mTextViewWhatTime.setText(STUDYING);
            } else {
                mTextViewWhatTime.setText(ON_BREAK);
            }
        } else {
            mTextViewWhatTime.setText(GET_STARTED);
        }
    }

    private void updateButtons() {
        mStarsCount.setText("= " + points);
        if (mTimerRunning) {
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText("Pause");
        } else {
            mButtonStartPause.setText("Start");

            if (mTimeLeftInMillis < 1000) {
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

            if (mTimeLeftInMillis < START_TIME_IN_MILLIS_STUDY) {
                mButtonReset.setVisibility(View.VISIBLE);
            } else {
                mButtonReset.setVisibility(View.INVISIBLE);
            }

        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);
        editor.putInt("points", points);

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);

        if (studyTime) {
            mTimeLeftInMillis = prefs.getLong("millisLeft", START_TIME_IN_MILLIS_STUDY);
        } else {
            mTimeLeftInMillis = prefs.getLong("millisLeft", START_TIME_IN_MILLIS_BREAK);
        }
        mTimerRunning = prefs.getBoolean("timerRunning", false);
        points = prefs.getInt("points", 0);

        updateCountDownText();
        updateButtons();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
                updateButtons();
            } else {
                startTimer();
            }
        }
    }
}