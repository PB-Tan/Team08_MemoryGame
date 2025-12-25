using MemoryGameAPI.Models;

namespace MemoryGameAPI.Data;

public static class DbInitializer
{
    public static void Initialize(AppDbContext context)
    {
        // 检查是否已经有数据
        if (context.Users.Any())
        {
            return; // 数据库已经初始化过了
        }

        Console.WriteLine("正在初始化数据库...");

        // 添加测试用户
        var users = new User[]
        {
            new User { Username = "alice", Password = "pass123", IsPaidUser = false },
            new User { Username = "bob", Password = "pass456", IsPaidUser = true },
            new User { Username = "charlie", Password = "pass789", IsPaidUser = false },
            new User { Username = "david", Password = "pass000", IsPaidUser = true },
            new User { Username = "eve", Password = "pass111", IsPaidUser = false }
        };

        context.Users.AddRange(users);
        context.SaveChanges();

        Console.WriteLine($"已添加 {users.Length} 个测试用户");

        // 添加一些测试分数
        var scores = new Score[]
        {
            new Score { UserId = 1, Username = "alice", CompletionTimeSeconds = 125 },
            new Score { UserId = 2, Username = "bob", CompletionTimeSeconds = 98 },
            new Score { UserId = 3, Username = "charlie", CompletionTimeSeconds = 156 },
            new Score { UserId = 4, Username = "david", CompletionTimeSeconds = 110 },
            new Score { UserId = 5, Username = "eve", CompletionTimeSeconds = 142 },
            new Score { UserId = 1, Username = "alice", CompletionTimeSeconds = 88 },
            new Score { UserId = 2, Username = "bob", CompletionTimeSeconds = 95 }
        };

        context.Scores.AddRange(scores);
        context.SaveChanges();

        Console.WriteLine($"已添加 {scores.Length} 条测试分数");
        Console.WriteLine("数据库初始化完成!");
        Console.WriteLine();
        Console.WriteLine("=== 测试用户账号 ===");
        Console.WriteLine("alice / pass123 (免费用户)");
        Console.WriteLine("bob / pass456 (付费用户)");
        Console.WriteLine("charlie / pass789 (免费用户)");
        Console.WriteLine("david / pass000 (付费用户)");
        Console.WriteLine("eve / pass111 (免费用户)");
        Console.WriteLine("====================");
    }
}
