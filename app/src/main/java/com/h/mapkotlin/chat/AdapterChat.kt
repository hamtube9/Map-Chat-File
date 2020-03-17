package com.h.mapkotlin.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.h.mapkotlin.R
import kotlinx.android.synthetic.main.item_response.view.*
import kotlinx.android.synthetic.main.item_send.view.*

class AdapterChat (var context : Context,var list:ArrayList<Chat>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class SendHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    inner class ResponseHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    companion object{
        const val VIEW_TYPE_SEND = 1
        const val VIEW_TYPE_RESPONSE = 2
    }

    override fun getItemViewType(position: Int): Int {
        val time = list[position].time
        return if (time!= ""){
            VIEW_TYPE_SEND
        }else{
            VIEW_TYPE_RESPONSE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  when(viewType){
            VIEW_TYPE_SEND -> { SendHolder(LayoutInflater.from(context).inflate(R.layout.item_send,parent,false))}
            VIEW_TYPE_RESPONSE -> { ResponseHolder(LayoutInflater.from(context).inflate(R.layout.item_response,parent,false))}
            else ->{ ResponseHolder(LayoutInflater.from(context).inflate(R.layout.item_response,parent,false))}
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is SendHolder ->{
                holder.itemView.tvSend.text = list[position].msg
            }
            is ResponseHolder ->{
                holder.itemView.tvResponse.text = list[position].msg

            }
        }
    }
}