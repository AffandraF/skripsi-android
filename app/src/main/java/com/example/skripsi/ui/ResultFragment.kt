package com.example.skripsi.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.skripsi.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ResultFragment : BottomSheetDialogFragment() {

    private var classificationResult: String? = null
    private var confidence: String? = null
    private var recommendations: String? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            classificationResult = it.getString(ARG_RESULT)
            confidence = it.getString(ARG_CONFIDENCE)
            recommendations = it.getString(ARG_RECOMMENDATIONS)
            imageUri = it.getParcelable(ARG_IMAGE_URI)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        val resultTextView = view.findViewById<TextView>(R.id.classificationResult)
        val confidenceTextView = view.findViewById<TextView>(R.id.classificationConfidence)
        val recommendationsTextView = view.findViewById<TextView>(R.id.recommendationsText)
        val resultImageView = view.findViewById<ImageView>(R.id.resultImage)

        resultTextView.text = classificationResult ?: "Unknown Disease"
        confidenceTextView.text = confidence ?: "Unknown"
        recommendationsTextView.text = recommendations ?: "No recommendations available"

        imageUri?.let {
            resultImageView.setImageURI(it)
        }

        return view
    }

    companion object {
        private const val ARG_RESULT = "classificationResult"
        private const val ARG_CONFIDENCE = "confidence"
        private const val ARG_RECOMMENDATIONS = "recommendations"
        private const val ARG_IMAGE_URI = "imageUri"

        fun newInstance(result: String, confidence: String, recommendations: String?, imageUri: Uri) =
            ResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RESULT, result)
                    putString(ARG_CONFIDENCE, confidence)
                    putString(ARG_RECOMMENDATIONS, recommendations)
                    putParcelable(ARG_IMAGE_URI, imageUri)
                }
            }
    }
}
