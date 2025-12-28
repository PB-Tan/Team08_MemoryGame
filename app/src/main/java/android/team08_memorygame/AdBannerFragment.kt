package android.team08_memorygame

import android.os.Bundle
import android.team08_memorygame.databinding.FragmentAdBannerBinding
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import org.json.JSONArray
import java.net.URL
import kotlinx.coroutines.withContext

data class  AdContent(val id: Int,
                      val name: String,
                      val imageUrl: String,
                      val redirectUrl: String,
                      val bitmap: Bitmap? = null
)
class AdBannerFragment : Fragment() {
    private var _binding: FragmentAdBannerBinding?=null
    private val binding get()=_binding!!

    // hardcode advertisements for now
//    private val adList = listOf(
//        AdContent(R.drawable.ad, "https://www.google.com"),
//        AdContent(R.drawable.ad_mc, "https://www.mcdonalds.com"),
//        AdContent(R.drawable.ad_nike, "https://www.nike.com"),
//    )

    private var adList = mutableListOf<AdContent>()

    // track which ad is showing
    private var currentAdIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAdBannerBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // fetch backend data
        fetchBackendData()
    }

    private fun fetchBackendData(){
        // launch coroutine to do network work
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
            try{
                // fetch JSON data
                val jsonStr = URL("http://10.0.2.2:5254/api/advertisements").readText()

                // parse JSON
                val jsonArray = JSONArray(jsonStr)
                val tempList = mutableListOf<AdContent>()

                for(i in 0 until jsonArray.length()){
                    val item = jsonArray.getJSONObject(i)
                    val id = item.getInt("id")
                    val name = item.getString("name")
                    val imageUrl = item.getString("imageUrl")
                    val url = item.getString("url")

                    // download image immediately the manual way
                    // open a stream to image url and decode it into bitmap
                    val imageStream = URL(imageUrl).openStream()
                    val bitmap = BitmapFactory.decodeStream(imageStream)

                    tempList.add(AdContent(id,name,imageUrl,url,bitmap))
                }

                // update UI on main thread
                withContext(Dispatchers.Main){
                    adList.clear()
                    adList.addAll(tempList)

                    // only start carousel if ads are found
                    if(adList.isNotEmpty()){
                        startCarousel()
                    }
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    private fun startCarousel(){
        // start 30 sec timer loop here using the carousel feature Coroutines
        // safer than timers since they stop automatically when user closes the app
        viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                updateAdUI()
                // change this to 30 for real application. 5 for testing
                delay(5_000)
                // move to next index and loop back to 0 if at end
                currentAdIndex = (currentAdIndex + 1) % adList.size
            }
        }
    }

    private fun updateAdUI(){
        if(_binding == null || adList.isEmpty()) return
        val currentAd = adList[currentAdIndex]
        //update image
        binding.apply {
            if(currentAd.bitmap != null){
                adImageView.setImageBitmap(currentAd.bitmap)
            }
            adImageView.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("url_to_load", currentAd.redirectUrl)
                }
                findNavController().navigate(R.id.action_adBannerFragment_to_adWebViewFragment,bundle)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}