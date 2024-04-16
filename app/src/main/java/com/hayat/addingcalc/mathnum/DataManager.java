package com.hayat.addingcalc.mathnum;



import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



public class DataManager {
    private MainActivity mainActivity;
    private static final String JSON_FILE = "settings.json";
    static final String HISTORY_FILE = "history.json";
    
    public DataManager(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    
    public void saveToJSONSettings(String name, String value, Context applicationContext) {
        JSONObject jsonObj = new JSONObject();
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e("saveToHistory", "Failed to create new file");
                    return;
                }
            }
            String content = new String(Files.readAllBytes(file.toPath()));
            if (!content.isEmpty()) {
                jsonObj = new JSONObject(new JSONTokener(content));
            }

            JSONObject dataObj = new JSONObject();
            dataObj.put("value", value);

            jsonObj.put(name, dataObj);

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonObj.toString());
                fileWriter.flush();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveToJSONSettings(String name, boolean value, Context applicationContext) {
        JSONObject jsonObj = new JSONObject();
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e("saveToHistory", "Failed to create new file");
                    return;
                }
            }
            String content = new String(Files.readAllBytes(file.toPath()));
            if (!content.isEmpty()) {
                jsonObj = new JSONObject(new JSONTokener(content));
            }

            JSONObject dataObj = new JSONObject();
            dataObj.put("value", String.valueOf(value));

            jsonObj.put(name, dataObj);

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonObj.toString());
                fileWriter.flush();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJSONSettingsData(String name, Context applicationContext) throws JSONException {
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                if (jsonObj.has(name)) {
                    return jsonObj.getJSONObject(name);
                } else {
                    Log.e("getDataForName", "Data with name " + name + " not found. Trying to Create ...");
                }
            } else {
                Log.e("getDataForName", "JSON file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

  
    



    public void addValueWithCustomNameToJSONSettings(String name, String valueName, String value, Context applicationContext) {
        try {
            File file = new File(applicationContext.getFilesDir(), JSON_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                JSONObject dataObj;
                if (jsonObj.has(name)) {
                    dataObj = jsonObj.getJSONObject(name);
                } else {
                    dataObj = new JSONObject();
                }

                if (!dataObj.has(valueName)) {
                    dataObj.put(valueName, value);
                    jsonObj.put(name, dataObj);

                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(jsonObj.toString());
                    fileWriter.flush();
                    fileWriter.close();

                    Log.d("addValueWithCustomName", "Value " + value + " with name " + valueName + " added successfully to " + name + ".");
                } else {
                    Log.d("addValueWithCustomName", "Value with name " + valueName + " already exists for " + name + ".");
                }
            } else {
                Log.e("addValueWithCustomName", "JSON file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

 
    public void initializeSettings(Context applicationContext) {
        try {
            initializeSetting("selectedSpinnerSetting", "System", applicationContext);
            initializeSetting("functionMode", "Deg", applicationContext);
            initializeSetting("settingReleaseNotesSwitch", "true", applicationContext);
            initializeSetting("removeValue", "false", applicationContext);
            initializeSetting("settingsTrueDarkMode", "false", applicationContext);
            initializeSetting("showScienceRow", "false", applicationContext);
            initializeSetting("rotate_op", "false", applicationContext);
            initializeSetting("lastnumber", "0", applicationContext);
            initializeSetting("historyTextViewNumber", "0", applicationContext);
            initializeSetting("result_text", "0", applicationContext);
            initializeSetting("calculate_text", "", applicationContext);
            initializeSetting("lastop", "+", applicationContext);
            initializeSetting("isNotation", "false", applicationContext);
            initializeSetting("eNotation", "false", applicationContext);
            initializeSetting("showShiftRow", "false", applicationContext);
            initializeSetting("showPatchNotes", "false", applicationContext);
            initializeSetting("shiftRow", "1", applicationContext);
            initializeSetting("logX", "false", applicationContext);
            initializeSetting("calculationMode", "Vereinfacht", applicationContext);
            initializeSetting("currentVersion", "1.6.4", applicationContext);
            initializeSetting("old_version", "0", applicationContext);
            initializeSetting("returnToCalculator", "false", applicationContext);
            initializeSetting("allowNotification", "false", applicationContext);
            initializeSetting("allowDailyNotifications", "false", applicationContext);
            initializeSetting("allowRememberNotifications", "false", applicationContext);
            initializeSetting("allowDailyNotificationsActive", "true", applicationContext);
            initializeSetting("allowRememberNotificationsActive", "true", applicationContext);
            initializeSetting("notificationSent", "false", applicationContext);
            initializeSetting("pressedCalculate", "false", applicationContext);
            initializeSetting("refactorPI", "true", applicationContext);
            initializeSetting("historyMode", "single", applicationContext);
            initializeSetting("dayPassed", "true", applicationContext);
            initializeSetting("convertMode", "Entfernung", applicationContext);
            initializeSetting("numberOfDecimals", "2", applicationContext);
            initializeSetting("showConverterDevelopmentMessage", "true", applicationContext);
            initializeSetting("report", "", applicationContext);
            initializeSetting("lastActivity", "Main", applicationContext);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeSetting(String key, String defaultValue, Context applicationContext) throws JSONException {
        if (getJSONSettingsData(key, applicationContext) == null) {
            if(key.equals("historyTextViewNumber")) {
                saveToHistory(key, defaultValue, applicationContext);
            }
            saveToJSONSettings(key, defaultValue, applicationContext);

            if(key.equals("convertMode")) {
                addValueWithCustomNameToJSONSettings("convertMode", "WinkelCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "FlächeCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "SpeicherCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "EntfernungCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "VolumenCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "MasseGewichtCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "ZeitCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "TemperaturCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "StromSpannungCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "StromStärkeCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "GeschwindigkeitCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "EnergieCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "DruckCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "DrehmomentCurrent", "0", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "ArbeitCurrent", "0", applicationContext);

                addValueWithCustomNameToJSONSettings("convertMode", "WinkelNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "FlächeNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "SpeicherNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "EntfernungNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "VolumenNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "MasseGewichtNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "ZeitNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "TemperaturNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "StromSpannungNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "StromStärkeNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "GeschwindigkeitNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "EnergieNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "DruckNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "DrehmomentNumber",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("convertMode", "ArbeitNumber",  "", applicationContext);
            } else if(key.equals("report")) {
                addValueWithCustomNameToJSONSettings("report", "name",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("report", "title",  "", applicationContext);
                addValueWithCustomNameToJSONSettings("report", "text",  "", applicationContext);
            }
        }
    }


    public void saveNumbers(Context applicationContext) {
        if (mainActivity != null) {
            try {
                // Save calculate_text using dataManager
                saveToJSONSettings("calculate_text", mainActivity.getCalculateText(), applicationContext);

                // Save result_text using dataManager
                saveToJSONSettings("result_text", mainActivity.getResultText(), applicationContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method loads numbers from two files and sets the text of two TextViews.
     */
    public void loadNumbers() throws JSONException {
        if (mainActivity != null) {
            JSONObject calculateText = getJSONSettingsData("calculate_text", mainActivity.getApplicationContext());
            JSONObject resultText = getJSONSettingsData("result_text", mainActivity.getApplicationContext());

            TextView calculatelabel = mainActivity.findViewById(R.id.calculate_label);
            TextView resultlabel = mainActivity.findViewById(R.id.result_label);

            if (calculatelabel != null && resultlabel != null) {
                try {
                    calculatelabel.setText(calculateText.getString("value"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                try {
                    final String value = String.valueOf(resultText.getString("value").isEmpty());
                    if (!value.isEmpty()) {
                        try {
                            resultlabel.setText(resultText.getString("value"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        resultlabel.setText("0");
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    public void saveToHistory(String name, String date, String details, String calculation, Context applicationContext) {
        JSONObject jsonObj = new JSONObject();
        try {
            File file = new File(applicationContext.getFilesDir(), HISTORY_FILE);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e("saveToHistory", "Failed to create new file");
                    return;
                }
            }
            String content = new String(Files.readAllBytes(file.toPath()));
            if (!content.isEmpty()) {
                jsonObj = new JSONObject(new JSONTokener(content));
            }

            JSONObject dataObj = new JSONObject();
            dataObj.put("date", date);
            dataObj.put("details", details);
            dataObj.put("calculation", calculation);

            jsonObj.put(name, dataObj);

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonObj.toString());
                fileWriter.flush();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveToHistory(String name, String value, Context applicationContext) {
        JSONObject jsonObj = new JSONObject();
        try {
            File file = new File(applicationContext.getFilesDir(), HISTORY_FILE);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e("saveToHistory", "Failed to create new file");
                    return;
                }
            }
            String content = new String(Files.readAllBytes(file.toPath()));
            if (!content.isEmpty()) {
                jsonObj = new JSONObject(new JSONTokener(content));
            }

            JSONObject dataObj = new JSONObject();
            dataObj.put("value", value);

            jsonObj.put(name, dataObj);

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(jsonObj.toString());
                fileWriter.flush();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getHistoryData(String name, Context applicationContext) {
        try {
            File file = new File(applicationContext.getFilesDir(), HISTORY_FILE);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObj = new JSONObject(new JSONTokener(content));

                if (jsonObj.has(name)) {
                    return jsonObj.getJSONObject(name);
                } else {
                    //Log.e("getDataForName", "Data with name " + name + " not found.");
                }
            } else {
                //Log.e("getDataForName", "JSON file not found.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


}