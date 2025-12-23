package android.team08_memorygame

import android.os.Bundle
import android.team08_memorygame.databinding.FragmentAdWebViewBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.activity.ComponentDialog

class AdWebViewFragment : androidx.fragment.app.DialogFragment() {
    private var _binding: FragmentAdWebViewBinding?=null
    private val binding get()=_binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAdWebViewBinding.inflate(inflater,container,false)
        initUI()
        binding.webview.loadUrl("https://www.google.com")
        return binding.root
    }

    private fun initUI(){
        binding.webview.webViewClient = WebViewClient()
        binding.webview.settings.javaScriptEnabled=true
        binding.webview.webChromeClient=object: WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if(newProgress==100){
                    binding.progressBar.visibility=View.GONE
                } else {
                    binding.progressBar.visibility=View.VISIBLE
                    binding.progressBar.progress=newProgress
                }
            }
        }
        binding.progressBar.setMax(100)
//        this is referencing the back button that comes with the android os
        (requireDialog() as ComponentDialog).onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed(){
                findNavController().navigate(R.id.action_adWebViewFragment_to_premiumFragment)
            }
        })
        binding.closeAdButton.setOnClickListener {
            findNavController().navigate(R.id.action_adWebViewFragment_to_premiumFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}