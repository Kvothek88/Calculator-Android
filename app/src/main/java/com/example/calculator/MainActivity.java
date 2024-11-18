package com.example.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText numberInput;
    TextView resultBox;

    boolean isResultCalculated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberInput = (EditText) findViewById(R.id.numberInput);
        resultBox = (TextView) findViewById(R.id.resultBox);

        numberInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String input = s.toString();
                boolean operatorCheck = isLastCharacterOperator(input);
                boolean inputQuantity = hasMoreThanOneEntry(input);
                Number result = calculateResult(input);
                if (operatorCheck || !inputQuantity){
                    resultBox.setText("");
                } else {
                    resultBox.setText(String.valueOf(result));
                }
                //numberInput.requestFocus();
                //numberInput.setSelection(numberInput.getText().length());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        int[] buttonIds = {
                R.id.btnDivide, R.id.btn7,
                R.id.btn8, R.id.btn9, R.id.btnMultiply,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btnMinus,
                R.id.btn1, R.id.btn2, R.id.btn3, R.id.btnPlus,
                R.id.btn0, R.id.btnDot,
        };

        for (int id : buttonIds){
            Button button = findViewById(id);
            button.setOnClickListener(v -> {
                String currentText = numberInput.getText().toString();
                String buttonText = button.getText().toString();
                char buttonChar = buttonText.charAt(0);
                if (Character.isDigit(buttonChar) && isResultCalculated){
                    numberInput.setText("");
                    currentText = "";
                    isResultCalculated = false;
                }
                if (!Character.isDigit(buttonChar) && isResultCalculated)
                    isResultCalculated = false;
                if (isLastCharacterOperator(currentText) && (buttonChar == '-' || buttonChar == '+' || buttonChar == '*' || buttonChar == '/')){
                    currentText = currentText.substring(0, currentText.length() - 1);
                }
                if (buttonChar == '.'){
                    if (checkDot(currentText)){
                        String updatedText = getString(R.string.concatenated_text, currentText, buttonText);
                        numberInput.setText(updatedText);
                    }
                }
                else {
                    String updatedText = getString(R.string.concatenated_text, currentText, buttonText);
                    numberInput.setText(updatedText);
                }
            });
        }

        Button buttonAC = findViewById(R.id.btnAC);
        Button buttonCE = findViewById(R.id.btnCE);
        Button buttonToggle = findViewById(R.id.btnToggleSign);
        Button buttonEquals = findViewById(R.id.btnEquals);

        buttonCE.setOnClickListener(v -> {
            if (isResultCalculated)
                isResultCalculated = false;
            String currentText = numberInput.getText().toString();
            String updatedText = currentText.substring(0, currentText.length() - 1);
            numberInput.setText(updatedText);
        });

        buttonAC.setOnClickListener(v -> {
            numberInput.getText().clear();
            isResultCalculated = false;
        });

        buttonEquals.setOnClickListener(v -> {
            String currentText = String.valueOf(numberInput.getText());
            Number result = calculateResult(currentText);
            numberInput.setText(String.valueOf(result));
            numberInput.setCursorVisible(false);
            resultBox.setText("");
            isResultCalculated = true;
        });

        buttonToggle.setOnClickListener(v -> {
            String currentText = String.valueOf(numberInput.getText());
            boolean operatorCheck = isLastCharacterOperator(currentText);
            boolean inputQuantity = hasMoreThanOneEntry(currentText);
            if (operatorCheck){
                if (currentText.charAt(currentText.length() - 1) == '-'){
                    currentText = currentText.substring(0, currentText.length() - 1) + '+';
                }
                else if (currentText.charAt(currentText.length() - 1) == '+'){
                    currentText = currentText.substring(0, currentText.length() - 1) + '-';
                }
                numberInput.setText(currentText);
            } else {
                if (!inputQuantity){
                    currentText = toggleSign(currentText);
                    numberInput.setText(currentText);
                }
                else {
                    char lastSign = getLastOperator(currentText);
                    char secondToLast = getSecondToLastOperator(currentText);
                    if (lastSign == '+' || lastSign == '-'){
                        if (secondToLast == '*' || secondToLast == '/'){
                            currentText = removeLastOperator(currentText);
                            numberInput.setText(currentText);
                        } else {
                            currentText = changeLastOperator(currentText);
                            numberInput.setText(currentText);
                        }
                    }
                    else if (lastSign == '*' || lastSign == '/'){
                        String lastChar = currentText.substring(currentText.length() - 1);
                        currentText = currentText.substring(0,currentText.length() - 1);
                        lastChar = toggleSign(lastChar);
                        currentText += lastChar;
                        numberInput.setText(currentText);
                    }
                }
            }
        });

    }

    private Number calculateResult(String input){
        try {
            input = input.trim();

            if (input.startsWith("+")){
                input = input.substring(1);
                numberInput.setText(input);
            }

            double result = 0;

            if (input.startsWith("-")) {
                input = input.substring(1);
                result = -Double.parseDouble(input.split("[+\\-*/]")[0].trim());
            } else {
                result = Double.parseDouble(input.split("[+\\-*/]")[0].trim());
            }

            List<String> parts = new ArrayList<String>();

            parts = extractNumbers(input);

            char[] operators = input.replaceAll("[^+\\-*/]", "").toCharArray();

            List<Character> operatorList = new ArrayList<>();
            for (char op : operators) {
                operatorList.add(op);
            }
            List<Integer> positions = findConsecutiveOperators(input);

            int opIndex = 0;

            // Iterate through the rest of the numbers and apply operations
            for (int i = 1; i < parts.size(); i++) {
                double nextNum = Double.parseDouble(parts.get(i).trim());
                double nextNumber = nextNum % 1 == 0 ? (int) Math.floor(nextNum) : nextNum;

                char operator = operatorList.get(opIndex);

                if (containsNumber(positions,opIndex)) {
                    nextNumber *= -1;
                    operatorList.remove(opIndex + 1);
                }


                // Perform the operation based on the current operator
                switch (operator) {
                    case '+':
                        result += nextNumber;
                        break;
                    case '-':
                        result -= nextNumber;
                        break;
                    case '*':
                        result *= nextNumber;
                        break;
                    case '/':
                        if (nextNumber != 0) {
                            result /= nextNumber;
                        } else {
                            throw new ArithmeticException("Division by zero");
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown operator");
                }

                opIndex++;
            }

            if (result % 1 == 0) {
                // Return as Integer if it's a whole number
                return (int) result;
            } else {
                // Otherwise return as Double
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Return 0 if there's an error
            return 0;
        }
    }

    public List<String> extractNumbers(String input) {
        List<String> numbersList = new ArrayList<>();
        StringBuilder currentNumber = new StringBuilder();

        for (char ch : input.toCharArray()) {
            if (Character.isDigit(ch) || ch == '.') {
                currentNumber.append(ch);
            } else if ("+-*/".indexOf(ch) != -1) {
                if (currentNumber.length() > 0) {
                    numbersList.add(currentNumber.toString());
                    currentNumber.setLength(0);
                }
            }
        }

        if (currentNumber.length() > 0) {
            numbersList.add(currentNumber.toString());
        }

        return numbersList;
    }

    public boolean containsNumber(List<Integer> list, int number) {
        // Use binarySearch to check for the number
        int index = Collections.binarySearch(list, number);

        // If binarySearch returns a non-negative index, the number is in the list
        return index >= 0;
    }

    public String removeLastOperator(String str) {
        int lastOperatorIndex = findLastOperatorIndex(str);
        if (lastOperatorIndex != -1) {
            return str.substring(0, lastOperatorIndex) + str.substring(lastOperatorIndex + 1);
        }
        return str;
    }

    public int findLastOperatorIndex(String str) {
        for (int i = str.length() - 1; i >= 0; i--) {
            char ch = str.charAt(i);
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/') {
                return i;
            }
        }
        return -1;
    }

    public char getLastOperator(String input) {
        for (int i = input.length() - 1; i >= 0; i--) {
            char ch = input.charAt(i);
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/') {
                return ch;
            }
        }
        return '\0';  // Return null character if no operator found
    }

    public char getSecondToLastOperator(String input) {
        int count = 0;
        for (int i = input.length() - 1; i >= 0; i--) {
            char ch = input.charAt(i);
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/') {
                count++;
                if (count == 2)
                    return ch;
            }
        }
        return '\0';  // Return null character if no operator found
    }

    public boolean isLastCharacterOperator(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        char lastChar = input.charAt(input.length() - 1);

        return "+-*/".indexOf(lastChar) >= 0;
    }

    public boolean hasMoreThanOneEntry(String s){
        String[] parts = s.split("[+\\-*/]");

        return parts.length > 1;
    }

    public String toggleSign(String number) {
        if (number.startsWith("-")) {
            return number.substring(1);
        } else {
            return "-" + number;
        }
    }

    public boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    public List<Integer> findConsecutiveOperators(String input) {
        List<Integer> positions = new ArrayList<>();

        // First, extract the operators from the input string
        char[] operators = input.replaceAll("[^+\\-*/]", "").toCharArray();

        // Loop through the input string (except for the last character)
        for (int i = 0; i < input.length() - 1; i++) {
            char currentChar = input.charAt(i);
            char nextChar = input.charAt(i + 1);

            // Check if both currentChar and nextChar are operators
            if (isOperator(currentChar) && isOperator(nextChar)) {
                // If consecutive operators are found, find their position in the operators array
                // Find which operator in the operators array corresponds to this position in input
                int operatorIndex = -1;

                for (int j = 0; j <= i; j++) {
                    if (isOperator(input.charAt(j))) {
                        operatorIndex++;
                    }
                }

                // Add the operator index to positions list (index in the operators array)
                positions.add(operatorIndex);
            }
        }

        return positions;
    }


    public String changeLastOperator(String input) {
        for (int i = input.length() - 1; i >= 0; i--) {
            char ch = input.charAt(i);

            if (ch == '+' || ch == '-') {
                // Reverse the operator
                char newOperator = (ch == '+') ? '-' : '+';
                // Replace the operator and return the updated string
                return input.substring(0, i) + newOperator + input.substring(i + 1);
            }
        }
        return input;
    }

    public boolean checkDot(String input){
        if (isLastCharacterOperator(input))
            return false;
        String[] parts = input.split("[+\\-*/]");
        String lastElement = parts[parts.length - 1];

        if (isInteger(lastElement)){
            return true;
        }
        else if (isDouble(lastElement)){
            return false;
        }
        else{
            return false;
        }
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void onClick(View v) {

    }
}