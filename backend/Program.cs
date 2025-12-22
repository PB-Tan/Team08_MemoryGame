using Microsoft.EntityFrameworkCore;
using MemoryGameAPI.Data;

var builder = WebApplication.CreateBuilder(args);

// 添加服务
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// 配置 Entity Framework 和 SQLite
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlite(builder.Configuration.GetConnectionString("DefaultConnection")));

// 配置 CORS - 允许局域网访问
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

// 配置监听所有网络接口（局域网访问）
builder.WebHost.UseUrls("http://0.0.0.0:5000");

var app = builder.Build();

// 初始化数据库和种子数据
using (var scope = app.Services.CreateScope())
{
    var context = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    context.Database.EnsureCreated();
    DbInitializer.Initialize(context);
}

// 配置 HTTP 请求管道
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors("AllowAll");
app.UseAuthorization();
app.MapControllers();

Console.WriteLine("===========================================");
Console.WriteLine("Memory Game API 已启动!");
Console.WriteLine("本地访问: http://localhost:5000");
Console.WriteLine("Swagger文档: http://localhost:5000/swagger");
Console.WriteLine("===========================================");
Console.WriteLine("请在Android中使用你的电脑IP地址访问");
Console.WriteLine("例如: http://192.168.1.100:5000");
Console.WriteLine("===========================================");

app.Run();
