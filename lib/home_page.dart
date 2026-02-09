import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'settings_page.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> with WidgetsBindingObserver {
  static const platform = MethodChannel('transaction_voice');
  bool _isPermissionGranted = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _checkPermission();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _checkPermission();
    }
  }

  Future<void> _checkPermission() async {
    bool granted = false;
    try {
      final bool result = await platform.invokeMethod(
        'isNotificationAccessGranted',
      );
      granted = result;
    } on PlatformException catch (e) {
      debugPrint("Failed to get permission status: '${e.message}'.");
    }

    if (mounted) {
      setState(() {
        _isPermissionGranted = granted;
      });
    }
  }

  Future<void> _openNotificationSettings() async {
    try {
      await platform.invokeMethod('openNotificationSettings');
    } on PlatformException catch (e) {
      debugPrint("Failed to open settings: '${e.message}'.");
    }
  }

  Future<void> _testVoiceTTS() async {
    try {
      print("speak test 2");
      await platform.invokeMethod('speak', {
        'text': 'Thử nghiệm giọng nói. 1 2 3.',
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text("TTS request sent. Check volume.")),
        );
      }
    } on PlatformException catch (e) {
      debugPrint("Failed to invoke TTS: '${e.message}'.");
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text("TTS Error: ${e.message}"),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Banking Notification"),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const SettingsPage()),
              );
            },
          ),
        ],
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                _isPermissionGranted ? Icons.check_circle : Icons.error,
                color: _isPermissionGranted ? Colors.green : Colors.red,
                size: 80,
              ),
              const SizedBox(height: 16),
              Text(
                _isPermissionGranted ? "Service Active" : "Permission Required",
                style: Theme.of(context).textTheme.headlineMedium,
              ),
              const SizedBox(height: 8),
              if (!_isPermissionGranted)
                const Text(
                  "To listen to banking notifications, please grant Notification Access.",
                  textAlign: TextAlign.center,
                ),
              const SizedBox(height: 24),
              if (!_isPermissionGranted)
                ElevatedButton.icon(
                  onPressed: _openNotificationSettings,
                  icon: const Icon(Icons.settings),
                  label: const Text("Open Notification Settings"),
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 24,
                      vertical: 12,
                    ),
                  ),
                ),
              const SizedBox(height: 48),
              OutlinedButton.icon(
                onPressed: () {
                  _testVoiceTTS();
                },
                icon: const Icon(Icons.volume_up),
                label: const Text("Test Voice TTS"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
