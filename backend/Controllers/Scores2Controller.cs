using Microsoft.AspNetCore.Mvc;
using MemoryGameAPI.Models;
using MemoryGameAPI.Repositories;

namespace MemoryGameAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class Scores2Controller : Controller
    {
        private readonly UserRepository _userRepo;
        private readonly ScoreRepository _scoreRepo;
        // Constructor injection
        public Scores2Controller(UserRepository userRepo, ScoreRepository scoreRepo)
        {
            _userRepo = userRepo;
            _scoreRepo = scoreRepo;
        }

        //POST /api/Scores2
        [HttpPost]
        public IActionResult SubmitScores([FromForm]string reqUsername,[FromForm] int reqCompletionTimeSeconds)
        {

            //First, we check if user exists in db
            User? reqUser = _userRepo.findUserByUsername(reqUsername);
            var userNotFoundMessage = new
            {
                Success = false,
                Message = "user not found"
            };
            if (reqUser == null) return new ObjectResult(userNotFoundMessage);

            // User now is validated to exists in database
            // Second, we check if scores exists.
            // If scores does not exists, then user is a first-time player
            // we will create a fresh score for new user.
            Score? existingScore =_scoreRepo.GetScoreByUsername(reqUsername);
            if (existingScore == null)
            {
                Score newScore = new Score()
                {
                    Username = reqUsername,
                    UserId = reqUser.UserId,
                    CompletionTimeSeconds = reqCompletionTimeSeconds,
                    CreatedAt = DateTime.Now                    
                };
               _scoreRepo.CreateScore(newScore);

                // we now exit the method by return a Success result
                var newScoreCreatedMessage = new
                {
                    Success = true,
                    Message = "User is first time player. Score has been created"
                };
                return new ObjectResult(newScoreCreatedMessage);
            }
            
            // By this point, user has an existing score inside database
            // third, we test if existing score is better or worse than incoming score
            // if incoming score is better, we update the score
            if (existingScore.CompletionTimeSeconds > reqCompletionTimeSeconds)
            {
                Score newScore = new Score()
                {
                    ScoreId = existingScore.ScoreId,
                    Username = reqUsername,
                    UserId = reqUser.UserId,
                    CompletionTimeSeconds = reqCompletionTimeSeconds,
                    CreatedAt = DateTime.Now
                };
               _scoreRepo.UpdateScore(newScore);

                // we now exit the method by returning a Success result
                var newScoreUpdatedMessage = new
                {
                    Success = true,
                    Message = "User's Score has been updated"
                };
                return new ObjectResult(newScoreUpdatedMessage);
            }

            //By this point, the user exists and score has existed. But existing score is worse
            //Thus, we do not make any changes to the database, and exit the program.
            var noScoreUpdatedMessage = new
            {
                Success = true,
                Message = "User did not perform better than previous attempt. No records were updated"
            };
            return new ObjectResult(noScoreUpdatedMessage);
        }

        [HttpGet("top5")]
        //GET /api/Scores2/top5
        public IActionResult GetTop5Scores()
        {
            List<Score> top5 =_scoreRepo.GetTop5Scores();
            return new ObjectResult(top5);
        }

        //GET /api/Scores2/
        //For testing purposes
        [HttpGet("AllScores")]
        public IActionResult GetAllScores()
        {
            List<Score> allScores = new List<Score>();
            allScores =_scoreRepo.GetAllScores();
            return new ObjectResult(allScores);
        }

       
       
    }
}
