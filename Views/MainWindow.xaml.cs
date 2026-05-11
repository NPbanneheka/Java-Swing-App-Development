using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Media;
using Microsoft.Win32;
using GateFlowPro.Models;
using GateFlowPro.Services;

namespace GateFlowPro.Views;

public partial class MainWindow : Window
{
    private readonly AppUser _user;

    public MainWindow()
    {
        InitializeComponent();
        _user = Session.CurrentUser ?? new AppUser { FullName = "Guest", Role = "Gate Officer", Username = "guest" };
        UserText.Text = _user.FullName;
        RoleText.Text = _user.Role;
        ShowDashboard();
    }

    private void SetTitle(string title, string sub)
    {
        TitleText.Text = title;
        SubtitleText.Text = sub;
    }

    private Border Card(UIElement child) => new()
    {
        Style = (Style)FindResource("Card"),
        Child = child
    };

    private static TextBlock Label(string text) => new() { Text = text, FontWeight = FontWeights.SemiBold, Margin = new Thickness(0, 4, 0, 0) };
    private static TextBox Box(string? text = null) => new() { Text = text ?? "" };

    private void Dashboard_Click(object sender, RoutedEventArgs e) => ShowDashboard();
    private void Delivery_Click(object sender, RoutedEventArgs e) => ShowDocumentForm("Delivery Note");
    private void Schedule_Click(object sender, RoutedEventArgs e) => ShowDocumentForm("Transport Schedule");
    private void Vehicle_Click(object sender, RoutedEventArgs e) => ShowVehicleMaster();
    private void Search_Click(object sender, RoutedEventArgs e) => ShowSearch();
    private void Reports_Click(object sender, RoutedEventArgs e) => ShowReports();
    private void Profile_Click(object sender, RoutedEventArgs e) => ShowProfile();
    private void Users_Click(object sender, RoutedEventArgs e) => ShowUsers();
    private void Logout_Click(object sender, RoutedEventArgs e)
    {
        Session.CurrentUser = null;
        new LoginWindow().Show();
        Close();
    }

