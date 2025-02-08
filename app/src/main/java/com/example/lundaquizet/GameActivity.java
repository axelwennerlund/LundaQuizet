package com.example.lundaquizet;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private TextView questionText;
    private Button option1, option2, option3, option4;
    private Question currentQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        questionText = findViewById(R.id.questionText);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        // Test question
        currentQuestion = new Question(
                "Vilket år grundades Lunds universitet?",
                new String[]{"1666", "1855", "1912", "1523"},
                "1666"
        );

        showQuestion(currentQuestion);

        View.OnClickListener answerClickListener = v -> checkAnswer(((Button) v).getText().toString());
        option1.setOnClickListener(answerClickListener);
        option2.setOnClickListener(answerClickListener);
        option3.setOnClickListener(answerClickListener);
        option4.setOnClickListener(answerClickListener);
    }

    private void showQuestion(Question question) {
        questionText.setText(question.getQuestion());

        List<String> shuffledOptions = Arrays.asList(question.getOptions());
        Collections.shuffle(shuffledOptions);

        option1.setText(shuffledOptions.get(0));
        option2.setText(shuffledOptions.get(1));
        option3.setText(shuffledOptions.get(2));
        option4.setText(shuffledOptions.get(3));
    }

    private void checkAnswer(String selectedAnswer) {
        if (selectedAnswer.equals(currentQuestion.getAnswer())) {
            Toast.makeText(this, "Rätt!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Fel! Rätt svar är: " + currentQuestion.getAnswer(), Toast.LENGTH_SHORT).show();
        }
    }
}