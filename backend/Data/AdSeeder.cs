using MemoryGameAPI.Models;
using MySql.Data.MySqlClient;

namespace MemoryGameAPI.Data;

public static class AdSeeder
{
    /// <summary>
    /// Initialize advertisement data in MySQL database
    /// </summary>
    /// <param name="connectionString">MySQL connection string</param>
    public static void SeedAdvertisements(string connectionString)
    {
        Console.WriteLine("Initializing Advertisement data...");
        
        try
        {
            using (var conn = new MySqlConnection(connectionString))
            {
                conn.Open();
                
                // Check if advertisements table exists, if not create it
                CreateAdvertisementsTableIfNotExists(conn);
                
                // Check if data already exists
                if (HasExistingAdvertisements(conn))
                {
                    Console.WriteLine("Advertisement data already exists, skipping seeding.");
                    return;
                }
                
                // Seed the advertisement data
                InsertAdvertisementData(conn);
                
                Console.WriteLine("Advertisement data seeded successfully!");
                Console.WriteLine();
                Console.WriteLine("=== Advertisement Data ===");
                Console.WriteLine("1. mcAd - McDonald's");
                Console.WriteLine("2. nikeAd - Nike");
                Console.WriteLine("3. nusAd - NUS");
                Console.WriteLine("==========================");
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error seeding advertisement data: {ex.Message}");
            throw;
        }
    }
    
    /// <summary>
    /// Create advertisements table if it doesn't exist
    /// </summary>
    private static void CreateAdvertisementsTableIfNotExists(MySqlConnection conn)
    {
        string createTableSql = @"
            CREATE TABLE IF NOT EXISTS advertisements (
                id INT NOT NULL AUTO_INCREMENT,
                Name VARCHAR(100) NOT NULL,
                ImageUrl VARCHAR(500) DEFAULT NULL,
                Url VARCHAR(500) NOT NULL,
                PRIMARY KEY (id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;";
            
        using (var cmd = new MySqlCommand(createTableSql, conn))
        {
            cmd.ExecuteNonQuery();
        }
        
        Console.WriteLine("Advertisements table created/verified.");
    }
    
    /// <summary>
    /// Check if advertisement data already exists
    /// </summary>
    private static bool HasExistingAdvertisements(MySqlConnection conn)
    {
        string checkSql = "SELECT COUNT(*) FROM advertisements";
        using (var cmd = new MySqlCommand(checkSql, conn))
        {
            var count = Convert.ToInt32(cmd.ExecuteScalar());
            return count > 0;
        }
    }
    
    /// <summary>
    /// Insert the advertisement seed data
    /// </summary>
    private static void InsertAdvertisementData(MySqlConnection conn)
    {
        var advertisements = new[]
        {
            new { Name = "mcAd", ImageUrl = "https://i.postimg.cc/1tnzGw1Y/ad-mc.png", Url = "https://www.mcdonalds.com.sg/" },
            new { Name = "nikeAd", ImageUrl = "https://i.postimg.cc/Pxyk8qCJ/ad-nike.jpg", Url = "https://www.nike.com/sg/" },
            new { Name = "nusAd", ImageUrl = "https://i.postimg.cc/FFVMP1FW/ad-nus.png", Url = "https://nus.edu.sg/" }
        };
        
        string insertSql = @"
            INSERT INTO advertisements (Name, ImageUrl, Url) 
            VALUES (@Name, @ImageUrl, @Url)";
        
        int insertedCount = 0;
        
        foreach (var ad in advertisements)
        {
            using (var cmd = new MySqlCommand(insertSql, conn))
            {
                cmd.Parameters.AddWithValue("@Name", ad.Name);
                cmd.Parameters.AddWithValue("@ImageUrl", ad.ImageUrl);
                cmd.Parameters.AddWithValue("@Url", ad.Url);
                
                cmd.ExecuteNonQuery();
                insertedCount++;
            }
        }
        
        Console.WriteLine($"Successfully inserted {insertedCount} advertisements.");
    }
    
    /// <summary>
    /// Get all advertisements from database (for verification)
    /// </summary>
    public static List<Advertisement> GetAllAdvertisements(string connectionString)
    {
        var advertisements = new List<Advertisement>();
        
        using (var conn = new MySqlConnection(connectionString))
        {
            conn.Open();
            
            string sql = "SELECT id, Name, ImageUrl, Url FROM advertisements ORDER BY id";
            using (var cmd = new MySqlCommand(sql, conn))
            {
                using (var reader = cmd.ExecuteReader())
                {
                    while (reader.Read())
                    {
                        advertisements.Add(new Advertisement
                        {
                            Id = reader.GetInt32("id"),
                            Name = reader.GetString("Name"),
                            ImageUrl = reader.IsDBNull(reader.GetOrdinal("ImageUrl")) ? null : reader.GetString("ImageUrl"),
                            Url = reader.GetString("Url")
                        });
                    }
                }
            }
        }
        
        return advertisements;
    }
    
    /// <summary>
    /// Clear all advertisement data (for testing purposes)
    /// </summary>
    public static void ClearAdvertisementData(string connectionString)
    {
        using (var conn = new MySqlConnection(connectionString))
        {
            conn.Open();
            
            string sql = "DELETE FROM advertisements";
            using (var cmd = new MySqlCommand(sql, conn))
            {
                var deletedCount = cmd.ExecuteNonQuery();
                Console.WriteLine($"Cleared {deletedCount} advertisement records.");
            }
        }
    }
}