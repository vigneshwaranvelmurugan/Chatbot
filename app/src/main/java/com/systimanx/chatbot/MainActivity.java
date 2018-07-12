package com.systimanx.chatbot;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener, RecognitionListener {
    ImageView send,mic;
    EditText editmessage;
    private TextToSpeech tts;
    DatabaseReference ref;
    RecyclerView recylerview;
    ArrayList<String> keyarray=new ArrayList<String>();
    ArrayList<chatmodel> chatarray=new ArrayList<chatmodel>();
    RecyclerView.Adapter adapter;
     AIRequest aiRequest;
     AIDataService aiDataService;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    int in=0;
    String text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editmessage=(EditText)findViewById(R.id.editmessage);
        recylerview=(RecyclerView)findViewById(R.id.recylerview);
        recylerview.setHasFixedSize(true);


        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        recylerview.setLayoutManager(layoutManager);

        ref = FirebaseDatabase.getInstance().getReference();
        ref.keepSynced(true);
        send=(ImageView) findViewById(R.id.send);
        mic=(ImageView) findViewById(R.id.voicesend);

        //Configure with AIConfiguration
        final AIConfiguration config = new AIConfiguration("54b939c578ec494381140c1ef06ad706",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(this ,config);

        //send request in AIConfiguration
         aiRequest = new AIRequest();

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
            }
        });

        editmessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length()!=0){
                    mic.setVisibility(View.GONE);
                    send.setVisibility(View.VISIBLE);

                }
                else if (charSequence.toString().trim().length()==0){
                    send.setVisibility(View.GONE);
                    mic.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length()!=0){
                   mic.setVisibility(View.GONE);
                    send.setVisibility(View.VISIBLE);

                }
                else if (charSequence.toString().trim().length()==0){
                    send.setVisibility(View.GONE);
                    mic.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        recognizerIntent.putExtra("android.speech.extra.DICTATION_MODE", true);

        recordpermision();
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                speech.startListening(recognizerIntent);//text spech recognize
                mic.setEnabled(false);
                in=1;

            }
        });


        firebasedataread();



        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (editmessage.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"Enter Message",Toast.LENGTH_SHORT).show();
                }
                else {

                    final String message=editmessage.getText().toString();
                    sendmessage(message);//send request in api.ai server

                }

            }
        });







    }

    @Override
    public void onResult(AIResponse result) {

//get result in api.ai
        Result result1 = result.getResult();
        String message = result1.getResolvedQuery();
        System.out.println("message"+message);
        String reply = result1.getFulfillment().getSpeech();
        System.out.println("replay"+reply);

    }

    @Override
    public void onError(AIError error) {
        System.out.println("result"+error);

    }

    @Override
    public void onAudioLevel(float level) {
        System.out.println("result"+"level");

    }

    @Override
    public void onListeningStarted() {
        System.out.println("result"+"start");

    }

    @Override
    public void onListeningCanceled() {
        System.out.println("result"+"cancell");

    }

    @Override
    public void onListeningFinished() {
        System.out.println("result"+"finish");


    }




    private void sendmessage(final String usermesage){//send usermessge in api.ai

        final String message=editmessage.getText().toString();
        aiRequest.setQuery(usermesage);//send edittext text
        final chatmodel chatmodel = new chatmodel(usermesage, "user");
        chatarray.add(new chatmodel(usermesage, "user"));
        ref.child("chat").push().setValue(chatmodel);//store usermessge in firebase
        editmessage.setText("");
        final RecyclerView.Adapter adapter = new Chatadpter(chatarray, new Chatadpter.customerlistadapterListner() {
            @Override
            public void robotext(int position) {



            }
        });
        recylerview.setAdapter(adapter);


        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }
            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {

                    if (aiResponse != null) {

                        Result result = aiResponse.getResult();//get responce in api.ai
                        String reply = result.getFulfillment().getSpeech();
                        System.out.println("replay"+reply);
                          tts.speak(reply, TextToSpeech.QUEUE_ADD, null);//spech text
                        if (reply.equals("")){
                            reply="Sorry, can you say that again?";
                        }



                        final chatmodel chatmodel = new chatmodel(reply, "robot");
                        ref.child("chat").push().setValue(chatmodel);//store servermessge in firebase

                        firebasedataread();



                    }
                }
            }
        }.execute(aiRequest);


    }
    public void firebasedataread(){
        chatarray.clear();
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef = database.child("chat");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){


                    chatarray.add(new chatmodel(singleSnapshot.child("msgText").getValue().toString(),singleSnapshot.child("msgUser").getValue().toString()));//get firebase data

                    keyarray.add(singleSnapshot.getKey());

                    System.out.println("keyarray"+singleSnapshot.getKey());
                }
              adapter = new Chatadpter(chatarray,new Chatadpter.customerlistadapterListner() {
                    @Override
                    public void robotext(final int position) {


                   System.out.println("keyyarraypostion"+keyarray.get(position));



                    }
                });
                recylerview.setAdapter(adapter);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
            Log.d("Log", "destroy");
        }

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d("Log", "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d("Log", "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.d("Log", "onEndOfSpeech");
        mic.setEnabled(true);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d("Log", "FAILED " + errorMessage);
        Toast.makeText(getApplicationContext(),errorMessage,Toast.LENGTH_SHORT).show();
        mic.setEnabled(true);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.d("Log", "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.d("Log", "onPartialResults");

        ArrayList<String> matches = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";

        text = matches.get(0);
        sendmessage(text);

    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.d("Log", "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.d("Log", "onResults");

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.d("Log", "onRmsChanged: " + rmsdB);

    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    public void recordpermision() {
        try {
            if (Build.VERSION.SDK_INT > 22) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);
                    return;
                }
            }
            else if (Build.VERSION.SDK_INT<22){
                return;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
