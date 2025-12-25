namespace MemoryGameAPI.Models;

public class Room
{
    public string RoomId { get; set; } = string.Empty;
    public string RoomCode { get; set; } = string.Empty;
    public string RoomName { get; set; } = string.Empty;
    public string HostUsername { get; set; } = string.Empty;
    public int MaxPlayers { get; set; } = 8;
    public DateTime CreatedAt { get; set; } = DateTime.Now;
    public RoomStatus Status { get; set; } = RoomStatus.Waiting;

    // 导航属性
    public ICollection<RoomMember> Members { get; set; } = new List<RoomMember>();
}

public enum RoomStatus
{
    Waiting = 0,
    Playing = 1,
    Finished = 2
}

public class RoomMember
{
    public int Id { get; set; }
    public string RoomId { get; set; } = string.Empty;
    public string Username { get; set; } = string.Empty;
    public bool IsHost { get; set; }
    public DateTime JoinedAt { get; set; } = DateTime.Now;
    public int? CompletionTimeSeconds { get; set; }
    public bool HasFinished { get; set; } = false;

    // 导航属性
    public Room? Room { get; set; }
}
