namespace GateFlowPro.Models;

public class AppUser
{
    public int Id { get; set; }
    public string FullName { get; set; } = string.Empty;
    public string Username { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
    public string Role { get; set; } = "Gate Officer";
    public string Phone { get; set; } = string.Empty;
    public string PhotoPath { get; set; } = string.Empty;
    public string CreatedAt { get; set; } = string.Empty;
}
