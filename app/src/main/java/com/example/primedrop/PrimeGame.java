package com.example.primedrop;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

/**
 * Główna klasa gry - to tutaj dokonują się wszystkie obliczenia.
 */
public class PrimeGame {

    // Stale - ustawienia
    private static final int MONKEY_MOVING_DISTANCE = 14;
    private static final String GAME_DATA = "GAME_DATA";
    private static final String NAJWYZSZY_WYNIK = "NAJWYZSZY_WYNIK";
    private static final int BANANA_MOVING_DISTANCE = 12;
    private static final int POINTS_PER_HIT = 10;
    private static final int[] PRIMES = new int[]{1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41,
            43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97};
    private static final int[] COMPOSITE = new int[]{4, 6, 8, 9, 10, 12, 14, 15, 16, 18, 20, 21, 22, 24, 25, 26, 27, 28, 30, 32, 33, 34, 35, 36, 38, 39,
            40, 42, 44, 45, 46, 48, 49, 50, 51, 52, 54, 55, 56, 57, 58, 60, 62, 63, 64, 65, 66, 68, 69, 70,
            72, 74, 75, 76, 77, 78, 80, 81, 82, 84, 85, 86, 87, 88, 90, 91, 92, 93, 94, 95, 96, 98, 99, 100};
    public static final int BONUS_BANANA_DISTANCE_CHANGE = 20;
    public static final int IMAGE_SIZE = 100;

    private final Random rnd = new Random();

    private BananaGameObject primeBanana;
    private BananaGameObject compositeBanana;
    private BananaGameObject bonusBanana;

    private MonkeyGameObject monkey;
    private Activity context;

    private boolean bonusBananaIsActive;

    private int frameWidth;
    private int frameHeight;
    private int initialFrameWidth;

    private SharedPreferences sharedPreferences;
    private int record;
    private int time;
    private int result;

    // Zmienne powiazane z widokiem gry
    private View startGameView;
    private View gameView;
    private TextView recordTextView;
    private ImageView monkeyView;
    private Drawable leftLookingMonkeyDrawable;
    private Drawable rightLookingMonkeyDrawable;
    private TextView primeBananaView;
    private TextView compositeBananaView;
    private ImageView bonusBananaView;
    private TextView resultTextView;

    private SoundPlayer soundPlayer;

    private boolean activeGame;

    /**
     * Kontstruktor - tutaj inicjujemy wszystkie zmienne.
     *
     * @param context - kontekst uruchomienia gry
     */
    public PrimeGame(Activity context) {
        this.context = context;

        activeGame = false;
        primeBanana = new BananaGameObject();
        // Te wartosci wczytac z widoku - przerobic
        primeBanana.setSizeX(IMAGE_SIZE);
        primeBanana.setSizeY(IMAGE_SIZE);
        compositeBanana = new BananaGameObject();
        compositeBanana.setSizeY(IMAGE_SIZE);
        compositeBanana.setSizeX(IMAGE_SIZE);
        bonusBanana = new BananaGameObject();
        bonusBanana.setSizeX(IMAGE_SIZE);
        bonusBanana.setSizeY(IMAGE_SIZE);
        monkey = new MonkeyGameObject();
        monkey.setSizeX(IMAGE_SIZE);
        monkey.setSizeY(IMAGE_SIZE);

        soundPlayer = new SoundPlayer(context);

        // Widok
        startGameView = context.findViewById(R.id.startGry);
        gameView = context.findViewById(R.id.poleGry);
        recordTextView = context.findViewById(R.id.poleRekord);
        monkeyView = context.findViewById(R.id.malpa);
        leftLookingMonkeyDrawable = context.getResources().getDrawable(R.drawable.malpa2);
        rightLookingMonkeyDrawable = context.getResources().getDrawable(R.drawable.malpa1);
        primeBananaView = context.findViewById(R.id.bananyPierw);
        compositeBananaView = context.findViewById(R.id.bananyZloz);
        bonusBananaView = context.findViewById(R.id.banan);
        resultTextView = context.findViewById(R.id.poleWynik);

        //Rekord punktowy
        sharedPreferences = context.getSharedPreferences(GAME_DATA, Context.MODE_PRIVATE);
        record = sharedPreferences.getInt(NAJWYZSZY_WYNIK, 0);
        recordTextView.setText("Najwyzszy wynik : " + record);

    }

