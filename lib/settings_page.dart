import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SettingsPage extends StatefulWidget {
  const SettingsPage({super.key});

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> {
  bool _voiceEnabled = true;
  double _minAmount = 0;
  final TextEditingController _amountController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadSettings();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    setState(() {
      _voiceEnabled = prefs.getBool('voice_enabled') ?? true;
      int val = prefs.getInt('min_amount') ?? 0;
      _minAmount = val.toDouble();
      _amountController.text = val.toString();
    });
  }

  Future<void> _saveVoiceEnabled(bool value) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool('voice_enabled', value);
    setState(() {
      _voiceEnabled = value;
    });
  }

  Future<void> _saveMinAmount(String value) async {
    final prefs = await SharedPreferences.getInstance();
    int? amount = int.tryParse(
      value.replaceAll(',', ''),
    ); // Simple handling if user types commas
    if (amount != null) {
      await prefs.setInt('min_amount', amount);
      setState(() {
        _minAmount = amount.toDouble();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Cài đặt")),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          SwitchListTile(
            title: const Text("Bật thông báo giọng nói"),
            value: _voiceEnabled,
            onChanged: _saveVoiceEnabled,
          ),
          const Divider(),
          const Text("Số tiền tối thiểu để đọc (VND)"),
          const SizedBox(height: 8),
          TextField(
            controller: _amountController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(
              border: OutlineInputBorder(),
              hintText: "Nhập số tiền (ví dụ: 50000)",
              suffixText: "VND",
            ),
            onChanged: _saveMinAmount,
          ),
          const SizedBox(height: 24),
          Card(
            color: Colors.blue.shade50,
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: const [
                  Text(
                    "Giải thích về quyền riêng tư",
                    style: TextStyle(fontWeight: FontWeight.bold),
                  ),
                  SizedBox(height: 8),
                  Text(
                    "Ứng dụng này lắng nghe thông báo từ các ứng dụng ngân hàng chỉ để đọc chi tiết giao dịch bằng giọng nói. "
                    "Không có dữ liệu nào được thu thập, lưu trữ từ xa hoặc chia sẻ. Tất cả xử lý đều diễn ra cục bộ trên thiết bị của bạn.",
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Center(
            child: Text(
              "Ứng dụng này KHÔNG liên kết với bất kỳ ngân hàng nào.",
              style: TextStyle(color: Colors.grey, fontSize: 12),
            ),
          ),
        ],
      ),
    );
  }
}
