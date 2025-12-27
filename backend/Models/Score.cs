namespace MemoryGameAPI.Models;

public class Score
{
    public int ScoreId { get; set; }
    public int UserId { get; set; }
    public string Username { get; set; } = string.Empty;
    public int CompletionTimeSeconds { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.Now;

    // 导航属性
    public User? User { get; set; }
}
