package com.example.lundaquizet;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements SensorEventListener {

    private TextView diceResult, questionText;
    private ImageView diceImage;
    private Button playButton, playAgainButton, homeButton;
    private Button option1, option2, option3, option4;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Vibrator vibrator;
    private long lastShakeTime = 0;
    private boolean isRollingEnabled = false;
    private static final int SHAKE_THRESHOLD = 10;
    private String[] categories = {"Historia", "Geografi", "Lunds Universitet", "Kända Personer", "Lund i Siffror", "Blandat"};
    private Question currentQuestion;
    private MediaPlayer diceSound;
    private MediaPlayer correctSound;
    private MediaPlayer errorSound;
    private Sensor proximitySensor;
    private boolean hintUsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        diceResult = findViewById(R.id.diceResult);
        questionText = findViewById(R.id.questionText);
        diceImage = findViewById(R.id.diceImage);
        playButton = findViewById(R.id.playButton);
        playAgainButton = findViewById(R.id.playAgainButton);
        homeButton = findViewById(R.id.homeButton);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        QuestionLoader.loadQuestions(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }

        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        playButton.setOnClickListener(v -> startGame());

        playAgainButton.setOnClickListener(v -> resetGame());

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        diceSound = MediaPlayer.create(this, R.raw.diceroll);
        correctSound = MediaPlayer.create(this, R.raw.correct);
        errorSound = MediaPlayer.create(this, R.raw.error);

        isRollingEnabled = false;
    }

    private void startGame() {
        isRollingEnabled = true;
        hintUsed = false;
        playButton.setVisibility(View.GONE);
        playAgainButton.setVisibility(View.GONE);
        homeButton.setVisibility(View.GONE);
        diceResult.setText("Skaka för att kasta tärningen!");
        questionText.setText("");

        enableAnswerButtons(false);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            detectShake(event);
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            detectHintGesture(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void showQuestion(Question question) {

        hintUsed = false;

        questionText.setText(question.getQuestion());

        List<String> shuffledOptions = Arrays.asList(question.getOptions());
        Collections.shuffle(shuffledOptions);

        option1.setText(shuffledOptions.get(0));
        option2.setText(shuffledOptions.get(1));
        option3.setText(shuffledOptions.get(2));
        option4.setText(shuffledOptions.get(3));

        enableAnswerButtons(true);

        View.OnClickListener answerClickListener = v -> checkAnswer(((Button) v).getText().toString());
        option1.setOnClickListener(answerClickListener);
        option2.setOnClickListener(answerClickListener);
        option3.setOnClickListener(answerClickListener);
        option4.setOnClickListener(answerClickListener);
    }

    private void checkAnswer(String selectedAnswer) {
        if (selectedAnswer.equals(currentQuestion.getAnswer())) {
            if (correctSound != null) {
                correctSound.seekTo(0);
                correctSound.start();
            }
            Toast.makeText(this, "Rätt!", Toast.LENGTH_SHORT).show();
        } else {
            if (errorSound != null) {
                errorSound.seekTo(0);
                errorSound.start();
            }
            Toast.makeText(this, "Fel! Rätt svar är: " + currentQuestion.getAnswer(), Toast.LENGTH_SHORT).show();
        }

        enableAnswerButtons(false);

        LinearLayout buttonLayout = findViewById(R.id.buttonLayout);
        Button playAgainButton = findViewById(R.id.playAgainButton);
        Button homeButton = findViewById(R.id.homeButton);

        buttonLayout.setVisibility(View.VISIBLE);
        playAgainButton.setVisibility(View.VISIBLE);
        homeButton.setVisibility(View.VISIBLE);
    }

    private void rollDice() {
        Random random = new Random();
        int diceRoll = random.nextInt(6) + 1;
        String category = categories[diceRoll - 1];

        if (diceSound != null) {
            diceSound.seekTo(0);
            diceSound.start();
        }

        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.dice_roll);
        diceImage.startAnimation(rotateAnimation);

        int[] diceDrawables = {
                R.drawable.dice1, R.drawable.dice2, R.drawable.dice3,
                R.drawable.dice4, R.drawable.dice5, R.drawable.dice6
        };

        diceImage.postDelayed(() -> {
            diceImage.setImageResource(diceDrawables[diceRoll - 1]);
        }, 300);

        diceResult.setText("Du slog: " + diceRoll + " (" + category + ")");
        vibrator.vibrate(200);

        currentQuestion = QuestionLoader.getQuestionByCategory(category);
        if (currentQuestion != null) {
            showQuestion(currentQuestion);
        } else {
            questionText.setText("Inga frågor tillgängliga för denna kategori.");
        }
        isRollingEnabled = false;
    }

    private void detectShake(SensorEvent event) {
        if (!isRollingEnabled) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        double acceleration = Math.sqrt(x * x + y * y + z * z);
        double deltaAcceleration = Math.abs(acceleration - SensorManager.GRAVITY_EARTH);

        if (deltaAcceleration > SHAKE_THRESHOLD) {
            rollDice();
        }
    }

    private void enableAnswerButtons(boolean enable) {
        option1.setEnabled(enable);
        option2.setEnabled(enable);
        option3.setEnabled(enable);
        option4.setEnabled(enable);
    }

    private void detectHintGesture(SensorEvent event) {
        if (currentQuestion != null && !hintUsed) {
            if (event.values[0] < proximitySensor.getMaximumRange()) {
                provideHint();
            }
        }
    }

    private void provideHint() {
        if (currentQuestion == null || hintUsed) return;

        String[] options = currentQuestion.getOptions();
        Random random = new Random();
        int removedIndex = -1;

        while (removedIndex == -1) {
            int index = random.nextInt(4);
            if (!options[index].equals(currentQuestion.getAnswer())) {
                removedIndex = index;
            }
        }

        switch (removedIndex) {
            case 0: option1.setText(""); option1.setEnabled(false); break;
            case 1: option2.setText(""); option2.setEnabled(false); break;
            case 2: option3.setText(""); option3.setEnabled(false); break;
            case 3: option4.setText(""); option4.setEnabled(false); break;
        }

        Toast.makeText(this, "Ledtråd använd! Ett felaktigt svar har tagits bort.", Toast.LENGTH_SHORT).show();
        hintUsed = true;
    }

    private void resetGame() {
        isRollingEnabled = true;
        playAgainButton.setVisibility(View.GONE);
        homeButton.setVisibility(View.GONE);
        diceResult.setText("Skaka för att kasta tärningen!");
        questionText.setText("");
        enableAnswerButtons(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (diceSound != null) {
            diceSound.release();
            diceSound = null;
        }
        if (correctSound != null) {
            correctSound.release();
            correctSound = null;
        }
        if (errorSound != null) {
            errorSound.release();
            errorSound = null;
        }
    }

}