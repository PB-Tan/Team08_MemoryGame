using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MemoryGameAPI.Data;
using MemoryGameAPI.Models;

namespace MemoryGameAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class RoomsController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly ILogger<RoomsController> _logger;

    public RoomsController(AppDbContext context, ILogger<RoomsController> logger)
    {
        _context = context;
        _logger = logger;
    }

    /// <summary>
    /// 创建房间
    /// POST /api/rooms
    /// </summary>
    [HttpPost]
    public async Task<IActionResult> CreateRoom([FromBody] CreateRoomRequest request)
    {
        _logger.LogInformation($"创建房间请求: {request.HostUsername}");

        if (string.IsNullOrWhiteSpace(request.HostUsername))
        {
            return BadRequest(new { success = false, message = "房主用户名不能为空" });
        }

        // 生成唯一房间码
        string roomCode = GenerateRoomCode();
        while (await _context.Rooms.AnyAsync(r => r.RoomCode == roomCode))
        {
            roomCode = GenerateRoomCode();
        }

        // 创建房间
        var room = new Room
        {
            RoomId = Guid.NewGuid().ToString(),
            RoomCode = roomCode,
            RoomName = string.IsNullOrWhiteSpace(request.RoomName)
                ? $"{request.HostUsername}的房间"
                : request.RoomName,
            HostUsername = request.HostUsername,
            MaxPlayers = request.MaxPlayers,
            Status = RoomStatus.Waiting,
            CreatedAt = DateTime.Now
        };

        _context.Rooms.Add(room);

        // 添加房主为第一个成员
        var hostMember = new RoomMember
        {
            RoomId = room.RoomId,
            Username = request.HostUsername,
            IsHost = true,
            JoinedAt = DateTime.Now
        };

        _context.RoomMembers.Add(hostMember);
        await _context.SaveChangesAsync();

        _logger.LogInformation($"房间创建成功: {room.RoomCode}");

        return Ok(new
        {
            success = true,
            roomCode = room.RoomCode,
            roomId = room.RoomId,
            roomName = room.RoomName,
            message = "房间创建成功"
        });
    }

    /// <summary>
    /// 加入房间
    /// POST /api/rooms/join
    /// </summary>
    [HttpPost("join")]
    public async Task<IActionResult> JoinRoom([FromBody] JoinRoomRequest request)
    {
        _logger.LogInformation($"加入房间请求: {request.Username} -> {request.RoomCode}");

        // 查找房间
        var room = await _context.Rooms
            .Include(r => r.Members)
            .FirstOrDefaultAsync(r => r.RoomCode == request.RoomCode);

        if (room == null)
        {
            return NotFound(new { success = false, message = "房间不存在" });
        }

        if (room.Status != RoomStatus.Waiting)
        {
            return BadRequest(new { success = false, message = "游戏已开始，无法加入" });
        }

        if (room.Members.Count >= room.MaxPlayers)
        {
            return BadRequest(new { success = false, message = "房间已满" });
        }

        // 检查是否已在房间中
        if (room.Members.Any(m => m.Username == request.Username))
        {
            return BadRequest(new { success = false, message = "已在房间中" });
        }

        // 加入房间
        var member = new RoomMember
        {
            RoomId = room.RoomId,
            Username = request.Username,
            IsHost = false,
            JoinedAt = DateTime.Now
        };

        _context.RoomMembers.Add(member);
        await _context.SaveChangesAsync();

        _logger.LogInformation($"{request.Username} 加入房间 {request.RoomCode}");

        return Ok(new
        {
            success = true,
            roomId = room.RoomId,
            roomName = room.RoomName,
            hostUsername = room.HostUsername,
            memberCount = room.Members.Count + 1,
            maxPlayers = room.MaxPlayers,
            message = "加入房间成功"
        });
    }

    /// <summary>
    /// 获取房间信息
    /// GET /api/rooms/{roomCode}
    /// </summary>
    [HttpGet("{roomCode}")]
    public async Task<IActionResult> GetRoom(string roomCode)
    {
        var room = await _context.Rooms
            .Include(r => r.Members)
            .FirstOrDefaultAsync(r => r.RoomCode == roomCode);

        if (room == null)
        {
            return NotFound(new { success = false, message = "房间不存在" });
        }

        return Ok(new
        {
            roomId = room.RoomId,
            roomCode = room.RoomCode,
            roomName = room.RoomName,
            hostUsername = room.HostUsername,
            status = room.Status.ToString(),
            memberCount = room.Members.Count,
            maxPlayers = room.MaxPlayers,
            members = room.Members.Select(m => new
            {
                username = m.Username,
                isHost = m.IsHost,
                hasFinished = m.HasFinished,
                completionTimeSeconds = m.CompletionTimeSeconds
            })
        });
    }

    /// <summary>
    /// 开始游戏（房主操作）
    /// POST /api/rooms/{roomCode}/start
    /// </summary>
    [HttpPost("{roomCode}/start")]
    public async Task<IActionResult> StartGame(string roomCode, [FromBody] StartGameRequest request)
    {
        _logger.LogInformation($"开始游戏请求: {roomCode} by {request.Username}");

        var room = await _context.Rooms.FirstOrDefaultAsync(r => r.RoomCode == roomCode);

        if (room == null)
        {
            return NotFound(new { success = false, message = "房间不存在" });
        }

        // 验证是否是房主
        if (room.HostUsername != request.Username)
        {
            return BadRequest(new { success = false, message = "只有房主可以开始游戏" });
        }

        if (room.Status != RoomStatus.Waiting)
        {
            return BadRequest(new { success = false, message = "游戏已开始" });
        }

        room.Status = RoomStatus.Playing;
        await _context.SaveChangesAsync();

        _logger.LogInformation($"房间 {roomCode} 游戏开始");

        return Ok(new { success = true, message = "游戏开始" });
    }

    /// <summary>
    /// 提交房间内游戏成绩
    /// POST /api/rooms/{roomCode}/scores
    /// </summary>
    [HttpPost("{roomCode}/scores")]
    public async Task<IActionResult> SubmitRoomScore(string roomCode, [FromBody] SubmitRoomScoreRequest request)
    {
        _logger.LogInformation($"房间分数提交: {roomCode} - {request.Username} - {request.CompletionTimeSeconds}秒");

        var room = await _context.Rooms
            .Include(r => r.Members)
            .FirstOrDefaultAsync(r => r.RoomCode == roomCode);

        if (room == null)
        {
            return NotFound(new { success = false, message = "房间不存在" });
        }

        var member = room.Members.FirstOrDefault(m => m.Username == request.Username);

        if (member == null)
        {
            return NotFound(new { success = false, message = "成员不在房间中" });
        }

        // 记录成绩
        member.CompletionTimeSeconds = request.CompletionTimeSeconds;
        member.HasFinished = true;

        await _context.SaveChangesAsync();

        // 检查是否所有人都完成了
        bool allFinished = room.Members.All(m => m.HasFinished);

        if (allFinished)
        {
            room.Status = RoomStatus.Finished;
            await _context.SaveChangesAsync();
            _logger.LogInformation($"房间 {roomCode} 所有玩家已完成");
        }

        return Ok(new
        {
            success = true,
            allFinished = allFinished,
            message = "分数提交成功"
        });
    }

    /// <summary>
    /// 获取房间排行榜
    /// GET /api/rooms/{roomCode}/leaderboard
    /// </summary>
    [HttpGet("{roomCode}/leaderboard")]
    public async Task<IActionResult> GetRoomLeaderboard(string roomCode)
    {
        var room = await _context.Rooms
            .Include(r => r.Members)
            .FirstOrDefaultAsync(r => r.RoomCode == roomCode);

        if (room == null)
        {
            return NotFound(new { success = false, message = "房间不存在" });
        }

        var leaderboard = room.Members
            .Where(m => m.HasFinished)
            .OrderBy(m => m.CompletionTimeSeconds)
            .Select((m, index) => new
            {
                rank = index + 1,
                username = m.Username,
                isHost = m.IsHost,
                completionTimeSeconds = m.CompletionTimeSeconds,
                formattedTime = TimeSpan.FromSeconds(m.CompletionTimeSeconds ?? 0).ToString(@"mm\:ss")
            })
            .ToList();

        var unfinished = room.Members
            .Where(m => !m.HasFinished)
            .Select(m => new
            {
                username = m.Username,
                isHost = m.IsHost,
                status = "未完成"
            })
            .ToList();

        return Ok(new
        {
            roomName = room.RoomName,
            finished = leaderboard,
            unfinished = unfinished
        });
    }

    /// <summary>
    /// 退出房间
    /// POST /api/rooms/{roomCode}/leave
    /// </summary>
    [HttpPost("{roomCode}/leave")]
    public async Task<IActionResult> LeaveRoom(string roomCode, [FromBody] LeaveRoomRequest request)
    {
        _logger.LogInformation($"退出房间: {request.Username} 离开 {roomCode}");

        var room = await _context.Rooms
            .Include(r => r.Members)
            .FirstOrDefaultAsync(r => r.RoomCode == roomCode);

        if (room == null)
        {
            return NotFound(new { success = false, message = "房间不存在" });
        }

        var member = room.Members.FirstOrDefault(m => m.Username == request.Username);

        if (member == null)
        {
            return NotFound(new { success = false, message = "成员不在房间中" });
        }

        // 移除成员
        _context.RoomMembers.Remove(member);

        // 如果房主离开且房间还有人，转移房主
        if (member.IsHost && room.Members.Count > 1)
        {
            var newHost = room.Members.First(m => m.Id != member.Id);
            newHost.IsHost = true;
            room.HostUsername = newHost.Username;
            _logger.LogInformation($"房主转移: {newHost.Username}");
        }

        // 如果房间空了，删除房间
        if (room.Members.Count == 1) // 因为还没保存，所以是1
        {
            _context.Rooms.Remove(room);
            _logger.LogInformation($"房间 {roomCode} 已删除（空房间）");
        }

        await _context.SaveChangesAsync();

        return Ok(new { success = true, message = "已退出房间" });
    }

    /// <summary>
    /// 生成6位房间码
    /// </summary>
    private string GenerateRoomCode()
    {
        const string chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        var random = new Random();
        return new string(Enumerable.Repeat(chars, 6)
            .Select(s => s[random.Next(s.Length)]).ToArray());
    }

    /// <summary>
    /// 获取所有房间（调试用）
    /// GET /api/rooms
    /// </summary>
    [HttpGet]
    public async Task<IActionResult> GetAllRooms()
    {
        var rooms = await _context.Rooms
            .Include(r => r.Members)
            .Select(r => new
            {
                r.RoomCode,
                r.RoomName,
                r.HostUsername,
                r.Status,
                memberCount = r.Members.Count,
                r.MaxPlayers,
                r.CreatedAt
            })
            .ToListAsync();

        return Ok(rooms);
    }
}
