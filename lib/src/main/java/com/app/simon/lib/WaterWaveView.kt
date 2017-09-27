package com.app.simon.waterrippleprogressview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.app.simon.lib.R
import org.jetbrains.anko.sp


/**
 * desc: 水波纹View
 * date: 2017/9/26

 * @author xw
 */
class WaterWaveView @JvmOverloads constructor(context: Context, val attrs: AttributeSet? = null, val defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private val TAG = WaterWaveView::class.java.simpleName

    /** 波浪宽度 */
    val WAVE_PER_WIDTH = sp(60)
    /** 波浪高度 */
    val WAVE_HEIGHT = sp(30)
    /** 移动距离 */
    val WAVE_MOVE_DISTANCE = sp(14)

    /** 背景颜色 */
    val BG_COLOR = Color.WHITE
    /** 外圈圆颜色 */
    val CIRCLE_COLOR = Color.parseColor("#FF73C7FE")
    /** 波浪颜色 */
    val WAVE_COLOR = Color.parseColor("#FF73C7FE")
    /** 进度文字颜色 */
    val PROGRESS_TEXT_COLOR = Color.BLACK
    /** 进度文字大小 */
    val PROGRESS_TEXT_SIZE = sp(30).toFloat()


    /** 波浪画笔 */
    private val wavePaint = Paint()
    /** 外圈圆画笔 */
    private val circlePaint = Paint()
    /** 背景画笔 */
    private val bgPaint = Paint()
    /** 文字画笔 */
    private val progressPaint = Paint()

    /** 起点 */
    private var startPoint: Point? = null
    /** 画布 */
    val wavePath = Path()

    /** sin曲线 1/4个周期的宽度 */
    private var wavePerWidth = WAVE_PER_WIDTH

    /** sin曲线振幅的高度 */
    private var waveHeight = WAVE_HEIGHT

    /** 移动距离 */
    var waveMoveDistance = WAVE_MOVE_DISTANCE

    /** 背景颜色 */
    var bgColor = BG_COLOR
    /** 外圈圆颜色 */
    var circleColor = CIRCLE_COLOR
    /** 波浪颜色 */
    var waveColor = WAVE_COLOR
    /** 进度文字颜色 */
    var progressTextColor = PROGRESS_TEXT_COLOR
    /** 进度文字大小 */
    var progressTextSize = PROGRESS_TEXT_SIZE
    /** 进度 */
    var progress = 0
    /** 是否自动增加 */
    var autoIncrement = true

    init {
        initStyleAttr()
        initView()
    }

    private fun initStyleAttr() {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.WaterWaveView, defStyleAttr, 0)
        (0..typedArray.indexCount)
                .map { typedArray.getIndex(it) }
                .forEach {
                    when (it) {
                    //波浪宽度，1/4周期
                        R.styleable.WaterWaveView_wavePerWidth -> {
                            wavePerWidth = typedArray.getDimension(it, WAVE_PER_WIDTH.toFloat()).toInt()
                        }
                    //波浪高度
                        R.styleable.WaterWaveView_waveHeight -> {
                            waveHeight = typedArray.getDimension(it, WAVE_HEIGHT.toFloat()).toInt()
                        }
                    //波浪移动速度
                        R.styleable.WaterWaveView_waveMoveDistance -> {
                            waveMoveDistance = typedArray.getDimension(it, WAVE_MOVE_DISTANCE.toFloat()).toInt()
                        }
                    //背景颜色
                        R.styleable.WaterWaveView_bgColor -> {
                            bgColor = typedArray.getColor(it, BG_COLOR)
                        }
                    //外圈圆颜色
                        R.styleable.WaterWaveView_circleColor -> {
                            circleColor = typedArray.getColor(it, CIRCLE_COLOR)
                        }
                    //波浪颜色
                        R.styleable.WaterWaveView_waveColor -> {
                            waveColor = typedArray.getColor(it, WAVE_COLOR)
                        }
                    //进度
                        R.styleable.WaterWaveView_progress -> {
                            progress = typedArray.getInt(it, 0)
                        }
                    //进度文字颜色
                        R.styleable.WaterWaveView_progressTextColor -> {
                            progressTextColor = typedArray.getColor(it, PROGRESS_TEXT_COLOR)
                        }
                    //进度文字大小
                        R.styleable.WaterWaveView_progressTextSize -> {
                            progressTextSize = typedArray.getDimension(it, PROGRESS_TEXT_SIZE)
                        }
                    //是否自动增长
                        R.styleable.WaterWaveView_autoIncrement -> {
                            autoIncrement = typedArray.getBoolean(it, true)
                        }
                        else -> {
                        }
                    }
                }
    }

    private fun initView() {
        //波浪
        wavePaint.color = waveColor
        //抗锯齿
        wavePaint.isAntiAlias = true

        //背景
        bgPaint.color = bgColor
        //描边
        bgPaint.style = Paint.Style.FILL
        bgPaint.isAntiAlias = true

        //外圈圆
        circlePaint.color = circleColor
        //描边
        circlePaint.style = Paint.Style.STROKE
        //画笔宽度
        circlePaint.strokeWidth = 4f
        circlePaint.isAntiAlias = true

        //进度文字
        progressPaint.color = progressTextColor
        //字体大小
        progressPaint.textSize = progressTextSize
        //基准点
        progressPaint.textAlign = Paint.Align.CENTER
        progressPaint.isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //如果宽高不同，取小值
        if (widthMeasureSpec != heightMeasureSpec) {
            val min = Math.min(widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(min, min)
        }
        //初始化的时候将起点移至屏幕外一个周期
        startPoint = Point(wavePerWidth * 4, height / 2)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            //裁剪画布为圆形
            val circlePath = Path()
            circlePath.addCircle((width / 2).toFloat(), (height / 2).toFloat(), (width / 2).toFloat(), Path.Direction.CW)
            //切割画布
            canvas.clipPath(circlePath)
//            canvas.drawPaint(circlePaint)
            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (width / 2).toFloat(), bgPaint)
            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (width / 2).toFloat(), circlePaint)

            //以下操作都是在这个圆形画布中操作
            //根据进度改变起点坐标的y值
            startPoint!!.y = (height - (progress / 100.0 * height)).toInt()
            //起点
            wavePath.moveTo(startPoint!!.x.toFloat(), startPoint!!.y.toFloat())
            //循环绘制正弦曲线 循环一次半个周期
            for (i in 1..8) {
                if (i % 2 == 1) {
                    //波峰
                    wavePath.quadTo((startPoint!!.x + (wavePerWidth * (i * 2 - 1))).toFloat(), (startPoint!!.y - waveHeight).toFloat(),
                            (startPoint!!.x + (wavePerWidth * 2) * i).toFloat(), startPoint!!.y.toFloat())
                } else {
                    //波谷
                    wavePath.quadTo((startPoint!!.x + (wavePerWidth * (i * 2 - 1))).toFloat(), (startPoint!!.y + waveHeight).toFloat(),
                            (startPoint!!.x + (wavePerWidth * 2) * i).toFloat(), startPoint!!.y.toFloat())
                }
            }

            //绘制封闭的曲线
            wavePath.lineTo(width.toFloat(), height.toFloat())//右下角，lineTo是从最后绘制的点开始
            wavePath.lineTo(startPoint!!.x.toFloat(), height.toFloat())//左下角
            wavePath.lineTo(startPoint!!.x.toFloat(), startPoint!!.y.toFloat())//起点
            wavePath.close()
            canvas.drawPath(wavePath, wavePaint)

            //中间的进度文字
            val rect = Rect(0, 0, width, height)
            //test
            /*val rectPaint = Paint()
            rectPaint.color = Color.BLUE
            rectPaint.style = Paint.Style.FILL
            canvas.drawRect(rect, rectPaint)*/

            val fontMetrics = progressPaint.fontMetrics
            val top = fontMetrics.top//为基线到字体上边框的距离,即上图中的top
            val bottom = fontMetrics.bottom//为基线到字体下边框的距离,即上图中的bottom
            val baseLineY = (rect.centerY() - top / 2 - bottom / 2) //基线中间点的y轴计算公式
            canvas.drawText(progress.toString() + "%", rect.centerX().toFloat(), baseLineY, progressPaint)

            ////判断是不是平移完了一个周期
            if (startPoint!!.x + waveMoveDistance >= 0) {
                //满了一个周期则恢复默认起点继续平移
                startPoint!!.x -= wavePerWidth * 4
            }
            //每次波形的平移量 40
            startPoint!!.x += waveMoveDistance

            Log.i(TAG, "startPoint: $startPoint")

            if (autoIncrement) {
                if (progress >= 100) {
                    progress = 0
                } else {
                    progress++
                }
            }

            postInvalidateDelayed(150)
            wavePath.reset()
        }
    }

    /** 设置进度 */
    fun updateProgress(currentProgress: Int) {
        progress = currentProgress
        //如果添加的话，会重叠刷新，直观就是速度变快（刷新频率变快了）
//        postInvalidateDelayed(150)
    }
}
