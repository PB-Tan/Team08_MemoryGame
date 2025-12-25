using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MemoryGameAPI.Data;
using MemoryGameAPI.Models;
using Microsoft.AspNetCore.Http;


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


    /// User login API
    /// POST /api/auth/login
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

        //store user credentials in session 
        HttpContext.Session.SetInt32("UserId", user.UserId);
        HttpContext.Session.SetString("Username", user.Username);
        HttpContext.Session.SetString("IsPaid", user.IsPaidUser ? "1" : "0");

        // return HTTP response to android
        return Ok(new LoginResponse
        {
            Success = true,
            UserId = user.UserId,
            Username = user.Username,
            IsPaid = user.IsPaidUser,
            Message = "Login successful"
        });
    }

    //test if session is working
    [HttpGet("me")]
    public IActionResult Me()
    {
        var userId = HttpContext.Session.GetInt32("UserId");
        if (userId == null)
            return Unauthorized(new { success = false, message = "Not logged in" });

        return Ok(new
        {
            success = true,
            userId = userId.Value,
            username = HttpContext.Session.GetString("Username"),
            isPaid = HttpContext.Session.GetString("IsPaid") == "1"
        });
    }

    //clear session if we need to log out
    [HttpPost("logout")]
    public IActionResult Logout()
    {
        HttpContext.Session.Clear();
        return Ok(new { success = true, message = "Logged out" });
    }


    /// Get all users (for testing)
    /// GET /api/auth/users
    [HttpGet("users")]
    public async Task<ActionResult<IEnumerable<User>>> GetAllUsers()
    {
        var users = await _context.Users.ToListAsync();
        return Ok(users);
    }
}
