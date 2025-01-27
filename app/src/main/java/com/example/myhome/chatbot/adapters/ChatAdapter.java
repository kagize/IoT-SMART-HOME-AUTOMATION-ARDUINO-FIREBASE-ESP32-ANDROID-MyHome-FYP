package com.example.myhome.chatbot.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhome.R;
import com.example.myhome.chatbot.Message;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressWarnings("FieldMayBeFinal")
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private List<Message> messageList;
    private Activity activity;

    public ChatAdapter(List<Message> messageList, Activity activity) {
        this.messageList = messageList;
        this.activity = activity;
    }

    @NonNull @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.adapter_message_one, parent, false);
        return new MyViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String message = messageList.get(position).getMessage();
        boolean isReceived = messageList.get(position).getIsReceived();
        if(isReceived){
            //Here, the 'send' button is hidden, and the time is also set. Otherwise, it will appear twice, so it is written as 'visible' and 'gone'.
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("HH:mm");
            Date date = new Date();
            String dateformatted = dateFormat.format(date);
            holder.messageReceive.setVisibility(View.VISIBLE);
            holder.messageReceiveTime.setVisibility(View.VISIBLE);
            holder.messageName.setVisibility(View.VISIBLE);
            holder.boticon.setVisibility(View.VISIBLE);
            holder.messageReceiveTime.setText(dateformatted);
            holder.messageSend.setVisibility(View.GONE);
            holder.messageReceive.setText(message);
        }else {
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("HH:mm");
            Date date = new Date();
            String dateformatted = dateFormat.format(date);
            holder.messageSend.setVisibility(View.VISIBLE);
            holder.mesageSendTime.setText(dateformatted);
            holder.messageReceive.setVisibility(View.GONE);
            holder.messageReceiveTime.setVisibility(View.GONE);
            holder.messageName.setVisibility(View.GONE);
            holder.boticon.setVisibility(View.GONE);
            holder.messageSend.setText(message);
        }
    }

    @Override public int getItemCount() {
        return messageList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{


        TextView messageSend,mesageSendTime;
        TextView messageReceive,messageReceiveTime,messageName;
        ImageView boticon;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            messageSend = itemView.findViewById(R.id.message_send);
            messageReceive = itemView.findViewById(R.id.message_receive);
            messageReceiveTime = itemView.findViewById(R.id.text_message_time);
            messageName = itemView.findViewById(R.id.text_message_name);
            boticon = itemView.findViewById(R.id.image_message_profile);
            mesageSendTime = itemView.findViewById(R.id.text_message_send_time);
        }
    }
}