    private void ShowDashboard()
    {
        SetTitle("Dashboard", "Today overview and quick actions");
        var grid = new Grid();
        grid.ColumnDefinitions.Add(new ColumnDefinition());
        grid.ColumnDefinitions.Add(new ColumnDefinition());
        grid.ColumnDefinitions.Add(new ColumnDefinition());
        grid.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
        grid.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });

        AddStat(grid, 0, "Total Records", DatabaseService.Count("GateRecords").ToString(), "Delivery notes and schedules");
        AddStat(grid, 1, "Vehicles", DatabaseService.Count("Vehicles").ToString(), "Master data entries");
        AddStat(grid, 2, "Users", DatabaseService.Count("Users").ToString(), "Local system accounts");

        var info = new StackPanel();
        info.Children.Add(new TextBlock { Text = "Recommended Workflow", FontSize = 22, FontWeight = FontWeights.Bold });
        info.Children.Add(new TextBlock { Text = "1. Add/update vehicle details in Vehicle Master.\n2. Enter Vehicle No in Delivery Note or Transport Schedule.\n3. Transporter and Type will auto-fill.\n4. Save the record and print on a blank sheet.", Margin = new Thickness(0, 12, 0, 0), FontSize = 16, Foreground = Brushes.DimGray });
        var card = Card(info);
        Grid.SetRow(card, 1); Grid.SetColumnSpan(card, 3);
        grid.Children.Add(card);
        MainContent.Content = grid;
    }

    private void AddStat(Grid grid, int col, string title, string value, string sub)
    {
        var sp = new StackPanel();
        sp.Children.Add(new TextBlock { Text = title, Foreground = Brushes.DimGray });
        sp.Children.Add(new TextBlock { Text = value, FontSize = 38, FontWeight = FontWeights.Bold, Foreground = (Brush)FindResource("PrimaryBrush") });
        sp.Children.Add(new TextBlock { Text = sub, Foreground = Brushes.Gray });
        var b = Card(sp);
        Grid.SetColumn(b, col);
        grid.Children.Add(b);
    }

    private void ShowDocumentForm(string docType)
    {
        bool isSchedule = docType == "Transport Schedule";
        SetTitle(docType, isSchedule ? "Print official transport schedule on blank sheet" : "Print official delivery note / gate pass on blank sheet");

        var root = new Grid();
        root.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1.1, GridUnitType.Star) });
        root.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(0.9, GridUnitType.Star) });

        var form = new StackPanel();
        var docNo = Box((isSchedule ? "SCH-" : "DN-") + DateTime.Now.ToString("yyyyMMddHHmm"));
        var date = Box(DateTime.Now.ToString("yyyy-MM-dd"));
        var vehicleNo = Box();
        var transporter = Box();
        var vtype = Box();
        var invoice = Box();
        var cases = Box();
        var town = Box();
        var customer = Box();
        var remarks = Box();
        var scheduleNo = Box(isSchedule ? "SCH-" + DateTime.Now.ToString("HHmm") : "");
        var shipmentNo = Box();

        vehicleNo.TextChanged += (_, _) =>
        {
            var v = DatabaseService.FindVehicle(vehicleNo.Text);
            if (v != null)
            {
                transporter.Text = v.Transporter;
                vtype.Text = v.VehicleType;
            }
        };

        form.Children.Add(Label(isSchedule ? "Schedule No" : "Delivery Note No")); form.Children.Add(docNo);
        form.Children.Add(Label("Date")); form.Children.Add(date);
        if (isSchedule) { form.Children.Add(Label("Shipment No")); form.Children.Add(shipmentNo); }
        form.Children.Add(Label("Vehicle No")); form.Children.Add(vehicleNo);
        form.Children.Add(Label("Transporter - Auto Fill")); form.Children.Add(transporter);
        form.Children.Add(Label("Type - Auto Fill")); form.Children.Add(vtype);
        form.Children.Add(Label("Invoice No")); form.Children.Add(invoice);
        form.Children.Add(Label("Cases")); form.Children.Add(cases);
        form.Children.Add(Label("Town")); form.Children.Add(town);
        if (isSchedule) { form.Children.Add(Label("Customer")); form.Children.Add(customer); }
        form.Children.Add(Label(isSchedule ? "Remarks At" : "Remarks")); form.Children.Add(remarks);

        var btnRow = new StackPanel { Orientation = Orientation.Horizontal, Margin = new Thickness(0, 8, 0, 0) };
        var save = new Button { Content = "Save Record", Style = (Style)FindResource("PrimaryButton"), Width = 150, Margin = new Thickness(0, 8, 10, 0) };
        var print = new Button { Content = "Print Preview / Print", Style = (Style)FindResource("LightButton"), Width = 190, Margin = new Thickness(0, 8, 10, 0) };
        btnRow.Children.Add(save); btnRow.Children.Add(print); form.Children.Add(btnRow);

        GateRecord BuildRecord() => new()
        {
            DocumentNo = docNo.Text,
            RecordType = docType,
            Date = date.Text,
            VehicleNo = vehicleNo.Text.Trim().ToUpper(),
            Transporter = transporter.Text,
            VehicleType = vtype.Text,
            InvoiceNo = invoice.Text,
            Cases = cases.Text,
            Town = town.Text,
            Customer = customer.Text,
            Remarks = remarks.Text,
            ScheduleNo = scheduleNo.Text,
            ShipmentNo = shipmentNo.Text,
            GeneratedBy = _user.FullName,
            GeneratedAt = DateTime.Now.ToString("yyyy-MM-dd HH:mm")
        };

        save.Click += (_, _) =>
        {
            if (string.IsNullOrWhiteSpace(vehicleNo.Text) || string.IsNullOrWhiteSpace(docNo.Text))
            {
                MessageBox.Show("Document No and Vehicle No are required.", "Required", MessageBoxButton.OK, MessageBoxImage.Information);
                return;
            }
            DatabaseService.AddRecord(BuildRecord());
            MessageBox.Show("Record saved successfully.", "Saved", MessageBoxButton.OK, MessageBoxImage.Information);
            ShowDashboard();
        };
        print.Click += (_, _) => PrintRecord(BuildRecord());

        var preview = new StackPanel();
        preview.Children.Add(new TextBlock { Text = "Blank Sheet Print Model", FontSize = 22, FontWeight = FontWeights.Bold });
        preview.Children.Add(new TextBlock { Text = "මෙම layout එක software එකෙන්ම draw කරලා print වෙන නිසා pre-printed paper අවශ්‍ය නැහැ. Delivery Note සහ Transport Schedule දෙකම same paper type එකට print කරන්න පුළුවන්.", TextWrapping = TextWrapping.Wrap, Margin = new Thickness(0, 12, 0, 0), Foreground = Brushes.DimGray });
        preview.Children.Add(new Border { Height = 270, Margin = new Thickness(0, 22, 0, 0), BorderBrush = Brushes.LightGray, BorderThickness = new Thickness(1), Child = new TextBlock { Text = docType + "\n\nOfficial print preview will open when you click Print.", HorizontalAlignment = HorizontalAlignment.Center, VerticalAlignment = VerticalAlignment.Center, TextAlignment = TextAlignment.Center, FontSize = 20, Foreground = Brushes.Gray } });

        var left = Card(form); var right = Card(preview);
        Grid.SetColumn(left, 0); Grid.SetColumn(right, 1);
        root.Children.Add(left); root.Children.Add(right);
        MainContent.Content = root;
    }

    private void ShowVehicleMaster()
    {
        SetTitle("Vehicle Master", "Auto-fill transporter and vehicle type using vehicle number");
        var grid = new Grid();
        grid.RowDefinitions.Add(new RowDefinition { Height = GridLength.Auto });
        grid.RowDefinitions.Add(new RowDefinition());
        var form = new StackPanel { Orientation = Orientation.Horizontal };
        var no = Box(); no.Width = 170;
        var tr = Box(); tr.Width = 230;
        var type = Box(); type.Width = 160;
        var add = new Button { Content = "Save Vehicle", Style = (Style)FindResource("PrimaryButton"), Width = 140, Margin = new Thickness(12, 18, 0, 12) };
        form.Children.Add(new StackPanel { Width = 180, Children = { Label("Vehicle No"), no } });
        form.Children.Add(new StackPanel { Width = 240, Children = { Label("Transporter"), tr } });
        form.Children.Add(new StackPanel { Width = 170, Children = { Label("Type"), type } });
        form.Children.Add(add);
        var table = VehicleGrid();
        add.Click += (_, _) =>
        {
            if (string.IsNullOrWhiteSpace(no.Text) || string.IsNullOrWhiteSpace(tr.Text) || string.IsNullOrWhiteSpace(type.Text)) return;
            DatabaseService.AddVehicle(new Vehicle { VehicleNo = no.Text, Transporter = tr.Text, VehicleType = type.Text });
            table.ItemsSource = DatabaseService.GetVehicles();
            no.Text = tr.Text = type.Text = "";
        };
        var top = Card(form); Grid.SetRow(top, 0); grid.Children.Add(top);
        var bottom = Card(table); Grid.SetRow(bottom, 1); grid.Children.Add(bottom);
        MainContent.Content = grid;
    }

    private DataGrid VehicleGrid()
    {
        var dg = new DataGrid { ItemsSource = DatabaseService.GetVehicles(), Margin = new Thickness(0) };
        dg.Columns.Add(new DataGridTextColumn { Header = "Vehicle No", Binding = new System.Windows.Data.Binding("VehicleNo"), Width = new DataGridLength(1, DataGridLengthUnitType.Star) });
        dg.Columns.Add(new DataGridTextColumn { Header = "Transporter", Binding = new System.Windows.Data.Binding("Transporter"), Width = new DataGridLength(2, DataGridLengthUnitType.Star) });
        dg.Columns.Add(new DataGridTextColumn { Header = "Type", Binding = new System.Windows.Data.Binding("VehicleType"), Width = new DataGridLength(1, DataGridLengthUnitType.Star) });
        return dg;
    }

    private DataGrid RecordsGrid(string keyword = "")
    {
        var dg = new DataGrid { ItemsSource = DatabaseService.GetRecords(keyword) };
        dg.Columns.Add(new DataGridTextColumn { Header = "Doc No", Binding = new System.Windows.Data.Binding("DocumentNo"), Width = 130 });
        dg.Columns.Add(new DataGridTextColumn { Header = "Type", Binding = new System.Windows.Data.Binding("RecordType"), Width = 150 });
        dg.Columns.Add(new DataGridTextColumn { Header = "Date", Binding = new System.Windows.Data.Binding("Date"), Width = 110 });
        dg.Columns.Add(new DataGridTextColumn { Header = "Vehicle", Binding = new System.Windows.Data.Binding("VehicleNo"), Width = 110 });
        dg.Columns.Add(new DataGridTextColumn { Header = "Transporter", Binding = new System.Windows.Data.Binding("Transporter"), Width = 160 });
        dg.Columns.Add(new DataGridTextColumn { Header = "Invoice", Binding = new System.Windows.Data.Binding("InvoiceNo"), Width = 110 });
        dg.Columns.Add(new DataGridTextColumn { Header = "Town", Binding = new System.Windows.Data.Binding("Town"), Width = 140 });
        dg.Columns.Add(new DataGridTextColumn { Header = "Cases", Binding = new System.Windows.Data.Binding("Cases"), Width = 90 });
        dg.Columns.Add(new DataGridTextColumn { Header = "Generated By", Binding = new System.Windows.Data.Binding("GeneratedBy"), Width = 150 });
        return dg;
    }

    private void ShowSearch()
    {
        SetTitle("Search Records", "Find old delivery notes and schedules quickly");
        var root = new StackPanel();
        var row = new StackPanel { Orientation = Orientation.Horizontal };
        var search = Box(); search.Width = 360;
        var btn = new Button { Content = "Search", Style = (Style)FindResource("PrimaryButton"), Width = 120, Margin = new Thickness(10, 18, 0, 12) };
        row.Children.Add(new StackPanel { Children = { Label("Search by document, vehicle, invoice, town or customer"), search } });
        row.Children.Add(btn);
        var table = RecordsGrid();
        btn.Click += (_, _) => table.ItemsSource = DatabaseService.GetRecords(search.Text);
        root.Children.Add(Card(row));
        root.Children.Add(Card(table));
        MainContent.Content = root;
    }

    private void ShowReports()
    {
        SetTitle("Reports", "Saved records and print history overview");
        var sp = new StackPanel();
        var print = new Button { Content = "Print Records Table", Style = (Style)FindResource("PrimaryButton"), Width = 170, HorizontalAlignment = HorizontalAlignment.Left };
        var table = RecordsGrid();
        print.Click += (_, _) =>
        {
            var pd = new PrintDialog();
            if (pd.ShowDialog() == true) pd.PrintVisual(table, "GateFlowPro Records Report");
        };
        sp.Children.Add(print);
        sp.Children.Add(new Border { Height = 12 });
        sp.Children.Add(table);
        MainContent.Content = Card(sp);
    }

    private void ShowProfile()
    {
        SetTitle("My Profile", "Update name, password, phone and profile photo path");
        var sp = new StackPanel { Width = 520, HorizontalAlignment = HorizontalAlignment.Left };
        var name = Box(_user.FullName); var pass = Box(_user.Password); var phone = Box(_user.Phone); var photo = Box(_user.PhotoPath);
        sp.Children.Add(Label("Full Name")); sp.Children.Add(name);
        sp.Children.Add(Label("Password")); sp.Children.Add(pass);
        sp.Children.Add(Label("Phone")); sp.Children.Add(phone);
        sp.Children.Add(Label("Photo Path")); sp.Children.Add(photo);
        var choose = new Button { Content = "Choose Photo", Style = (Style)FindResource("LightButton") };
        var save = new Button { Content = "Save Profile", Style = (Style)FindResource("PrimaryButton") };
        choose.Click += (_, _) =>
        {
            var dlg = new OpenFileDialog { Filter = "Images|*.jpg;*.jpeg;*.png;*.bmp" };
            if (dlg.ShowDialog() == true) photo.Text = dlg.FileName;
        };
        save.Click += (_, _) =>
        {
            _user.FullName = name.Text; _user.Password = pass.Text; _user.Phone = phone.Text; _user.PhotoPath = photo.Text;
            DatabaseService.UpdateUser(_user);
            UserText.Text = _user.FullName;
            MessageBox.Show("Profile updated.");
        };
        sp.Children.Add(choose); sp.Children.Add(save);
        MainContent.Content = Card(sp);
    }

    private void ShowUsers()
    {
        SetTitle("Users", "Local user accounts created in this desktop application");
        var dg = new DataGrid { ItemsSource = DatabaseService.GetUsers() };
        dg.Columns.Add(new DataGridTextColumn { Header = "Full Name", Binding = new System.Windows.Data.Binding("FullName"), Width = 220 });
        dg.Columns.Add(new DataGridTextColumn { Header = "Username", Binding = new System.Windows.Data.Binding("Username"), Width = 130 });
        dg.Columns.Add(new DataGridTextColumn { Header = "Role", Binding = new System.Windows.Data.Binding("Role"), Width = 130 });
        dg.Columns.Add(new DataGridTextColumn { Header = "Phone", Binding = new System.Windows.Data.Binding("Phone"), Width = 140 });
        dg.Columns.Add(new DataGridTextColumn { Header = "Created", Binding = new System.Windows.Data.Binding("CreatedAt"), Width = 170 });
        MainContent.Content = Card(dg);
    }

    private void PrintRecord(GateRecord g)
    {
        var doc = new FlowDocument { PageWidth = 793, PageHeight = 1122, PagePadding = new Thickness(55), FontFamily = new FontFamily("Segoe UI"), FontSize = 12 };
        var title = new Paragraph(new Run(g.RecordType == "Transport Schedule" ? "Nestlé  TRANSPORT SCHEDULE" : "Nestlé  Delivery Note"))
        { FontSize = 24, FontWeight = FontWeights.Bold, TextAlignment = TextAlignment.Center, Margin = new Thickness(0, 0, 0, 18) };
        doc.Blocks.Add(title);
        doc.Blocks.Add(new Paragraph(new Run($"Date: {g.Date}        Transporter: {g.Transporter}        Vehicle No: {g.VehicleNo}        Type: {g.VehicleType}")));
        doc.Blocks.Add(new Paragraph(new Run(g.RecordType == "Transport Schedule" ? $"Schedule No: {g.DocumentNo}        Shipment No: {g.ShipmentNo}" : $"Delivery Note No: {g.DocumentNo}")));

        var table = new Table { CellSpacing = 0, BorderBrush = Brushes.Black, BorderThickness = new Thickness(1) };
        for (int i = 0; i < 5; i++) table.Columns.Add(new TableColumn { Width = new GridLength(i == 1 ? 220 : 120) });
        var rg = new TableRowGroup(); table.RowGroups.Add(rg);
        AddRow(rg, "Invoice No", g.RecordType == "Transport Schedule" ? "Customer" : "Cases", "Town", "Remarks", g.RecordType == "Transport Schedule" ? "Cases" : "");
        AddRow(rg, g.InvoiceNo, g.RecordType == "Transport Schedule" ? g.Customer : g.Cases, g.Town, g.Remarks, g.RecordType == "Transport Schedule" ? g.Cases : "");
        doc.Blocks.Add(table);
        doc.Blocks.Add(new Paragraph(new Run($"Generated By: {g.GeneratedBy}                                    Generated Date / Time: {g.GeneratedAt}")) { Margin = new Thickness(0, 28, 0, 0) });
        doc.Blocks.Add(new Paragraph(new Run("Approved By: ........................................        Remarks: ........................................")) { Margin = new Thickness(0, 18, 0, 0) });

        var preview = new Window { Title = "Print Preview - " + g.DocumentNo, Width = 860, Height = 720, WindowStartupLocation = WindowStartupLocation.CenterOwner, Owner = this };
        var viewer = new FlowDocumentScrollViewer { Document = doc, Margin = new Thickness(15) };
        var btn = new Button { Content = "Print", Style = (Style)FindResource("PrimaryButton"), Width = 120, HorizontalAlignment = HorizontalAlignment.Right, Margin = new Thickness(10) };
        btn.Click += (_, _) =>
        {
            var pd = new PrintDialog();
            if (pd.ShowDialog() == true)
            {
                IDocumentPaginatorSource idp = doc;
                pd.PrintDocument(idp.DocumentPaginator, g.DocumentNo);
            }
        };
        var dock = new DockPanel(); DockPanel.SetDock(btn, Dock.Bottom); dock.Children.Add(btn); dock.Children.Add(viewer);
        preview.Content = dock;
        preview.ShowDialog();
    }

    private static void AddRow(TableRowGroup rg, params string[] cells)
    {
        var row = new TableRow(); rg.Rows.Add(row);
        foreach (var c in cells)
        {
            row.Cells.Add(new TableCell(new Paragraph(new Run(c)))
            {
                BorderBrush = Brushes.Black,
                BorderThickness = new Thickness(0.5),
                Padding = new Thickness(6),
                LineHeight = 26
            });
        }
    }
}
