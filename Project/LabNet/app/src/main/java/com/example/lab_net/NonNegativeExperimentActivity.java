package com.example.lab_net;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.common.graph.Graphs;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class NonNegativeExperimentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListView trialList;

    // Count adapters and lists
    private ArrayAdapter<NonNegativeIntegerTrial> trialArrayAdapter;
    private ArrayList<NonNegativeIntegerTrial> trialDataList;
    private FirebaseFirestore db;

    // experiment
    private String experimentId;
    private String experimentTitle, experimentDescription, experimentRegion, trialType;
    private String owner;

    // TextViews
    TextView experiment_title, experiment_description, experiment_region;

    // Trials
    private String trialId;
    private String trialTitle;
    private String result;
    Button addTrialButton;
    EditText addTrialTitle, addTrialResult;

    Button add_trial_button;
    ImageButton edit_experiment_button;

    String checkTitle, checkResult;

    //side menu
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.experiment_owner_activity);

        //side menu
        setToolbar();

        experimentId = getIntent().getStringExtra(ExperimentActivity.EXPERIMENT_ID_EXTRA);

        trialList = (ListView) findViewById(R.id.trial_list);
        trialDataList = new ArrayList<>();
        trialArrayAdapter = new CustomNonNegativeTrialList(this, trialDataList);
        trialList.setAdapter(trialArrayAdapter);
        db = FirebaseFirestore.getInstance();

        // fill experiment details textViews
        DocumentReference documentReference = db.collection("Experiments").document(experimentId);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        experimentTitle = documentSnapshot.getData().get("Title").toString();
                        experimentDescription = documentSnapshot.getData().get("Description").toString();
                        experimentRegion = documentSnapshot.getData().get("Region").toString();
                        owner = documentSnapshot.getData().get("Owner").toString();

                        //get trialtype to make respective dialog box appear
                        trialType = documentSnapshot.getData().get("TrialType").toString();

                        // set textviews in experiment_owner_activity to experiment details
                        experiment_title.setText(experimentTitle);
                        experiment_description.setText("Description: " + experimentDescription);
                        experiment_region.setText("Region: " + experimentRegion);
                    }
                }
            }
        });

        // Fills in trialDataList
        db.collection("Trials").whereEqualTo("ExperimentId", experimentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                trialId = document.getId();
                                trialTitle = document.getData().get("Title").toString();
                                trialType = document.getData().get("Title").toString();
                                result = (String) document.getData().get("Result");
                                trialDataList.add(new NonNegativeIntegerTrial(trialId, trialTitle,Long.valueOf(result)));
                            }

                        }
                        trialArrayAdapter.notifyDataSetChanged();
                    }

                });
        trialList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteTrial(position);
                return true;

            }
        });

        experiment_title = findViewById(R.id.experimentTitle);
        experiment_description = findViewById(R.id.experimentDescription);
        experiment_region = findViewById(R.id.experimentRegion);



        edit_experiment_button = (ImageButton) findViewById(R.id.editExperimentButton);
        edit_experiment_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editExperiment();
            }
        });

        add_trial_button = (Button) findViewById(R.id.addRemoveTrialsButton);
        add_trial_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTrial();
            }
        });

    }

    //side menu created from youtube: Android Navigation Drawer Menu Material Design
    // by Coding With Tea
    private void setToolbar(){
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.bringToFront();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.app_teal));
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_profile:
                Intent profileIntent = new Intent(getApplicationContext(),UserProfile.class);
                profileIntent.putExtra(UserProfile.USER_ID_EXTRA, owner);
                startActivity(profileIntent);
                break;
            case R.id.nav_qr:
                //TODO
                break;
            case R.id.nav_statistics:
                if (trialArrayAdapter.getCount() == 0) {
                    Toast.makeText(NonNegativeExperimentActivity.this, "No stats available for this experiment", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), Statistics.class);
                    intent.putExtra("trialDataList", (Serializable) trialDataList);
                    intent.putExtra("ExperimentId", experimentId);
                    intent.putExtra("check",2);
                    startActivity(intent);
                }
                break;
            case R.id.nav_graphs:
                if (trialArrayAdapter.getCount() == 0) {
                    Toast.makeText(NonNegativeExperimentActivity.this, "No Histograms available for this experiment", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), Histogram.class);
                    intent.putExtra("trialDataList", (Serializable) trialDataList);
                    intent.putExtra("ExperimentId", experimentId);
                    intent.putExtra("check", 0);
                    startActivity(intent);
                }
                break;
            case R.id.nav_qa:
                Intent qaIntent = new Intent(getApplicationContext(), QuestionsActivity.class);
                qaIntent.putExtra("check", "OwnerActivity");
                qaIntent.putExtra("experimentID", experimentId);
                startActivity(qaIntent);
                break;
            case R.id.nav_completeExp:
                //TODO
                break;
            case R.id.nav_publishExp:
                //TODO
                break;
            case R.id.nav_deleteExp:
                deleteExperiment();
                break;
        }
        return true;
    }

    private void editExperiment() {
        AlertDialog.Builder settingsBuilder = new AlertDialog.Builder(NonNegativeExperimentActivity.this);
        View settingsView = getLayoutInflater().inflate(R.layout.edit_experiment_dialog, null);

        EditText edit_title = settingsView.findViewById(R.id.editTitle);
        EditText edit_description = settingsView.findViewById(R.id.editDescription);
        EditText edit_region = settingsView.findViewById(R.id.editRegion);

        settingsBuilder.setView(settingsView);
        AlertDialog setDialog = settingsBuilder.create();
        setDialog.setCanceledOnTouchOutside(true);

        setDialog.show();

        edit_title.setText(experimentTitle);
        edit_description.setText(experimentDescription);
        edit_region.setText(experimentRegion);

        Button update_button = (Button) settingsView.findViewById(R.id.updateExperimentButton);
        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference updateExperimentDoc = db.collection("Experiments").document(experimentId);
                updateExperimentDoc.update("Title", edit_title.getText().toString(),
                        "Description", edit_description.getText().toString(),
                        "Region", edit_region.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(NonNegativeExperimentActivity.this, "Experiment edited", Toast.LENGTH_LONG).show();
                            setDialog.dismiss();

                            experimentTitle = edit_title.getText().toString();
                            experimentDescription = edit_description.getText().toString();
                            experimentRegion = edit_region.getText().toString();

                            experiment_title.setText(experimentTitle);
                            experiment_description.setText("Description: " + experimentDescription);
                            experiment_region.setText("Region: " + experimentRegion);
                        } else {
                            Toast.makeText(NonNegativeExperimentActivity.this, "experiment not edited", Toast.LENGTH_LONG).show();
                            setDialog.dismiss();
                        }

                    }
                });
            }

        });
    }

    private void addTrial() {
        AlertDialog.Builder settingsBuilder = new AlertDialog.Builder(NonNegativeExperimentActivity.this);
        View settingsView = getLayoutInflater().inflate(R.layout.edit_trial_dialog, null);


        settingsBuilder.setView(settingsView);
        AlertDialog setDialog = settingsBuilder.create();
        setDialog.setCanceledOnTouchOutside(true);
        setDialog.show();

        addTrialButton = (Button) settingsView.findViewById(R.id.addTrial);
        addTrialTitle = (EditText) settingsView.findViewById(R.id.addTrialTitle);
        addTrialResult = (EditText) settingsView.findViewById(R.id.addTrialResult);
        Toast.makeText(NonNegativeExperimentActivity.this, "Enter a non-negative Integer", Toast.LENGTH_LONG).show();
        addTrialButton.setEnabled(false);

        addTrialTitle.addTextChangedListener(addTextWatcher);
        addTrialResult.addTextChangedListener(addTextWatcher);

        final CollectionReference collectionReference = db.collection("Trials");
        String trialId = collectionReference.document().getId();

/*        Button getLocationButton = (Button) settingsView.findViewById(R.id.getLocationButton);
        getLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(trialId);
            }
        });*/

        addTrialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = addTrialResult.getText().toString();
                String title = addTrialTitle.getText().toString();
                // add to firebase
                HashMap<String, Object> data = new HashMap<>();
                data.put("Title", title);
                data.put("Result", result);
                data.put("ExperimentId", experimentId);
                collectionReference
                        .document(trialId)
                        .set(data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                trialDataList.add(new NonNegativeIntegerTrial(trialId, title, Long.valueOf(result)));
                                trialArrayAdapter.notifyDataSetChanged();
                                Toast.makeText(NonNegativeExperimentActivity.this, "Trial added", Toast.LENGTH_LONG).show();
                                setDialog.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NonNegativeExperimentActivity.this, "Trial not added", Toast.LENGTH_LONG).show();
                            }
                        });

                setDialog.dismiss();
            }
        });
    }


    // method responsible for deleting trials
    public void deleteTrial(int position) {
        NonNegativeIntegerTrial trial = trialDataList.get(position);
        trialDataList.remove(position);
        trialArrayAdapter.notifyDataSetChanged();
        db.collection("Trials").document(trial.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(NonNegativeExperimentActivity.this, "Trial Deleted", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NonNegativeExperimentActivity.this, "Trial not deleted", Toast.LENGTH_LONG).show();
                    }
                });
        db.collection("Trials").whereEqualTo("ExperimentId", experimentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            trialDataList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                trialId = document.getId();
                                trialTitle = document.getData().get("Title").toString();
                                trialType = document.getData().get("Title").toString();

                                result = (String) document.getData().get("Result");
                                trialDataList.add(new NonNegativeIntegerTrial(trialId, trialTitle, Long.valueOf(result)));
                            }
                            trialArrayAdapter.notifyDataSetChanged();
                        }
                    }
                });

    }

    // invoked upon delete experiment button click
    private void deleteExperiment() {
        final CollectionReference collectionReference = db.collection("Experiments");

        AlertDialog.Builder alert = new AlertDialog.Builder(NonNegativeExperimentActivity.this);
        alert.setTitle("Alert");
        alert.setMessage("Confirm delete experiment?");
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                collectionReference.document(experimentId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(NonNegativeExperimentActivity.this, "experiment deleted", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(NonNegativeExperimentActivity.this, "experiment not deleted", Toast.LENGTH_LONG).show();
                            }
                        });
                dialog.dismiss();
                // change to UserProfile
                Intent intent = new Intent(getApplicationContext(), UserProfile.class);
                intent.putExtra("userID", owner);
                startActivity(intent);

            }
        });
        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();

    }

    private TextWatcher addTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkResult = addTrialResult.getText().toString();
            checkTitle = addTrialTitle.getText().toString();
            addTrialButton.setEnabled(isPositive(checkResult)  && !checkTitle.isEmpty());

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