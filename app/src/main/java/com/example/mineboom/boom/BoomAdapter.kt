package com.example.sinner.letsteacher.activity.clearboom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.mineboom.R

/**
 * Created by sinner on 2017-10-13.
 * mail: wo5553435@163.com
 * github: https://github.com/wo5553435
 */
class BoomAdapter(
    var context: Context,
    var datas: List<BoomItem>?,
    var onEventClick: OnEventClick?
) :
    RecyclerView.Adapter<BoomAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.layout_item_boom, parent, false
        ), onEventClick
    )

    fun test(){
        
    }

    var isShowOver =false;//是否是展示结果模式

//    fun setShowOver(flag:Boolean){
//        isShowOver=flag
//    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads == null || payloads.size == 0)
            onBindViewHolder(holder!!, position)
        else {
            //holder?.btn.text = "更新"
        }
    }

    fun setData(datas: List<BoomItem>?) {
        this.datas = datas
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // holder.tv_name?.text=datas[position].toString()
        datas?.get(position)?.apply {
            if (isSecurity) {//当前是否被标记
                if(!isBoom&&isShowOver){//当前被错误标记
                    holder.btn.text = ""
                    holder.btn.background = context.resources.getDrawable(R.drawable.ic_android_errorflag_24dp)
                }else {
                    if(isShowOver){
                        holder.btn.text = "雷"
                        holder.btn.resources
                    }else {
                        holder.btn.text = ""
                        holder.btn.background =
                            context.resources.getDrawable(R.drawable.ic_android_flag_24dp)
                    }
                }
            }  else if (isShow) {//当前被翻开
                if (isBoom) {
                    holder.btn.text = "雷"
                    holder.btn.background =
                        (if (isBoomclick) context.resources.getDrawable(R.drawable.background_kitkat_red) else context.resources.getDrawable(
                            R.drawable.background_kitkat_blue
                        ))
                } else {
                        holder.btn.text = if (roundCount > 0) "" + roundCount else ""
                        holder.btn.background =
                            context.resources.getDrawable(R.drawable.background_kitkat_white)
                }
            }  else {//当前没有翻看过
                    holder.btn.text = ""
                    holder.btn.background = context.resources.getDrawable(R.drawable.circle_blue_bg)
            }
        }


    }

    inline fun onItemClick(crossinline f: (View, Int) -> Unit,crossinline  foo:(View ,Int)-> Unit): BoomAdapter {
        onEventClick = object : OnEventClick() {
            override fun onItemClick(view: View, position: Int) {
                f(view, position)
            }

            override fun onItemLongClick(view: View, position: Int) {
                foo(view,position)
            }
        }
        return this
    }


    fun ChangePositionStatus(vararg positions: Int) {
        positions.forEach {
            notifyItemChanged(it)
        }
    }

    override fun getItemCount() = datas?.size ?: 0

    inner class ViewHolder(itemView: View, private val onEventClick: BoomAdapter.OnEventClick?) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener  ,View.OnLongClickListener{
        //var tv_name:TextView?=null
        lateinit var btn: Button

        init {
            //tv_name= itemView.findViewById(R.id.tv_item_text) as TextView?
            itemView?.run {
                btn = findViewById(R.id.boom_item_view) as Button
                btn.setOnClickListener(this@ViewHolder)
                btn.setOnLongClickListener(this@ViewHolder)
            }
        }

        override fun onClick(view: View) {
            onEventClick?.onItemClick(view, adapterPosition)
        }

        override fun onLongClick(v: View): Boolean {
            onEventClick?.onItemLongClick(v,adapterPosition)
            return true
        }
    }

    abstract class OnEventClick {
        abstract fun onItemClick(view: View, position: Int)
        abstract fun onItemLongClick(view: View,position: Int)
    }
}

