using Microsoft.AspNetCore.Mvc;
using MemoryGameAPI.Data;
using MemoryGameAPI.Models;
using MemoryGameAPI.Repositories;

namespace MemoryGameAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class Auth2Controller : ControllerBase
    {
        UserRepository userRepo = new UserRepository();

        [HttpPost("login")]
        public IActionResult Login([FromForm] string reqUsername, [FromForm] string reqPassword)
        {
            //validate if incoming username and password are empty
            if (string.IsNullOrWhiteSpace(reqPassword) || string.IsNullOrWhiteSpace(reqUsername))
            {
                return new ObjectResult(new
                {
                    Success = false,
                    Message = "Username and/or password cannot be empty"
                });
            }

            //next we check if the requested user exists in db and check if password is valid
            User? reqUser = userRepo.findUserByUsername(reqUsername);
            if (reqUser == null || reqUser.Password != reqPassword)
            {
                return new ObjectResult(new
                {
                    Success = false,
                    Message = "Username or password incorrect"
                });
            }
            else
            {
                //By this point, user is found and pwd has been authenticated
                //store user credentials in session
                HttpContext.Session.SetInt32("UserId", reqUser.UserId);
                HttpContext.Session.SetString("Username", reqUser.Username);
                HttpContext.Session.SetString("IsPaid", reqUser.IsPaidUser ? "1" : "0"); // stores as 1 or 0 for paid and free user

                //return success message to frontend
                return new ObjectResult(new
                {
                    Success = true,
                    Message = "User has been authenticated"
                });
            }
        }

        [HttpGet("AllUsers")]
        public IActionResult GetAllUsers()
        {
            List<User> userList = userRepo.GetAllUsers();
            return new ObjectResult(userList);
        }

        //clear session if we need to log out
        [HttpGet("logout")]
        public IActionResult Logout()
        {
            HttpContext.Session.Clear();
            return new ObjectResult (new
            { 
                Success = true, 
                Message = "Logged out" 
            });
        }

    }
}
