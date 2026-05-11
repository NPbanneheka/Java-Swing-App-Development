using System.Windows;
using GateFlowPro.Models;
using GateFlowPro.Services;

namespace GateFlowPro.Views;

public partial class LoginWindow : Window
{
    public LoginWindow()
    {
        InitializeComponent();
    }

    private void Login_Click(object sender, RoutedEventArgs e)
    {
        var user = DatabaseService.Login(UsernameBox.Text, PasswordBox.Password);
        if (user == null)
        {
            MessageBox.Show("Invalid username or password.", "Login Failed", MessageBoxButton.OK, MessageBoxImage.Warning);
            return;
        }
        Session.CurrentUser = user;
        new MainWindow().Show();
        Close();
    }

    private void Register_Click(object sender, RoutedEventArgs e)
    {
        new RegisterWindow { Owner = this }.ShowDialog();
    }
}
