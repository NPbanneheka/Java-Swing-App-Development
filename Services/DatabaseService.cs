using System.IO;
using Microsoft.Data.Sqlite;
using GateFlowPro.Models;

namespace GateFlowPro.Services;

public static class DatabaseService
{
    private static readonly string DataDir = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Data");
    private static readonly string DbPath = Path.Combine(DataDir, "gateflowpro.db");
    private static string ConnectionString => $"Data Source={DbPath}";

    public static void Initialize()
    {
        Directory.CreateDirectory(DataDir);
        using var con = new SqliteConnection(ConnectionString);
        con.Open();
        Execute(con, @"CREATE TABLE IF NOT EXISTS Users(
            Id INTEGER PRIMARY KEY AUTOINCREMENT,
            FullName TEXT NOT NULL,
            Username TEXT NOT NULL UNIQUE,
            Password TEXT NOT NULL,
            Role TEXT NOT NULL,
            Phone TEXT,
            PhotoPath TEXT,
            CreatedAt TEXT NOT NULL);");
        Execute(con, @"CREATE TABLE IF NOT EXISTS Vehicles(
            Id INTEGER PRIMARY KEY AUTOINCREMENT,
            VehicleNo TEXT NOT NULL UNIQUE,
            Transporter TEXT NOT NULL,
            VehicleType TEXT NOT NULL);");
        Execute(con, @"CREATE TABLE IF NOT EXISTS GateRecords(
            Id INTEGER PRIMARY KEY AUTOINCREMENT,
            DocumentNo TEXT NOT NULL,
            RecordType TEXT NOT NULL,
            Date TEXT,
            Transporter TEXT,
            VehicleNo TEXT,
            VehicleType TEXT,
            InvoiceNo TEXT,
            Cases TEXT,
            Town TEXT,
            Customer TEXT,
            Remarks TEXT,
            ScheduleNo TEXT,
            ShipmentNo TEXT,
            Status TEXT,
            GeneratedBy TEXT,
            GeneratedAt TEXT);");

        if (GetUserByUsername("admin") == null)
        {
            AddUser(new AppUser
            {
                FullName = "System Administrator",
                Username = "admin",
                Password = "admin123",
                Role = "Admin",
                Phone = "",
                CreatedAt = DateTime.Now.ToString("yyyy-MM-dd HH:mm")
            });
        }

        if (GetVehicles().Count == 0)
        {
            AddVehicle(new Vehicle { VehicleNo = "NW-1234", Transporter = "ABC Transport", VehicleType = "Truck" });
            AddVehicle(new Vehicle { VehicleNo = "CP-4567", Transporter = "Nestle Transport", VehicleType = "Lorry" });
            AddVehicle(new Vehicle { VehicleNo = "WP-8899", Transporter = "External Hire", VehicleType = "Container" });
        }
    }

    private static void Execute(SqliteConnection con, string sql)
    {
        using var cmd = con.CreateCommand();
        cmd.CommandText = sql;
        cmd.ExecuteNonQuery();
    }

    private static SqliteConnection Open()
    {
        var con = new SqliteConnection(ConnectionString);
        con.Open();
        return con;
    }

    public static AppUser? Login(string username, string password)
    {
        using var con = Open();
        using var cmd = con.CreateCommand();
        cmd.CommandText = "SELECT * FROM Users WHERE Username=$u AND Password=$p LIMIT 1";
        cmd.Parameters.AddWithValue("$u", username.Trim());
        cmd.Parameters.AddWithValue("$p", password);
        using var r = cmd.ExecuteReader();
        return r.Read() ? MapUser(r) : null;
    }

    public static AppUser? GetUserByUsername(string username)
    {
        using var con = Open();
        using var cmd = con.CreateCommand();
        cmd.CommandText = "SELECT * FROM Users WHERE Username=$u LIMIT 1";
        cmd.Parameters.AddWithValue("$u", username.Trim());
        using var r = cmd.ExecuteReader();
        return r.Read() ? MapUser(r) : null;
    }

