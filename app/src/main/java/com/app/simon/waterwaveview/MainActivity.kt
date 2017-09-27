package com.app.simon.waterwaveview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assignViews()
    }

    private fun assignViews() {
        val arrayList = ArrayList<Int>()
        arrayList.add(0)
        arrayList.add(10)
        arrayList.add(25)
        arrayList.add(40)
        arrayList.add(53)
        arrayList.add(68)
        arrayList.add(82)
        arrayList.add(100)
        doAsync {
            arrayList.forEach {
                val progress = it
                uiThread {
                    water_wave_view.updateProgress(progress)
                }
                Thread.sleep(2000)
            }
        }
    }
}
