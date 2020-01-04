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
    private int frameHeight, frameWidth, initialFrameWidth;

    //Grafika
    private TextView bananyZloz;
    private int bananZlozWartosc = 0;
    private ImageView malpa, banan;

    private float bananX, bananY;

    //Wynik
    private TextView poleWynik;
    private int wynik, Czas;

    //Klasa
    private Timer timer;
    private Handler handler = new Handler();
    private SoundPlayer soundPlayer;

    //Start
    private boolean start_flg = false;
    private boolean banan_flg = false;

    private PrimeGame primeGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Utworzenie obiektu klasy PrimeGame, przesyłamy do kontruktora kontext w którym zostanie
        // gra uruchomiona i gdzie ma utworzyć wszystkie elementy
        primeGame = new PrimeGame(this);

        soundPlayer = new SoundPlayer(this);

        poleGry = findViewById(R.id.poleGry);
        banan = findViewById(R.id.banan);
        poleWynik = findViewById(R.id.poleWynik);
    }

    public void zmianaPozycji() {

        //Banan
        /*
        if (!banan_flg && Czas % 10000 == 0) {
            banan_flg = true;
            bananY = -5000;
            bananX = (float) Math.floor(Math.random() * (frameWidth - banan.getWidth()));
        }

        if (banan_flg) {
            bananY += 20;

            float bananCenterX = bananX + banan.getWidth() / 2;
            float bananCenterY = bananY + banan.getHeight() / 2;

            if (hitCheck(bananCenterX, bananCenterY)) {
                bananY = frameHeight + 30;
                wynik += 30;

                // Zmiana szerokosci ramki

                if (initialFrameWidth > frameWidth * 110 / 100) {
                    frameWidth = frameWidth * 110 / 100;
                    zmianaSzerokosciPola(frameWidth);
                }
                soundPlayer.playHitBananSound();
            }

            if (bananY > frameHeight) banan_flg = false;
            banan.setX(bananX);
            banan.setY(bananY);
        }
        */

        poleWynik.setText("Wynik : " + wynik);

        primeGame.calculateFrame();
        primeGame.drawFrame();

    }

    public void zmianaSzerokosciPola(int frameWidth) {
        ViewGroup.LayoutParams params = poleGry.getLayoutParams();
        params.width = frameWidth;
        poleGry.setLayoutParams(params);
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

        zmianaSzerokosciPola(initialFrameWidth);

        banan.setVisibility(View.INVISIBLE);

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

        frameWidth = initialFrameWidth;

        banan.setY(3000.0f);
        bananY = banan.getY();

        banan.setVisibility(View.VISIBLE);

        Czas = 0;
        wynik = 0;
        poleWynik.setText("Wynik : 0");

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

