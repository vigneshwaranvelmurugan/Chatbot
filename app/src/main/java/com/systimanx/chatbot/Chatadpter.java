package com.systimanx.chatbot;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by systimanx on 6/7/18.
 */


public class Chatadpter extends RecyclerView.Adapter<Chatadpter.ViewHolder>  {

    private ArrayList<chatmodel> chatarray;

    private final static int FADE_DURATION = 1000; //FADE_DURATION in milliseconds
    chatmodel chatmodel;
    Context mcontext;
    public Chatadpter.customerlistadapterListner onClickListener;


    public Chatadpter(ArrayList<chatmodel> chatarray, customerlistadapterListner customerlistadapterListner) {
        this.chatarray=chatarray;
        this.onClickListener=customerlistadapterListner;
    }


    @Override
    public Chatadpter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyleitem, viewGroup, false);
        return new Chatadpter.ViewHolder(view);
    }
    public interface customerlistadapterListner {

        void robotext(int position);
    }
    @Override
    public void onBindViewHolder(final Chatadpter.ViewHolder viewHolder, final int i) {


        if (chatarray.get(i).getMsgUser().equals("user")){
            viewHolder.usertext.setVisibility(View.VISIBLE);
            viewHolder.robottext.setVisibility(View.GONE);
            viewHolder.usertext.setText(chatarray.get(i).getMsgText());



        }
        else if (chatarray.get(i).getMsgUser().equals("robot")){
            viewHolder.usertext.setVisibility(View.GONE);
            viewHolder.robottext.setVisibility(View.VISIBLE);
            viewHolder.robottext.setText(chatarray.get(i).getMsgText());

        }

        if (viewHolder.robottext.getText().toString().equals("Sorry, can you say that again?")){
            viewHolder.robottext.setTextColor(Color.parseColor("#FF0000"));
        }
        else {
            viewHolder.robottext.setTextColor(Color.parseColor("#000000"));

        }
        viewHolder.robottext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.robotext(i);

            }
        });




    }

    @Override
    public int getItemCount() {
        return chatarray.size();

    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView usertext,robottext;


        public ViewHolder(View view) {
            super(view);
            mcontext = itemView.getContext();

            usertext = (TextView)view.findViewById(R.id.usertext);
            robottext = (TextView)view.findViewById(R.id.robottext);


        }
    }


}
