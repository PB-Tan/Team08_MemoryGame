namespace MemoryGameAPI.Models;

// 登录请求
public class LoginRequest
{
    public string Username { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
}

// 登录响应
public class LoginResponse
{
    public bool Success { get; set; }
    public int UserId { get; set; }
    public string Username { get; set; } = string.Empty;
    public bool IsPaid { get; set; }
    public string Message { get; set; } = string.Empty;
}

// 提交分数请求
public class ScoreRequest
{
    public string Username { get; set; } = string.Empty;
    public int CompletionTimeSeconds { get; set; }
}

// 创建房间请求
public class CreateRoomRequest
{
    public string RoomName { get; set; } = string.Empty;
    public string HostUsername { get; set; } = string.Empty;
    public int MaxPlayers { get; set; } = 8;
}

// 加入房间请求
public class JoinRoomRequest
{
    public string RoomCode { get; set; } = string.Empty;
    public string Username { get; set; } = string.Empty;
}

// 开始游戏请求
public class StartGameRequest
{
    public string Username { get; set; } = string.Empty;
}

// 提交房间分数请求
public class SubmitRoomScoreRequest
{
    public string RoomCode { get; set; } = string.Empty;
    public string Username { get; set; } = string.Empty;
    public int CompletionTimeSeconds { get; set; }
}

// 退出房间请求
public class LeaveRoomRequest
{
    public string Username { get; set; } = string.Empty;
}
