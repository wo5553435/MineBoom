package com.example.sinner.letsteacher.activity.clearboom

import android.content.Context
import android.util.ArrayMap
import android.util.Log
import java.util.concurrent.LinkedBlockingQueue
import kotlin.collections.ArrayList

/**
 * 扫雷管理类
 * Created by sinner on 2017-09-17.
 * mail: wo5553435@163.com
 * github: https://github.com/wo5553435
 *
 */
class BoomManager(var context: Context) {
    val allmap = ArrayMap<Pair<Int, Int>, BoomItem>()//boom全表
    val boommap = ArrayMap<Pair<Int, Int>, BoomItem>()//boom表
    var currentlevel = Levels.NORMAL
    var checkarray = LinkedBlockingQueue<BoomItem>()//需要检验的数组
    var checkedarray = LinkedBlockingQueue<BoomItem>()// 完成校验的数组
    var isGameOver = false;
    var isSecuriryStatu = false;//是否是堡垒状态
    var knowdata = ArrayList<BoomItem>()//已经完成身份验证的数据组 (弃用)
    var boomdata: ArrayList<BoomItem>? = null
    var flagedMap = ArrayList<BoomItem>()

    /**
     * 随机获取雷表（保存在boommap中）
     */
    fun getrandomBoom() {
        var x = ArrayMap<Int, String>()
        allmap.clear()
        val xsize = currentlevel.xcount - 1
        val ysize = currentlevel.ycount - 1
        var index = 0
        for (x in 0..xsize) {
            for (y in 0..ysize) {
                var location = Pair(x, y)
                allmap.put(
                    location,
                    BoomItem(x, y, false, index).apply { isShow = false;local = location })
                index++;
            }
        }
        /**
         * 随机填充雷到指定数目
         */
        while (boommap.size < currentlevel.boommax) {
            var bx: Int = (Math.random() * (xsize + 1)).toInt()
            var by = (Math.random() * (ysize + 1)).toInt()
            var key = Pair(bx, by)
            if (!boommap.contains(key)) {//重复雷不加入 （这里可以优化）
                allmap.get(key)?.isBoom = true
                boommap.put(key, allmap.get(key))//保存到雷表
            }
        }
    }

    /**
     * 获取周围八个点坐标（删选超框坐标）
     */
    fun getAroundPair(key: Pair<Int, Int>): List<Pair<Int, Int>> {
        val rangeofX = key.first - 1..key.first + 1
        val rangeofY = key.second - 1..key.second + 1
        val result = mutableListOf<Pair<Int, Int>>()
        with(result) {
            for (i in rangeofX) {
                for (j in rangeofY) {
                    add(Pair(i, j))
                }
            }
            filter { value -> value.first == key.first && value.second == key.second }//过滤本身
            filter { value -> value.first < 0 || value.second < 0 }// 过滤超出最小值范围
            filter { values -> values.first > currentlevel.xcount || values.second > currentlevel.ycount }//过滤超出最大值范围
        }
        return result
    }

    /**
     * 计算有雷附近8个按钮的雷数
     */
    fun Countboom() {
        for (value in boommap) {
            var x = value.key.first
            var y = value.key.second
            getAroundPair(value.key).forEach {
                with(it) {
                    if (allmap.contains(this) && allmap.get(this)?.roundCount == 0) {// 如果没有计算过 则开始计算这个点周围雷数
                        allmap[this]?.roundCount = getRoundBoom(first, second)
                    }
                }
            }

        }
    }

    /**
     * 获取当前等级 x y轴区域值
     */
    fun getXYCount(): Pair<Int, Int> {
        var x = 0;
        var y = 0;
        x = currentlevel.xcount
        y = currentlevel.ycount
        return Pair(x, y);
    }

    fun getBoomData(): List<BoomItem>? {
        if (boomdata == null) initBoomData()
        return boomdata
    }

    fun reset() {
        allmap.clear();allmap == null
        boommap.clear();boomdata?.clear();boomdata = null
        checkarray.clear();checkedarray.clear()
        flagedMap?.clear()
        isGameOver = false
        isSecuriryStatu = false
    }

    /**
     * 初始化
     */
    fun initBoomData() {
        getrandomBoom();
        Countboom()
        boomdata = ArrayList<BoomItem>().apply { allmap.values.forEach { add(it) }; }
    }

    /**
     * 将一个点周围存在的最多8个点全推入栈
     */
    fun pullPoint(point: BoomItem) {
        getRoundPoint(point.local).forEach {
            if (!checkedarray.contains(allmap.get(it))) {// 当前 已检查过列中不包含的 元素不再检查
                if (!checkarray.contains(allmap.get(it)) && !checkedarray.contains(allmap.get(it)))//只有当两边都没有他的时候加进去
                {
                    Log.e("增加检查项", "-" + allmap.get(it)?.x + "---" + allmap.get(it)?.y)
                    checkarray.offer(allmap.get(it)) // 最外层while没有走完就添加上了 所以会继续while
                }
//                allmap.get(it)?.isshow = true
            } else {
                Log.e("检查过了", "" + it.first + "--" + it.second)
            }
        }
    }


    /**
     * 检查当前雷是否在池中
     */
    fun CheckBoom(boom: BoomItem, adapter: BoomAdapter): Boolean {
        boom.isShow = true //
        if (boom.isBoom) {
            boom.isBoomclick = true
            isGameOver = true
            return true
        }
        checkarray.offer(allmap.get(boom.local))//在需要检查的队列中加入 当前选择的的坐标
        //下面的代码没有单独开线程去读取回调 后期优化 但实际意义不大
        while (checkarray.peek() != null) {//队列中还有值时候继续
            var temp = checkarray.poll()
            checkpoint(temp)//考虑到
            if (adapter !== null) adapter.notifyItemChanged(temp.index)
        }
        isGameOver = checkOver()
        return false
    }

