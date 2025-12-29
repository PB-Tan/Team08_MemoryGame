using MemoryGameAPI.Models;
using MySql.Data.MySqlClient;

namespace MemoryGameAPI.Repositories
{
    public class UserRepository
    {
        private readonly string _connectionString;
        public UserRepository(IConfiguration configuration)
        {
            _connectionString = configuration.GetConnectionString("DefaultConnection")
                ?? throw new ArgumentNullException(nameof(configuration), "Connection string 'DefaultConnection' not found.");
        }

        public User? findUserByUsername(String username)
        {
            using (MySqlConnection conn = new MySqlConnection(_connectionString))
            {
                conn.Open();

                //assuming username is unique
                string sql = @"SELECT * FROM Users WHERE Username = @Username";
                MySqlCommand cmd = new MySqlCommand(sql, conn);
                cmd.Parameters.AddWithValue("@Username", username);

                MySqlDataReader reader = cmd.ExecuteReader();

                //assuming that username is unique and non-conflicting
                User? reqUser = null;
                if (reader.Read())
                {
                    reqUser = new User ()
                    {
                        Username = (string)reader["username"],
                        Password = (string)reader["password"],
                        UserId = (int)reader["userid"],
                        IsPaidUser = (bool)reader["isPaidUser"]
                    };
                }

                conn.Close();
                return reqUser;
            }
        }

        public List<User> GetAllUsers()
        {
            using (MySqlConnection conn = new MySqlConnection(_connectionString))
            {
                conn.Open();
                string sql = @"SELECT * FROM Users";

                MySqlCommand cmd = new MySqlCommand(sql, conn);
                MySqlDataReader reader = cmd.ExecuteReader();
                List<User> userList = new List<User>();
                while (reader.Read())
                {
                    User nextUser = new User()                    
                    {
                        Username = (string)reader["username"],
                        Password = (string)reader["password"],
                        UserId = (int)reader["userid"],
                        IsPaidUser = (bool)reader["isPaidUser"]
                    };
                    userList.Add(nextUser);
                }

                conn.Close();
                return userList;
            }
        }
    }
}