    public static void AddUser(AppUser u)
    {
        using var con = Open();
        using var cmd = con.CreateCommand();
        cmd.CommandText = @"INSERT INTO Users(FullName,Username,Password,Role,Phone,PhotoPath,CreatedAt)
                            VALUES($f,$u,$p,$r,$ph,$photo,$c)";
        cmd.Parameters.AddWithValue("$f", u.FullName);
        cmd.Parameters.AddWithValue("$u", u.Username);
        cmd.Parameters.AddWithValue("$p", u.Password);
        cmd.Parameters.AddWithValue("$r", u.Role);
        cmd.Parameters.AddWithValue("$ph", u.Phone ?? "");
        cmd.Parameters.AddWithValue("$photo", u.PhotoPath ?? "");
        cmd.Parameters.AddWithValue("$c", string.IsNullOrWhiteSpace(u.CreatedAt) ? DateTime.Now.ToString("yyyy-MM-dd HH:mm") : u.CreatedAt);
        cmd.ExecuteNonQuery();
    }

    public static void UpdateUser(AppUser u)
    {
        using var con = Open();
        using var cmd = con.CreateCommand();
        cmd.CommandText = @"UPDATE Users SET FullName=$f, Password=$p, Role=$r, Phone=$ph, PhotoPath=$photo WHERE Id=$id";
        cmd.Parameters.AddWithValue("$f", u.FullName);
        cmd.Parameters.AddWithValue("$p", u.Password);
        cmd.Parameters.AddWithValue("$r", u.Role);
        cmd.Parameters.AddWithValue("$ph", u.Phone ?? "");
        cmd.Parameters.AddWithValue("$photo", u.PhotoPath ?? "");
        cmd.Parameters.AddWithValue("$id", u.Id);
        cmd.ExecuteNonQuery();
    }

    public static List<AppUser> GetUsers()
    {
        var list = new List<AppUser>();
        using var con = Open();
        using var cmd = con.CreateCommand();
        cmd.CommandText = "SELECT * FROM Users ORDER BY Id DESC";
        using var r = cmd.ExecuteReader();
        while (r.Read()) list.Add(MapUser(r));
        return list;
    }

    public static void AddVehicle(Vehicle v)
    {
        using var con = Open();
        using var cmd = con.CreateCommand();
        cmd.CommandText = @"INSERT OR REPLACE INTO Vehicles(VehicleNo,Transporter,VehicleType) VALUES($no,$tr,$type)";
        cmd.Parameters.AddWithValue("$no", v.VehicleNo.Trim().ToUpper());
        cmd.Parameters.AddWithValue("$tr", v.Transporter);
        cmd.Parameters.AddWithValue("$type", v.VehicleType);
        cmd.ExecuteNonQuery();
    }

    public static List<Vehicle> GetVehicles()
    {
        var list = new List<Vehicle>();
        using var con = Open();
        using var cmd = con.CreateCommand();
        cmd.CommandText = "SELECT * FROM Vehicles ORDER BY VehicleNo";
        using var r = cmd.ExecuteReader();
        while (r.Read()) list.Add(new Vehicle
        {
            Id = r.GetInt32(0),
            VehicleNo = r.GetString(1),
            Transporter = r.GetString(2),
            VehicleType = r.GetString(3)
        });
        return list;
    }

    public static Vehicle? FindVehicle(string vehicleNo)
    {
        using var con = Open();
        using var cmd = con.CreateCommand();
        cmd.CommandText = "SELECT * FROM Vehicles WHERE UPPER(VehicleNo)=UPPER($no) LIMIT 1";
        cmd.Parameters.AddWithValue("$no", vehicleNo.Trim());
        using var r = cmd.ExecuteReader();
        return r.Read() ? new Vehicle { Id = r.GetInt32(0), VehicleNo = r.GetString(1), Transporter = r.GetString(2), VehicleType = r.GetString(3) } : null;
    }

