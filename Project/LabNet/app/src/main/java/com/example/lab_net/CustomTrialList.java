package com.example.lab_net;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CustomTrialList extends ArrayAdapter<CountTrial> {

    private ArrayList<CountTrial> trials;
    private Context context;

    public CustomTrialList(Context context, ArrayList<CountTrial> trials){
        super(context,0, trials);
        this.trials = trials;
        this.context = context;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        return super.getView(position, convertView, parent);
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.content, parent,false);
        }

        CountTrial trial = (CountTrial) trials.get(position);
        TextView titleText = view.findViewById(R.id.title_text);
        TextView resultText = view.findViewById(R.id.result_text);

        titleText.setText(trial.getTitle());
        resultText.setText(trial.getCount().toString());


        return view;

    }
}

