package com.example.android.logindemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibm.cloud.sdk.core.http.Response;
import com.ibm.cloud.sdk.core.http.ServiceCall;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.MessageInput;
import com.ibm.watson.assistant.v2.model.MessageOptions;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;
import com.ibm.watson.assistant.v2.model.SessionResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private Button logout;
    private ArrayList messageArrayList;
    private Assistant watsonAssistant;
    private Response<SessionResponse> watsonAssistantSession;
    TextView messagesTextView;
    EditText inputEditText;
    Button sendButton;
    Context context;
    private ChatAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        messageArrayList = new ArrayList<>();
        mAdapter = new ChatAdapter(messageArrayList);
        recyclerView = findViewById(R.id.recycler_view);

        context = this;
        inputEditText = findViewById(R.id.inputEditText);
        sendButton = findViewById(R.id.sendButton);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = inputEditText.getText().toString();
                inputEditText.setText("");

                sendMessage(input);
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();

        //logout = (Button)findViewById(R.id.btnLogout);

        /*logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logout();
            }
        });*/

    }

    private void Logout(){
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(SecondActivity.this, MainActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.logoutMenu:{
                Logout();
                break;
            }
            case R.id.profileMenu:
                startActivity(new Intent(SecondActivity.this, ProfileActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    private void getResponse(String input) {

        IamAuthenticator authenticator = new IamAuthenticator("6pYZKMxTxWfXbWfPxcJVyNHuN8x7qMbHhz1r6yOiT3YP");
        Assistant assistant = new Assistant("v2", authenticator);
        assistant.setServiceUrl("https://api.eu-gb.assistant.watson.cloud.ibm.com/instances/2fa38d9d-d1ff-4621-a862-f579e25aa899");


        String workspaceId = "{workspaceId}";
        //String urlAssistant = "https://gateway.watsonplatform.net/assistant/api/v1/workspaces/"+workspaceId+"/message?version=2018-09-20";
        String urlAssistant = "https://api.eu-gb.assistant.watson.cloud.ibm.com/instances/2fa38d9d-d1ff-4621-a862-f579e25aa899";
        //String authentication = "{base64username:password}";
        String authentication = "bWFuYXNhLnNhbmdhbTc3QGdtYWlsLmNvbTpNYW5zYW5ANzA2Mjk3";

        //creo la estructura json de input del usuario
        JSONObject inputJsonObject = new JSONObject();
        try {
            inputJsonObject.put("text",input);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("input", inputJsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AndroidNetworking.post(assistant.getServiceUrl())
                .addHeaders("Content-Type","application/json")
                .addHeaders("Authorization","Basic " + authenticator.getApiKey())
                .addJSONObjectBody(jsonBody)
                .setPriority(Priority.HIGH)
                .setTag(getString(R.string.app_name))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String outputAssistant = "";

                        //parseo la respuesta del json
                        try {
                            String outputJsonObject = response.getJSONObject("output").getJSONArray("text").getString(0);
                            messagesTextView.append(Html.fromHtml("<p><b>Chatbot:</b> " + outputJsonObject + "</p>"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(context,"Error de conexión", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendMessage(String input){
        watsonAssistant = new Assistant("2022-09-19", new IamAuthenticator("6pYZKMxTxWfXbWfPxcJVyNHuN8x7qMbHhz1r6yOiT3YP"));
        watsonAssistant.setServiceUrl("https://api.eu-gb.assistant.watson.cloud.ibm.com/instances/2fa38d9d-d1ff-4621-a862-f579e25aa899");

        Message inputMessage = new Message();
        inputMessage.setMessage(input);
        inputMessage.setId("1");
        messageArrayList.add(inputMessage);

        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    if (watsonAssistantSession == null) {
                        ServiceCall<SessionResponse> call = watsonAssistant.createSession(new CreateSessionOptions.Builder().assistantId("6b811e19-b193-4799-b164-6f66e840724b").build());
                        watsonAssistantSession = call.execute();
                    }

                    MessageInput messageInput = new MessageInput.Builder()
                            .text(input)
                            .build();
                    MessageOptions options = new MessageOptions.Builder()
                            .assistantId("6b811e19-b193-4799-b164-6f66e840724b")
                            .input(messageInput)
                            .sessionId(watsonAssistantSession.getResult().getSessionId())
                            .build();
                    Response<MessageResponse> response = watsonAssistant.message(options).execute();
                    //Log.i(TAG, "run: " + response.getResult());
                    if (response != null &&
                            response.getResult().getOutput() != null &&
                            !response.getResult().getOutput().getGeneric().isEmpty()) {

                        List<RuntimeResponseGeneric> responses = response.getResult().getOutput().getGeneric();

                        for (RuntimeResponseGeneric r : responses) {
                            Message outMessage;


                            outMessage = new Message();
                            outMessage.setMessage(r.text());
                            outMessage.setId("2");
                            messageArrayList.add(outMessage);
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                                if (mAdapter.getItemCount() > 1) {
                                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);

                                }

                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}



