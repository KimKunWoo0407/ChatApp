package com.kkw.mychatapp

import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder

interface IItemClickListener {
    fun onItemClick(holder:ViewHolder, v: View, position:Int)
}