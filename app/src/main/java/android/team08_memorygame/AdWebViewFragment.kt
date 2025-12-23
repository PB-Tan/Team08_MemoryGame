package android.team08_memorygame

import android.os.Bundle
import android.team08_memorygame.databinding.FragmentAdWebViewBinding
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class AdWebViewFragment : Fragment() {
    private var _binding: FragmentAdWebViewBinding?=null
    private val binding get()=_binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}