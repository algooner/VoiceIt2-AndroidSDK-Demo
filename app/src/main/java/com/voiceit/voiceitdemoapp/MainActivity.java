package com.voiceit.voiceitdemoapp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import cz.msebera.android.httpclient.Header;

import org.json.JSONException;
import org.json.JSONObject;

import com.voiceit.voiceit2.VoiceItAPI2;

public class MainActivity extends AppCompatActivity {

    private VoiceItAPI2 myVoiceIt2 = new VoiceItAPI2("CHANGEME",
            "CHANGEME");
    public String userId = "";
    private String phrase = "never forget tomorrow is a new day";
    private String contentLanguage = "no-STT";
    private boolean doLivenessCheck = true; // Liveness detection not used for enrollment views

    SharedPreferences sharedPref;
    SharedPreferences.Editor prefEditor;

    TextView displayText;
    TextView createDeleteUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide action bar
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
            Log.d("","Cannot hide action bar");
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        prefEditor = sharedPref.edit();

        displayText = findViewById(R.id.displayText);
        createDeleteUserButton = findViewById(R.id.createDeleteUserButton);

        phrase = sharedPref.getString("phrase", phrase);
        String ip = sharedPref.getString("ipAddress", "Null");
        if(!ip.equals("Null")) {
            myVoiceIt2.setURL(ip);
        }

        userId = sharedPref.getString("userId", "Null");
        if(userId.equals("Null")) {
            createDeleteUserButton.setText("Create User");
            displayText.setText("No userId found");
            enableButtons(false);
        } else {
            createDeleteUserButton.setText("Delete User");
            displayText.setText("Created user: " + userId);
            enableButtons(true);
        }

    }

    public void settingsButton(View view) {
        AlertDialog.Builder alert;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alert = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            alert = new AlertDialog.Builder(this);
        }

        alert.setTitle("Server IP Address");

        String ip = sharedPref.getString("ipAddress", "Null");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);

        if(!ip.equals("Null")) {
            input.setText(ip);
        }
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                myVoiceIt2.setURL(input.getText().toString());
                prefEditor.putString("ipAddress", input.getText().toString());
                prefEditor.commit();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing
            }
        });

        alert.show();
    }

    public void phraseButton(View view) {
        AlertDialog.Builder alert;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alert = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            alert = new AlertDialog.Builder(this);
        }

        alert.setTitle("Voice Print Phrase");

        phrase = sharedPref.getString("phrase", phrase);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);

        if(!phrase.equals("Null")) {
            input.setText(phrase);
        }
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                prefEditor.putString("phrase", input.getText().toString());
                prefEditor.commit();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing
            }
        });

        alert.show();
    }

    private void enableButtons(boolean enabled) {
        findViewById(R.id.voiceEnrollmentButton).setEnabled(enabled);
        findViewById(R.id.faceEnrollmentButton).setEnabled(enabled);
        findViewById(R.id.videoEnrollmentButton).setEnabled(enabled);
        findViewById(R.id.voiceVerificationButton).setEnabled(enabled);
        findViewById(R.id.FaceVerificationButton).setEnabled(enabled);
        findViewById(R.id.VideoVerificationButton).setEnabled(enabled);
    }

    public void createOrDeleteUser(View view) {
        if(userId.equals("Null")) {
            myVoiceIt2.createUser(new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("createUser Result : " + response.toString());

                    try {
                        userId = response.getString("userId");
                        enableButtons(true);
                        displayText.setText("Created userId: " + userId);
                        createDeleteUserButton.setText("Delete User");
                        prefEditor.putString("userId", userId);
                        prefEditor.commit();
                    } catch (JSONException e) {
                        Log.e("createUser exception", response.toString());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (errorResponse != null) {
                        System.out.println("createUser Result : " + errorResponse.toString());
                        enableButtons(false);
                        displayText.setText(" ");
                        createDeleteUserButton.setText("Create User");
                        prefEditor.putString("userId", "Null");
                        prefEditor.commit();
                        userId = "Null";
                    } else {
                        displayText.setText("Check internet connection");
                    }
                }
            });
        } else {
            myVoiceIt2.deleteUser(userId, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("deleteUser Result : " + response.toString());

                    enableButtons(false);
                    displayText.setText("Deleted userId: " + userId);
                    createDeleteUserButton.setText("Create User");
                    prefEditor.putString("userId", "Null");
                    prefEditor.commit();
                    userId = "Null";
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    if (errorResponse != null) {
                        System.out.println("deleteUser Result : " + errorResponse.toString());
                        enableButtons(false);
                        displayText.setText(" ");
                        createDeleteUserButton.setText("Create User");
                        prefEditor.putString("userId", "Null");
                        prefEditor.commit();
                        userId = "Null";
                    } else {
                        displayText.setText("Check internet connection");
                    }
                }
            });
        }
    }

    public void encapsulatedVoiceEnrollment(View view) {
        myVoiceIt2.encapsulatedVoiceEnrollment(this, userId, contentLanguage, phrase, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedVoiceEnrollment Result : " + response.toString());
                displayText.setText("encapsulatedVoiceEnrollment Result : " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedVoiceEnrollment Result : " + errorResponse.toString());
                    displayText.setText("encapsulatedVoiceEnrollment Result : " + errorResponse.toString());
                }
            }
        });
    }

    public void encapsulatedVoiceVerification(View view) {
        myVoiceIt2.encapsulatedVoiceVerification(this, userId, contentLanguage, phrase, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedVoiceVerification Result : " + response.toString());
                displayText.setText("encapsulatedVoiceVerification Result : " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedVoiceVerification Result : " + errorResponse.toString());
                    displayText.setText("encapsulatedVoiceVerification Result : " + errorResponse.toString());
                }
            }
        });
    }

    public void encapsulatedVideoEnrollment(View view) {
        myVoiceIt2.encapsulatedVideoEnrollment(this, userId, contentLanguage, phrase, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedVideoEnrollment Result : " + response.toString());
                displayText.setText("encapsulatedVideoEnrollment Result : " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedVideoEnrollment Result : " + errorResponse.toString());
                    displayText.setText("encapsulatedVideoEnrollment Result : " + errorResponse.toString());
                }
            }
        });
    }

    public void encapsulatedVideoVerification(View view) {
        myVoiceIt2.encapsulatedVideoVerification(this, userId, contentLanguage, phrase, doLivenessCheck, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedVideoVerification Result : " + response.toString());
                displayText.setText("encapsulatedVideoVerification Result : " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedVideoVerification Result : " + errorResponse.toString());
                    displayText.setText("encapsulatedVideoVerification Result : " + errorResponse.toString());
                }
            }
        });
    }

    public void encapsulatedFaceEnrollment(View view) {
        myVoiceIt2.encapsulatedFaceEnrollment(this, userId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedFaceEnrollment Result : " + response.toString());
                displayText.setText("encapsulatedFaceEnrollment Result : " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedFaceEnrollment Result : " + errorResponse.toString());
                    displayText.setText("encapsulatedFaceEnrollment Result : " + errorResponse.toString());
                }
            }
        });
    }

    public void encapsulatedFaceVerification(View view) {
        myVoiceIt2.encapsulatedFaceVerification(this, userId, doLivenessCheck, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("encapsulatedFaceVerification Result : " + response.toString());
                displayText.setText("encapsulatedFaceVerification Result : " + response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    System.out.println("encapsulatedFaceVerification Result : " + errorResponse.toString());
                    displayText.setText("encapsulatedFaceVerification Result : " + errorResponse.toString());
                }
            }
        });
    }
}
