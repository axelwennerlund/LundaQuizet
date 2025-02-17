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
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements SensorEventListener {

    private TextView diceResult, questionText;
    private ImageView diceImage;
    private Button playButton;
    private Button option1, option2, option3, option4;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Vibrator vibrator;
    private boolean isRollingEnabled = false;
    private static final int SHAKE_THRESHOLD = 10;
    private String[] categories = {"Historia", "Geografi", "Lunds Universitet", "Kända Personer", "Lund i Siffror", "Blandat"};
    private Question currentQuestion;
    private MediaPlayer diceSound;
    private MediaPlayer correctSound;
    private MediaPlayer errorSound;
    private MediaPlayer swishSound;
    private MediaPlayer popSound;
    private Sensor proximitySensor;
    private boolean hintUsed = false;
    private boolean isFaceDown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        diceResult = findViewById(R.id.diceResult);
        questionText = findViewById(R.id.questionText);
        diceImage = findViewById(R.id.diceImage);
        playButton = findViewById(R.id.playButton);
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

        diceSound = MediaPlayer.create(this, R.raw.diceroll);
        correctSound = MediaPlayer.create(this, R.raw.correct);
        errorSound = MediaPlayer.create(this, R.raw.error);
        swishSound = MediaPlayer.create(this, R.raw.swishsound);
        popSound = MediaPlayer.create(this, R.raw.pop);

        isRollingEnabled = false;
    }

    private void startGame() {
        isRollingEnabled = true;
        hintUsed = false;
        playButton.setVisibility(View.GONE);
        diceResult.setText("Skaka för att kasta tärningen!");
        questionText.setText("");

        enableAnswerButtons(false);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            detectShake(event);
            detectFaceDown(event);
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
        boolean isCorrect = selectedAnswer.equals(currentQuestion.getAnswer());
        String message = isCorrect ? "Rätt!" : "Fel! Rätt svar är " + currentQuestion.getAnswer();

        if (isCorrect) {
            if (correctSound != null) {
                correctSound.seekTo(0);
                correctSound.start();
            }
        } else {
            if (errorSound != null) {
                errorSound.seekTo(0);
                errorSound.start();
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Ditt svar: " + selectedAnswer)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Spela igen", (dialog, which) -> resetGame())
                .setNegativeButton("Hem", (dialog, which) -> {
                    Intent intent = new Intent(GameActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show();

        enableAnswerButtons(false);
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
        if (isFaceDown) return;

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

        if (popSound != null) {
            popSound.seekTo(0);
            popSound.start();
        }

        hintUsed = true;
    }

    private void detectFaceDown(SensorEvent event) {
        float z = event.values[2];

        if (z < -9.0 && !isFaceDown) {
            isFaceDown = true;
            switchQuestion();
        } else if (z > -5.0 && isFaceDown) {
            isFaceDown = false;
        }
    }

    private void switchQuestion() {
        if (currentQuestion != null) {
            String category = currentQuestion.getCategory();
            Question newQuestion = QuestionLoader.getQuestionByCategory(category);

            if (newQuestion != null && !newQuestion.equals(currentQuestion)) {
                currentQuestion = newQuestion;
                showQuestion(newQuestion);
                if (swishSound != null) {
                    swishSound.seekTo(0);
                    swishSound.start();
                }
            }
        }
    }

    private void resetGame() {
        isRollingEnabled = true;
        diceResult.setText("Skaka för att kasta tärningen!");
        questionText.setText("");
        option1.setText("");
        option2.setText("");
        option3.setText("");
        option4.setText("");
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
        if (swishSound != null) {
            swishSound.release();
            swishSound = null;
        }
        if (popSound != null) {
            popSound.release();
            popSound = null;
        }
    }

}