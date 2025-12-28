using MySql.Data.MySqlClient;

namespace MemoryGameAPI.Data
{
    public class SeedDb
    {
        public static void Initialize(string connectionString)
        {
            using (MySqlConnection conn = new MySqlConnection(connectionString))
            {
                conn.Open();

                // Create tables if they don't exist
                CreateTables(conn);

                // Seed initial data
                SeedData(conn);

                // Seed advertisements
                AdSeeder.SeedAdvertisements(connectionString);

                conn.Close();
            }
        }

        private static void CreateTables(MySqlConnection conn)
        {
            // Create Users table
            string createUsersTable = @"
            CREATE TABLE IF NOT EXISTS Users (
                UserId INT AUTO_INCREMENT PRIMARY KEY,
                Username VARCHAR(50) NOT NULL UNIQUE,
                Password VARCHAR(100) NOT NULL,
                IsPaidUser BOOLEAN DEFAULT FALSE,
                CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP
            )";

            // Create Scores table
            string createScoresTable = @"
            CREATE TABLE IF NOT EXISTS Scores (
                ScoreId INT AUTO_INCREMENT PRIMARY KEY,
                UserId INT NOT NULL,
                Username VARCHAR(50) NOT NULL,
                CompletionTimeSeconds INT NOT NULL,
                CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE
            )";

            MySqlCommand cmd = new MySqlCommand(createUsersTable, conn);
            cmd.ExecuteNonQuery();
            cmd = new MySqlCommand(createScoresTable, conn);
            cmd.ExecuteNonQuery();
        }

        private static void SeedData(MySqlConnection conn)
        {
            // Check if user table already has data
            string countUserSql = "SELECT COUNT(*) FROM Users";
            MySqlCommand cmd = new MySqlCommand(countUserSql, conn);
            int userCount = Convert.ToInt32(cmd.ExecuteScalar());

            if (userCount > 0)
            {
                Console.WriteLine("User Data already exists, skipping seeding users...");
            }
            else
            {
                // User data has not been initialized, so             
                // Seed dummy users
                string seedUsers = @"
                INSERT INTO Users (Username, Password, IsPaidUser, CreatedAt) VALUES 
                ('alice', 'alice123', false, NOW()),
                ('bob', 'bob123', false, NOW()),
                ('charlie', 'charlie123', true, NOW()),
                ('david', 'david123', false, NOW()),
                ('diana', 'diana123', true, NOW()),
                ('eve', 'eve123', false, NOW())";

                cmd = new MySqlCommand(seedUsers, conn);
                cmd.ExecuteNonQuery();
            }

            // Check if score table already has data
            string countScoreSql = "SELECT COUNT(*) FROM Scores";
            cmd = new MySqlCommand(countScoreSql, conn);
            int scoreCount = Convert.ToInt32(cmd.ExecuteScalar());

            if (scoreCount > 0)
            {
                Console.WriteLine("Score Data already exists, skipping seeding scores...");
            }
            else
            {
                // Seed Scores 
                string seedScores = @"
                INSERT INTO Scores (UserId, Username, CompletionTimeSeconds, CreatedAt) VALUES 
                (1, 'alice', 45, NOW()),
                (2, 'bob', 67, NOW() - INTERVAL 4 DAY),
                (3, 'charlie', 23, NOW() - INTERVAL 3 DAY),
                (4, 'david', 89, NOW() - INTERVAL 2 DAY),
                (5, 'diana', 34, NOW() - INTERVAL 1 DAY),
                (6, 'eve', 38, NOW())";

                cmd = new MySqlCommand(seedScores, conn);
                cmd.ExecuteNonQuery();
            }
        }
    }
}

