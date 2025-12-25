using Microsoft.EntityFrameworkCore;
using MemoryGameAPI.Models;

namespace MemoryGameAPI.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options)
    {
    }

    public DbSet<User> Users { get; set; }
    public DbSet<Score> Scores { get; set; }
    public DbSet<Room> Rooms { get; set; }
    public DbSet<RoomMember> RoomMembers { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // User 
        modelBuilder.Entity<User>(entity =>
        {
            entity.HasKey(e => e.UserId);
            entity.Property(e => e.Username).IsRequired().HasMaxLength(50);
            entity.Property(e => e.Password).IsRequired().HasMaxLength(100);
        });

        // Score 
        modelBuilder.Entity<Score>(entity =>
        {
            entity.HasKey(e => e.ScoreId);
            entity.Property(e => e.Username).IsRequired().HasMaxLength(50);
            entity.HasOne(e => e.User)
                  .WithMany()
                  .HasForeignKey(e => e.UserId)
                  .OnDelete(DeleteBehavior.Cascade);
        });

        // Room 
        modelBuilder.Entity<Room>(entity =>
        {
            entity.HasKey(e => e.RoomId);
            entity.Property(e => e.RoomCode).IsRequired().HasMaxLength(10);
            entity.Property(e => e.RoomName).IsRequired().HasMaxLength(100);
            entity.Property(e => e.HostUsername).IsRequired().HasMaxLength(50);
            entity.HasIndex(e => e.RoomCode).IsUnique();
        });

        // RoomMember 
        modelBuilder.Entity<RoomMember>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Username).IsRequired().HasMaxLength(50);
            entity.HasOne(e => e.Room)
                  .WithMany(r => r.Members)
                  .HasForeignKey(e => e.RoomId)
                  .OnDelete(DeleteBehavior.Cascade);
        });
    }
}
