using Microsoft.AspNetCore.Mvc;
using MemoryGameAPI.Models;

namespace MemoryGameAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class Scores2Controller : Controller
    {


        //POST /api/Scores2
        [HttpPost]
        public IActionResult SubmitScores(string Username, int CompletionTimeSeconds)
        {
            Score requestScore = new Score();

            //read the existing score inside db
            string sql = @$"SELECT * FROM Score WHERE Score.Username = {Username}";

            // MySqlCommand cmd = new MySqlCommand(sql, conn); // This line is incomplete and will cause a compile error. You may need to fix it based on your context.
        }
    }
}
