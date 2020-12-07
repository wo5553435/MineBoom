package com.example.sinner.letsteacher.views.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.example.mineboom.R
import com.example.sinner.letsteacher.activity.clearboom.BoomManager

/**
 * Created by sinner on 2017-11-02.
 * mail: wo5553435@163.com
 * github: https://github.com/wo5553435
 */
class MenuDialog(context: Context,styleid:Int,var currentlv: BoomManager.Levels): Dialog(context,styleid) {
    lateinit var levels:RadioGroup
    lateinit var btn_restart:TextView
    lateinit var btn_continue:TextView
    var listener:onResult?=null
    var currentlvs=-1;
   init {
       when(currentlv){
           BoomManager.Levels.VERYEASY -> currentlvs=0
           BoomManager.Levels.EASY -> currentlvs=1
           BoomManager.Levels.NORMAL -> currentlvs=2
           BoomManager.Levels.HARD -> currentlvs=3

       }
   }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_dialog_menu)
        initView()
        initData()
    }

    private fun initData() {
    }

    private fun initView() {
        levels= findViewById(R.id.gp_clearb_levels) as RadioGroup
        (levels.getChildAt(currentlvs) as RadioButton ).isChecked=true
        btn_restart= findViewById(R.id.tv_clearb_restart) as TextView
        btn_continue=findViewById(R.id.tv_clearb_continue) as TextView
        levels.setOnCheckedChangeListener { radioGroup, i ->
            Log.e("radio----","i:"+i)
            when(i){
                R.id.rb_clear_1 ->  {currentlvs=0;  currentlv=BoomManager.Levels.VERYEASY}
                R.id.rb_clear_2 ->   {currentlvs=1; currentlv=BoomManager.Levels.EASY}
                R.id.rb_clear_3 ->  {currentlvs=2;  currentlv=BoomManager.Levels.NORMAL}
                R.id.rb_clear_4 ->  {currentlvs=3; currentlv=BoomManager.Levels.HARD}
            }

        }
        btn_continue.setOnClickListener{
            dismiss()
        }

        btn_restart.setOnClickListener {
            listener?.onAction(currentlvs)
            dismiss()
        }
    }

    inline  fun setOnResultListener(crossinline f:(position:Int) -> Unit){
        listener=object :onResult{
            override fun onAction(level: Int) {
                    f(level)
            }
        }
    }

    interface  onResult{
        fun onAction(level: Int)
    }
}