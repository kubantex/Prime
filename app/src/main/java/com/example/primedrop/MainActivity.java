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
    private LinearLayout startGry;

    //Grafika
    private TextView bananyPierw, bananyZloz;
    private int bananPierwWartosc = 0, bananZlozWartosc = 0;
    private ImageView malpa, banan;
    private Drawable imgMalpaLewo, imgMalpaPrawo ;

    //Rozmiar malpy
    private int malpaRozmiar;

    //Pozycja
    private float malpaX, malpaY;
    private float bananyPierwX, bananyPierwY;
    private float bananyZlozX, bananyZlozY;
    private float bananX, bananY;


    //Wynik
    private TextView poleWynik, poleRekord;
    private int Wynik, Rekord, Czas;
    private SharedPreferences ustawienia;


    //Klasa
    private Timer timer;
    private Handler handler = new Handler();
    private SoundPlayer soundPlayer;

    //Start
    private boolean start_flg = false;
    private boolean action_flg = false;
    private boolean banan_flg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundPlayer = new SoundPlayer(this);

        poleGry = findViewById(R.id.poleGry);
        startGry = findViewById(R.id.startGry);
        malpa = findViewById(R.id.malpa);
        bananyPierw = findViewById(R.id.bananyPierw);
        bananyZloz = findViewById(R.id.bananyZloz);
        banan = findViewById(R.id.banan);
        poleWynik = findViewById(R.id.poleWynik);
        poleRekord = findViewById(R.id.poleRekord);


        imgMalpaLewo = getResources().getDrawable(R.drawable.malpa2);
        imgMalpaPrawo = getResources().getDrawable(R.drawable.malpa1);

        //Rekord punktowy

        ustawienia = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE);
        Rekord = ustawienia.getInt("NAJWYZSZY_WYNIK", 0);
        poleRekord.setText("Najwyzszy wynik : " + Rekord);

    }

    public void zmianaPozycji() {
        //Banany z liczba pierwsza
        bananyPierwY +=12;

        float bananyPierwCenterX = bananyPierwX + bananyPierw.getWidth()/2;
        float bananyPierwCenterY = bananyPierwY + bananyPierw.getHeight()/2;

        if (hitCheck(bananyPierwCenterX, bananyPierwCenterY)) {
            bananyPierwY = frameHeight + 100;
            calculateAndSetNewPrimeNumber();
            Wynik += 10;
            soundPlayer.playHitPierwSound();
        }

        if (bananyPierwY > frameHeight && bananyPierwCenterX!=bananyZlozX){
            bananyPierwY = - 100;
            bananyPierwX = (float) Math.floor(Math.random() * (frameWidth - bananyPierw.getWidth()));
        }
        bananyPierw.setX(bananyPierwX);
        bananyPierw.setY(bananyPierwY);

        //Banan
        if (!banan_flg && Czas % 10000 == 0){
            banan_flg = true;
            bananY = -5000;
            bananX = (float)Math.floor(Math.random()*(frameWidth - banan.getWidth()));
        }

        if (banan_flg){
            bananY += 20;

            float bananCenterX = bananX + banan.getWidth() /2;
            float bananCenterY = bananY + banan.getHeight() /2;

            if (hitCheck(bananCenterX,bananCenterY)){
                bananY = frameHeight + 30;
                Wynik += 30;

                // Zmiana szerokosci ramki

                if (initialFrameWidth > frameWidth * 110/100){
                    frameWidth = frameWidth * 110/100;
                    zmianaSzerokosciPola(frameWidth);
                }
                soundPlayer.playHitBananSound();
            }

            if (bananY > frameHeight) banan_flg = false;
            banan.setX(bananX);
            banan.setY(bananY);
        }

        //Banany z liczba zlozona

        bananyZlozY += 12;

        float bananyZlozCenterX = bananyZlozX + bananyZloz.getWidth() /2;
        float bananyZlozCenterY = bananyZlozY + bananyZloz.getHeight() /2;

        if (hitCheck(bananyZlozCenterX, bananyZlozCenterY)) {
            bananyZlozY = frameHeight + 100;

            calculateAndSetNewCompositeNumber();

            // Zmiana szerokosci ramki

            frameWidth = frameWidth * 80/100;
            zmianaSzerokosciPola(frameWidth);
            soundPlayer.playHitZlozSound();
            if (frameWidth <= malpaRozmiar) {
                koniecGry();


            }
        }

        if (bananyZlozY > frameHeight) {
            bananyZlozY = -100;
            bananyZlozX = (float) Math.floor(Math.random() * (frameWidth - bananyZloz.getWidth()));
        }

        bananyZloz.setX(bananyZlozX);
        bananyZloz.setY(bananyZlozY);

        //Ruch Malpy
        if (action_flg){
            //W prawo
            malpaX += 14;
            malpa.setImageDrawable(imgMalpaPrawo);
        }else {
            //w lewo
            malpaX -= 14;
            malpa.setImageDrawable(imgMalpaLewo);
        }

        //Sprawdzanie pozycji malpy
        if (malpaX < 0) {
            malpaX = 0;
            malpa.setImageDrawable(imgMalpaPrawo);
        }
        if (frameWidth - malpaRozmiar < malpaX) {
            malpaX = frameWidth - malpaRozmiar;
            malpa.setImageDrawable(imgMalpaLewo);
        }

        malpa.setX(malpaX);

        poleWynik.setText("Wynik : " + Wynik);
    }




    public boolean hitCheck(float x, float y){
        if (malpaX <= x && x<= malpaX + malpaRozmiar &&
                malpaY <= y && y<= frameHeight) {
            return true;
        }
        return false;

    }

    public void zmianaSzerokosciPola(int frameWidth){
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

        try{
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        zmianaSzerokosciPola(initialFrameWidth);

        startGry.setVisibility(View.VISIBLE);
        malpa.setVisibility(View.INVISIBLE);
        bananyZloz.setVisibility(View.INVISIBLE);
        bananyPierw.setVisibility(View.INVISIBLE);
        banan.setVisibility(View.INVISIBLE);


        //Aktualizacja najwyzszego wyniku

        if(Wynik > Rekord){
            Rekord = Wynik;
            poleRekord.setText("Najwyzszy wynik : " + Rekord);

            SharedPreferences.Editor edycja = ustawienia.edit();
            edycja.putInt("NAJWYZSZY_WYNIK", Rekord);
            edycja.commit();
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (start_flg) {
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                action_flg = true;
            }else if (event.getAction() == MotionEvent.ACTION_UP) {
                action_flg = false;
            }
        }
        return true;
    }


    public void startGame(View view) {

        calculateAndSetNewCompositeNumber();
        calculateAndSetNewPrimeNumber();

        start_flg = true;
        startGry.setVisibility(View.INVISIBLE);

        if (frameHeight ==0) {
            frameHeight = poleGry.getHeight();
            frameWidth = poleGry.getWidth();
            initialFrameWidth = frameWidth;

            malpaRozmiar = malpa.getHeight();
            malpaX = malpa.getX();
            malpaY = malpa.getY();

        }

        frameWidth = initialFrameWidth;

        malpa.setX(0.0f);
        bananyPierw.setY(3000.0f);
        bananyZloz.setY(3000.0f);
        banan.setY(3000.0f);

        bananyPierwY = bananyPierw.getY();
        bananyZlozY = bananyZloz.getY();
        bananY = banan.getY();

        malpa.setVisibility(View.VISIBLE);
        bananyPierw.setVisibility(View.VISIBLE);
        bananyZloz.setVisibility(View.VISIBLE);
        banan.setVisibility(View.VISIBLE);

        Czas = 0;
        Wynik = 0;
        poleWynik.setText("Wynik : 0");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (start_flg){
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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            finishAndRemoveTask();
        }else{
            finish();
        }
    }

    private void calculateAndSetNewCompositeNumber() {

        int[] composite = {4,6,8,9,10,12,14,15,16,18,20,21,22,24,25,26,27,28,30,32,33,34,35,36,38,39,
                40,42,44,45,46,48,49,50,51,52,54,55,56,57,58,60,62,63,64,65,66,68,69,70,
                72,74,75,76,77,78,80,81,82,84,85,86,87,88,90,91,92,93,94,95,96,98,99,100};
        int x = new Random().nextInt(composite.length);
        bananZlozWartosc = composite[x];
        bananyZloz.setText(String.valueOf(bananZlozWartosc));

    }

    private void calculateAndSetNewPrimeNumber() {

        int[] prime = {1,2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97};
        int rnd = new Random().nextInt(prime.length);
        bananPierwWartosc = prime[rnd];
        bananyPierw.setText(String.valueOf(bananPierwWartosc));
}



}

