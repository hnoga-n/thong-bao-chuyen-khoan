# Banking Notification

A Flutter app that reads banking transaction notifications aloud using Text-to-Speech (TTS).

## Features

- Listens to notifications from Vietnamese banking apps (ACB, Vietcombank, Momo)
- Automatically speaks transaction amounts in Vietnamese
- Configurable minimum amount threshold
- Toggle voice notifications on/off

## Supported Banks

- ACB (`com.acb.acbapp`)
- Vietcombank (`com.vietcombank.vcb`)
- Momo (`com.mservice.momotransfer`)

## Setup

1. Install the app on your Android device
2. Grant **Notification Access** permission:
   - Go to **Settings > Apps > Special app access > Notification access**
   - Enable **banking_notification**
3. Ensure Vietnamese TTS voice is installed:
   - Go to **Settings > Language & input > Text-to-speech**
   - Download Vietnamese voice data if not installed

## Usage

1. Open the app
2. Grant notification access when prompted
3. Use **Test Voice** button to verify TTS is working
4. Configure settings:
   - **Voice Enabled**: Toggle speech on/off
   - **Minimum Amount**: Set minimum transaction amount to announce

## Requirements

- Android 6.0+
- Vietnamese TTS voice installed (Google TTS recommended)
