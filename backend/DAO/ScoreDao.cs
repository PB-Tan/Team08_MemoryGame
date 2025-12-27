public class ScoreDao{
public List<Score> GetTop5Scores(string? username = null)
{
    List<Score> scores = new List<Score>();

    using (SqlConnection conn = new SqlConnection(connectionString))
    {
        conn.Open();

        string sql;
        SqlCommand cmd;

        if (!string.IsNullOrEmpty(username))
        {
            //retrieve scores filtered by username
            sql = @"
                SELECT TOP 5 Username, CompletionTimeSeconds
                FROM Scores
                WHERE Username = @Username
                ORDER BY CompletionTimeSeconds ASC";
            cmd = new SqlCommand(sql, conn);
            cmd.Parameters.AddWithValue("@Username", username);
        }
        else
        {
            // no retrieve by username
            sql = @"
                SELECT TOP 5 Username, CompletionTimeSeconds
                FROM Scores
                ORDER BY CompletionTimeSeconds ASC";
            cmd = new SqlCommand(sql, conn);
        }

        using (SqlDataReader reader = cmd.ExecuteReader())
        {
            while (reader.Read())
            {
                scores.Add(new Score
                {
                    Username = reader["Username"].ToString()!,
                    CompletionTimeSeconds = Convert.ToInt32(reader["CompletionTimeSeconds"])
                });
            }
        }
    }

    return scores;
}}