    /**
     * 校验该模块是否是雷区
     */
    fun CheckBoom(boom: BoomItem): Boolean {
        boom.isShow = true //
        if (boom.isBoom) {
            boom.isBoomclick = true
            isGameOver = true;
            return true;
        }
        checkarray.offer(allmap.get(boom.local))//请注意hash值key  大数量情况下没有测  尽量用统一数据源
        while (checkarray.peek() != null) {//队列中还有值时候继续
            var temp = checkarray.poll()
            checkpoint(temp)
        }
        isGameOver = checkOver()
        return false;
    }

    /**
     * 将所有雷翻出来
     */
    fun showAllBoom(adapter: BoomAdapter?) {
        adapter?.isShowOver = true
        boommap.values.forEach {
            it.isShow = true;if (adapter != null) adapter.notifyItemChanged(it.index)
        }
        flagedMap.forEach {
            if (!it.isBoom && it.isSecurity) adapter?.notifyItemChanged(it.index)
        }

    }

    /**
     * 检测某个点是否是可以展开周围八个点,如果为0则将周围点加入完成队列
     */
    fun checkpoint(point: BoomItem) {
        if (point != null) {
            point?.isShow = true//翻开
            checkedarray.offer(allmap.get(point.local)) //完成检查后放入 检查完成队列中
            Log.e("检查过" + "" + point.x + "------" + point.y, "当前已经检查过的数据大小" + checkedarray.size)
            if (isSecurityArea(point.local.first, point.local.second)) {//安全点（ 周围不存在雷或旗帜）
                Log.e("周围为空", "准备扩散")
                pullPoint(point)
            }
        }
    }

    /**
     * 检验是否完成游戏
     */
    fun checkOver(): Boolean = with(boommap) {
        for (item in values) {
            if (item.isShow == true) return true
        }
        if (checkedarray.size == (currentlevel.xcount * currentlevel.ycount - currentlevel.boommax)) return true//完成全部构建
        false
    }

    /**
     * 设置当前区域是否是堡垒状态
     */
    fun setAreaSecurity(status: Boolean, item: BoomItem, adapter: BoomAdapter) {
        item.isSecurity = status
        if (status) {//新增
            flagedMap.add(item)
        } else {//移除之前有的
            flagedMap.remove(item)
        }
        adapter.notifyItemChanged(item.index)
    }


    /**
     * 获取当前还剩多少
     */
    fun getRestFlagCount() {

    }

    /**
     * 获取某个点周围最多八个点
     */
    fun getRoundPoint(point: Pair<Int, Int>): List<Pair<Int, Int>> {
        return with(point) {//考虑到和adapter的索引对应 这里的xy值是反的 不是xy轴
            listOf(
                Pair(first - 1, second - 1), Pair(first - 1, second), Pair(first - 1, second + 1),
                Pair(first, second - 1), Pair(first, second + 1)
                , Pair(first + 1, second - 1), Pair(first + 1, second), Pair(first + 1, second + 1)
            )
                .filter { it.first >= 0 && it.second >= 0 && it.first < currentlevel.xcount && it.second < currentlevel.ycount } //过滤边缘
        }
    }

    /**
     * 单纯地计算有几个雷
     */
    fun getRoundBoom(x: Int, y: Int): Int {
        var count = 0
        //  getRoundPoint(Pair(x,y)).forEach { if(allmap.get(it)?.isBoom?:false) count++   }
        count = with(Pair(x, y)) {
            var sum = 0
            if (allmap.get(Pair(x - 1, y - 1))?.isBoom ?: false) sum++ //左上
            if (allmap.get(Pair(x, y - 1))?.isBoom ?: false) sum++//中上
            if (allmap.get(Pair(x + 1, y - 1))?.isBoom ?: false) sum++//右上
            if (allmap.get(Pair(x - 1, y))?.isBoom ?: false) sum++//中左
            if (allmap.get(Pair(x + 1, y))?.isBoom ?: false) sum++//中右
            if (allmap.get(Pair(x - 1, y + 1))?.isBoom ?: false) sum++//下左
            if (allmap.get(Pair(x, y + 1))?.isBoom ?: false) sum++//下中
            if (allmap.get(Pair(x + 1, y + 1))?.isBoom ?: false) sum++//下右
            sum
        }
        return count
    }

    /**
     * 计算该点周围最多8个是否没有雷或者旗帜
     */
    fun isSecurityArea(x: Int, y: Int): Boolean {
        getRoundPoint(Pair(x, y)).forEach {
            if (allmap.get(it)?.let { it.isBoom || it.isSecurity } ?: false) return false;
        }
        return true
    }

    val LEVEL_VERYEASY_BOOMCOUNT = 3

    val LEVEL_EASY_BOOMCOUNT = 10

    val LEVEL_NORMAL_BOOMCOUNT = 20;

    val LEVEL_HARD_BOOMCOUNT = 40;

    enum class Levels(val boommax: Int, val xcount: Int, val ycount: Int) {

        VERYEASY(3, 5, 5),
        EASY(10, 8, 8),
        NORMAL(20, 10, 10),
        HARD(40, 15, 15)
    }


    fun isOver() = isGameOver


    /**
     * 该区域是否是可以被点击 (非堡垒模式下)
     */
    fun isCanClick(item: BoomItem) = !item.isShow && !item.isSecurity


    /**
     * 是否是已经展开状态
     */
    fun isShowed(item: BoomItem) = item.isShow


    /**
     * 切换堡垒状态
     */
    fun switchSecurityStatu() {
        isSecuriryStatu = !isSecuriryStatu
    }

    /**
     * 返回是否是堡垒
     */
    fun isSecurityStatus() = isSecuriryStatu
}

