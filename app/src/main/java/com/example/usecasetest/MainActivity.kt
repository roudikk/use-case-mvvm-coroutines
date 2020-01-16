package com.example.usecasetest

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "Use Case Test"

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel.upload().observe(this, Observer {
            progressBar.progressTintList = ColorStateList.valueOf(
                if (it.progress == 100) {
                    Color.parseColor("#4CAF50")
                } else {
                    Color.parseColor("#03A9F4")
                }
            )
            progressBar.progress = it.progress
            appendTextView("Progress: ${it.progress}, Result: ${it.result}")
        })

        viewModel.viewState().observe(this, Observer {
            appendTextView(
                when (it) {
                    is MainViewModel.ViewState.Loading -> {
                        progressBar.progressBackgroundTintList = ColorStateList.valueOf(
                            Color.parseColor("#FFFFFF")
                        )
                        textView.text = null
                        "Loading.."
                    }
                    is MainViewModel.ViewState.Error -> {
                        progressBar.progressBackgroundTintList = ColorStateList.valueOf(
                            Color.parseColor("#E91E63")
                        )
                        progressBar.progress = 0
                        "Error: ${it.exception}"
                    }
                    is MainViewModel.ViewState.Cancelled -> {
                        progressBar.progressBackgroundTintList = ColorStateList.valueOf(
                            Color.parseColor("#FF9800")
                        )
                        progressBar.progress = 0
                        "Cancelled!"
                    }
                    is MainViewModel.ViewState.Success -> "Completed!"
                }
            )
        })

        viewModel.backgroundTask().observe(this, Observer {
            Toast.makeText(this, R.string.background_task_finished, Toast.LENGTH_LONG).show()
        })

        startOver.setOnClickListener { viewModel.loadData(true) }
        cancel.setOnClickListener { viewModel.cancel() }
        error.setOnClickListener { viewModel.throwError() }
    }

    private fun appendTextView(text: String) {
        textView.append("$text\n")
    }
}