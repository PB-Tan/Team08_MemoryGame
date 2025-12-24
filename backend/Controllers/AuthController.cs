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
    /// User login
    /// POST /api/auth/login
    /// </summary>
    [HttpPost("login")]
    public async Task<ActionResult<LoginResponse>> Login([FromBody] LoginRequest request)
    {
        _logger.LogInformation($"Login attempt: {request.Username}");

        // Validate input
        if (string.IsNullOrWhiteSpace(request.Username) ||
            string.IsNullOrWhiteSpace(request.Password))
        {
            return Ok(new LoginResponse
            {
                Success = false,
                Message = "Username and password cannot be empty"
            });
        }

        // Find user, authenticating with database
        var user = await _context.Users
            .FirstOrDefaultAsync(u =>
                u.Username.ToLower() == request.Username.ToLower() &&
                u.Password == request.Password);

        // if user is not found
        if (user == null)
        {
            _logger.LogWarning($"Login failed: {request.Username}");
            return Ok(new LoginResponse
            {
                Success = false,
                Message = "Username or password is incorrect"
            });
        }

        // Login successful
        _logger.LogInformation($"Login successful: {user.Username}");
        //implement GUID after successful authentication...
        return Ok(new LoginResponse
        {
            Success = true,
            UserId = user.UserId,
            Username = user.Username,
            IsPaid = user.IsPaidUser,
            Message = "Login successful"
        });
    }

    /// <summary>
    /// Get all users (for testing)
    /// GET /api/auth/users
    /// </summary>
    [HttpGet("users")]
    public async Task<ActionResult<IEnumerable<User>>> GetAllUsers()
    {
        var users = await _context.Users.ToListAsync();
        return Ok(users);
    }
}
