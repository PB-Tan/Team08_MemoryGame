using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MemoryGameAPI.Data;
using MemoryGameAPI.Models;

namespace MemoryGameAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ScoresController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly ILogger<ScoresController> _logger;

    public ScoresController(AppDbContext context, ILogger<ScoresController> logger)
    {
        _context = context;
        _logger = logger;
    }

    /// <summary>
    /// Submit game score
    /// POST /api/scores
    /// </summary>
    [HttpPost]
    public async Task<IActionResult> SubmitScore([FromBody] ScoreRequest request)
    {
        //incoming request body should contain username from session after authentication?
        _logger.LogInformation($"Received score submission: {request.Username} - {request.CompletionTimeSeconds} seconds");

        // Validate input
        if (string.IsNullOrWhiteSpace(request.Username))
        {
            return BadRequest(new { success = false, message = "Username cannot be empty" });
        }

        if (request.CompletionTimeSeconds <= 0)
        {
            return BadRequest(new { success = false, message = "Invalid completion time" });
        }

        // Check whether user exists inside database
        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Username.ToLower() == request.Username.ToLower());

        if (user == null)
        {
            return BadRequest(new { success = false, message = "User does not exist" });
        }

        // Find user's existing best score
        var existingScore = await _context.Scores
            .Where(s => s.UserId == user.UserId)
            .OrderBy(s => s.CompletionTimeSeconds)
            .FirstOrDefaultAsync();

        if (existingScore == null || request.CompletionTimeSeconds < existingScore.CompletionTimeSeconds)
        {
            // If no existing score or new score is better, save it
            if (existingScore == null)
            {
                // Create new score record
                var score = new Score
                {
                    UserId = user.UserId,
                    Username = user.Username,
                    CompletionTimeSeconds = request.CompletionTimeSeconds,
                    CreatedAt = DateTime.Now
                };

                //saving the best score/new score
                _context.Scores.Add(score);
            }
            else
            {
                // update existing score to best score 
                existingScore.CompletionTimeSeconds = request.CompletionTimeSeconds;
                existingScore.CreatedAt = DateTime.Now;
            }

            await _context.SaveChangesAsync();
            _logger.LogInformation($"Score saved successfully: {user.Username} - Best time: {request.CompletionTimeSeconds} seconds");
            return Ok(new { success = true, message = "Score submitted successfully", isNewBest = true });
        }
        else
        {
            // score 
            // Score not better than existing best, not saved
            _logger.LogInformation($"Score not saved: {user.Username} - Current best: {existingScore.CompletionTimeSeconds} seconds, new score: {request.CompletionTimeSeconds} seconds");
            return Ok(new { success = true, message = "Score did not beat best record", isNewBest = false });
        }
    }

    /// <summary>
    /// Get leaderboard Top 5
    /// GET /api/scores/top5
    /// </summary>
    [HttpGet("top5")]
    public async Task<IActionResult> GetTop5()
    {
        var scores = await _context.Scores
            .OrderBy(s => s.CompletionTimeSeconds)
            .Take(5)
            .ToListAsync();

        var top5 = scores.Select((s, index) => new
        {
            rank = index + 1,
            username = s.Username,
            completionTimeSeconds = s.CompletionTimeSeconds,
            formattedTime = TimeSpan.FromSeconds(s.CompletionTimeSeconds).ToString(@"mm\:ss"),
            createdAt = s.CreatedAt
        }).ToList();

        return Ok(top5);
    }

    /// <summary>
    /// Get all scores (for debugging)
    /// GET /api/scores
    /// </summary>
    [HttpGet]
    public async Task<IActionResult> GetAllScores()
    {
        var scores = await _context.Scores
            .OrderBy(s => s.CompletionTimeSeconds)
            .ToListAsync();

        return Ok(scores);
    }

    /// <summary>
    /// Clear all scores (for debugging)
    /// DELETE /api/scores
    /// </summary>
    [HttpDelete]
    public async Task<IActionResult> ClearScores()
    {
        _context.Scores.RemoveRange(_context.Scores);
        await _context.SaveChangesAsync();

        return Ok(new { message = "All scores cleared" });
    }
}