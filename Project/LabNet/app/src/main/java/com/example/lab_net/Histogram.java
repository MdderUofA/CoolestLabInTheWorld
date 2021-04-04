package com.example.lab_net;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Collections;

public class Histogram extends AppCompatActivity {

    BarChart barChart;
    ArrayList<BarEntry> barEntries;
    LineChart lineChart;
    ArrayList<ILineDataSet> iLineDataSets;
    ArrayList<Entry> entries;
    private ArrayList<Double> results;
    private ArrayList<NonNegativeIntegerTrial> nonNegativeTrials;
    private ArrayList <CountTrial> countTrials;
    private ArrayList <MeasurementTrial> measurementTrials;
    private ArrayList <BinomialTrial> binomialTrials;
    private ArrayList<Double> frequency;
    private int i;
    int checkActivity;
    float j;
    private ArrayList<String> dateDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histogram);
        Intent intent = getIntent();
        checkActivity = intent.getExtras().getInt("check");
        if(checkActivity == 0){
            nonNegative();
        }
        if(checkActivity == 1){
            count();
        }
        if(checkActivity == 2){
            binomial();
        }
        if(checkActivity == 3){
            measurement();
        }
    }

    public void nonNegative() {
        barChart = (BarChart) findViewById(R.id.barGraph);

        Intent intent = getIntent();
        results = new ArrayList<>();

        nonNegativeTrials = new ArrayList<>();
        nonNegativeTrials = (ArrayList<NonNegativeIntegerTrial>) intent.getSerializableExtra("trialDataList");
        for (i = 0; i < nonNegativeTrials.size(); i++) {
            results.add(Double.valueOf(nonNegativeTrials.get(i).getNonNegativeCount()));
        }
        barEntries = new ArrayList<>();
        frequency = new ArrayList<>();

        /*for (i = 0; i < results.size(); i++) {
            frequency.add()
        }*/
        Collections.sort(results);
        for (i = 0; i < results.size(); i++) {
            barEntries.add(new BarEntry(Float.valueOf(String.valueOf(results.get(i))), Collections.frequency(results, results.get(i))));
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "results");

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
    }

    public void count() {
        barChart = (BarChart) findViewById(R.id.barGraph);

        Intent intent = getIntent();
        results = new ArrayList<>();

        countTrials = new ArrayList<>();
        countTrials = (ArrayList<CountTrial>) intent.getSerializableExtra("trialDataList");
        for (i = 0; i < countTrials.size(); i++) {
            results.add(Double.valueOf(countTrials.get(i).getCount()));
        }
        barEntries = new ArrayList<>();
        frequency = new ArrayList<>();


        Collections.sort(results);
        for (i = 0; i < results.size(); i++) {
            barEntries.add(new BarEntry(Float.valueOf(String.valueOf(results.get(i))), Collections.frequency(results, results.get(i))));
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "results");

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);

        lineChart = (LineChart) findViewById(R.id.lineChart);
        entries = new ArrayList<>();
        dateDataList = new ArrayList<>();
        dateDataList = (ArrayList<String>) intent.getSerializableExtra("dateDataList");
        for(i = 0; i < dateDataList.size(); i++){
            entries.add(new Entry(Float.valueOf(dateDataList.get(i)),i));
        }
        LineDataSet lineDataSet = new LineDataSet(entries, "dateDataList");

        iLineDataSets = new ArrayList<>();
        iLineDataSets.add(lineDataSet);

        LineData lineData = new LineData(iLineDataSets);
        lineChart.setData(lineData);

        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

    }
    private void binomial() {
        barChart = (BarChart) findViewById(R.id.barGraph);

        Intent intent = getIntent();
        results = new ArrayList<>();

        binomialTrials = new ArrayList<>();
        binomialTrials = (ArrayList<BinomialTrial>) intent.getSerializableExtra("trialDataList");
        for (i = 0; i < binomialTrials.size(); i++) {
            if(binomialTrials.get(i).getResult().toLowerCase().equals("pass")){
                results.add(1.0);
            }
            else{
                results.add(0.0);
            }

        }
        barEntries = new ArrayList<>();
        frequency = new ArrayList<>();

        /*for (i = 0; i < results.size(); i++) {
            frequency.add()
        }*/
        Collections.sort(results);
        for (i = 0; i < results.size(); i++) {
            barEntries.add(new BarEntry(Float.valueOf(String.valueOf(results.get(i))), Collections.frequency(results, results.get(i))));
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "results");

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
    }
    private void measurement() {
        barChart = (BarChart) findViewById(R.id.barGraph);

        Intent intent = getIntent();
        results = new ArrayList<>();

        measurementTrials = new ArrayList<>();
        measurementTrials = (ArrayList<MeasurementTrial>) intent.getSerializableExtra("trialDataList");
        for (i = 0; i < measurementTrials.size(); i++) {
            results.add(Double.valueOf(measurementTrials.get(i).getMeasurement()));
        }
        barEntries = new ArrayList<>();
        frequency = new ArrayList<>();

        /*for (i = 0; i < results.size(); i++) {
            frequency.add()
        }*/
        Collections.sort(results);
        for (i = 0; i < results.size(); i++) {
            barEntries.add(new BarEntry(Float.valueOf(String.valueOf(results.get(i))), Collections.frequency(results, results.get(i))));
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "results");

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
    }
}