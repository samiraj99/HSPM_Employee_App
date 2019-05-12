package com.sam.hspm_employee_app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ReceiptListAdapter extends ArrayAdapter<ReceiptHelper> {

    private Context mContext;
    private int mResource;

    public ReceiptListAdapter(Context context, int resource, ArrayList<ReceiptHelper> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //get the persons information
       String solvedIssue = getItem(position).getIssuedProblem();
       int amount = getItem(position).getAmount();

       //Helper helper = new Helper(solvedIssue, amount);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent,false);

        TextView TV_solveIssue = convertView.findViewById(R.id.textView1);
        TextView TV_amount = convertView.findViewById(R.id.textView2);

        TV_solveIssue.setText(solvedIssue);
        TV_amount.setText(String.valueOf(amount));

        return convertView;
    }

}
