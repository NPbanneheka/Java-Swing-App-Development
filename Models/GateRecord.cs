namespace GateFlowPro.Models;

public class GateRecord
{
    public int Id { get; set; }
    public string DocumentNo { get; set; } = string.Empty;
    public string RecordType { get; set; } = string.Empty; // Delivery Note or Schedule
    public string Date { get; set; } = string.Empty;
    public string Transporter { get; set; } = string.Empty;
    public string VehicleNo { get; set; } = string.Empty;
    public string VehicleType { get; set; } = string.Empty;
    public string InvoiceNo { get; set; } = string.Empty;
    public string Cases { get; set; } = string.Empty;
    public string Town { get; set; } = string.Empty;
    public string Customer { get; set; } = string.Empty;
    public string Remarks { get; set; } = string.Empty;
    public string ScheduleNo { get; set; } = string.Empty;
    public string ShipmentNo { get; set; } = string.Empty;
    public string Status { get; set; } = "Issued";
    public string GeneratedBy { get; set; } = string.Empty;
    public string GeneratedAt { get; set; } = string.Empty;
}
