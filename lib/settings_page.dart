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
      appBar: AppBar(title: const Text("Settings")),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          SwitchListTile(
            title: const Text("Enable Voice Notification"),
            value: _voiceEnabled,
            onChanged: _saveVoiceEnabled,
          ),
          const Divider(),
          const Text("Minimum Amount to Speak (VND)"),
          const SizedBox(height: 8),
          TextField(
            controller: _amountController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(
              border: OutlineInputBorder(),
              hintText: "Enter amount (e.g. 50000)",
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
                    "Privacy Explanation",
                    style: TextStyle(fontWeight: FontWeight.bold),
                  ),
                  SizedBox(height: 8),
                  Text(
                    "This app listens to notifications from banking apps solely to read out the transaction details using Text-to-Speech. "
                    "No data is collected, stored remotely, or shared. All processing happens locally on your device.",
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          const Center(
            child: Text(
              "This app is NOT affiliated with any bank.",
              style: TextStyle(color: Colors.grey, fontSize: 12),
            ),
          ),
        ],
      ),
    );
  }
}
