using MySql.Data.MySqlClient;

namespace MemoryGameAPI.Data
{
    public class SeedDb
    {
        public static void Initialize(string connectionString)
        {
            using (MySQLConnection conn = new MySqlConnection(connectionString))
            {
                conn.Open();

                // Create tables if they don't exist
                CreateTables(conn);

                // Seed initial data
                SeedData(conn);
            }
        }

        private static void CreateTables(MySqlConnection conn)
        {
            // Create Users table
            var createUsersTable = @"
            CREATE TABLE IF NOT EXISTS Users (
                UserId INT AUTO_INCREMENT PRIMARY KEY,
                Username VARCHAR(50) NOT NULL UNIQUE,
                Password VARCHAR(100) NOT NULL,
                IsPaidUser BOOLEAN DEFAULT FALSE,
                CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP
            )";

            // Create Scores table
            var createScoresTable = @"
            CREATE TABLE IF NOT EXISTS Scores (
                ScoreId INT AUTO_INCREMENT PRIMARY KEY,
                UserId INT NOT NULL,
                Username VARCHAR(50) NOT NULL,
                CompletionTimeSeconds INT NOT NULL,
                CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (UserId) REFERENCES Users(UserId) ON DELETE CASCADE
            )";

            ExecuteCommand(conn, createUsersTable);
            ExecuteCommand(conn, createScoresTable);
        }

        private static void SeedData(MySqlConnection conn)
        {
            // Check if user table already has data
            string countSql = "SELECT COUNT(*) FROM Users";
            using (MySqlCommand cmd = new MySqlCommand(countSql, conn))
            {
                int userCount = (int)cmd.ExecuteScalar();
                if (userCount > 0)
                {
                    Console.WriteLine("User Data already exists, skipping seeding users...");
                    return;
                }
            }
                
            
            
            // Seed dummy users
            var seedUsers = @"
            INSERT INTO Users (Username, Password, IsPaidUser, CreatedAt) VALUES 
            ('testuser1', 'password123', FALSE, NOW()),
            ('testuser2', 'password456', FALSE, NOW()),
            ('speedrunner', 'fastpass', TRUE, NOW()),
            ('casual_gamer', 'easypass', FALSE, NOW()),
            ('memory_master', 'strongpass', TRUE, NOW()),
            ('demo_player', 'demo123', FALSE, NOW())";

            string seedDummyUsers = @"
            INSERT INTO Users (Username, Password, IsPaidUser, CreatedAt) VALUES
            ('alice', 'pass123', 'FALSE' "

            ExecuteCommand(conn, seedUsers);

            // Seed Scores (using the user IDs we just created)
            var seedScores = @"
            INSERT INTO Scores (UserId, Username, CompletionTimeSeconds, CreatedAt) VALUES 
            (1, 'testuser1', 45, NOW() - INTERVAL 5 DAY),
            (2, 'testuser2', 67, NOW() - INTERVAL 4 DAY),
            (3, 'speedrunner', 23, NOW() - INTERVAL 3 DAY),
            (4, 'casual_gamer', 89, NOW() - INTERVAL 2 DAY),
            (5, 'memory_master', 34, NOW() - INTERVAL 1 DAY),
            (1, 'testuser1', 38, NOW()),
            (3, 'speedrunner', 19, NOW()),
            (6, 'demo_player', 56, NOW()),
            (2, 'testuser2', 72, NOW() - INTERVAL 1 HOUR),
            (5, 'memory_master', 28, NOW() - INTERVAL 30 MINUTE)";

            ExecuteCommand(conn, seedScores);

            Console.WriteLine("✅ MySQL database seeded with initial data");
            Console.WriteLine($"   📊 Created 6 test users (2 paid, 4 free)");
            Console.WriteLine($"   🏆 Created 10 sample scores");
        }

        private static void ExecuteCommand(MySqlConnection connection, string sql)
        {
            using var command = new MySqlCommand(sql, connection);
            command.ExecuteNonQuery();
        }
    }
}