    /**
     * Wykonujemy obliczenia gry - wykonywane w pętli. Tutaj nie zmieniamy widoku - tylko obliczamy.
     * Podzieliłem obliczanie logiki gry oraz jej wyświetlanie - dzięki czemu póżniejsze ewentualne
     * zmiany są łatwiejsze, można zmienić sposób wyświetlania czy platformę. Wpływa też na czytelność
     * kodu.
     */
    public void calculateFrame() {
        calculatePrimeBananaPosition();
        calculateBonusBananaPosition();
        calculateComposieteBananaPosition();
        calculateMonkeyPosition();
    }

    private void calculateBonusBananaPosition() {
        if (!bonusBananaIsActive && time % 10000 == 0) {
            bonusBananaIsActive = true;
            bonusBanana.setPositionY(-5000);
            bonusBanana.setPositionX((float) Math.floor(Math.random() * (frameWidth - bonusBanana.getSizeX())));
        }

        if (bonusBananaIsActive) {
            bonusBanana.setPositionY(bonusBanana.getPositionY() + BONUS_BANANA_DISTANCE_CHANGE);

            float bananCenterX = bonusBanana.getPositionX() + bonusBanana.getSizeX() / 2;
            float bananCenterY = bonusBanana.getPositionY() + bonusBanana.getSizeY() / 2;

            if (hitCheck(bananCenterX, bananCenterY)) {
                bonusBanana.setPositionY(frameHeight + 30);
                result += 30;

                // Zmiana szerokosci ramki
                if (initialFrameWidth > frameWidth * 110 / 100) {
                    frameWidth = frameWidth * 110 / 100;
                    changeGameFrameWidth(frameWidth);
                }
                soundPlayer.playHitBananSound();
            }

            if (bonusBanana.getPositionY() > frameHeight) {
                bonusBananaIsActive = false;
            }
        }
    }

    private void calculateComposieteBananaPosition() {
        //Banany z liczba zlozona
        compositeBanana.setPositionY(compositeBanana.getPositionY() + BANANA_MOVING_DISTANCE);

        float bananyZlozCenterX = compositeBanana.getPositionX() + compositeBanana.getSizeX() / 2;
        float bananyZlozCenterY = compositeBanana.getPositionY() + compositeBanana.getSizeY() / 2;

        if (hitCheck(bananyZlozCenterX, bananyZlozCenterY)) {
            compositeBanana.setPositionY(frameHeight + 100);

            // Zmiana szerokosci ramki
            frameWidth = frameWidth * 80 / 100;
            changeGameFrameWidth(frameWidth);
            soundPlayer.playHitZlozSound();
            if (frameWidth <= monkey.getSizeX()) {
                endGame();
            }
        }

        if (compositeBanana.getPositionY() > frameHeight) {
            calculateAndSetNewCompositeNumber();
            compositeBanana.setPositionY(-100);
            compositeBanana.setPositionX((float) Math.floor(Math.random() * (frameWidth - compositeBanana.getSizeX())));
        }
    }

    private void calculatePrimeBananaPosition() {
        //Banany z liczba pierwsza
        primeBanana.setPositionY(primeBanana.getPositionY() + BANANA_MOVING_DISTANCE);

        float primeBananaCenterX = primeBanana.getPositionX() + primeBanana.getSizeX() / 2;
        float primeBananaCenterY = primeBanana.getPositionY() + primeBanana.getSizeY() / 2;

        // Sprawdzenie czy złapaliśmy banana naszą małpką
        if (hitCheck(primeBananaCenterX, primeBananaCenterY)) {
            result += POINTS_PER_HIT;
            primeBanana.setPositionY(frameHeight + 100);
            soundPlayer.playHitPierwSound();
        }

        // Sprawdzanie czy banan nie przeleciał przez ekran i porównujemy z composite banana?
        // Nie wiem czy to dobrze działa
        if (primeBanana.getPositionY() > frameHeight && primeBananaCenterX != compositeBanana.getPositionX()) {
            calculateAndSetNewPrimeNumber();
            primeBanana.setPositionY(-100);
            primeBanana.setPositionX((float) Math.floor(Math.random() * (frameWidth - primeBanana.getSizeX())));
        }
    }

    private void calculateAndSetNewPrimeNumber() {
        int n = rnd.nextInt(PRIMES.length);
        primeBanana.setNumberValue(PRIMES[n]);
    }

    private void calculateAndSetNewCompositeNumber() {
        int x = rnd.nextInt(COMPOSITE.length);
        compositeBanana.setNumberValue(COMPOSITE[x]);
    }


