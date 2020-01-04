package com.example.primedrop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //Pole gry
    FrameLayout poleGry;

    //Wynik
    private TextView poleWynik;
    private int wynik;

    //Klasa
    private Timer timer;
    private Handler handler = new Handler();

    //Start
    private boolean start_flg = false;

    private PrimeGame primeGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Utworzenie obiektu klasy PrimeGame, przesyłamy do kontruktora kontext w którym zostanie
        // gra uruchomiona i gdzie ma utworzyć wszystkie elementy
        primeGame = new PrimeGame(this);

        poleGry = findViewById(R.id.poleGry);
        poleWynik = findViewById(R.id.poleWynik);
    }

    public void zmianaPozycji() {
        primeGame.calculateFrame();
        primeGame.drawFrame();

    }

    public void koniecGry() {
        //Stop zegara
        timer.cancel();
        timer = null;
        start_flg = false;

        //Przed wyswietleniem ekranu startowego sleep 1s

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        primeGame.endGame();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (start_flg) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                primeGame.moveMonkey(Direction.RIGHT);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                primeGame.moveMonkey(Direction.LEFT);
            }
        }
        return true;
    }


    public void startGame(View view) {

        // Startujemy grę
        primeGame.startGame();

        start_flg = true;

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (start_flg) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            zmianaPozycji();
                        }
                    });
                }
            }
        }, 0, 20);

    }

    public void quitGame(View viev) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }



}

