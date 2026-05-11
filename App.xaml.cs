using System.Windows;
using GateFlowPro.Services;

namespace GateFlowPro;

public partial class App : Application
{
    protected override void OnStartup(StartupEventArgs e)
    {
        base.OnStartup(e);
        DatabaseService.Initialize();
    }
}
