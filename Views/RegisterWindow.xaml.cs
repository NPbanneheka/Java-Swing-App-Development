using System.Windows;
using System.Windows.Controls;
using GateFlowPro.Models;
using GateFlowPro.Services;

namespace GateFlowPro.Views;

public partial class RegisterWindow : Window
{
    public RegisterWindow()
    {
        InitializeComponent();
    }

    private void Create_Click(object sender, RoutedEventArgs e)
    {
        if (string.IsNullOrWhiteSpace(FullNameBox.Text) || string.IsNullOrWhiteSpace(UsernameBox.Text) || string.IsNullOrWhiteSpace(PasswordBox.Password))
        {
            MessageBox.Show("Please fill Full Name, Username and Password.", "Required", MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }
        if (DatabaseService.GetUserByUsername(UsernameBox.Text) != null)
        {
            MessageBox.Show("Username already exists.", "Duplicate", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }
        DatabaseService.AddUser(new AppUser
        {
            FullName = FullNameBox.Text.Trim(),
            Username = UsernameBox.Text.Trim(),
            Password = PasswordBox.Password,
            Phone = PhoneBox.Text.Trim(),
            Role = ((ComboBoxItem)RoleBox.SelectedItem).Content.ToString() ?? "Gate Officer"
        });
        MessageBox.Show("Account created successfully.", "Success", MessageBoxButton.OK, MessageBoxImage.Information);
        Close();
    }
}
