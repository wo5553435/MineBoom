package com.example.myapplication.boom

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Zhoushoubiao on 20-12-4.
 * Desc:
 */
abstract class BasicActivity : AppCompatActivity() {
    lateinit var activity: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getContentViewId())
        activity =this
        initView()
        initData()
    }

    abstract  fun initView()

    abstract fun initData()

    abstract fun getContentViewId():Int
}