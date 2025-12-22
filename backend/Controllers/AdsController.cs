using Microsoft.AspNetCore.Mvc;

namespace MemoryGameAPI.Controllers;

[ApiController]
[Route("api/images")]
public class AdsController : ControllerBase
{
    private readonly ILogger<AdsController> _logger;

    // 静态广告图片URL列表
    private static readonly List<string> AdUrls = new()
    {
        "https://via.placeholder.com/800x200/FF6347/FFFFFF?text=Ad+1",
        "https://via.placeholder.com/800x200/4682B4/FFFFFF?text=Ad+2",
        "https://via.placeholder.com/800x200/32CD32/FFFFFF?text=Ad+3",
        "https://via.placeholder.com/800x200/FFD700/000000?text=Ad+4",
        "https://via.placeholder.com/800x200/FF1493/FFFFFF?text=Ad+5"
    };

    public AdsController(ILogger<AdsController> logger)
    {
        _logger = logger;
    }

    /// <summary>
    /// 获取所有广告图片
    /// GET /api/images/ads
    /// </summary>
    [HttpGet("ads")]
    public ActionResult<IEnumerable<string>> GetAds()
    {
        _logger.LogInformation("获取广告列表");
        return Ok(AdUrls);
    }

    /// <summary>
    /// 获取随机广告
    /// GET /api/images/ads/random
    /// </summary>
    [HttpGet("ads/random")]
    public ActionResult GetRandomAd()
    {
        if (!AdUrls.Any())
        {
            return NotFound(new { message = "没有可用的广告" });
        }

        var random = new Random();
        var randomAd = AdUrls[random.Next(AdUrls.Count)];

        _logger.LogInformation($"返回随机广告: {randomAd}");
        return Ok(new { imageUrl = randomAd });
    }
}
