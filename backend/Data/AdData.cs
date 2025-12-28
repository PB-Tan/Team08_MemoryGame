using MemoryGameAPI.Models;
using MySqlConnector;

namespace MemoryGameAPI.Data
{
    public class AdData
    {
        private readonly string _connectionString;
        public AdData(string connectionString)
        {
            _connectionString = connectionString;
        }

        // Methods to interact with the database would go here
        public List<Advertisement> GetAdvertisements()
        {
            List<Advertisement> advertisements = new List<Advertisement>();
            // Implementation to retrieve advertisements from the database
            using (var conn = new MySqlConnection(_connectionString))
            {
                conn.Open();
                string sql = "SELECT Id, Name, ImageUrl, Url FROM Advertisements";
                MySqlCommand cmd = new MySqlCommand(sql, conn);
                MySqlDataReader reader = cmd.ExecuteReader();

                while (reader.Read())
                {
                    Advertisement advertisement = new Advertisement()
                    {
                        Id = reader.GetInt32("Id"),
                        Name = reader.GetString("Name"),
                        ImageUrl = reader.IsDBNull(reader.GetOrdinal("ImageUrl")) ? null : reader.GetString("ImageUrl"),
                        Url = reader.GetString("Url")
                    };
                    advertisements.Add(advertisement);
                }
            }
            return advertisements;
        }
    }
}
}
