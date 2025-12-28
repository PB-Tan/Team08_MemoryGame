using MemoryGameAPI.Data;
using MemoryGameAPI.Models;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace MemoryGameAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class HaziqAdsController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        private readonly AdData _advertisementData;

        public HaziqAdsController(IConfiguration configuration)
        {
            _configuration = configuration;
            string connectionString = _configuration.GetConnectionString("DefaultConnection");
            _advertisementData = new AdData(connectionString);
        }

        [HttpGet]
        public ActionResult<IEnumerable<Advertisement>> Get()
        {
            List<Advertisement> ads = _advertisementData.GetAdvertisements();
            Response.Headers.Append("X-Total-Count", ads.Count.ToString());
            Response.Headers.Append("X-Author", "Zeke211");
            Response.Headers.Append("X-Server-Time", DateTime.Now.ToString());

            return Ok(ads);
        }
    }
}
}
