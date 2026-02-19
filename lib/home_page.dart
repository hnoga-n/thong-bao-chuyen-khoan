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
  bool _isServiceConnected = false;
  bool _isReconnecting = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _checkStatus();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _checkStatus();
    }
  }

  Future<void> _checkStatus() async {
    bool granted = false;
    bool connected = false;

    try {
      // Check if permission is granted
      granted = await platform.invokeMethod('isNotificationAccessGranted');

      if (granted) {
        // Check if service is currently connected
        connected = await platform.invokeMethod('isServiceConnected');
        debugPrint("Permission: $granted, Service connected: $connected");

        // If not connected, try to reconnect automatically
        if (!connected && !_isReconnecting) {
          await _autoReconnect();
          // Check again after reconnect attempt
          await Future.delayed(const Duration(milliseconds: 1500));
          connected = await platform.invokeMethod('isServiceConnected');
          debugPrint("After reconnect attempt, connected: $connected");
        }
      }
    } on PlatformException catch (e) {
      debugPrint("Failed to get status: '${e.message}'.");
    }

    if (mounted) {
      setState(() {
        _isPermissionGranted = granted;
        _isServiceConnected = connected;
      });
    }
  }

  Future<void> _autoReconnect() async {
    if (_isReconnecting) return;

    setState(() {
      _isReconnecting = true;
    });

    try {
      debugPrint("Attempting automatic reconnection...");

      // First try simple rebind
      await platform.invokeMethod('rebindNotificationService');
      await Future.delayed(const Duration(milliseconds: 500));

      bool connected = await platform.invokeMethod('isServiceConnected');

      // If still not connected, try force reconnect (toggle component)
      if (!connected) {
        debugPrint("Simple rebind failed, trying force reconnect...");
        await platform.invokeMethod('forceReconnectService');
      }
    } on PlatformException catch (e) {
      debugPrint("Auto reconnect failed: '${e.message}'.");
    } finally {
      if (mounted) {
        setState(() {
          _isReconnecting = false;
        });
      }
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
          const SnackBar(
            content: Text("Đã gửi yêu cầu TTS. Kiểm tra âm lượng."),
          ),
        );
      }
    } on PlatformException catch (e) {
      debugPrint("Failed to invoke TTS: '${e.message}'.");
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text("Lỗi TTS: ${e.message}"),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    // Determine status
    final bool isFullyActive = _isPermissionGranted && _isServiceConnected;
    final bool needsReconnect =
        _isPermissionGranted && !_isServiceConnected && !_isReconnecting;

    return Scaffold(
      appBar: AppBar(
        title: const Text("Thông Báo Giao Dịch"),
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
              if (_isReconnecting) ...[
                const SizedBox(
                  width: 80,
                  height: 80,
                  child: CircularProgressIndicator(strokeWidth: 6),
                ),
                const SizedBox(height: 16),
                Text(
                  "Đang kết nối lại...",
                  style: Theme.of(context).textTheme.headlineMedium,
                ),
              ] else ...[
                Icon(
                  isFullyActive
                      ? Icons.check_circle
                      : needsReconnect
                      ? Icons.warning_amber_rounded
                      : Icons.error,
                  color: isFullyActive
                      ? Colors.green
                      : needsReconnect
                      ? Colors.orange
                      : Colors.red,
                  size: 80,
                ),
                const SizedBox(height: 16),
                Text(
                  isFullyActive
                      ? "Đang hoạt động"
                      : needsReconnect
                      ? "Mất kết nối"
                      : "Cần cấp quyền",
                  style: Theme.of(context).textTheme.headlineMedium,
                ),
              ],
              const SizedBox(height: 8),
              if (!_isPermissionGranted && !_isReconnecting)
                const Text(
                  "Để lắng nghe thông báo ngân hàng, vui lòng cấp quyền truy cập thông báo.",
                  textAlign: TextAlign.center,
                ),
              if (needsReconnect)
                const Text(
                  "Không thể tự động kết nối lại. Vui lòng thử lại hoặc bật/tắt quyền thủ công.",
                  textAlign: TextAlign.center,
                  style: TextStyle(color: Colors.orange),
                ),
              const SizedBox(height: 24),
              if (!_isPermissionGranted && !_isReconnecting)
                ElevatedButton.icon(
                  onPressed: _openNotificationSettings,
                  icon: const Icon(Icons.settings),
                  label: const Text("Mở cài đặt quyền thông báo"),
                  style: ElevatedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 24,
                      vertical: 12,
                    ),
                  ),
                ),
              if (needsReconnect) ...[
                ElevatedButton.icon(
                  onPressed: _autoReconnect,
                  icon: const Icon(Icons.refresh),
                  label: const Text("Thử kết nối lại"),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.orange,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(
                      horizontal: 24,
                      vertical: 12,
                    ),
                  ),
                ),
                const SizedBox(height: 8),
                TextButton(
                  onPressed: _openNotificationSettings,
                  child: const Text("Thủ công: Bật/tắt quyền"),
                ),
              ],
              const SizedBox(height: 48),
              OutlinedButton.icon(
                onPressed: _isReconnecting ? null : _testVoiceTTS,
                icon: const Icon(Icons.volume_up),
                label: const Text("Thử giọng nói"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
