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
    /// 提交游戏分数
    /// POST /api/scores
    /// </summary>
    [HttpPost]
    public async Task<IActionResult> SubmitScore([FromBody] ScoreRequest request)
    {
        _logger.LogInformation($"收到分数提交: {request.Username} - {request.CompletionTimeSeconds}秒");

        // 验证输入
        if (string.IsNullOrWhiteSpace(request.Username))
        {
            return BadRequest(new { success = false, message = "用户名不能为空" });
        }

        if (request.CompletionTimeSeconds <= 0)
        {
            return BadRequest(new { success = false, message = "无效的完成时间" });
        }

        // 查找用户ID
        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Username.ToLower() == request.Username.ToLower());

        if (user == null)
        {
            return BadRequest(new { success = false, message = "用户不存在" });
        }

        // 查找用户的现有最佳成绩
        var existingScore = await _context.Scores
            .Where(s => s.UserId == user.UserId)
            .OrderBy(s => s.CompletionTimeSeconds)
            .FirstOrDefaultAsync();

        if (existingScore == null || request.CompletionTimeSeconds < existingScore.CompletionTimeSeconds)
        {
            // 如果没有现有成绩，或新成绩更好，则保存
            if (existingScore == null)
            {
                // 创建新的分数记录
                var score = new Score
                {
                    UserId = user.UserId,
                    Username = user.Username,
                    CompletionTimeSeconds = request.CompletionTimeSeconds,
                    CreatedAt = DateTime.Now
                };

                _context.Scores.Add(score);
            }
            else
            {
                // 更新现有成绩
                existingScore.CompletionTimeSeconds = request.CompletionTimeSeconds;
                existingScore.CreatedAt = DateTime.Now;
            }

            await _context.SaveChangesAsync();
            _logger.LogInformation($"分数保存成功: {user.Username} - 最佳成绩: {request.CompletionTimeSeconds}秒");
            return Ok(new { success = true, message = "分数提交成功", isNewBest = true });
        }
        else
        {
            // 成绩不如现有最佳成绩，不保存
            _logger.LogInformation($"分数未保存: {user.Username} - 当前最佳: {existingScore.CompletionTimeSeconds}秒，新成绩: {request.CompletionTimeSeconds}秒");
            return Ok(new { success = true, message = "成绩未超越最佳记录", isNewBest = false });
        }
    }

    /// <summary>
    /// 获取排行榜 Top 5
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
    /// 获取所有分数（调试用）
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
    /// 清空所有分数（调试用）
    /// DELETE /api/scores
    /// </summary>
    [HttpDelete]
    public async Task<IActionResult> ClearScores()
    {
        _context.Scores.RemoveRange(_context.Scores);
        await _context.SaveChangesAsync();

        return Ok(new { message = "所有分数已清空" });
    }
}
