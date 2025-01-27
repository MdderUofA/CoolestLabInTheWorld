package com.example.lab_net;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;

/**
 * The User Profile activity that displays the user information,
 * as well as buttons for editing user info, browsing and creating experiments.
 *
 * @author Vidhi Patel, Qasim Akhtar
 */
public class UserProfile extends AppCompatActivity implements View.OnClickListener {

    public static final String EXPERIMENT_ID_EXTRA = "com.example.lab_net.experiment_activity.id";

    public static final  String USER_ID_EXTRA = "com.example.lab_net.user_profile.user_id";
    private final String Tag = "Sample";
    private String userId,firstNameText,lastNameText,emailText,phoneText;
    private FirebaseFirestore db;
    private DocumentReference documentReference;

    private String experimentId;

    private ImageButton editUser;
    private Button browse, addExp;
    private ListView subExpListView, myExpListView;
    private ArrayList<Experiment> myExperimentsDataList;
    private ArrayList<SubscribedExperiment> subscribedExperimentsDataList;
    private ArrayAdapter<Experiment> myExperimentAdapter;
    private ArrayAdapter<SubscribedExperiment> subscribedExperimentsAdapter;
    private TextView usernameTextView, firstNameTextView, lastNameTextView,emailTextView,phoneTextView;

    private String status;

    EditText expTitle, expDescription, expRegion, expMinTrials;
    Button create;
    String experimentTrialType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //initialize the database
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        userId = intent.getStringExtra(UserProfile.USER_ID_EXTRA);

        status = "open";
        documentReference = db.collection("UserProfile").document(userId);

        //user information variable
        usernameTextView = (TextView) findViewById(R.id.username);
        firstNameTextView = (TextView) findViewById(R.id.firstName);
        lastNameTextView = (TextView) findViewById(R.id.lastName);
        emailTextView = (TextView) findViewById(R.id.email);
        phoneTextView = (TextView) findViewById(R.id.phone);

        //my experiment variable
        myExpListView = findViewById(R.id.myExpListView);
        myExperimentsDataList = new ArrayList<>();
        myExperimentAdapter = new CustomMyExperimentsList(this, myExperimentsDataList);
        myExpListView.setAdapter(myExperimentAdapter);

        subExpListView = findViewById(R.id.subExpListView);
        subscribedExperimentsDataList = new ArrayList<>();
        subscribedExperimentsAdapter = new CustomSubscribedExperimentList(this, subscribedExperimentsDataList);
        subExpListView.setAdapter(subscribedExperimentsAdapter);

        getUserInfo();
        getMyExperiments();
        getSubscribedExperiments();

        //initialize the buttons
        editUser = (ImageButton) findViewById(R.id.editUserInfo);
        editUser.setOnClickListener(this);
        browse = (Button) findViewById(R.id.browseButton);
        browse.setOnClickListener(this);
        addExp = (Button) findViewById(R.id.addExpButton);
        addExp.setOnClickListener(this);

