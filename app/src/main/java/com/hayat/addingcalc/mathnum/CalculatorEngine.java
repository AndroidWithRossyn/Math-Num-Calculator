package com.hayat.addingcalc.mathnum;



import android.annotation.SuppressLint;
import android.util.Log;

import static com.hayat.addingcalc.mathnum.NumberHelper.PI;
import static com.hayat.addingcalc.mathnum.NumberHelper.e;
import static com.hayat.addingcalc.mathnum.ParenthesesBalancer.*;

import org.json.JSONException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CalculatorEngine {

    @SuppressLint("StaticFieldLeak")
    private static MainActivity mainActivity;
    public static void setMainActivity(MainActivity activity) {
        mainActivity = activity;
    }

   private static final MathContext MC = new MathContext(35, RoundingMode.HALF_UP);
    public static final String ROOT = "√";
    public static final String THIRD_ROOT = "³√";


    public static String calculate(String calc) {
        Log.d("calculate", "\n\ncalculate input String: "+ calc);
        try {
            String trim;
            if(String.valueOf(calc.charAt(0)).equals("+")) {
                calc = calc.substring(1);
            } else if(String.valueOf(calc.charAt(0)).equals("-")) {
                calc = "0" + calc;
            }

            calc = fixExpression(calc);
            String commonReplacements = calc.replace('×', '*')
                    .replace('÷', '/')
                    .replace("=", "")
                    .replace("E", "e")
                    .replace("π", PI)
                    .replaceAll("е", e)
                    .replaceAll(" ", "")
                    .replace("½", "0.5")
                    .replace("⅓", "0.33333333333")
                    .replace("¼", "0.25");

            trim = commonReplacements/*.replace(".", "").replace(",", ".")*/.trim();
            trim = balanceParentheses(trim);

            Log.e("calculate", "Trim:" + trim);

            if (isScientificNotation(trim)) {
                try {
                    DataManager dataManager = new DataManager(mainActivity);
                    dataManager.saveToJSONSettings("isNotation", true, mainActivity.getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String result = convertScientificToDecimal(trim);
                return removeNonNumeric(result);
            }

            final List<String> tokens = tokenize(trim);

            for (int i = 0; i < tokens.size() - 1; i++) {
                try {
                    if (tokens.get(i).equals("/") && tokens.get(i + 1).equals("-")) {
                        // Handle negative exponent in division
                        tokens.remove(i + 1);
                        tokens.add(i + 1, "NEG_EXPONENT");
                    }
                } catch (Exception e) {
                    // do nothing
                }
            }


            final BigDecimal result = evaluate(tokens);

            double resultDouble = result.doubleValue();

            if (Double.isInfinite(resultDouble)) {
                return mainActivity.getString(R.string.errorMessage1);
            }
            final String replace = result.stripTrailingZeros().toPlainString()/*.replace('.', ',')*/;
            Log.d("calculate", "return value :" + replace+"\n\n");

            return replace;
        } catch (ArithmeticException e) {

            if (Objects.equals(e.getMessage(), mainActivity.getString(R.string.errorMessage1))) {
                return mainActivity.getString(R.string.errorMessage1);
            } else {
                return e.getMessage();
            }
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (Exception e) {
            Log.e("Exception", e.toString());
            return mainActivity.getString(R.string.errorMessage2);
        }
    }

    public static boolean isSymbol(final String character) {
        return (String.valueOf(character).equals("¼") || String.valueOf(character).equals("⅓") || String.valueOf(character).equals("½") ||
                String.valueOf(character).equals("е") || String.valueOf(character).equals("e") || String.valueOf(character).equals("π"));
    }

    public static String fixExpression(String input) {
        StringBuilder sb = new StringBuilder();

        if(input.length() >= 2) {
            for (int i = 0; i < input.length(); i++) {
                char currentChar = input.charAt(i);
                sb.append(currentChar);

                if (i + 1 < input.length()) {
                    char nextChar = input.charAt(i + 1);

                    boolean charFound = checkForChar(currentChar);
                    if (charFound) {
                        continue;
                    }

                    if(isOperator(String.valueOf(currentChar)) && isSymbol(String.valueOf(nextChar))) {
                        continue;
                    }

                    if(Character.isDigit(currentChar) && isOperator(String.valueOf(nextChar))) {
                        continue;
                    }

                    if(String.valueOf(currentChar).equals("(") && String.valueOf(nextChar).equals("³")) {
                        continue;
                    }

                    if(Character.isDigit(currentChar) && String.valueOf(nextChar).equals("(")) {
                        sb.append('×');
                        continue;
                    }

                    if((Character.isDigit(currentChar) && isSymbol(String.valueOf(nextChar))) || (isSymbol(String.valueOf(currentChar)) && Character.isDigit(nextChar))) {
                        sb.append('×');
                        continue;
                    }

                    if((isSymbol(String.valueOf(currentChar)) && String.valueOf(nextChar).equals("(")) || (String.valueOf(currentChar).equals(")") && isSymbol(String.valueOf(nextChar))) ) {
                        sb.append('×');
                        continue;
                    }

                    if (shouldInsertMultiplication(currentChar, nextChar) && (!Character.isDigit(currentChar) && !Character.isDigit(nextChar)) && !isOperator(String.valueOf(nextChar))) {
                        sb.append('×');
                        continue;
                    }

                    if(Character.isDigit(currentChar) && String.valueOf(currentChar).equals("(") && !isOperator(String.valueOf(nextChar))) {
                        sb.append('×');
                        continue;
                    }

                    if(Character.isDigit(currentChar) && Character.isLetter(nextChar) && !isOperator(String.valueOf(nextChar))) {
                        sb.append('×');
                        continue;
                    }

                    if ((currentChar == 'π' && Character.isDigit(nextChar)) ||
                            (nextChar == 'π' && Character.isDigit(currentChar)) && !isOperator(String.valueOf(nextChar))) {
                        sb.append('×'); // Insert '×' between 'π' and digit
                        continue;
                    }

                    if ((Character.isDigit(nextChar) && String.valueOf(nextChar).equals("(") && !isOperator(String.valueOf(nextChar))) ||
                            (nextChar == '³')) {
                        sb.append('×');
                        continue;
                    }

                    if (String.valueOf(currentChar).equals("!") && Character.isDigit(nextChar) && !isOperator(String.valueOf(nextChar))) {
                        sb.append('×');
                    }
                }
            }
            if (sb.length() > 0 && sb.substring(sb.length() - 2, sb.length()).equals("×=")) {
                sb.delete(sb.length() - 2, sb.length());
            }
            return sb.toString();
        }
        return input;
    }

    public static boolean checkForChar(char currentChar) {
        String[] errorMessages = {
                mainActivity.getString(R.string.errorMessage1),
                mainActivity.getString(R.string.errorMessage2),
                mainActivity.getString(R.string.errorMessage3),
                mainActivity.getString(R.string.errorMessage4),
                mainActivity.getString(R.string.errorMessage5),
                mainActivity.getString(R.string.errorMessage6),
                mainActivity.getString(R.string.errorMessage7),
                mainActivity.getString(R.string.errorMessage8),
                mainActivity.getString(R.string.errorMessage9),
                mainActivity.getString(R.string.errorMessage10),
                mainActivity.getString(R.string.errorMessage11),
                mainActivity.getString(R.string.errorMessage12),
                mainActivity.getString(R.string.errorMessage13),
                mainActivity.getString(R.string.errorMessage14),
                mainActivity.getString(R.string.errorMessage15),
                mainActivity.getString(R.string.errorMessage16),
                mainActivity.getString(R.string.errorMessage17)
        };

        for (String errorMessage : errorMessages) {
            if (errorMessage.indexOf(currentChar) != -1) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldInsertMultiplication(char currentChar, char nextChar) {
        Set<Character> validChars = createValidCharsSet();
        List<String> places = Arrays.asList("s", "i", "n", "c", "o", "t", "a", "l", "h", "g", "⁻", "¹", "³", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉", "");

        if (places.contains(String.valueOf(currentChar)) && places.contains(String.valueOf(nextChar))) {
            return false; // Don't insert '*' between 'sin' and next character
        }

        return (!validChars.contains(currentChar) && !validChars.contains(nextChar)) ||
                (Character.isDigit(currentChar) && (Character.isLetter(nextChar) || nextChar == '√')) ||
                (currentChar == ')' && (Character.isDigit(nextChar) || nextChar == '(' || nextChar == '√')) ||
                (currentChar == ')' && isMathFunction("", 0)) ||
                (isMathFunction("", 0) && (Character.isDigit(nextChar) || nextChar == '(' || nextChar == '√')) ||
                (isMathFunction("", 0) && isMathFunction("", 1));
    }

    public static Set<Character> createValidCharsSet() {
        Set<Character> validChars = new HashSet<>();
        validChars.add('+');
        validChars.add('-');
        validChars.add('*');
        validChars.add('×');
        validChars.add('/');
        validChars.add('÷');
        validChars.add('.');
        validChars.add(',');
        validChars.add('(');
        validChars.add(')');
        validChars.add('√');
        validChars.add('^');
        validChars.add('!');
        return validChars;
    }

    public static boolean isMathFunction(String input, int startIndex) {
        Set<String> mathFunctions = createMathFunctionsSet();
        for (String mathFunction : mathFunctions) {
            if (input.regionMatches(startIndex, mathFunction, 0, mathFunction.length())) {
                // Check if the entire function is present
                if (startIndex + mathFunction.length() >= input.length() || !Character.isLetter(input.charAt(startIndex + mathFunction.length()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Set<String> createMathFunctionsSet() {
        Set<String> mathFunctions = new HashSet<>();
        mathFunctions.add("^");
        mathFunctions.add("√");
        mathFunctions.add("³√");
        mathFunctions.add("ln");
        mathFunctions.add("sin");
        mathFunctions.add("cos");
        mathFunctions.add("tan");
        mathFunctions.add("log");
        mathFunctions.add("sinh");
        mathFunctions.add("cosh");
        mathFunctions.add("tanh");
        mathFunctions.add("log₂");
        mathFunctions.add("log₃");
        mathFunctions.add("log₄");
        mathFunctions.add("log₅");
        mathFunctions.add("log₆");
        mathFunctions.add("log₇");
        mathFunctions.add("log₈");
        mathFunctions.add("log₉");
        mathFunctions.add("sin⁻¹");
        mathFunctions.add("cos⁻¹");
        mathFunctions.add("tan⁻¹");
        mathFunctions.add("sinh⁻¹");
        mathFunctions.add("cosh⁻¹");
        mathFunctions.add("tanh⁻¹");
        return mathFunctions;
    }



    public static boolean isScientificNotation(final String str) {
       final String formattedInput = str.replace(",", ".");
        final Pattern pattern = Pattern.compile("^([-+]?\\d+(\\.\\d+)?)([eE][-+]?\\d+)$");
       final Matcher matcher = pattern.matcher(formattedInput);
        return matcher.matches();
    }

    public static String convertScientificToDecimal(final String str) {

        final Pattern pattern = Pattern.compile("([-+]?\\d+(\\.\\d+)?)([eE][-+]?\\d+)");
        final Matcher matcher = pattern.matcher(str);
        final StringBuffer sb = new StringBuffer();


        while (matcher.find()) {

            final String numberPart = matcher.group(1);
            String exponentPart = matcher.group(3);


            if (exponentPart != null) {
                exponentPart = exponentPart.substring(1);
            }


            if (exponentPart != null) {
                final int exponent = Integer.parseInt(exponentPart);
                assert numberPart != null;
                final String sign = numberPart.startsWith("-") ? "-" : "";
                BigDecimal number = new BigDecimal(numberPart);

                if (numberPart.startsWith("-")) {
                    number = number.negate();
                }


                BigDecimal scaledNumber;
                if (exponent >= 0) {
                    scaledNumber = number.scaleByPowerOfTen(exponent);
                } else {
                    scaledNumber = number.divide(BigDecimal.TEN.pow(-exponent), MC);
                }


                String result = sign + scaledNumber.stripTrailingZeros().toPlainString();
                if (result.startsWith(".")) {
                    result = "0" + result;
                }
                matcher.appendReplacement(sb, result);
            }
        }


        matcher.appendTail(sb);


        if (sb.indexOf("--") != -1) {
            sb.replace(sb.indexOf("--"), sb.indexOf("--") + 2, "-");
        }


        Log.i("convertScientificToDecimal", "sb:" + sb);
        return sb.toString();
    }


    public static String removeNonNumeric(final String str) {
        // Replace all non-numeric and non-decimal point characters in the string with an empty string
        return str.replaceAll("[^0-9.,\\-]", "");
    }


    public static List<String> tokenize(final String expression) {

        Log.i("tokenize","Input Expression: " + expression);


        String expressionWithoutSpaces = expression.replaceAll("\\s+", "");

        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();

        for (int i = 0; i < expressionWithoutSpaces.length(); i++) {
            char c = expressionWithoutSpaces.charAt(i);


            if (Character.isDigit(c) || c == '.' || (c == '-' && (i == 0 || expressionWithoutSpaces.charAt(i - 1) == '('
                    || isOperator(String.valueOf(expressionWithoutSpaces.charAt(i - 1)))
                    || expressionWithoutSpaces.charAt(i - 1) == ','))) {
                currentToken.append(c);
            } else if (i + 3 < expressionWithoutSpaces.length() && expressionWithoutSpaces.startsWith("³√", i)) {

                tokens.add(expressionWithoutSpaces.substring(i, i + 2));
                i += 1;
            } else {
               if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                if (i + 3 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 3);
                    if (function.equals("ln(")) {
                        tokens.add(function); // Add the full function name
                        i += 2; // Skip the next characters (already processed)
                        continue;
                    }
                }
                if (i + 4 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 4);
                    if (function.equals("sin(") || function.equals("cos(") || function.equals("tan(")) {
                        tokens.add(function); // Add the full function name
                        i += 3; // Skip the next characters (already processed)
                        continue;
                    }
                    if (function.equals("log(")) {
                        tokens.add(function); // Add the full function name
                        i += 3; // Skip the next characters (already processed)
                        continue;
                    }
                }
                if (i + 5 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 5);
                    if (function.equals("sinh(") || function.equals("cosh(") || function.equals("tanh(")) {
                        tokens.add(function); // Add the full function name
                        i += 4; // Skip the next characters (already processed)
                        continue;
                    }
                    if (function.equals("log₂(") || function.equals("log₃(") || function.equals("log₄(") ||
                            function.equals("log₅(") || function.equals("log₆(") || function.equals("log₇(") ||
                            function.equals("log₈(") || function.equals("log₉(")) {
                        tokens.add(function); // Add the full function name
                        i += 4; // Skip the next characters (already processed)
                        continue;
                    }
                }
                if (i + 6 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 6);
                     if (function.equals("sin⁻¹(") || function.equals("cos⁻¹(") || function.equals("tan⁻¹(")) {
                        tokens.add(function); // Add the full function name
                        i += 5; // Skip the next characters (already processed)
                        continue;
                    }
                }
                if (i + 7 <= expressionWithoutSpaces.length()) {
                    String function = expressionWithoutSpaces.substring(i, i + 7);
                    if (function.equals("sinh⁻¹(") || function.equals("cosh⁻¹(") || function.equals("tanh⁻¹(")) {
                        tokens.add(function); // Add the full function name
                        i += 6; // Skip the next characters (already processed)
                        continue;
                    }
                }

                tokens.add(Character.toString(c));
            }
        }

        // Add the last token if it exists
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        // Debugging: Print tokens
        Log.i("tokenize","Tokens: " + tokens);

        return tokens;
    }


    public static BigDecimal evaluate(final List<String> tokens) {
        // Convert the infix expression to postfix
        final List<String> postfixTokens = infixToPostfix(tokens);
        Log.i("evaluate", "Postfix Tokens: " + postfixTokens);

        // Evaluate the postfix expression and return the result
        return evaluatePostfix(postfixTokens);
    }


    public static BigDecimal applyOperator(final BigDecimal operand1, final BigDecimal operand2, final String operator) {
        DataManager dataManager = new DataManager(mainActivity);
        final String mode;
        try {
            mode = dataManager.getJSONSettingsData("functionMode", mainActivity.getApplicationContext()).getString("value");
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }

        switch (operator) {
            case "+":
                return operand1.add(operand2, MC);
            case "-":
                return operand1.subtract(operand2, MC);
            case "*":
                return operand1.multiply(operand2, MC);
            case "/":
                if (operand2.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException(mainActivity.getString(R.string.errorMessage3));
                } else {
                    return operand1.divide(operand2, MC);
                }
            case ROOT:
                if (operand2.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage4));
                } else {
                    return BigDecimal.valueOf(Math.sqrt(operand2.doubleValue()));
                }
            case THIRD_ROOT:
                return BigDecimal.valueOf(Math.pow(operand2.doubleValue(), 1.0 / 3.0));
            case "!":
                return factorial(operand1);
            case "^":
                return pow(operand1, operand2);
            case "log(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(10)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₂(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(2)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₃(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(3)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₄(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(4)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₅(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(5)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₆(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(6)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₇(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(7)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₈(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(8)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "log₉(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue()) / Math.log(9)).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "ln(":
                return BigDecimal.valueOf(Math.log(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
            case "sin(":
                if (mode.equals("Rad")) {
                    return BigDecimal.valueOf(Math.sin(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.sin(Math.toRadians(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "sinh(":
                if (mode.equals("Rad")) {
                    return BigDecimal.valueOf(Math.sinh(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.sinh(Math.toRadians(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "sin⁻¹(":
                if (mode.equals("Rad")) {
                    return BigDecimal.valueOf(Math.asin(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.toDegrees(Math.asin(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "sinh⁻¹(":
                return asinh(operand2);
            case "cos(":
                if (mode.equals("Rad")) {
                    return BigDecimal.valueOf(Math.cos(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.cos(Math.toRadians(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "cosh(":
                if (mode.equals("Rad")) {
                    return BigDecimal.valueOf(Math.cosh(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.cosh(Math.toRadians(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "cos⁻¹(":
                if (mode.equals("Rad")) {
                    return BigDecimal.valueOf(Math.acos(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.toDegrees(Math.acos(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "cosh⁻¹(":
                return acosh(operand2);
            case "tan(":
                if (mode.equals("Rad")) {
                    return BigDecimal.valueOf(Math.tan(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.tan(Math.toRadians(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "tanh(":
                if (mode.equals("Rad")) {
                    return BigDecimal.valueOf(Math.tanh(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.tanh(Math.toRadians(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "tan⁻¹(":
                if (mode.equals("Rad")) {
                    return BigDecimal.valueOf(Math.atan(operand2.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    return BigDecimal.valueOf(Math.toDegrees(Math.atan(operand2.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
            case "tanh⁻¹(":
                return atanh(operand2);
            default:
                throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage5));
        }
    }

    public static BigDecimal factorial(BigDecimal number) {
        // Check if the number is greater than 170
        if (number.compareTo(new BigDecimal("170")) > 0) {
            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage1));
        }

        // Check if the number is negative
        boolean isNegative = number.compareTo(BigDecimal.ZERO) < 0;
        // If the number is negative, convert it to positive
        if (isNegative) {
            number = number.negate();
        }

        // Check if the number is an integer. If not, throw an exception
        if (number.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage6));
        }

        // Initialize the result as 1
        BigDecimal result = BigDecimal.ONE;

        // Calculate the factorial of the number
        while (number.compareTo(BigDecimal.ONE) > 0) {
            result = result.multiply(number);
            number = number.subtract(BigDecimal.ONE);
        }

        // If the original number was negative, return the negative of the result. Otherwise, return the result.
        return isNegative ? result.negate() : result;
    }

    public static BigDecimal pow(BigDecimal base, BigDecimal exponent) {
        // Convert the base and exponent to double values
        double baseDouble = base.doubleValue();
        double exponentDouble = exponent.doubleValue();

        // Check if the base is zero and the exponent is negative
        if (baseDouble == 0 && exponentDouble < 0) {
            throw new ArithmeticException(mainActivity.getString(R.string.errorMessage3));
        }

        // Check if the base is negative and the exponent is an integer
        double resultDouble;
        if (baseDouble < 0 && exponentDouble == (int) exponentDouble) {
            baseDouble = -baseDouble;
            resultDouble = -Math.pow(baseDouble, exponentDouble);
        } else {
            resultDouble = Math.pow(baseDouble, exponentDouble);
        }

        // If the result is too large to be represented as a double, throw an exception
        if (Double.isInfinite(resultDouble)) {
            throw new ArithmeticException(mainActivity.getString(R.string.errorMessage1));
        }

        // Convert the result back to a BigDecimal and return it
        try {
            return new BigDecimal(resultDouble, MC).stripTrailingZeros();
        } catch (NumberFormatException e) {
            throw new NumberFormatException(mainActivity.getString(R.string.errorMessage7));
        }
    }


    public static BigDecimal evaluatePostfix(final List<String> postfixTokens) {
        // Create a stack to store numbers
        final List<BigDecimal> stack = new ArrayList<>();

        // Iterate through each token in the postfix list
        for (final String token : postfixTokens) {
            // Debugging: Print current token
            Log.i("evaluatePostfix","Token: " + token);

            // If the token is a number, add it to the stack
            if (isNumber(token)) {
                stack.add(new BigDecimal(token));
            } else if (isOperator(token)) {
                // If the token is an operator, apply the operator to the numbers in the stack
                applyOperatorToStack(token, stack);
            } else if (isFunction(token)) {
                // If the token is a function, evaluate the function and add the result to the stack
                evaluateFunction(token, stack);
            } else {
                // If the token is neither a number, operator, nor function, throw an exception
                Log.i("evaluatePostfix","Token is neither a number nor an operator");
                throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage2));
            }

            // Debugging: Print current stack
            Log.i("evaluatePostfix","Stack: " + stack);
        }

        // If there is more than one number in the stack at the end, throw an exception
        if (stack.size() != 1) {
            Log.i("evaluatePostfix","Stacksize != 1");
            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage2));
        }

        // Return the result
        return stack.get(0);
    }

    private static void applyOperatorToStack(String operator, List<BigDecimal> stack) {
        // If the operator is "!", apply the operator to only one number
        if (operator.equals("!")) {
            final BigDecimal operand1 = stack.remove(stack.size() - 1);
            final BigDecimal result = applyOperator(operand1, BigDecimal.ZERO, operator);
            stack.add(result);
        }
        // If the operator is not "!", apply the operator to two numbers
        else {
            final BigDecimal operand2 = stack.remove(stack.size() - 1);
            // If the operator is not ROOT and THIRDROOT, apply the operator to two numbers
            if (!operator.equals(ROOT) && !operator.startsWith(THIRD_ROOT)) {
                final BigDecimal operand1 = stack.remove(stack.size() - 1);
                final BigDecimal result = applyOperator(operand1, operand2, operator);
                stack.add(result);
            }
            // If the operator is ROOT, apply the operator to only one number
            else {
                BigDecimal result;
                switch (operator) {
                    case ROOT:
                        if (operand2.compareTo(BigDecimal.ZERO) < 0) {
                            // If the operand is negative, throw an exception or handle it as needed
                            throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage4));
                        } else {
                            result = BigDecimal.valueOf(Math.sqrt(operand2.doubleValue()));
                        }
                        break;
                    case THIRD_ROOT:
                        result = BigDecimal.valueOf(Math.pow(operand2.doubleValue(), 1.0 / 3.0));
                        break;
                    default:
                        // Handle other operators if needed
                        throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage2));
                }
                stack.add(result);
            }
        }
    }


    private static void evaluateFunction(String function, List<BigDecimal> stack) {

        DataManager dataManager = new DataManager(mainActivity);
        final String mode;
        try {
            mode = dataManager.getJSONSettingsData("functionMode", mainActivity.getApplicationContext()).getString("value");
        } catch (JSONException ex) {
            throw new RuntimeException(ex);
        }
        BigDecimal operand;

        switch (function) {
            case "log(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage8));
                }
                stack.add(BigDecimal.valueOf(Math.log10(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₂(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage8));
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(2)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₃(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage8));
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(3)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₄(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage8));
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(4)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₅(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage8));
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(5)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₆(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage8));
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(6)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₇(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage8));
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(7)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₈(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage8));
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(8)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "log₉(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage8));
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue()) / Math.log(9)).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "ln(": {
                operand = stack.remove(stack.size() - 1);
                if (operand.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage8));
                }
                stack.add(BigDecimal.valueOf(Math.log(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN));
                break;
            }
            case "sin(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (mode.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.sin(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.sin(Math.toRadians(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "sinh(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (mode.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.sinh(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.sinh(Math.toRadians(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "sin⁻¹(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (operand.doubleValue() < -1 || operand.doubleValue() > 1) {
                    throw new ArithmeticException(mainActivity.getString(R.string.errorMessage9));
                }
                if (mode.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.asin(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.toDegrees(Math.asin(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "sinh⁻¹(":
                operand = stack.remove(stack.size() - 1);
                stack.add(asinh(operand));
                break;
            case "cos(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (mode.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.cos(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.cos(Math.toRadians(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "cosh(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (mode.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.cosh(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.cosh(Math.toRadians(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "cos⁻¹(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (operand.doubleValue() < -1 || operand.doubleValue() > 1) {
                    throw new ArithmeticException(mainActivity.getString(R.string.errorMessage9));
                }
                if (mode.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.acos(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.toDegrees(Math.acos(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "cosh⁻¹(":
                operand = stack.remove(stack.size() - 1);
                stack.add(acosh(operand));
                break;
            case "tan(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (mode.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.tan(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    double degrees = operand.doubleValue();
                    if (isMultipleOf90(degrees)) {
                        // Check if the tangent of multiples of 90 degrees is being calculated
                        throw new ArithmeticException(mainActivity.getString(R.string.errorMessage8));
                    }
                    result = BigDecimal.valueOf(Math.tan(Math.toRadians(degrees))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "tanh(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (mode.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.tanh(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    double degrees = operand.doubleValue();
                    result = BigDecimal.valueOf(Math.tanh(Math.toRadians(degrees))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "tan⁻¹(": {
                operand = stack.remove(stack.size() - 1);
                BigDecimal result;
                if (mode.equals("Rad")) {
                    result = BigDecimal.valueOf(Math.atan(operand.doubleValue())).setScale(MC.getPrecision(), RoundingMode.DOWN);
                } else { // if mode equals 'Deg'
                    result = BigDecimal.valueOf(Math.toDegrees(Math.atan(operand.doubleValue()))).setScale(MC.getPrecision(), RoundingMode.DOWN);
                }
                stack.add(result);
                break;
            }
            case "tanh⁻¹(":
                operand = stack.remove(stack.size() - 1);
                stack.add(atanh(operand));
                break;
        }
    }


    private static boolean isMultipleOf90(double degrees) {
        // Check if degrees is a multiple of 90
        return Math.abs(degrees % 90) == 0;
    }


    public static List<String> infixToPostfix(final List<String> infixTokens) {
        final List<String> postfixTokens = new ArrayList<>();
        final Stack<String> stack = new Stack<>();

        for (int i = 0; i < infixTokens.size(); i++) {
            final String token = infixTokens.get(i);
            // Debugging: Print current token and stack
            Log.i("infixToPostfix", "Current Token: " + token);
            Log.i("infixToPostfix", "Stack: " + stack);

            if (isNumber(token)) {
                postfixTokens.add(token);
            } else if (isFunction(token)) {
                stack.push(token);
            } else if (isOperator(token) && token.equals("-")) {
                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(token) && !isFunction(stack.peek())) {
                    postfixTokens.add(stack.pop());
                }
                stack.push(token);
            } else if (isOperator(token)) {
                while (!stack.isEmpty() && precedence(stack.peek()) >= precedence(token) && !isFunction(stack.peek())) {
                    postfixTokens.add(stack.pop());
                }
                stack.push(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    postfixTokens.add(stack.pop());
                }
                if (!stack.isEmpty() && stack.peek().equals("(")) {
                    stack.pop(); // Remove the opening parenthesis
                    if (!stack.isEmpty() && isFunction(stack.peek())) {
                        postfixTokens.add(stack.pop());
                    }
                }
            }

            // Debugging: Print postfixTokens and stack after processing current token
            Log.i("infixToPostfix", "Postfix Tokens: " + postfixTokens);
            Log.i("infixToPostfix", "Stack after Token Processing: " + stack);
        }

        while (!stack.isEmpty()) {
            postfixTokens.add(stack.pop());
        }

        // Debugging: Print final postfixTokens
        Log.i("infixToPostfix", "Final Postfix Tokens: " + postfixTokens);

        return postfixTokens;
    }


    public static boolean isFunction(final String token) {
        // Check if the token is one of the recognized trigonometric functions
        return token.equals("sin(") || token.equals("cos(") || token.equals("tan(") ||
                token.equals("sinh(") || token.equals("cosh(") || token.equals("tanh(") ||
                token.equals("log(") || token.equals("log₂(") || token.equals("log₃(") ||
                token.equals("log₄(") || token.equals("log₅(") || token.equals("log₆(") ||
                token.equals("log₇(") || token.equals("log₈(") || token.equals("log₉(")  ||
                token.equals("ln(") || token.equals("sin⁻¹(") || token.equals("cos⁻¹(") ||
                token.equals("tan⁻¹(") || token.equals("sinh⁻¹(") || token.equals("cosh⁻¹(") ||
                token.equals("tanh⁻¹(");
    }

    // Inverse hyperbolic sine
    public static BigDecimal asinh(BigDecimal x) {
        BigDecimal term1 = x.pow(2).add(BigDecimal.ONE, MathContext.DECIMAL128);
        BigDecimal term2 = x.add(new BigDecimal(Math.sqrt(term1.doubleValue()), MathContext.DECIMAL128));

        return new BigDecimal(Math.log(term2.doubleValue()), MathContext.DECIMAL128);
    }

    // Inverse hyperbolic cosine
    public static BigDecimal acosh(BigDecimal x) {
        BigDecimal term1 = x.pow(2).subtract(BigDecimal.ONE, MathContext.DECIMAL128);
        BigDecimal term2 = x.add(new BigDecimal(Math.sqrt(term1.doubleValue()), MathContext.DECIMAL128));

        return new BigDecimal(Math.log(term2.doubleValue()), MathContext.DECIMAL128);
    }

    // Inverse hyperbolic tangent
    public static BigDecimal atanh(BigDecimal x) {
        BigDecimal term1 = BigDecimal.ONE.add(x, MathContext.DECIMAL128);
        BigDecimal term2 = BigDecimal.ONE.subtract(x, MathContext.DECIMAL128);

        if (x.compareTo(BigDecimal.valueOf(-1)) <= 0 || x.compareTo(BigDecimal.valueOf(1)) >= 0) {
            throw new ArithmeticException(mainActivity.getString(R.string.errorMessage9));
        }

        BigDecimal quotient = term1.divide(term2, MathContext.DECIMAL128);
        return new BigDecimal(0.5 * Math.log(quotient.doubleValue()), MathContext.DECIMAL128);
    }


    public static boolean isNumber(final String token) {
        // Try to create a new BigDecimal from the token
        try {
            new BigDecimal(token);
            // If successful, the token is a number
            return true;
        }
        // If a NumberFormatException is thrown, the token is not a number
        catch (final NumberFormatException e) {
            return false;
        }
    }

    public static boolean isOperator(final String token) {
        // Check if the token is one of the recognized non-functional operators
        return token.contains("+") || token.contains("-") || token.contains("*") || token.contains("/") ||
                token.contains("×") || token.contains("÷") ||
                token.contains("^") || token.contains("√") || token.contains("!") || token.contains("³√");
    }

    public static boolean isStandardOperator(final String token) {
        // Check if the token is one of the recognized non-functional operators
        return token.contains("+") || token.contains("-") || token.contains("*") || token.contains("/")
                || token.contains("×") || token.contains("÷");
    }

    public static int precedence(final String operator) {
        // If the operator is an opening parenthesis, return 0
        switch (operator) {
            case "(":
                return 0;

            // If the operator is addition or subtraction, return 1
            case "+":
            case "-":
                return 1;

            // If the operator is multiplication or division, return 2
            case "*":
            case "/":
                return 2;

            // If the operator is exponentiation, return 3
            case "^":
                return 3;

            // If the operator is root, return 4
            case "√":
            case "³√":
                return 4;

            // If the operator is factorial, return 5
            case "!":
                return 5;

            // If the operator is sine, cosine, or tangent ..., return 6
            case "log(":
            case "log₂(":
            case "log₃(":
            case "log₄(":
            case "log₅(":
            case "log₆(":
            case "log₇(":
            case "log₈(":
            case "log₉(":
            case "ln(":
            case "sin(":
            case "cos(":
            case "tan(":
            case "sinh(":
            case "cosh(":
            case "tanh(":
            case "sinh⁻¹(":
            case "cosh⁻¹(":
            case "tanh⁻¹(":
            case "sin⁻¹(":
            case "cos⁻¹(":
            case "tan⁻¹(":
                return 6;

            // If the operator is not recognized, throw an exception
            default:
                throw new IllegalArgumentException(mainActivity.getString(R.string.errorMessage2));
        }
    }
}