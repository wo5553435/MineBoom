package com.example.sinner.letsteacher.activity.clearboom

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mineboom.R
import com.example.myapplication.boom.BasicActivity

//import com.example.sinner.letsteacher.utils.DiffCallback
import com.example.sinner.letsteacher.views.dialog.MenuDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_clear_boom.*
import java.util.concurrent.TimeUnit


class ClearBoomActivity : BasicActivity() {
    lateinit var manager: BoomManager
    lateinit var data_boom: ArrayList<BoomItem>
    var sub: Observer<Long>? = null;
    var disposable: Disposable? = null
    var currentlv = BoomManager.Levels.NORMAL;
    lateinit var adapter: BoomAdapter

    override fun initView() {
        tv_spendtime.text = "000"
        tv_clear_boommenu.setOnClickListener { showMenu() }
        btn_flag.setOnClickListener { switchFlagStatu() }
    }

    /**
     * 切换标记状态
     */
    private fun switchFlagStatu() {
        manager?.switchSecurityStatu()
        btn_flag.text = if (manager?.isSecuriryStatu) "关闭标记" else "开启标记"
    }

    override fun initData() {

        manager = BoomManager(activity)
        initBoom()
    }

    override fun getContentViewId() = R.layout.activity_clear_boom

    /**
     * 初始化雷区
     */
    private fun initBoom() {
        manager.currentlevel = currentlv
        btn_flag.text="开启标记"
        data_boom = manager.getBoomData() as ArrayList<BoomItem>? ?: ArrayList()
        rv_data_boomlayout.layoutManager = GridLayoutManager(
            activity,
            manager.currentlevel.xcount,
            GridLayoutManager.VERTICAL,
            false
        )
        rv_data_boomlayout.adapter =
            BoomAdapter(activity, data_boom, object : BoomAdapter.OnEventClick() {
                override fun onItemClick(view: View, position: Int) {
                    sub ?: startcount(tv_spendtime)
                    var item =data_boom.get(position)
                    if(manager.isOver()){//结束了就不用继续了
                        return
                    }
                    if (manager.isSecuriryStatu ) {//堡垒模式
                        manager.setAreaSecurity(!item.isSecurity,item,
                            rv_data_boomlayout.adapter as BoomAdapter
                        )
                    }else if (manager.isCanClick(data_boom.get(position)) ) { //排雷模式
                        if (manager.CheckBoom(
                                data_boom.get(position),
                                rv_data_boomlayout.adapter as BoomAdapter
                            )
                        ) {
                            Toast.makeText(activity, "对不起！游戏结束", Toast.LENGTH_SHORT).show()
                            manager.showAllBoom(rv_data_boomlayout.adapter as BoomAdapter)
                            overGame()
                            return
                            //rv_data_boomlayout.adapter.notifyDataSetChanged()
                        }
                        if (manager.checkOver()) {
                            overGame()
                            Toast.makeText(activity, "恭喜!游戏结束", Toast.LENGTH_SHORT).show()
                            manager.showAllBoom(rv_data_boomlayout.adapter as BoomAdapter)
                            //rv_data_boomlayout.adapter.notifyDataSetChanged()
                        }
                        //本身这块逻辑 我想分开adapter 和 bean
                        //后来发现新老数据更新 每次都会有保存与遍历 并且总是在开辟新地址或参数赋值 很舍本求末
                        //如果换成了全局在高难度情况下刷新很爆炸 所以折中adapter换了一步一刷新
                        //最优逻辑是 在manager check 返回展开数组 为空就是雷 多数组就挨个一起更新
                        //rv_data_boomlayout.adapter.notifyDataSetChanged()
//                        val diffResult = DiffUtil.calculateDiff(DiffCallback(data_boom, olddatas), false)
//                        diffResult.dispatchUpdatesTo(rv_data_boomlayout.adapter)
//                        olddatas=data_boom
//                        (rv_data_boomlayout.adapter as BoomAdapter).setData(olddatas)

                    }
                }

                override fun onItemLongClick(view: View, position: Int) {
                    if(manager.isOver() ){//结束了就不用继续了
                        return
                    }
                    var item =data_boom.get(position)
                    if(!item.isShow) {//未展示的才可以被标记
                        manager.setAreaSecurity(
                            !item.isSecurity, item,
                            rv_data_boomlayout.adapter as BoomAdapter
                        )
                    }
                }
            })
    }

    /**
     * 善后工作
     */
    private fun overGame() {

        //sub?.unsubscribe()
        disposable?.dispose()
    }


    /**
     * 展示菜单
     */
    private fun showMenu() {
        MenuDialog(activity, R.style.Theme_AppCompat_Dialog, currentlv).apply {
            setOnResultListener { i ->
                reStartGame(i)
            }
            show()
        }
    }

    private fun reStartGame(level: Int) {
        tv_spendtime.text = "000"
        disposable?.dispose();sub = null;
        currentlv =
            if (level == 0) BoomManager.Levels.VERYEASY
            else if (level == 1) BoomManager.Levels.EASY
            else if (level == 2) BoomManager.Levels.NORMAL
            else BoomManager.Levels.HARD
        manager?.reset()
        initBoom()
    }

    private fun startcount(tv: TextView) {
        if (sub != null) {
            return;
        }
        sub = object : Observer<Long> {
            override fun onError(p0: Throwable?) {
            }

            override fun onNext(p0: Long) {
                if (p0 < 1000) {
                    tv.text = "" + p0
                }
            }


            override fun onComplete() {
            }

            override fun onSubscribe(d: Disposable?) {
                disposable = d;
            }
        }
        Observable.interval(0, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(sub)
    }


    /**
     * 展示当前剩余雷数（l雷总数- 旗帜数目）
     */
    fun showRestBoomCount(){

    }
}
