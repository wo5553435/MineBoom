package com.example.sinner.letsteacher.activity.clearboom

/**
 * Created by sinner on 2017-10-13.
 * mail: wo5553435@163.com
 * github: https://github.com/wo5553435
 */
data class BoomItem(val x:Int, var y:Int, var isBoom:Boolean,var index:Int){
    var roundCount=0//八方向周围雷数
    var isShow=false//是否已经翻开
    var isBoomclick=false;//是否被点炸(雷专属)
    var local=Pair(x,y)// 坐标值
    var isSecurity =false;//是否是安全状态（不可点击）
}