        myExpView();
        subExpView();

    }

    //Set buttons
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.editUserInfo:
                editUserDialog();
                break;

            case R.id.browseButton:
                Intent searchIntent = new Intent(this, SearchableListActivity.class);
                searchIntent.putExtra(SearchableList.SEARCHABLE_FILTER_EXTRA,
                        SearchableList.SEARCH_EXPERIMENTS);
                startActivity(searchIntent);
                break;

            case R.id.addExpButton:
                addExpDialog();
                break;

        }
    }

    /**
     * Get the User's subscribed Experiments
     */
    public void getSubscribedExperiments() {
        db.collection("SubscribedExperiments")
                .whereEqualTo("Subscriber", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            subscribedExperimentsDataList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getData().get("ExperimentId").toString();
                                String title = document.getData().get("ExperimentTitle").toString();
                                String subscriber = document.getData().get("Subscriber").toString();
                                String type = document.getData().get("TrialType").toString();
                                subscribedExperimentsDataList
                                        .add(new SubscribedExperiment(id,title,subscriber,type));
                            }
                            subscribedExperimentsAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    /**
     * Get experiments from the database that were created by the user.
     */
    public void getMyExperiments() {
        db.collection("Experiments")
                .whereEqualTo("Owner", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            myExperimentsDataList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String experimentId = document.getId();
                                String experimentTitle = document.getData().get("Title").toString();
                                String experimentDescription = document.getData().get("Description").toString();
                                String experimentRegion = document.getData().get("Region").toString();
                                String experimentOwner = document.getData().get("Owner").toString();
                                int experimentMinTrials = Integer.valueOf(document.getData().get("MinTrials").toString());
                                String experimentTrialType = document.getData().get("TrialType").toString();
                                String experimentEnableLocation = document.getData().get("EnableLocation").toString();

                                myExperimentsDataList.add(new Experiment(experimentId,experimentTitle,
                                        experimentDescription,experimentRegion,experimentOwner,experimentMinTrials,experimentTrialType, experimentEnableLocation));
                            }
                            myExperimentAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    /**
     * Get user information from the database.
     */
    public void getUserInfo() {
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()){
                        firstNameText = documentSnapshot.getData().get("firstName").toString();
                        lastNameText = documentSnapshot.getData().get("lastName").toString();
                        emailText = documentSnapshot.getData().get("email").toString();
                        phoneText = documentSnapshot.getData().get("phone").toString();

                        usernameTextView.setText(userId);
                        firstNameTextView.setText(firstNameText);
                        lastNameTextView.setText(lastNameText);
                        emailTextView.setText(emailText);
                        phoneTextView.setText(phoneText);
                    }
                }
            }
        });
    }

    /**
     * Create a dialog with the user information that can be edited.
     * Update the database with the new information, or delete the profile if user requested.
     */
    private void editUserDialog() {
        AlertDialog.Builder settingsBuilder = new AlertDialog.Builder(UserProfile.this);
        View settingsView = getLayoutInflater().inflate(R.layout.edit_user_dialog,null);

        EditText setFirstName = (EditText) settingsView.findViewById(R.id.editTextFirstName);
        EditText setLastName = (EditText) settingsView.findViewById(R.id.editTextLastName);
        EditText setEmail = (EditText) settingsView.findViewById(R.id.editTextEmail);
        EditText setPhone = (EditText) settingsView.findViewById(R.id.editTextSettingsPhone);
        Button update = (Button) settingsView.findViewById(R.id.updateButton);
        Button deleteProfile = (Button) settingsView.findViewById(R.id.deleteUserButton);

        settingsBuilder.setView(settingsView);
        AlertDialog setDialog = settingsBuilder.create();
        setDialog.setCanceledOnTouchOutside(true);

        setFirstName.setText(firstNameText);
        setLastName.setText(lastNameText);
        setEmail.setText(emailText);
        setPhone.setText(phoneText);

        //update user info
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedFirst = setFirstName.getText().toString();
                String updatedLast = setLastName.getText().toString();
                String updatedEmail = setEmail.getText().toString();
                String updatedPhone = setPhone.getText().toString();

                documentReference.update(
                        "email", updatedEmail,
                        "firstName", updatedFirst,
                        "lastName", updatedLast,
                        "phone", updatedPhone
                ).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Changes Saved", Toast.LENGTH_LONG).show();
                            setDialog.dismiss();
                            getUserInfo();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        //delete profile and return to main
        deleteProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollectionReference collectionReference = db.collection("UserProfile");
                collectionReference.document(userId).delete();
                Intent intent1 = new Intent(UserProfile.this, MainActivity.class);
                startActivity(intent1);
            }
        });

        setDialog.show();
    }

    /**
     * Create a dialog to create a new experiment with title, description, and location.
     * Select types of trials and if location is required.
     */
    private void addExpDialog() {
        AlertDialog.Builder addBuilder = new AlertDialog.Builder(UserProfile.this);
        View addView = getLayoutInflater().inflate(R.layout.add_exp_dialog,null);
        final CollectionReference collectionReference = db.collection("Experiments");


        String trialTypes[] = {"Count-based", "Binomial", "Measurement", "NonNegativeInteger"};

        //location spinner
        String enableLocation[] = {"No", "Yes"};

        expTitle = (EditText) addView.findViewById(R.id.addExpTitle);
        expDescription = (EditText) addView.findViewById(R.id.addExpDescription);
        expRegion = (EditText) addView.findViewById(R.id.addExpRegion);
        expMinTrials = addView.findViewById(R.id.addExpMinTrials);
        Spinner dropdown = (Spinner) addView.findViewById(R.id.dropdownTrialType);
        Spinner dropdown2 = (Spinner) addView.findViewById(R.id.dropdownLocation);
        create = (Button) addView.findViewById(R.id.createButton);

        expMinTrials.addTextChangedListener(addTextWatcher);

        expTitle.addTextChangedListener(addTextWatcher);
        expDescription.addTextChangedListener(addTextWatcher);
        expRegion.addTextChangedListener(addTextWatcher);
        expMinTrials.addTextChangedListener(addTextWatcher);
        create.setEnabled(false);

        addBuilder.setView(addView);
        AlertDialog addDialog = addBuilder.create();
        addDialog.setCanceledOnTouchOutside(true);

        //trial type dropdown menu
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,trialTypes);
        dropdown.setAdapter(adapter);

        //location dropdown menu
        ArrayAdapter<String> adapter2 =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,enableLocation);
        dropdown2.setAdapter(adapter2);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //userprofile collection, have to update experiment list
                String title = expTitle.getText().toString().trim();
                String description = expDescription.getText().toString().trim();
                String region = expRegion.getText().toString().trim();
                int minTrials = Integer.valueOf(expMinTrials.getText().toString());
                String trialType = dropdown.getSelectedItem().toString();
                String enableLocation = dropdown2.getSelectedItem().toString();

                Map<String,Object> data = new HashMap<>();
                data.put("Title",title);
                data.put("Description",description);
                data.put("Region",region);
                data.put("MinTrials",minTrials);
                data.put("TrialType",trialType);
                data.put("Owner",userId);
                data.put("EnableLocation", enableLocation);
                data.put("Status", status);

                experimentId = collectionReference.document().getId();
                collectionReference.document(experimentId).set(data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(UserProfile.this, "Experiment created", Toast.LENGTH_LONG).show();
                                addDialog.dismiss();
                                getMyExperiments();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UserProfile.this, "Experiment not created", Toast.LENGTH_LONG).show();
                                addDialog.dismiss();
                            }
                        });
            }
        });

        addDialog.show();

    }


    /**
     * Display subscribed experiments in the User Profile.
     */
    private void subExpView() {
        subExpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SubscribedExperiment subsExp = subscribedExperimentsDataList.get(position);
                String checkType = subsExp.getTrialType();
                String expId = subsExp.getId();
                if(checkType.equals("Binomial")){
                    Intent intent = new Intent(UserProfile.this, SubscribedBinomialExperimentActivity.class);
                    intent.putExtra(EXPERIMENT_ID_EXTRA,expId);
                    startActivity(intent);
                }

                if(checkType.equals("Count-based")) {
                    Intent intent = new Intent(UserProfile.this, SubscribedCountExperimentActivity.class);
                    //intent.putExtra("experimentId",expId);
                    intent.putExtra(EXPERIMENT_ID_EXTRA,expId);
                    startActivity(intent);
                }
                if(checkType.equals("NonNegativeInteger")) {
                    Intent intent = new Intent(UserProfile.this, SubscribedNonNegativeExperimentActivity.class);
                    intent.putExtra(EXPERIMENT_ID_EXTRA,expId);
                    startActivity(intent);
                }
                if(checkType.equals("Measurement")) {
                    Intent intent = new Intent(UserProfile.this, SubscribedMeasurementExperimentActivity.class);
                    intent.putExtra(EXPERIMENT_ID_EXTRA,expId);
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * Display user created experiments in the User Profile.
     */
    private void myExpView (){
        myExpListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Experiment experiment = myExperimentsDataList.get(position);
                experimentTrialType = experiment.getTrialType();
                // add condition to check trial type
                if(experimentTrialType.equals("Binomial")){
                    Intent intent = new Intent(UserProfile.this, BinomialExperimentActivity.class);
                    intent.putExtra(EXPERIMENT_ID_EXTRA,experiment.getExperimentId());
                    startActivity(intent);
                }
                if(experimentTrialType.equals("Count-based")) {
                    Intent intent = new Intent(UserProfile.this, CountExperimentActivity.class);
                    intent.putExtra(EXPERIMENT_ID_EXTRA,experiment.getExperimentId());
                    startActivity(intent);
                }
                if(experimentTrialType.equals("NonNegativeInteger")) {
                    Intent intent = new Intent(UserProfile.this, NonNegativeExperimentActivity.class);
                    //intent.putExtra("experimentId", experiment.getExperimentId());
                    intent.putExtra(EXPERIMENT_ID_EXTRA,experiment.getExperimentId());
                    startActivity(intent);
                }
                if(experimentTrialType.equals("Measurement")) {
                    Intent intent = new Intent(UserProfile.this, MeasurementExperimentActivity.class);
                    //intent.putExtra("experimentId", experiment.getExperimentId());
                    intent.putExtra(EXPERIMENT_ID_EXTRA,experiment.getExperimentId());
                    startActivity(intent);
                }

            }
        });
    }


    @Override
    public void onBackPressed() { }

    private TextWatcher addTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String checkTitle = expTitle.getText().toString();
            String checkDescription = expDescription.getText().toString();
            String checkRegion = expRegion.getText().toString();
            String checkMinTrials = expMinTrials.getText().toString();

            create.setEnabled(!checkTitle.isEmpty()
                    && !checkDescription.isEmpty()
                    && !checkRegion.isEmpty()
                    && !checkMinTrials.isEmpty()
                    && isPositive(checkMinTrials));

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     *  Checks if the input is a positive integer
     * @param check
     * @return Boolean(true or false)
     *
     */
    private boolean isPositive(String check) {
        try {
            return Integer.parseInt(check) > 0;
        }
        catch (Exception e) {
            return false;
        }
    }
}