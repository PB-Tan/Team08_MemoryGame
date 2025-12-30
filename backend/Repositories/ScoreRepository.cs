using MemoryGameAPI.Data;
using MemoryGameAPI.Models;
using MySql.Data.MySqlClient;

namespace MemoryGameAPI.Repositories;

public class ScoreRepository
{
    private readonly string _connectionString;
    public ScoreRepository(IConfiguration configuration)
    {
        _connectionString = configuration.GetConnectionString("DefaultConnection")
            ?? throw new ArgumentNullException(nameof(configuration), "Connection string 'DefaultConnection' not found.");
    }
    public Score? GetScoreByUsername(String username)
    {
        using (MySqlConnection conn = new MySqlConnection(_connectionString))
        {
            conn.Open();

            string sql = @$"
                SELECT *
                FROM Scores 
                WHERE Username = @Username";

            MySqlCommand cmd = new MySqlCommand(sql, conn);
            cmd.Parameters.AddWithValue("@Username", username); //Paramterized query to prevent SQL injection
            MySqlDataReader reader = cmd.ExecuteReader();

            if (reader.Read())
            {
                Score requestScore = new Score()
                {
                    ScoreId = (int)reader["ScoreID"],
                    UserId = (int)reader["UserId"],
                    Username = (string)reader["Username"],
                    CompletionTimeSeconds = (int)reader["CompletionTimeSeconds"],
                    CreatedAt = (DateTime)reader["CreatedAt"]
                };
                return requestScore;
            }
            conn.Close();
            return null;
        }
    }

    public List<Score> GetTop5Scores()
    {
        List<Score> Top5Scores = new List<Score>();
        
        using (MySqlConnection conn = new MySqlConnection(_connectionString))
        {
            conn.Open();
            
            string sql = @"
                SELECT ScoreId, UserId, Username, CompletionTimeSeconds, CreatedAt 
                FROM Scores 
                ORDER BY CompletionTimeSeconds ASC 
                LIMIT 5";
                
            MySqlCommand cmd = new MySqlCommand(sql, conn);
            MySqlDataReader reader = cmd.ExecuteReader();
            
            while (reader.Read())
            {
                Score nextScore = new Score()
                {
                    ScoreId = (int)reader["ScoreId"],
                    UserId = (int)reader["UserId"],
                    Username = (string)reader["Username"],
                    CompletionTimeSeconds = (int)reader["CompletionTimeSeconds"],
                    CreatedAt = (DateTime)reader["CreatedAt"]
                };
                Top5Scores.Add(nextScore);
            }
        }
        
        return Top5Scores;
    }

    public List<Score> GetAllScores()
    {
        List<Score> scoreList = new List<Score>();
        
        using (MySqlConnection conn = new MySqlConnection(_connectionString))
        {
            conn.Open();
            
            string sql = @"
                SELECT *
                FROM Scores 
                ORDER BY CompletionTimeSeconds ASC";
                
            MySqlCommand cmd = new MySqlCommand(sql, conn);
            MySqlDataReader reader = cmd.ExecuteReader();
            while (reader.Read())
            {
                Score nextScore = new Score()
                {
                    ScoreId = (int)reader["ScoreId"],
                    UserId = (int)reader["UserId"],
                    Username = (string)reader["Username"],
                    CompletionTimeSeconds = (int)reader["CompletionTimeSeconds"],
                    CreatedAt = (DateTime)reader["CreatedAt"]
                };
                scoreList.Add(nextScore);
            }
        }
        return scoreList;
    }


    public Score CreateScore(Score score)
    {
        using (MySqlConnection conn = new MySqlConnection(_connectionString))
        {
            conn.Open();
            
            string sql = @"
                INSERT INTO Scores (UserId, Username, CompletionTimeSeconds, CreatedAt) 
                VALUES (@UserId, @Username, @CompletionTimeSeconds, @CreatedAt);
                SELECT LAST_INSERT_ID();";
                
            MySqlCommand cmd = new MySqlCommand(sql, conn);
            cmd.Parameters.AddWithValue("@UserId", score.UserId);
            cmd.Parameters.AddWithValue("@Username", score.Username);
            cmd.Parameters.AddWithValue("@CompletionTimeSeconds", score.CompletionTimeSeconds);
            cmd.Parameters.AddWithValue("@CreatedAt", score.CreatedAt);
            
            // Get the generated ScoreId
            var result = cmd.ExecuteScalar();
            if (result != null)
            {
                score.ScoreId = (int)result;
            }
        }
        
        return score;
    }

    public Score UpdateScore(Score score)
    {
        using (MySqlConnection conn = new MySqlConnection(_connectionString))
        {
            conn.Open();
            
            string sql = @"
                UPDATE Scores 
                SET UserId = @UserId, 
                    Username = @Username, 
                    CompletionTimeSeconds = @CompletionTimeSeconds, 
                    CreatedAt = @CreatedAt 
                WHERE ScoreId = @ScoreId";
                
            MySqlCommand cmd = new MySqlCommand(sql, conn);
            cmd.Parameters.AddWithValue("@ScoreId", score.ScoreId);
            cmd.Parameters.AddWithValue("@UserId", score.UserId);
            cmd.Parameters.AddWithValue("@Username", score.Username);
            cmd.Parameters.AddWithValue("@CompletionTimeSeconds", score.CompletionTimeSeconds);
            cmd.Parameters.AddWithValue("@CreatedAt", score.CreatedAt);
            
            cmd.ExecuteNonQuery();
        }
        
        return score;
    }
}