package com.example.lab_net;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;

public class Statistics extends AppCompatActivity {

    public static final String EXPERIMENT_ID_EXTRA =
            "com.example.subscribed_experiment_activity.experiment_id";

    public static final String RESULT_LIST_EXTRA =
            "com.example.subscribed_experiment_activity.result_list";

    private TextView meanView;
    private TextView medianView;

    private float sum = 0;
    private double mean = 0;
    private double median = 0;;
    private ArrayList<CountTrial> trials;
    private ArrayList<Long> results;
    private String expId;

    private Button doneButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_dialog);
        meanView = (TextView) findViewById(R.id.mean_view);

        //get from ExperimentActivity
        Intent intent = getIntent();
        results = new ArrayList<>();
        results = (ArrayList<Long>) intent.getSerializableExtra(Statistics.RESULT_LIST_EXTRA);
        expId = intent.getStringExtra(Statistics.EXPERIMENT_ID_EXTRA);

        for (int i = 0; i < results.size(); i++) {
            sum = results.get(i) + sum;
        }
        if (results.size() != 0) {
            mean = (sum / (results.size()));
        } else {
            mean = 0;
        }
        sum = 0;

        doneButton = (Button) findViewById(R.id.doneButton);
        meanView.setText("Mean: " + mean);

        // median
        Collections.sort(results);
        int mid = results.size() / 2;

        if (results.size() % 2 == 0){
            median = (results.get(mid-1) + results.get(mid)) / 2.0;
        }
        else {
            median = results.get(mid);
        }

        medianView = findViewById(R.id.median_view);
        medianView.setText("Median: " + median);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), ExperimentActivity.class);
                intent1.putExtra(ExperimentActivity.EXPERIMENT_ID_EXTRA, expId);
                startActivity(intent1);
            }
        });
    }

}