    public static void AddRecord(GateRecord g)
    {
        using var con = Open();
        using var cmd = con.CreateCommand();
        cmd.CommandText = @"INSERT INTO GateRecords(DocumentNo,RecordType,Date,Transporter,VehicleNo,VehicleType,InvoiceNo,Cases,Town,Customer,Remarks,ScheduleNo,ShipmentNo,Status,GeneratedBy,GeneratedAt)
                            VALUES($doc,$rt,$date,$tr,$veh,$type,$inv,$cases,$town,$cus,$rem,$sch,$ship,$status,$by,$at)";
        cmd.Parameters.AddWithValue("$doc", g.DocumentNo);
        cmd.Parameters.AddWithValue("$rt", g.RecordType);
        cmd.Parameters.AddWithValue("$date", g.Date);
        cmd.Parameters.AddWithValue("$tr", g.Transporter);
        cmd.Parameters.AddWithValue("$veh", g.VehicleNo);
        cmd.Parameters.AddWithValue("$type", g.VehicleType);
        cmd.Parameters.AddWithValue("$inv", g.InvoiceNo);
        cmd.Parameters.AddWithValue("$cases", g.Cases);
        cmd.Parameters.AddWithValue("$town", g.Town);
        cmd.Parameters.AddWithValue("$cus", g.Customer);
        cmd.Parameters.AddWithValue("$rem", g.Remarks);
        cmd.Parameters.AddWithValue("$sch", g.ScheduleNo);
        cmd.Parameters.AddWithValue("$ship", g.ShipmentNo);
        cmd.Parameters.AddWithValue("$status", g.Status);
        cmd.Parameters.AddWithValue("$by", g.GeneratedBy);
        cmd.Parameters.AddWithValue("$at", string.IsNullOrWhiteSpace(g.GeneratedAt) ? DateTime.Now.ToString("yyyy-MM-dd HH:mm") : g.GeneratedAt);
        cmd.ExecuteNonQuery();
    }

    public static List<GateRecord> GetRecords(string keyword = "")
    {
        var list = new List<GateRecord>();
        using var con = Open();
        using var cmd = con.CreateCommand();
        cmd.CommandText = @"SELECT * FROM GateRecords
            WHERE $k='' OR DocumentNo LIKE $like OR VehicleNo LIKE $like OR InvoiceNo LIKE $like OR Town LIKE $like OR Customer LIKE $like
            ORDER BY Id DESC";
        cmd.Parameters.AddWithValue("$k", keyword.Trim());
        cmd.Parameters.AddWithValue("$like", $"%{keyword.Trim()}%");
        using var r = cmd.ExecuteReader();
        while (r.Read()) list.Add(MapRecord(r));
        return list;
    }

    public static int Count(string table)
    {
        using var con = Open();
        using var cmd = con.CreateCommand();
        cmd.CommandText = $"SELECT COUNT(*) FROM {table}";
        return Convert.ToInt32(cmd.ExecuteScalar());
    }

    private static AppUser MapUser(SqliteDataReader r) => new()
    {
        Id = r.GetInt32(r.GetOrdinal("Id")),
        FullName = r.GetString(r.GetOrdinal("FullName")),
        Username = r.GetString(r.GetOrdinal("Username")),
        Password = r.GetString(r.GetOrdinal("Password")),
        Role = r.GetString(r.GetOrdinal("Role")),
        Phone = r["Phone"]?.ToString() ?? "",
        PhotoPath = r["PhotoPath"]?.ToString() ?? "",
        CreatedAt = r.GetString(r.GetOrdinal("CreatedAt"))
    };

    private static GateRecord MapRecord(SqliteDataReader r) => new()
    {
        Id = Convert.ToInt32(r["Id"]),
        DocumentNo = r["DocumentNo"]?.ToString() ?? "",
        RecordType = r["RecordType"]?.ToString() ?? "",
        Date = r["Date"]?.ToString() ?? "",
        Transporter = r["Transporter"]?.ToString() ?? "",
        VehicleNo = r["VehicleNo"]?.ToString() ?? "",
        VehicleType = r["VehicleType"]?.ToString() ?? "",
        InvoiceNo = r["InvoiceNo"]?.ToString() ?? "",
        Cases = r["Cases"]?.ToString() ?? "",
        Town = r["Town"]?.ToString() ?? "",
        Customer = r["Customer"]?.ToString() ?? "",
        Remarks = r["Remarks"]?.ToString() ?? "",
        ScheduleNo = r["ScheduleNo"]?.ToString() ?? "",
        ShipmentNo = r["ShipmentNo"]?.ToString() ?? "",
        Status = r["Status"]?.ToString() ?? "",
        GeneratedBy = r["GeneratedBy"]?.ToString() ?? "",
        GeneratedAt = r["GeneratedAt"]?.ToString() ?? ""
    };
}
