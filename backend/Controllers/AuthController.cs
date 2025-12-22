using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MemoryGameAPI.Data;
using MemoryGameAPI.Models;

namespace MemoryGameAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly ILogger<AuthController> _logger;

    public AuthController(AppDbContext context, ILogger<AuthController> logger)
    {
        _context = context;
        _logger = logger;
    }

    /// <summary>
    /// 用户登录
    /// POST /api/auth/login
    /// </summary>
    [HttpPost("login")]
    public async Task<ActionResult<LoginResponse>> Login([FromBody] LoginRequest request)
    {
        _logger.LogInformation($"登录尝试: {request.Username}");

        // 验证输入
        if (string.IsNullOrWhiteSpace(request.Username) ||
            string.IsNullOrWhiteSpace(request.Password))
        {
            return Ok(new LoginResponse
            {
                Success = false,
                Message = "用户名和密码不能为空"
            });
        }

        // 查找用户
        var user = await _context.Users
            .FirstOrDefaultAsync(u =>
                u.Username.ToLower() == request.Username.ToLower() &&
                u.Password == request.Password);

        if (user == null)
        {
            _logger.LogWarning($"登录失败: {request.Username}");
            return Ok(new LoginResponse
            {
                Success = false,
                Message = "用户名或密码错误"
            });
        }

        // 登录成功
        _logger.LogInformation($"登录成功: {user.Username}");
        return Ok(new LoginResponse
        {
            Success = true,
            UserId = user.UserId,
            Username = user.Username,
            IsPaid = user.IsPaidUser,
            Message = "登录成功"
        });
    }

    /// <summary>
    /// 获取所有用户（测试用）
    /// GET /api/auth/users
    /// </summary>
    [HttpGet("users")]
    public async Task<ActionResult<IEnumerable<User>>> GetAllUsers()
    {
        var users = await _context.Users.ToListAsync();
        return Ok(users);
    }
}
