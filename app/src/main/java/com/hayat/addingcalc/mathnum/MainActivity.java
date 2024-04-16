package com.hayat.addingcalc.mathnum;


import static com.hayat.addingcalc.mathnum.CalculatorEngine.fixExpression;
import static com.hayat.addingcalc.mathnum.CalculatorEngine.isOperator;
import static com.hayat.addingcalc.mathnum.CalculatorEngine.isStandardOperator;
import static com.hayat.addingcalc.mathnum.CalculatorEngine.setMainActivity;
import static com.hayat.addingcalc.mathnum.NumberHelper.PI;
import static com.hayat.addingcalc.mathnum.ParenthesesBalancer.balanceParentheses;
import static com.hayat.addingcalc.mathnum.ToastHelper.showToastLong;
import static com.hayat.addingcalc.mathnum.ToastHelper.showToastShort;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private DataManager dataManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        setMainActivity(this);

        dataManager = new DataManager(this);
        dataManager.initializeSettings(this);

        setUpListeners();
        showOrHideScienceButtonState();

        try {
            dataManager.loadNumbers();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
        scrollToEnd(findViewById(R.id.calculate_scrollview));
        scrollToEnd(findViewById(R.id.result_scrollview));

    }


    private void scrollToStart(final HorizontalScrollView scrollView) {
        if (scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_LEFT));
        }
    }

    private void scrollToEnd(final HorizontalScrollView scrollView) {
        if (scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_RIGHT));
        }
    }


    private void scrollToBottom(final HorizontalScrollView scrollView) {
        if (scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        }
    }


    private void setUpListeners() {
        setEmptyButtonListener(R.id.clearresult, "CE");
        setEmptyButtonListener(R.id.clearall, "C");
        setEmptyButtonListener(R.id.backspace, "⌫");

        setOperationButtonListener(R.id.divide, "/");
        setOperationButtonListener(R.id.multiply, "*");
        setOperationButtonListener(R.id.subtract, "-");
        setOperationButtonListener(R.id.add, "+");
        setNegateButtonListener(R.id.negative);

        setNumberButtonListener(R.id.zero);
        setNumberButtonListener(R.id.one);
        setNumberButtonListener(R.id.two);
        setNumberButtonListener(R.id.three);
        setNumberButtonListener(R.id.four);
        setNumberButtonListener(R.id.five);
        setNumberButtonListener(R.id.six);
        setNumberButtonListener(R.id.seven);
        setNumberButtonListener(R.id.eight);
        setNumberButtonListener(R.id.nine);

        setCalculateButtonListener(R.id.calculate);
        setCommaButtonListener(R.id.comma);

        setButtonListener(R.id.clipOn, this::parenthesesOnAction);
        setButtonListener(R.id.clipOff, this::parenthesesOffAction);

        setButtonListener(R.id.power, this::powerAction);
        setButtonListener(R.id.root, this::rootAction);
        setButtonListener(R.id.faculty, this::factorial);

        setButtonListener(R.id.sinus, this::sinusAction);
        setButtonListener(R.id.cosinus, this::cosinusAction);
        setButtonListener(R.id.tangens, this::tangensAction);
        setButtonListener(R.id.sinush, this::sinushAction);
        setButtonListener(R.id.cosinush, this::cosinushAction);
        setButtonListener(R.id.tangensh, this::tangenshAction);

        setButtonListener(R.id.asinus, this::aSinusAction);
        setButtonListener(R.id.acosinus, this::aCosinusAction);
        setButtonListener(R.id.atangens, this::aTangensAction);

        setButtonListener(R.id.asinush, this::aSinusHAction);
        setButtonListener(R.id.acosinush, this::aCosinusHAction);
        setButtonListener(R.id.atangensh, this::aTangensHAction);

        setButtonListener(R.id.log, this::logAction);
        setButtonListener(R.id.log2x, this::log2Action);
        setButtonListener(R.id.ln, this::lnAction);
        setButtonListener(R.id.logxx, this::logXAction);

        // important: "е" and "e" are different characters
        setButtonListener(R.id.e, this::eAction);
        setButtonListener(R.id.е, this::еAction);
        setButtonListener(R.id.pi, this::piAction);

        setButtonListenerWithoutChangedWeight(R.id.half, this::halfAction);
        setButtonListenerWithoutChangedWeight(R.id.third, this::thirdAction);
        setButtonListenerWithoutChangedWeight(R.id.quarter, this::quarterAction);

        setButtonListener(R.id.thirdRoot, this::thirdRootAction);

        setImageButtonListenerWithoutChangedWeight(R.id.scientificButton, this::setScienceButtonState);
        setImageButtonListenerWithoutChangedWeight(R.id.shift, this::setShiftButtonState);

        setLongTextViewClickListener(R.id.calculate_label, this::saveCalculateLabelData);
        setLongTextViewClickListener(R.id.result_label, this::saveResultLabelData);

        if (findViewById(R.id.functionMode_text) != null) {
            findViewById(R.id.functionMode_text).setOnClickListener(view -> changeFunctionMode());
        }
        if (findViewById(R.id.shiftMode_text) != null) {
            findViewById(R.id.shiftMode_text).setOnClickListener(view -> setShiftButtonState());
        }
    }

    private void changeFunctionMode() {
        try {

            final TextView function_mode_text = findViewById(R.id.functionMode_text);
            final String mode;
            mode = dataManager.getJSONSettingsData("functionMode", getApplicationContext()).getString("value");
            switch (mode) {
                case "Deg":
                    dataManager.saveToJSONSettings("functionMode", "Rad", getApplicationContext());
                    break;
                case "Rad":
                    dataManager.saveToJSONSettings("functionMode", "Deg", getApplicationContext());
                    break;
            }

            try {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                function_mode_text.setText(dataManager.getJSONSettingsData("functionMode", getApplicationContext()).getString("value"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    private void setScienceButtonState() {
        try {
            final String value;
            value = dataManager.getJSONSettingsData("showScienceRow", getApplicationContext()).getString("value");

            // Toggle the state of the science button
            if (value.equals("false")) {
                dataManager.saveToJSONSettings("showScienceRow", "true", getApplicationContext());
            } else {
                dataManager.saveToJSONSettings("showScienceRow", "false", getApplicationContext());
            }

            // Handle the visual representation or behavior associated with the state change
            showOrHideScienceButtonState();


            if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true") &&
                    dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setCalculateText("");

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1.5F;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    private void setShiftButtonState() {
        try {
            if (dataManager.getJSONSettingsData("shiftRow", getApplicationContext()).getString("value").equals("3")) {
                dataManager.saveToJSONSettings("shiftRow", "1", getApplicationContext());
            } else {
                final String num = String.valueOf(Integer.parseInt(dataManager.getJSONSettingsData("shiftRow", getApplicationContext()).getString("value")) + 1);
                dataManager.saveToJSONSettings("shiftRow", num, getApplicationContext());
            }

            shiftButtonAction();

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }


    private void shiftButtonAction() {
        try {
            LinearLayout buttonRow1 = findViewById(R.id.scientificRow11);
            LinearLayout buttonRow2 = findViewById(R.id.scientificRow21);
            LinearLayout buttonRow12 = findViewById(R.id.scientificRow12);
            LinearLayout buttonRow22 = findViewById(R.id.scientificRow22);
            LinearLayout buttonRow13 = findViewById(R.id.scientificRow13);
            LinearLayout buttonRow23 = findViewById(R.id.scientificRow23);
            LinearLayout buttonRow3 = findViewById(R.id.scientificRow3);
            TextView shiftModeText = findViewById(R.id.shiftMode_text);

            // Read the current state of the shift button from the stored data
            final String shiftValue = dataManager.getJSONSettingsData("shiftRow", getApplicationContext()).getString("value");
            final String rowValue = dataManager.getJSONSettingsData("showScienceRow", getApplicationContext()).getString("value");

            // Toggle the visibility of different LinearLayouts and update TextView based on the shift button state
            if (rowValue.equals("true") && (buttonRow1 != null && buttonRow2 != null && buttonRow12 != null
                    && buttonRow22 != null && buttonRow13 != null && buttonRow23 != null && buttonRow3 != null
                    && shiftModeText != null)) {
                switch (shiftValue) {
                    case "1":
                        buttonRow1.setVisibility(View.VISIBLE);
                        buttonRow2.setVisibility(View.VISIBLE);
                        buttonRow12.setVisibility(View.GONE);
                        buttonRow22.setVisibility(View.GONE);
                        buttonRow13.setVisibility(View.GONE);
                        buttonRow23.setVisibility(View.GONE);
                        buttonRow3.setVisibility(View.VISIBLE);
                        shiftModeText.setText("1");
                        break;
                    case "2":
                        buttonRow1.setVisibility(View.GONE);
                        buttonRow2.setVisibility(View.GONE);
                        buttonRow12.setVisibility(View.VISIBLE);
                        buttonRow22.setVisibility(View.VISIBLE);
                        buttonRow13.setVisibility(View.GONE);
                        buttonRow23.setVisibility(View.GONE);
                        buttonRow3.setVisibility(View.VISIBLE);
                        shiftModeText.setText("2");
                        break;
                    case "3":
                        buttonRow1.setVisibility(View.GONE);
                        buttonRow2.setVisibility(View.GONE);
                        buttonRow12.setVisibility(View.GONE);
                        buttonRow22.setVisibility(View.GONE);
                        buttonRow13.setVisibility(View.VISIBLE);
                        buttonRow23.setVisibility(View.VISIBLE);
                        buttonRow3.setVisibility(View.VISIBLE);
                        shiftModeText.setText("3");
                        break;
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    private void showOrHideScienceButtonState() {
        TextView function_mode_text = findViewById(R.id.functionMode_text);
        TextView shiftModeText = findViewById(R.id.shiftMode_text);

        LinearLayout buttonRow1 = findViewById(R.id.scientificRow11);
        LinearLayout buttonRow12 = findViewById(R.id.scientificRow12);
        LinearLayout buttonRow13 = findViewById(R.id.scientificRow13);

        LinearLayout buttonRow2 = findViewById(R.id.scientificRow21);
        LinearLayout buttonRow22 = findViewById(R.id.scientificRow22);
        LinearLayout buttonRow23 = findViewById(R.id.scientificRow23);

        LinearLayout buttonRow3 = findViewById(R.id.scientificRow3);

        ImageButton shiftButton = findViewById(R.id.shift);

        LinearLayout buttonLayout = findViewById(R.id.button_layout);

        if (function_mode_text != null) {
            try {
                function_mode_text.setText(dataManager.getJSONSettingsData("functionMode", getApplicationContext()).getString("value"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        if (buttonLayout != null) {
            try {
                LinearLayout.LayoutParams layoutParams;
                if (dataManager.getJSONSettingsData("showScienceRow", getApplicationContext()).getString("value").equals("false")) {

                    layoutParams = (LinearLayout.LayoutParams) buttonLayout.getLayoutParams();
                    layoutParams.weight = 4;
                    buttonLayout.setLayoutParams(layoutParams);

                    buttonRow1.setVisibility(View.GONE);
                    buttonRow2.setVisibility(View.GONE);
                    buttonRow12.setVisibility(View.GONE);
                    buttonRow22.setVisibility(View.GONE);
                    buttonRow13.setVisibility(View.GONE);
                    buttonRow23.setVisibility(View.GONE);
                    shiftButton.setVisibility(View.GONE);
                    buttonRow3.setVisibility(View.GONE);

                    assert function_mode_text != null;
                    assert shiftModeText != null;
                    function_mode_text.setVisibility(View.GONE);
                    shiftModeText.setVisibility(View.GONE);
                } else {
                    layoutParams = (LinearLayout.LayoutParams) buttonLayout.getLayoutParams();
                    layoutParams.weight = 7;
                    buttonLayout.setLayoutParams(layoutParams);

                    buttonRow1.setVisibility(View.VISIBLE);
                    buttonRow2.setVisibility(View.VISIBLE);
                    buttonRow12.setVisibility(View.VISIBLE);
                    buttonRow22.setVisibility(View.VISIBLE);
                    buttonRow13.setVisibility(View.VISIBLE);
                    buttonRow23.setVisibility(View.VISIBLE);
                    shiftButton.setVisibility(View.VISIBLE);
                    buttonRow3.setVisibility(View.VISIBLE);

                    assert function_mode_text != null;
                    assert shiftModeText != null;
                    function_mode_text.setVisibility(View.VISIBLE);
                    shiftModeText.setVisibility(View.VISIBLE);
                }

                layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        shiftButtonAction();
    }


    private void setCommaButtonListener(int buttonId) {
        Button btn = findViewById(buttonId);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                CommaAction();
                dataManager.saveNumbers(getApplicationContext());

                try {
                    if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
            });
        }
    }


    private void setCalculateButtonListener(int buttonId) {
        Button btn = findViewById(buttonId);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                try {
                    Calculate();
                    dataManager.saveNumbers(getApplicationContext());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }


    private void setNumberButtonListener(int buttonId) {
        Button btn = findViewById(buttonId);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                final String num = v.getTag().toString();
                NumberAction(num);
                dataManager.saveNumbers(getApplicationContext());
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());

                try {
                    if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
            });
        }
    }

    private void setOperationButtonListener(int buttonId, String operation) {
        Button btn = findViewById(buttonId);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                OperationAction(operation);
                dataManager.saveNumbers(getApplicationContext());
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                setCalculateText(replacePiWithSymbolInString(getCalculateText()));

                try {
                    if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
            });
        }
    }

    private void setEmptyButtonListener(int buttonId, String action) {
        Button btn = findViewById(buttonId);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                EmptyAction(action);
                dataManager.saveNumbers(getApplicationContext());
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);

                scrollToStart(findViewById(R.id.calculate_scrollview));
                scrollToStart(findViewById(R.id.result_scrollview));
            });
        }
    }


    private void setButtonListener(int textViewId, Runnable action) {
        TextView textView = findViewById(textViewId);
        if (textView != null) {
            textView.setOnClickListener(v -> {
                action.run();
                dataManager.saveNumbers(getApplicationContext());

                try {
                    if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
            });
        }
    }


    private void setButtonListenerWithoutChangedWeight(int textViewId, Runnable action) {
        TextView textView = findViewById(textViewId);
        if (textView != null) {
            textView.setOnClickListener(v -> {
                action.run();
                dataManager.saveNumbers(getApplicationContext());

                try {
                    if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void setImageButtonListenerWithoutChangedWeight(int textViewId, Runnable action) {
        ImageButton textView = findViewById(textViewId);
        if (textView != null) {
            textView.setOnClickListener(v -> {
                action.run();
                dataManager.saveNumbers(getApplicationContext());

                try {
                    if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }


    private void setLongTextViewClickListener(int textViewId, Runnable action) {
        // Find the TextView with the specified ID
        TextView textView = findViewById(textViewId);

        // Check if the TextView is not null
        if (textView != null) {
            // Set a long click listener for the TextView
            textView.setOnLongClickListener(v -> {
                // Execute the specified action when the TextView is long-clicked
                action.run();
                // Return false to indicate that the event is not consumed
                return false;
            });
        }
    }

    /**
     * Saves the result label data to the clipboard.
     */
    private void saveResultLabelData() {
        // Get the system clipboard manager
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Create a ClipData with plain text representing the result text
        ClipData clipData = ClipData.newPlainText("", getResultText());

        // Set the created ClipData as the primary clip on the clipboard
        clipboardManager.setPrimaryClip(clipData);

        showToastLong("The value has been saved ...", getApplicationContext());

    }

    /**
     * Saves the calculate label data to the clipboard.
     */
    private void saveCalculateLabelData() {
        // Get the system clipboard manager
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Create a ClipData with plain text representing the calculate text
        ClipData clipData = ClipData.newPlainText("", getCalculateText());

        // Set the created ClipData as the primary clip on the clipboard
        clipboardManager.setPrimaryClip(clipData);


        showToastLong("The value has been saved ...", getApplicationContext());

    }

    /**
     * Sets up the listener for all number buttons
     *
     * @param buttonId The ID of the button to which the listener is to be set.
     */
    private void setNegateButtonListener(int buttonId) {
        Button btn = findViewById(buttonId);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                NegativAction();
                dataManager.saveNumbers(getApplicationContext());

                try {
                    if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
            });
        }
    }

    private void setClipboardButtonListener(int buttonId, String action) {
        Button btn = findViewById(buttonId);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                ClipboardAction(action);
                dataManager.saveNumbers(getApplicationContext());
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());

                try {
                    if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                        scrollToEnd(findViewById(R.id.calculate_scrollview));
                        scrollToStart(findViewById(R.id.result_scrollview));
                    } else {
                        scrollToBottom(findViewById(R.id.calculate_scrollview));
                        scrollToBottom(findViewById(R.id.result_scrollview));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void resetIfPressedCalculate() {
        try {
            if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                setResultText("0");
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Appends or sets the text "sin(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void sinusAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("sin(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("sin(");
                        } else {
                            addCalculateText("sin(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }


    private void sinushAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("sinh(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("sinh(");
                        } else {
                            addCalculateText("sinh(");
                        }
                    }
                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }


    private void aSinusAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("sin⁻¹(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("sin⁻¹(");
                        } else {
                            addCalculateText("sin⁻¹(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }


    private void aSinusHAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("sinh⁻¹(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("sinh⁻¹(");
                        } else {
                            addCalculateText("sinh⁻¹(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }

    /**
     * Appends or sets the text "cos(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void cosinusAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("cos(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("cos(");
                        } else {
                            addCalculateText("cos(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }

    private void cosinushAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("cosh(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("cosh(");
                        } else {
                            addCalculateText("cosh(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }


    private void aCosinusAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("cos⁻¹(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("cos⁻¹(");
                        } else {
                            addCalculateText("cos⁻¹(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }

    /**
     * Appends or sets the text "sin⁻¹(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void aCosinusHAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("cosh⁻¹(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("cosh⁻¹(");
                        } else {
                            addCalculateText("cosh⁻¹(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }

    /**
     * Appends or sets the text "tan(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void tangensAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("tan(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("tan(");
                        } else {
                            addCalculateText("tan(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }

    /**
     * Appends or sets the text "tanh(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void tangenshAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("tanh(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("tanh(");
                        } else {
                            addCalculateText("tanh(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }

    /**
     * Appends or sets the text "tan⁻¹(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void aTangensAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("tan⁻¹(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("tan⁻¹(");
                        } else {
                            addCalculateText("tan⁻¹(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }

    /**
     * Appends or sets the text "sin⁻¹(" to the calculation input.
     * Scrolls to the bottom of the scroll view if it exists.
     */
    private void aTangensHAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("tanh⁻¹(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("tanh⁻¹(");
                        } else {
                            addCalculateText("tanh⁻¹(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }

    /**
     * This method performs the action of adding "log(" to the calculation text.
     * It checks if the "logX" flag is false in the JSON file, indicating that the logarithm function is not currently selected.
     * If the mode is not in "eNotation", it proceeds to add "log(" to the calculation text.
     * The method handles cases where the calculation text is empty or not, and whether to add "log(" with or without spaces depending on the calculation mode.
     * It also scrolls to the bottom of the scroll view if it exists.
     * After adding "log(" to the calculation text, it formats the result text accordingly.
     */
    private void logAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("log(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("log(");
                        } else {
                            addCalculateText("log(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }

    /**
     * This method performs the action of adding "log₂(" to the calculation text.
     * It follows a similar procedure to the logAction() method but adds "log₂(" instead of "log(".
     */
    private void log2Action() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("log₂(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("log₂(");
                        } else {
                            addCalculateText("log₂(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }

    private void logXAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    if (getCalculateText().contains("=")) {
                        setCalculateText("");
                        if (isInvalidInput(getResultText())) {
                            setResultText("0");
                        }
                        setRemoveValue(true);
                    }

                    dataManager.saveToJSONSettings("logX", "true", getApplicationContext());
                    if (getCalculateText().isEmpty()) {
                        setCalculateText("log");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("log");
                        } else {
                            addCalculateText("log");
                            setRotateOperator(true);
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }


    private void lnAction() {
        try {
            resetIfPressedCalculate();

            if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    checkCalculateText();

                    if (getCalculateText().isEmpty()) {
                        setCalculateText("ln(");
                    } else {
                        if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                            addCalculateTextWithoutSpace("ln(");
                        } else {
                            addCalculateText("ln(");
                        }
                    }

                    // Scroll to the bottom of the scroll view if it exists
                    if (findViewById(R.id.calculate_scrollview) != null) {
                        scrollToStart(findViewById(R.id.calculate_scrollview));
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        formatResultTextAfterType();
    }

    private void eAction() {
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
        // Check if logarithmic mode is disabled
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                showToastLong(getString(R.string.buttonIsDisabled), getApplicationContext());
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    // Read the current eNotation mode from the data manager
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");

                    // Check if eNotation mode is currently set to false
                    if (mode.equals("false")) {
                        // Check if the remove value flag is set
                        if (getRemoveValue()) {
                            setCalculateText("");
                            // If the result text is invalid, set it to "0"
                            if (isInvalidInput(getResultText())) {
                                setResultText("0");
                            }
                            setRemoveValue(false);
                        }

                        // Add or remove "e" based on its current presence in the result text
                        if (!getResultText().contains("e+") && !getResultText().contains("e-")) {
                            dataManager.saveToJSONSettings("eNotation", true, getApplicationContext());
                            addResultText("e");
                        } else if (getResultText().contains("e") && !getResultText().contains("e+") || !getResultText().contains("e-")) {
                            dataManager.saveToJSONSettings("eNotation", false, getApplicationContext());
                            setResultText(getResultText().replace("e", ""));
                        }
                    } else {
                        // Remove all occurrences of "e", "e+", and "e-" from the result text
                        setResultText(getResultText().replace("e+", "").replace("e-", "").replace("e", ""));
                        dataManager.saveToJSONSettings("eNotation", false, getApplicationContext());
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Format the result text after typing
        formatResultTextAfterType();
    }

    private void еAction() {
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
        // Check if logarithmic mode is disabled
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                addCalculateTextWithoutSpace("е");
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    // Read the current eNotation mode from the data manager
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");

                    // Check if eNotation mode is currently set to false
                    if (mode.equals("false")) {
                        // Check and handle the calculate text
                        checkCalculateText();

                        // Add "е" to the calculate text
                        if (getCalculateText().isEmpty()) {
                            setCalculateText("е");
                        } else {
                            final String text = getCalculateText().replace(" ", "")
                                    .replace("×", "*").replace("÷", "/");
                            char lastChar = text.charAt(text.length() - 1);

                            // Check the last character and add "е" accordingly
                            if (!isOperator(String.valueOf(lastChar)) && !String.valueOf(lastChar).equals("(")) {
                                if (getRotateOperator()) {
                                    addCalculateTextWithoutSpace("е");
                                } else {
                                    addCalculateText(getLastOp() + " е");
                                }
                            } else {
                                addCalculateText("е");
                            }
                        }
                        setRotateOperator(true);
                    }
                }
            }

            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Format the result text after typing
        formatResultTextAfterType();
    }


    private void piAction() {
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
        // Check if logarithmic mode is disabled
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                addCalculateTextWithoutSpace("π");
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    // Read the current eNotation mode from the data manager
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");

                    // Check if eNotation mode is currently set to false
                    if (mode.equals("false")) {
                        // Check and handle the calculate text
                        checkCalculateText();

                        // Add "π" to the calculate text
                        if (getCalculateText().isEmpty()) {
                            setCalculateText("π");
                        } else {
                            final String text = getCalculateText().replace(" ", "")
                                    .replace("×", "*").replace("÷", "/");
                            char lastChar = text.charAt(text.length() - 1);

                            // Check the last character and add "π" accordingly
                            if (!isOperator(String.valueOf(lastChar)) && !String.valueOf(lastChar).equals("(")) {
                                if (getRotateOperator()) {
                                    addCalculateTextWithoutSpace("π");
                                } else {
                                    addCalculateText(getLastOp() + " π");
                                }
                            } else {
                                addCalculateText("π");
                            }
                        }
                        setRotateOperator(true);
                    }
                }
            }

            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Format the result text after typing
        formatResultTextAfterType();
    }


    private void parenthesesOnAction() {
        // Check if calculate text is empty and set or add opening parenthesis accordingly
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    setResultText("0");
                    dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                }
                addCalculateTextWithoutSpace("(");
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("false")) {
                        checkCalculateText();

                        if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                            if (getCalculateText().isEmpty()) {
                                setCalculateText("(");
                            } else {
                                final String text = getCalculateText().replace(" ", "")
                                        .replace("×", "*").replace("÷", "/");
                                char lastChar = text.charAt(text.length() - 1);

                                if (!isOperator(String.valueOf(lastChar)) && !String.valueOf(lastChar).equals("(")) {
                                    if (getRotateOperator()) {
                                        addCalculateTextWithoutSpace("(");
                                    } else {
                                        addCalculateText(getLastOp() + " (");
                                    }
                                } else {
                                    addCalculateText("(");
                                }
                            }
                        } else {
                            addCalculateText(getResultText() + " (");
                            dataManager.saveToJSONSettings("logX", "false", getApplicationContext());
                            setRemoveValue(true);
                        }
                    }
                }
            }

            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();

        scrollToStart(findViewById(R.id.calculate_scrollview));
        setRotateOperator(false);
    }

    private void parenthesesOffAction() {
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    setResultText("0");
                    dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                }
                addCalculateTextWithoutSpace(")");
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");

                    if (mode.equals("false")) {
                        checkCalculateText();

                        if (getCalculateText().contains("=")) {
                            setCalculateText("");
                            if (isInvalidInput(getResultText())) {
                                setResultText("0");
                            }
                        }

                        Pattern pattern = Pattern.compile("√\\(\\d+\\)$");
                        Matcher matcher = pattern.matcher(getCalculateText());

                        if (!getCalculateText().isEmpty() && getCalculateText().contains("(")) {
                            if (matcher.find()) {
                                addCalculateText(")");
                            } else {
                                if (!getRotateOperator()) {
                                    addCalculateText(getResultText() + " )");
                                } else {
                                    final String text = getCalculateText().replace(" ", "")
                                            .replace("×", "*").replace("÷", "/");
                                    char lastChar = text.charAt(text.length() - 1);

                                    if (String.valueOf(lastChar).equals(")")) {
                                        addCalculateText(" )");
                                    } else {
                                        if (!String.valueOf(lastChar).equals("½") &&
                                                !String.valueOf(lastChar).equals("⅓") &&
                                                !String.valueOf(lastChar).equals("¼")) {
                                            addCalculateText(getLastOp() + " " + getResultText() + " )");
                                        } else {
                                            addCalculateTextWithoutSpace(")");
                                        }
                                    }
                                }
                            }
                            setRotateOperator(true);
                            if (findViewById(R.id.calculate_scrollview) != null) {
                                scrollToStart(findViewById(R.id.calculate_scrollview));
                            }
                        }
                    }
                }
            }

            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    private void factorial() {
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    addCalculateTextWithoutSpace(getResultText() + "!");
                }

                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("false")) {
                        final String calc_text = getCalculateText().replace(" ", "");

                        checkCalculateText();

                        if (calc_text.isEmpty()) {
                            addCalculateText(getResultText() + "!");
                            setRotateOperator(true);
                        } else {
                            String lastchar = String.valueOf(calc_text.replace(" ", "").charAt(calc_text.length() - 1));
                            if (lastchar.equals("!")) {
                                addCalculateText(getLastOp().replace("*", "×").replace("/", "÷") + " " + getResultText() + "!");
                                setRotateOperator(true);
                            } else if (lastchar.equals(")")) {
                                addCalculateText("!");
                                setRotateOperator(true);
                            } else {
                                addCalculateText(getResultText() + "!");
                                setRotateOperator(true);
                            }
                        }
                        if (findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }

            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    private void powerAction() {
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                    addCalculateTextWithoutSpace(getResultText() + "^");
                    return;
                }
                addCalculateTextWithoutSpace("^");
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).equals("false")) {
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("false")) {
                        setLastOp("^");
                        checkCalculateText();

                        if (!getRotateOperator()) {
                            setRemoveValue(true);
                            setLastNumber(getResultText());
                            if (getCalculateText().contains("=")) {
                                setCalculateText(getResultText() + " ^");
                            } else {
                                addCalculateText(getResultText() + " ^");
                            }
                            setRemoveValue(true);
                        } else {
                            addCalculateText("^");
                            setRemoveValue(true);
                            setRotateOperator(false);
                        }
                        if (findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }

            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    private void rootAction() {
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    setResultText("0");
                    dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                }
                addCalculateTextWithoutSpace("√(");
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    checkCalculateText();

                    if (mode.equals("false")) {
                        if (!getRotateOperator()) {
                            addCalculateText("√(");
                        } else if (!getCalculateText().isEmpty()) {
                            addCalculateText(getLastOp() + " √(");
                        }
                        setRemoveValue(true);
                        //setRotateOperator(true);
                        if (findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }

            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    private void thirdRootAction() {
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    setResultText("0");
                    dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                }
                addCalculateTextWithoutSpace("³√(");
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    checkCalculateText();

                    if (mode.equals("false")) {
                        if (!getRotateOperator()) {
                            addCalculateText("³√(");
                        } else if (!getCalculateText().isEmpty()) {
                            addCalculateText(getLastOp() + " ³√(");
                        }
                        setRemoveValue(true);
                        //setRotateOperator(true);
                        if (findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }

            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    private void halfAction() {
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    final String input = getResultText() + "÷2=";
                    setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + "÷2")));
                    formatResultTextAfterType();
                    addToHistory(input);
                    return;
                }
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                addCalculateTextWithoutSpace("½");
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    checkCalculateText();

                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("false")) {
                        if (!getRotateOperator()) {
                            addCalculateText("½");
                        } else if (!getCalculateText().isEmpty()) {
                            addCalculateText(getLastOp() + " ½");
                        }
                        setRemoveValue(true);
                        setRotateOperator(true);
                        if (findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }

            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    /**
     * This method performs the action of adding "⅓" (third) to the calculation text.
     * It follows a similar procedure to the halfAction() method but adds "⅓" instead of "½".
     */
    private void thirdAction() {
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    final String input = getResultText() + "÷3=";
                    setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + "÷3")));
                    formatResultTextAfterType();
                    addToHistory(input);
                    return;
                }
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                addCalculateTextWithoutSpace("⅓");
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    checkCalculateText();

                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("false")) {
                        if (!getRotateOperator()) {
                            addCalculateText("⅓");
                        } else if (!getCalculateText().isEmpty()) {
                            addCalculateText(getLastOp() + " ⅓");
                        }
                        setRemoveValue(true);
                        setRotateOperator(true);
                        if (findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }

            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }


    private void quarterAction() {
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    final String input = getResultText() + "÷4=";
                    setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + "÷4")));
                    formatResultTextAfterType();
                    addToHistory(input);
                    return;
                }
                dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                addCalculateTextWithoutSpace("¼");
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    checkCalculateText();

                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("false")) {
                        if (!getRotateOperator()) {
                            addCalculateText("¼");
                        } else if (!getCalculateText().isEmpty()) {
                            addCalculateText(getLastOp() + " ¼");
                        }
                        setRemoveValue(true);
                        setRotateOperator(true);
                        if (findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }

            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }


    private void changeButtonColors(ViewGroup layout, int foregroundColor, int backgroundColor) {
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                v.setBackgroundColor(backgroundColor);

                // If the child is a Button, change the foreground and background colors
                if (v instanceof Button) {
                    ((Button) v).setTextColor(foregroundColor);
                    v.setBackgroundColor(backgroundColor);
                }
                // If the child itself is a ViewGroup (e.g., a layout), call the function recursively
                else if (v instanceof ViewGroup) {
                    changeButtonColors((ViewGroup) v, foregroundColor, backgroundColor);
                }
            }
        }
    }

    private void changeTextViewColors(ViewGroup layout, int foregroundColor, int backgroundColor) {
        if (layout != null) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View v = layout.getChildAt(i);
                v.setBackgroundColor(backgroundColor);

                // If the child is a TextView, change the foreground and background colors
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(foregroundColor);
                    v.setBackgroundColor(backgroundColor);
                }
                // If the child itself is a ViewGroup (e.g., a layout), call the function recursively
                else if (v instanceof ViewGroup) {
                    changeTextViewColors((ViewGroup) v, foregroundColor, backgroundColor);
                }
            }
        }
    }


    protected void onDestroy() {
        super.onDestroy();
        try {
            if (dataManager.getJSONSettingsData("disablePatchNotesTemporary", getApplicationContext()).getString("value").equals("true")) {
                dataManager.saveToJSONSettings("disablePatchNotesTemporary", false, getApplicationContext());
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        formatResultTextAfterType();
    }


    public void NumberAction(String num) {
        boolean b = Integer.parseInt(num) >= 2 && Integer.parseInt(num) <= 9;
        setLastNumber(getResultText());
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("true") && b) {
                    dataManager.saveToJSONSettings("logX", "false", getApplicationContext());
                    String small_number = convertToSmallNumber(Integer.parseInt(num));
                    addCalculateTextWithoutSpace(small_number);
                    addCalculateTextWithoutSpace("(");
                } else {
                    addCalculateTextWithoutSpace(num);
                }
                final String calculate_text = balanceParentheses(getCalculateText());
                if (!isInvalidInput(calculate_text)) {
                    setResultText(CalculatorEngine.calculate(balanceParentheses(calculate_text)));
                } else {
                    setResultText("0");
                }
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    if (dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value").equals("true")) {
                        addResultText(num);
                        dataManager.saveToJSONSettings("eNotation", "false", getApplicationContext());
                    } else {
                        String calculateText = getCalculateText();
                        if (isInvalidInput(getResultText()) || isInvalidInput(calculateText)) {
                            setCalculateText("");
                            setRemoveValue(true);
                        }
                        if (getRemoveValue()) {
                            if (isInvalidInput(calculateText) || calculateText.contains("=")) {
                                setCalculateText("");
                            }
                            setResultText("0");
                            setRemoveValue(false);
                        }

                        if (getResultText().replace(".", "").replace(",", "").length() < 18) {
                            if (getResultText().equals("0")) {
                                setResultText(num);
                            } else if (getResultText().equals("-0")) {
                                // Replace "0" with the new digit, and add the negative sign back
                                setResultText("-" + num);
                            } else {
                                // Add the new digit, and add the negative sign back if needed
                                addResultText(num);
                            }
                        }
                    }
                } else {
                    if (b) {
                        dataManager.saveToJSONSettings("logX", "false", getApplicationContext());
                        String small_number = convertToSmallNumber(Integer.parseInt(num));
                        addCalculateTextWithoutSpace(small_number);
                        addCalculateTextWithoutSpace("(");
                        setRotateOperator(false);
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        setCalculateText(replacePiWithSymbolInString(getCalculateText()));
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();

        scrollToEnd(findViewById(R.id.calculate_scrollview));
    }


    private static String convertToSmallNumber(int num) {
        // Unicode block for subscript numbers: U+208x
        char subNull = '₀';
        int subNum = num + (int) subNull;
        return Character.toString((char) subNum);
    }


    public void ClipboardAction(final String c) {
        final String mode;
        try {
            mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (mode.equals("false")) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            switch (c) {
                case "MC": {
                    ClipData clipData = ClipData.newPlainText("", "");
                    clipboardManager.setPrimaryClip(clipData);
                    showToastLong(getString(R.string.clipboardCleared), getApplicationContext());
                    break;
                }
                case "MR":
                    handleMRAction(clipboardManager);
                    break;
                case "MS": {
                    ClipData clipData = ClipData.newPlainText("", getResultText());
                    clipboardManager.setPrimaryClip(clipData);
                    showToastShort(getString(R.string.savedvalue), getApplicationContext());
                    break;
                }
            }
        }
        formatResultTextAfterType();

        scrollToEnd(findViewById(R.id.calculate_scrollview));
        scrollToStart(findViewById(R.id.result_scrollview));
    }


    private void handleMRAction(ClipboardManager clipboardManager) {
        ClipData clipData = clipboardManager.getPrimaryClip();

        assert clipData != null;
        ClipData.Item item = clipData.getItemAt(0);
        String clipText = (String) item.getText();

        if (clipData.getItemCount() == 0) {
            // Handle the case where clipboard data is null or empty
            showToastShort(getString(R.string.clipboardIsEmpty), getApplicationContext());

            return;
        }

        String scientificNotationPattern = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";
        String mathTaskPattern = "[-+*/%^()0-9.eE\\s]+";

        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (!clipText.isEmpty()) {
                    addCalculateTextWithoutSpace(clipText.replace(" ", ""));
                    showToastShort(getString(R.string.pastedClipboard), getApplicationContext());
                }
            } else {
                if (clipText.matches(scientificNotationPattern) && !clipText.matches(mathTaskPattern)) {
                    processScientificNotation(clipText);
                } else if ((clipText.matches(mathTaskPattern) && !clipText.matches(scientificNotationPattern)) || clipText.matches("[-+]?[0-9]+")) {
                    processMathTaskOrNumber(clipText);
                } else {
                    showToastLong(getString(R.string.invalidInput), getApplicationContext());
                }

                if (!getCalculateText().isEmpty()) {
                    setRotateOperator(!isOperator(String.valueOf(getCalculateText().charAt(getCalculateText().length() - 1))));
                }
            }

            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
        layoutParams.weight = 1;
        findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);

        scrollToStart(findViewById(R.id.calculate_scrollview));
        scrollToStart(findViewById(R.id.result_scrollview));
    }


    private void processScientificNotation(String text) {
        final String resultText = CalculatorEngine.calculate(balanceParentheses(text));
        setResultText(text);
        formatResultTextAfterType();
        final String new_text = getResultText();
        setResultText(resultText);

        if (getCalculateText().isEmpty()) {
            setCalculateText(new_text);
        } else {
            addCalculateText(new_text);
        }
    }


    private void processMathTaskOrNumber(String text) {
        if (!text.matches("[-+]?[0-9]+")) {
            if (getCalculateText().isEmpty()) {
                setCalculateText(text);
            } else {
                addCalculateText(text);
            }
        } else {
            setResultText(text);
        }
    }


    public void OperationAction(final String op) {
        final String new_op = op.replace("*", "×").replace("/", "÷");
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    addCalculateTextWithoutSpace(getResultText() + new_op);
                    return;
                }
                addCalculateTextWithoutSpace(new_op);
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    setLastOp(op);

                    // Check if there is one operator at the end

                    final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                    if (mode.equals("true") && getResultText().length() > 1 && (new_op.equals("+") || new_op.equals("-"))) {
                        int lastIndex = getResultText().length() - 1;
                        char lastChar = getResultText().charAt(lastIndex);

                        // Check if the last character isn't an operator
                        if (!isOperator(String.valueOf(lastChar))) {
                            setResultText(getResultText() + new_op);
                            return;
                        } else {
                            setResultText(getResultText() + "0");
                            dataManager.saveToJSONSettings("eNotation", false, getApplicationContext());
                            setRemoveValue(true);
                        }
                    }

                    if (mode.equals("false")) {

                        if (!getRotateOperator()) {
                            setLastNumber(getResultText());
                            if (getCalculateText().contains("=")) {
                                setCalculateText(getResultText() + " " + new_op);
                            } else {
                                addCalculateText(getResultText() + " " + new_op);
                            }
                            setRemoveValue(true);
                        } else {
                            addCalculateText(new_op);
                            setRemoveValue(true);
                        }
                        setRotateOperator(false);
                        if (findViewById(R.id.calculate_scrollview) != null) {
                            scrollToStart(findViewById(R.id.calculate_scrollview));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
        scrollToEnd(findViewById(R.id.calculate_scrollview));
        scrollToStart(findViewById(R.id.result_scrollview));
    }

    public void EmptyAction(final String e) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();

        switch (e) {
            case "⌫":
                handleBackspaceAction();
                scrollToEnd(findViewById(R.id.calculate_scrollview));
                break;
            case "C":
                setResultText("0");
                setCalculateText("");
                setRotateOperator(false);
                dataManager.saveToJSONSettings("logX", "false", getApplicationContext());

                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
                break;
            case "CE":
                setResultText("0");

                layoutParams.weight = 1;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
                break;
        }
    }

    public static String removeOperatorsFromRight(String input) {
        for (int i = input.length() - 1; i >= 0; i--) {
            char currentChar = input.charAt(i);
            String currentToken = String.valueOf(currentChar);

            if (isStandardOperator(currentToken)) {
                input = input.substring(0, input.length() - 1);
            } else {
                break;
            }
        }

        return input;
    }


    private void handleBackspaceAction() {
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (!getCalculateText().isEmpty()) {
                    if (getCalculateText().endsWith("(")) {
                        setCalculateText(removeOperators(getCalculateText().substring(0, getCalculateText().length() - 1)));
                    } else {
                        if (getCalculateText().length() > 1) {
                            setCalculateText(getCalculateText().substring(0, getCalculateText().length() - 1));

                            if (Character.isDigit(getCalculateText().charAt(getCalculateText().length() - 1))) {
                                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                                if (isInvalidInput(getResultText())) {
                                    setResultText(CalculatorEngine.calculate(balanceParentheses(removeOperatorsFromRight(getCalculateText()))));
                                }
                            }
                        } else {
                            setCalculateText("");
                        }
                    }
                }

                if (getCalculateText().isEmpty()) {
                    setResultText("0");
                } else {
                    final String oldText = getResultText();
                    setResultText(CalculatorEngine.calculate(balanceParentheses(removeOperatorsFromRight(getCalculateText()))));
                    if (isInvalidInput(getResultText())) {
                        setResultText(oldText);
                    }
                }
            } else {
                String resultText = getResultText();
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    if (!isInvalidInput(getResultText())) {
                        if (!resultText.equals("0") && !resultText.isEmpty()) {
                            setResultText(resultText.substring(0, resultText.length() - 1));
                            if (resultText.equals("-")) {
                                setResultText(resultText + "0");
                            }
                        } else {
                            setResultText("0");
                        }
                    } else {
                        setCalculateText("");
                        setResultText("0");
                    }
                    if (getResultText().isEmpty() || getResultText().equals("")) {
                        setResultText("0");
                    }
                    dataManager.saveNumbers(getApplicationContext());
                } else {
                    setResultText(getResultText().replace("e+", "").replace("e-", "").replace("e", ""));
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();

    }

    public static String removeOperators(String input) {
        String[] operators = {"³√", "ln", "log₂", "log₃", "log₄", "log₅", "log₆", "log₇", "log₈",
                "log₉", "tanh⁻¹", "cosh⁻¹", "sinh⁻¹", "tan⁻¹", "cos⁻¹", "sin⁻¹", "tanh", "cosh",
                "sinh", "tan", "cos", "sin", "√"};

        StringBuilder result = new StringBuilder();
        int i = input.length() - 1;

        while (i >= 0) {
            boolean foundOperator = false;

            for (String operator : operators) {
                if (i - operator.length() + 1 >= 0 && input.substring(i - operator.length() + 1, i + 1).equals(operator)) {
                    i -= operator.length();
                    foundOperator = true;
                    break;
                }
            }

            if (!foundOperator) {
                break;
            }
        }

        result.append(input.substring(0, i + 1));

        return result.toString();
    }


    public void NegativAction() {
        dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                addCalculateTextWithoutSpace("-");
            } else {
                final char firstchar = getResultText().charAt(0);
                if (String.valueOf(firstchar).equals("-")) {
                    setResultText(getResultText().substring(1));
                } else {
                    setResultText("-" + getResultText());
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    public void CommaAction() {
        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    addCalculateTextWithoutSpace("0.");
                    setResultText("0");
                    dataManager.saveToJSONSettings("pressedCalculate", false, getApplicationContext());
                    Log.d("CommaAction", "CommaAction: pressedCalculate");
                    return;
                }
                Log.d("CommaAction", "CommaAction: addCalculateTextWithoutSpace");
                addCalculateTextWithoutSpace(".");
            } else {
                final String mode = dataManager.getJSONSettingsData("eNotation", getApplicationContext()).getString("value");
                if (mode.equals("false")) {
                    Log.d("CommaAction", "CommaAction: addCalculateTextWithoutSpace");
                    if (!getResultText().contains(".")) {
                        Log.d("CommaAction", "CommaAction: addResultText");
                        addResultText(".");

                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();
    }

    private String addSpaceToOperators(String string) {
        final String symbols = "+-*/÷×√^π½⅓¼=";
        StringBuilder stringBuilder = new StringBuilder();

        if (!string.isEmpty()) {
            string = string.replace(" ", "");
            for (int i = 0; i < string.length(); i++) {
                if (i + 1 < string.length()) {
                    if (String.valueOf(string.charAt(i)).equals("³") && String.valueOf(string.charAt(i + 1)).equals("√")) {
                        stringBuilder.append(string.charAt(i));
                        continue;
                    } else if (String.valueOf(string.charAt(i)).equals("√") && String.valueOf(string.charAt(i + 1)).equals("(")) {
                        stringBuilder.append(string.charAt(i));
                        continue;
                    }
                }

                if (symbols.indexOf(string.charAt(i)) != -1) {
                    if (i != 0 && !String.valueOf(string.charAt(i - 1)).equals("³")) {
                        stringBuilder.append(" ");
                    }
                    stringBuilder.append(string.charAt(i));
                    if (i + 1 < string.length() && isOperator(String.valueOf(symbols.indexOf(string.charAt(i + 1))))) {
                        stringBuilder.append(" ");
                    }
                } else {
                    stringBuilder.append(string.charAt(i));
                }
            }

            if (String.valueOf(stringBuilder.charAt(stringBuilder.length() - 1)).equals(" ")) {
                stringBuilder = stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
        }

        if (!String.valueOf(stringBuilder).isEmpty() && String.valueOf(stringBuilder.charAt(0)).equals(" ")) {
            stringBuilder.deleteCharAt(0);
        }
        return stringBuilder.toString();
    }

    @SuppressLint("SetTextI18n")
    public void Calculate() {
        addSpaceToOperators(getCalculateText());

        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                if (dataManager.getJSONSettingsData("pressedCalculate", getApplicationContext()).getString("value").equals("true")) {
                    return;
                }

                if (getCalculateText().isEmpty()) {
                    setResultText("0");
                } else {
                    setCalculateText(balanceParentheses(getCalculateText()));
                    setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                }
            } else {
                if (dataManager.getJSONSettingsData("logX", getApplicationContext()).getString("value").equals("false")) {
                    // Replace special characters for proper calculation
                    String calcText = getCalculateText().replace("*", "×").replace("/", "÷");

                    // Check if there is one operator at the end
                    if (getResultText().length() > 1) {
                        int lastIndex = getResultText().length() - 1;
                        char lastChar = getResultText().charAt(lastIndex);

                        // Check if the last character isn't an operator
                        if (isOperator(String.valueOf(lastChar))) {
                            setResultText(getResultText() + "0");
                            dataManager.saveToJSONSettings("eNotation", false, getApplicationContext());
                            setRemoveValue(false);
                            formatResultTextAfterType();
                            return;
                        }
                    }

                    // Check for valid input before performing calculations
                    if (!isInvalidInput(getResultText()) && !isInvalidInput(getCalculateText())) {
                        // Handle calculation based on the rotate operator flag
                        if (getRotateOperator()) {
                            if (!calcText.contains("=")) {
                                // Handle calculation when equals sign is not present
                                setLastNumber(getResultText());
                                setCalculateText(addSpaceToOperators(balanceParentheses(getCalculateText() + "=")));
                                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                            } else {
                                // Handle calculation when equals sign is present
                                if (!getCalculateText().replace("=", "").replace(" ", "").matches("^(sin|cos|tan)\\(.*\\)$")) {
                                    if (!getLastOp().isEmpty() && !getLastOp().equals("√")) {
                                        setCalculateText(addSpaceToOperators(getResultText() + getLastOp() + getLastNumber() + "="));
                                    } else {
                                        setCalculateText(addSpaceToOperators(getResultText() + "="));
                                    }
                                    setCalculateText(addSpaceToOperators(balanceParentheses(getCalculateText())));
                                    setResultText(CalculatorEngine.calculate(balanceParentheses(getResultText() + " " + getLastOp() + " " + getLastNumber())));
                                } else {
                                    setCalculateText(balanceParentheses(getCalculateText()));
                                    setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                                }
                            }
                        } else {
                            if (!calcText.contains("=")) {
                                // Handle calculation when equals sign is not present
                                setCalculateText(addSpaceToOperators(balanceParentheses(getCalculateText() + getResultText() + "=")));
                                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                            } else {
                                // Handle calculation when equals sign is present
                                if (!getCalculateText().replace("=", "").replace(" ", "").matches("^(sin|cos|tan)\\(.*\\)$")) {
                                    int nonNumericIndex = 0;
                                    while (nonNumericIndex < getCalculateText().length() && Character.isDigit(getCalculateText().charAt(nonNumericIndex))) {
                                        nonNumericIndex++;
                                    }

                                    setCalculateText(getResultText() + getCalculateText().substring(nonNumericIndex));
                                } else {
                                    setCalculateText(addSpaceToOperators(balanceParentheses(getCalculateText())));
                                }

                                setResultText(CalculatorEngine.calculate(balanceParentheses(getCalculateText())));
                            }
                        }

                        // Replace special characters back for displaying
                        setCalculateText(addSpaceToOperators(getCalculateText().replace("*", "×").replace("/", "÷")));

                        // Save history, update UI, and set removeValue flag
                        dataManager.saveNumbers(getApplicationContext());
                    } else {
                        // Handle invalid input by resetting result text and calculate text
                        setResultText("0");
                        setCalculateText("");
                    }

                    // Reset rotate operator flag, format result text, adjust text size, and scroll to bottom if necessary
                    setRotateOperator(false);
                    setRemoveValue(true);
                    setCalculateText(addSpaceToOperators(replacePiWithSymbolInString(getCalculateText())));
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        formatResultTextAfterType();


        if (!isNumber(getCalculateText()) &&
                (!getCalculateText().replace("=", "").replace(" ", "").equals("π") ||
                        !getCalculateText().replace("=", "").replace(" ", "").equals("e"))
                && !isInvalidInput(getResultText())) {

            addToHistoryAfterCalculate(balanceParentheses(getCalculateText()));
        }

        try {
            if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht") &&
                    !isInvalidInput(getResultText())) {
                dataManager.saveToJSONSettings("pressedCalculate", true, getApplicationContext());
                setCalculateText("");

                if (dataManager.getJSONSettingsData("calculationMode", getApplicationContext()).getString("value").equals("Vereinfacht")) {
                    scrollToEnd(findViewById(R.id.calculate_scrollview));
                    scrollToStart(findViewById(R.id.result_scrollview));
                } else {
                    scrollToBottom(findViewById(R.id.calculate_scrollview));
                    scrollToBottom(findViewById(R.id.result_scrollview));
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) findViewById(R.id.calculate_scrollview).getLayoutParams();
                layoutParams.weight = 1.5F;
                findViewById(R.id.calculate_scrollview).setLayoutParams(layoutParams);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String replacePiWithSymbolInString(String text) {
        try {
            if (dataManager.getJSONSettingsData("refactorPI", getApplicationContext()).getString("value").equals("true")) {
                boolean isPI = false;
                int start, end;
                int l, m, n;

                for (l = 0; l < text.length(); l++) {
                    if (!(l + 3 < text.length())) {
                        break;
                    }

                    isPI = text.startsWith("3,14", l);
                    if (isPI) {
                        start = l;
                        for (m = 0; m < PI.length(); m++) {
                            if (l + m >= text.length() || !String.valueOf(PI.charAt(m)).equals(String.valueOf(text.charAt(l + m)))) {
                                for (n = l + m; n < text.length(); n++) {
                                    if (!Character.isDigit(text.charAt(n))) {
                                        break;
                                    }
                                }
                                end = n;
                                String partBefore = text.substring(0, start);
                                String partAfter = text.substring(end);
                                text = partBefore + "π" + partAfter;
                                isPI = false;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return text;
    }

    public static boolean isNumber(String input) {
        String numberPattern = "^-?\\d+(\\,|\\.)?\\d*(\\.|\\,)?\\d*$";
        Pattern pattern = Pattern.compile(numberPattern);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    private void addToHistoryAfterCalculate(String input) {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd. MMMM yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(calendar.getTime());

        input = fixExpression(input.replace("=", "").replace(" ", ""));

        // Code snippet to save calculation to history
        final Context context1 = getApplicationContext();
        String finalInput = input;
        new Thread(() -> runOnUiThread(() -> {
            final String value;
            final int old_value;
            try {
                value = dataManager.getHistoryData("historyTextViewNumber", context1).getString("value");
                old_value = Integer.parseInt(dataManager.getHistoryData("historyTextViewNumber", context1).getString("value"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            final int new_value = old_value + 1;

            dataManager.saveToHistory("historyTextViewNumber", Integer.toString(new_value), context1);
            String calculate_text = finalInput;

            if (calculate_text.isEmpty()) {
                calculate_text = "0";
            }
            if (!calculate_text.contains("=")) {
                calculate_text = calculate_text + "=";
            }

            dataManager.saveToHistory(String.valueOf(old_value + 1), formattedDate, "",
                    addSpaceToOperators(balanceParentheses(calculate_text) + getResultText()), context1);
        })).start();
    }


    private void addToHistory(String input) {
        if (!isInvalidInput(input)) {
            input = fixExpression(input.replace(" ", "").replace("=", ""));
        }

        // Code snippet to save calculation to history
        String finalInput = input;
        new Thread(() -> runOnUiThread(() -> {
            try {
                final int old_value;
                old_value = Integer.parseInt(dataManager.getHistoryData("historyTextViewNumber", getApplicationContext()).getString("value"));
                final int new_value = old_value + 1;

                dataManager.saveToHistory("historyTextViewNumber", Integer.toString(new_value), getApplicationContext());
                String calculate_text = finalInput;

                if (calculate_text.isEmpty()) {
                    calculate_text = "0";
                }

                if (!calculate_text.contains("=")) {
                    calculate_text = calculate_text + "=";
                }
                dataManager.saveToJSONSettings(String.valueOf(old_value + 1), addSpaceToOperators(balanceParentheses(calculate_text)), getApplicationContext());
                //Log.i("Calculate", "historyTextViewNumber: " + dataManager.getHistoryData("historyTextViewNumber", getApplicationContext()).getString("value"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        })).start();
    }


    public boolean isInvalidInput(String text) {
        return text.equals(getString(R.string.errorMessage1)) ||
                text.equals(getString(R.string.errorMessage2)) ||
                text.equals(getString(R.string.errorMessage3)) ||
                text.equals(getString(R.string.errorMessage4)) ||
                text.equals(getString(R.string.errorMessage5)) ||
                text.equals(getString(R.string.errorMessage6)) ||
                text.equals(getString(R.string.errorMessage7)) ||
                text.equals(getString(R.string.errorMessage8)) ||
                text.equals(getString(R.string.errorMessage9)) ||
                text.equals(getString(R.string.errorMessage10)) ||
                text.equals(getString(R.string.errorMessage11)) ||
                text.equals(getString(R.string.errorMessage12)) ||
                text.equals(getString(R.string.errorMessage13)) ||
                text.equals(getString(R.string.errorMessage14)) ||
                text.equals(getString(R.string.errorMessage15)) ||
                text.equals(getString(R.string.errorMessage16)) ||
                text.equals(getString(R.string.errorMessage17)) ||

                text.contains(getString(R.string.errorMessage1)) ||
                text.contains(getString(R.string.errorMessage2)) ||
                text.contains(getString(R.string.errorMessage3)) ||
                text.contains(getString(R.string.errorMessage4)) ||
                text.contains(getString(R.string.errorMessage5)) ||
                text.contains(getString(R.string.errorMessage6)) ||
                text.contains(getString(R.string.errorMessage7)) ||
                text.contains(getString(R.string.errorMessage8)) ||
                text.contains(getString(R.string.errorMessage9)) ||
                text.contains(getString(R.string.errorMessage10)) ||
                text.contains(getString(R.string.errorMessage11)) ||
                text.contains(getString(R.string.errorMessage12)) ||
                text.contains(getString(R.string.errorMessage13)) ||
                text.contains(getString(R.string.errorMessage14)) ||
                text.contains(getString(R.string.errorMessage15)) ||
                text.contains(getString(R.string.errorMessage16)) ||
                text.contains(getString(R.string.errorMessage17));
    }

    public void formatResultTextAfterType() {
        // Get the result text
        String text = getResultText();

        Log.d("formatResultTextAfterType", "formatResultTextAfterType: "+ text);



        // Check if result text is not null
        if (text != null && !isInvalidInput(text)) {

            // Check if the number is negative
            boolean isNegative = text.startsWith("-");
            if (isNegative) {
                // If negative, remove the negative sign for further processing
                text = text.substring(1);
            }

            // Check for scientific notation
            if (text.toLowerCase().matches(".*[eE].*")) {
                try {
                    // Convert scientific notation to BigDecimal with increased precision
                    Log.d("formatResultTextAfterType", "scientific : notation " + text.replace(".", "").replace(",", "."));
                    BigDecimal bigDecimalResult = new BigDecimal(text.replace(".", "").replace(",", "."), MathContext.DECIMAL128);
                    String formattedNumber = bigDecimalResult.toPlainString();
                    Log.d("formatResultTextAfterType", "formattedNumber: " + formattedNumber);
                    formattedNumber = formattedNumber.replace(".", ",");
                    Log.d("formatResultTextAfterType", "formattedNumber: " + formattedNumber);

                    // Extract exponent part and shift decimal point accordingly
                    String[] parts = formattedNumber.split("[eE]");
                    if (parts.length == 2) {
                        int exponent = Integer.parseInt(parts[1]);
                        String[] numberParts = parts[0].split(",");
                        if (exponent < 0) {
                            // Shift decimal point to the left, allowing up to 9 positions
                            int shiftIndex = Math.min(numberParts[0].length() + exponent, 9);
                            formattedNumber = numberParts[0].substring(0, shiftIndex) + "." +
                                    numberParts[0].substring(shiftIndex) + numberParts[1] + "e" + exponent;
                        } else {
                            // Shift decimal point to the right
                            int shiftIndex = Math.min(numberParts[0].length() + exponent, numberParts[0].length());
                            formattedNumber = numberParts[0].substring(0, shiftIndex) + "." +
                                    numberParts[0].substring(shiftIndex) + numberParts[1];
                        }
                    }

                    // Add negative sign if necessary and set the result text
                    if (isNegative) {
                        formattedNumber = "-" + formattedNumber;
                    }
                    Log.d("formatResultTextAfterType", "formatResultTextAfterType final check on sici: "+ formattedNumber.replace("E", "e"));
                    setResultText(formattedNumber.replace("E", "e"));


                    formatResultTextAfterType();
                    return;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format: " + text);
                }
            }

            // Handle non-scientific notation
            Log.d("formatResultTextAfterType", "formatResultTextAfterType: non-scientific");
            int index = text.indexOf(',');
            String result;
            String result2;
            if (index != -1) {
                // Split the text into integral and fractional parts
                result = text.substring(0, index).replace(",", "");
                result2 = text.substring(index);
            } else {
                result = text.replace(",", "");
                result2 = "";
            }

            Log.d("formatResultTextAfterType", "result2: "+ result2);
            // Check for invalid input
            if (!isInvalidInput(getResultText())) {
                // Format the integral part using DecimalFormat
                DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
                // default: india
                symbols.setDecimalSeparator('.');
                symbols.setGroupingSeparator(',');


                DecimalFormat decimalFormat = new DecimalFormat("##,##,##,##,##,###.###", symbols);
                //decimalFormat.setGroupingUsed(false);
                try {
                    BigDecimal bigDecimalResult1 = new BigDecimal(result, MathContext.DECIMAL128);
                    String formattedNumber1 = decimalFormat.format(bigDecimalResult1);

                    // Set the result text with formatted numbers
                    Log.d("formatResultTextAfterType", "setResultText: "+ (isNegative ? "-" : "") + formattedNumber1 + result2);
                    setResultText((isNegative ? "-" : "") + formattedNumber1 + result2);
                } catch (NumberFormatException e) {
                    // Handle invalid number format in the integral part
                    System.out.println("Invalid number format: " + result);
                }
            } else if (getIsNotation()) {
                // Reset scientific notation flag if needed
                setIsNotation(false);
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        finishActivity(1);
    }


    private void checkCalculateText() {
        if (getCalculateText().contains("=")) {
            setCalculateText("");
            if (isInvalidInput(getResultText())) {
                setResultText("0");
            }
            setRemoveValue(true);
        }
    }

    public void setIsNotation(final boolean val) {
        dataManager.saveToJSONSettings("isNotation", val, getApplicationContext());
        //("setIsNotation", "isNotation: '" + val + "'");
    }

    public boolean getIsNotation() {
        try {
            return Boolean.parseBoolean(dataManager.getJSONSettingsData("isNotation", getApplicationContext()).getString("value"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRotateOperator(final boolean rotate) {
        dataManager.saveToJSONSettings("rotate_op", rotate, getApplicationContext());
        /*
        try {
            Log.i("setRotateOperator", "rotate_op: '" + dataManager.getJSONSettingsData("rotate_op", getApplicationContext()).getString("value") + "'");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        */
    }

    public boolean getRotateOperator() {
        try {
            //Log.i("setRotateOperator", "rotate_op: '" + dataManager.getJSONSettingsData("rotate_op", getApplicationContext()) + "'");
            return Boolean.parseBoolean(dataManager.getJSONSettingsData("rotate_op", getApplicationContext()).getString("value"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLastOp() {
        final String last_op;
        try {
            last_op = dataManager.getJSONSettingsData("lastop", getApplicationContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return last_op.replace("*", "×").replace("/", "÷");
    }

    public void setLastOp(final String s) {
        dataManager.saveToJSONSettings("lastop", s, getApplicationContext());
        /*
        try {
            Log.i("setLastOp", "lastOp: " + dataManager.getJSONSettingsData("lastop", getApplicationContext()).getString("value"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        */
    }

    public boolean getRemoveValue() {
        final String value;
        try {
            value = dataManager.getJSONSettingsData("removeValue", getApplicationContext()).getString("value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        dataManager.saveToJSONSettings("removeValue", "false", getApplicationContext());
        return value.equals("true");
    }

    public void setRemoveValue(final boolean b) {
        dataManager.saveToJSONSettings("removeValue", b, getApplicationContext());
        /*
        try {
            Log.i("setRemoveValue", "removeValue: " + dataManager.getJSONSettingsData("removeValue", getApplicationContext()).getString("value"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        */
    }

    public void setLastNumber(final String s) {
        //try {
        final String last_number = s.replace(".", "");
        dataManager.saveToJSONSettings("lastnumber", last_number, getApplicationContext());
        //Log.i("setLastNumber", "lastNumber: " + dataManager.getJSONSettingsData("lastnumber", getApplicationContext()));
        //} catch (JSONException e) {
        //    throw new RuntimeException(e);
        //}
    }

    public String getLastNumber() {
        try {
            final String last_number = dataManager.getJSONSettingsData("lastnumber", getApplicationContext()).getString("value");
            dataManager.saveToJSONSettings("lastnumber", "0", getApplicationContext());
            return last_number;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getResultText() {
        TextView resulttext = findViewById(R.id.result_label);
        if (resulttext != null) {
            return resulttext.getText().toString();
        }
        return null;
    }

    @SuppressLint("SetTextI18n")
    public void addResultText(final String s) {
        TextView resulttext = findViewById(R.id.result_label);
        if (resulttext != null) {
            resulttext.setText(getResultText() + s);
        }
    }

    public void setResultText(final String s) {
        TextView resulttext = findViewById(R.id.result_label);
        if (resulttext != null) {
            Log.d("calculate", "setResultText: "+ s);
            resulttext.setText(s);
            scrollToEnd(findViewById(R.id.result_scrollview));
        }
    }

    public String getCalculateText() {
        if (findViewById(R.id.calculate_label) != null) {
            TextView calculatetext = findViewById(R.id.calculate_label);
            return calculatetext.getText().toString();
        } else {
            return "";
        }
    }

    @SuppressLint("SetTextI18n")
    public void addCalculateText(final String s) {
        TextView calculatetext = findViewById(R.id.calculate_label);
        calculatetext.setText(getCalculateText() + " " + s);
    }

    @SuppressLint("SetTextI18n")
    public void addCalculateTextWithoutSpace(final String s) {
        TextView calculatetext = findViewById(R.id.calculate_label);
        calculatetext.setText(getCalculateText() + s);
    }

    public void setCalculateText(final String s) {
        if (findViewById(R.id.calculate_label) != null) {
            TextView calculatetext = findViewById(R.id.calculate_label);
            calculatetext.setText(s);
        }
    }
}