    private boolean hitCheck(float x, float y) {
        return monkey.getPositionX() <= x && x <= monkey.getPositionX() + monkey.getSizeX() &&
               monkey.getPositionY() <= y && y <= frameHeight;
    }

    private void calculateMonkeyPosition() {
        if (monkey.getMovingDirection() == Direction.RIGHT) {
            //W prawo
            monkey.setPositionX(monkey.getPositionX() + MONKEY_MOVING_DISTANCE);
            monkey.setLookingDirection(Direction.RIGHT);
        }
        if (monkey.getMovingDirection() == Direction.LEFT) {
            //w lewo
            monkey.setPositionX(monkey.getPositionX() - MONKEY_MOVING_DISTANCE);
            monkey.setLookingDirection(Direction.LEFT);
        }

        //Sprawdzanie pozycji malpy
        if (monkey.getPositionX() < 0) {
            monkey.setPositionX(0);
            monkey.setLookingDirection(Direction.RIGHT);
        }

        if (frameWidth - monkey.getSizeX() < monkey.getPositionX()) {
            monkey.setPositionX(frameWidth - monkey.getSizeX());
            monkey.setLookingDirection(Direction.LEFT);
        }
    }

    /**
     * Wyświetlamy wszystko wcześniej obliczone na ekranie. Też wykonywane jest w pętli.
     */
    public void drawFrame() {

        monkeyView.setX(monkey.getPositionX());
        monkeyView.setY(monkey.getPositionY());
        if (monkey.getLookingDirection() == Direction.LEFT) {
            monkeyView.setImageDrawable(leftLookingMonkeyDrawable);
        }
        if (monkey.getLookingDirection() == Direction.RIGHT) {
            monkeyView.setImageDrawable(rightLookingMonkeyDrawable);
        }

        primeBananaView.setX(primeBanana.getPositionX());
        primeBananaView.setY(primeBanana.getPositionY());
        primeBananaView.setText(String.valueOf(primeBanana.getNumberValue()));

        compositeBananaView.setX(compositeBanana.getPositionX());
        compositeBananaView.setY(compositeBanana.getPositionY());
        compositeBananaView.setText(String.valueOf(compositeBanana.getNumberValue()));

        bonusBananaView.setY(bonusBanana.getPositionY());
        bonusBananaView.setX(bonusBanana.getPositionX());

        resultTextView.setText("Wynik : " + result);

    }

    /**
     * Start gry - pokazujemy wszystkie elementy i resetujemy do wartosci startowych.
     */
    public void startGame() {
        activeGame = true;
        result = 0;

        bonusBananaIsActive = false;

        if (frameHeight == 0) {
            frameHeight = gameView.getHeight();
            frameWidth = gameView.getWidth();
            initialFrameWidth = frameWidth;
            monkey.setPositionY(frameHeight - monkey.getSizeY());
        }
        frameWidth = initialFrameWidth;
        changeGameFrameWidth(initialFrameWidth);

        startGameView.setVisibility(View.INVISIBLE);
        monkeyView.setVisibility(View.VISIBLE);
        primeBananaView.setVisibility(View.VISIBLE);
        compositeBananaView.setVisibility(View.VISIBLE);
        bonusBananaView.setVisibility(View.VISIBLE);

        primeBanana.setPositionY(3000.0f);
        compositeBanana.setPositionY(3000.0f);
        monkey.setPositionX(0);
        bonusBanana.setPositionY(3000.0f);
    }

    public void endGame() {
        activeGame = false;
        startGameView.setVisibility(View.VISIBLE);
        monkeyView.setVisibility(View.INVISIBLE);
        primeBananaView.setVisibility(View.INVISIBLE);
        compositeBananaView.setVisibility(View.INVISIBLE);
        bonusBananaView.setVisibility(View.INVISIBLE);

        //Aktualizacja najwyzszego wyniku
        if (result > record) {
            record = result;
            recordTextView.setText("Najwyzszy wynik : " + record);
            SharedPreferences.Editor edycja = sharedPreferences.edit();
            edycja.putInt(NAJWYZSZY_WYNIK, record);
            edycja.commit();
        }

        changeGameFrameWidth(initialFrameWidth);

    }

    public void changeGameFrameWidth(int frameWidth) {
        ViewGroup.LayoutParams params = gameView.getLayoutParams();
        params.width = frameWidth;
        gameView.setLayoutParams(params);
    }

    /**
     * Zmienia kierunek małpki
     *
     * @param direction
     */
    public void moveMonkey(Direction direction) {
        monkey.setMovingDirection(direction);
    }
}
