package com.example.projectuas

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SimulasiActivity : AppCompatActivity() {
    private lateinit var interpreter: Interpreter
    private val mModelPath = "gender.tflite"

    private lateinit var resultText: TextView
    private lateinit var long_hair: EditText
    private lateinit var forehead_width_cm: EditText
    private lateinit var forehead_height_cm: EditText
    private lateinit var nose_wide: EditText
    private lateinit var nose_long: EditText
    private lateinit var lips_thin: EditText
    private lateinit var distance_nose_to_lip_long: EditText
    private lateinit var checkButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulasi)

        resultText = findViewById(R.id.tvHasil)
        long_hair = findViewById(R.id.long_hair)
        forehead_width_cm = findViewById(R.id.forehead_width_cm)
        forehead_height_cm = findViewById(R.id.forehead_height_cm)
        nose_wide = findViewById(R.id.nose_wide)
        nose_long = findViewById(R.id.nose_long)
        lips_thin = findViewById(R.id.lips_thin)
        distance_nose_to_lip_long = findViewById(R.id.distance_nose_to_lip_long)
        checkButton = findViewById(R.id.btnPredict)

        checkButton.setOnClickListener {
            var result = doInference(
                long_hair.text.toString(),
                forehead_width_cm.text.toString(),
                forehead_height_cm.text.toString(),
                nose_wide.text.toString(),
                nose_long.text.toString(),
                lips_thin.text.toString(),
                distance_nose_to_lip_long.text.toString())
            runOnUiThread {
                resultText.text = if (result == 0) "Male" else "Female"
            }
        }
        initInterpreter()
    }

    private fun initInterpreter() {
        val options = Interpreter.Options().apply {
            setNumThreads(4)
            setUseNNAPI(false) // Matikan penggunaan NNAPI
        }
        interpreter = Interpreter(loadModelFile(assets, mModelPath), options)
    }

    private fun doInference(input1: String, input2: String, input3: String, input4: String, input5: String, input6: String, input7: String): Int{
        val inputVal = FloatArray(7)
        inputVal[0] = input1.toFloat()
        inputVal[1] = input2.toFloat()
        inputVal[2] = input3.toFloat()
        inputVal[3] = input4.toFloat()
        inputVal[4] = input5.toFloat()
        inputVal[5] = input6.toFloat()
        inputVal[6] = input7.toFloat()
        val output = Array(1) { FloatArray(1) }
        interpreter.run(inputVal, output)

        Log.e("result", (output[0].toList()+" ").toString())

        return if (output[0][0] >= 0.5f) 1 else 0
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}