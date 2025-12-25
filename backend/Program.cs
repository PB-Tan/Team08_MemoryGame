using Microsoft.EntityFrameworkCore;
using MemoryGameAPI.Data;
using Microsoft.AspNetCore.Session;


var builder = WebApplication.CreateBuilder(args);

// Add services
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Configure Entity Framework and SQLite
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlite(builder.Configuration.GetConnectionString("DefaultConnection")));

// Configure CORS - allow LAN access
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

//Enable session services
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSession(options =>
{
    options.IdleTimeout = TimeSpan.FromMinutes(5);
    options.Cookie.HttpOnly = true;
    options.Cookie.IsEssential = true;
});

// Configure to listen on all network interfaces (LAN access)
builder.WebHost.UseUrls("http://0.0.0.0:5000");

var app = builder.Build();

// Initialize database and seed data
using (var scope = app.Services.CreateScope())
{
    var context = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    context.Database.EnsureCreated();
    DbInitializer.Initialize(context);
}

// Configure session
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors("AllowAll");
app.UseSession();
app.UseAuthorization();
app.MapControllers();

Console.WriteLine("===========================================");
Console.WriteLine("Memory Game API started!");
Console.WriteLine("Local access:        http://localhost:5000");
Console.WriteLine("Swagger ui (Test):   http://localhost:5000/swagger");
Console.WriteLine("===========================================");
Console.WriteLine("Use your computer's IP address in Android to access");
Console.WriteLine("For example: http://10.0.2.2:5000/");
Console.WriteLine("===========================================");

app.Run();
