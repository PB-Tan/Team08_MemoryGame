namespace MemoryGameAPI.Models;

// Login request
public class LoginRequest
{
    public string Username { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
}

// Login response
public class LoginResponse
{
    public bool Success { get; set; }
    public int UserId { get; set; }
    public string Username { get; set; } = string.Empty;
    public bool IsPaid { get; set; }
    public string Message { get; set; } = string.Empty;
}

// Submit score request
public class ScoreRequest
{
    public string Username { get; set; } = string.Empty;
    public int CompletionTimeSeconds { get; set; }
}

// Create room request
public class CreateRoomRequest
{
    public string RoomName { get; set; } = string.Empty;
    public string HostUsername { get; set; } = string.Empty;
    public int MaxPlayers { get; set; } = 8;
}

// Join room request
public class JoinRoomRequest
{
    public string RoomCode { get; set; } = string.Empty;
    public string Username { get; set; } = string.Empty;
}

// Start game request
public class StartGameRequest
{
    public string Username { get; set; } = string.Empty;
}

// Submit room score request
public class SubmitRoomScoreRequest
{
    public string RoomCode { get; set; } = string.Empty;
    public string Username { get; set; } = string.Empty;
    public int CompletionTimeSeconds { get; set; }
}

// Leave room request
public class LeaveRoomRequest
{
    public string Username { get; set; } = string.Empty;
}
