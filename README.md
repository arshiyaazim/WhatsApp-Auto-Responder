# 🤖 WhatsApp Auto-Responder (AI-Powered)

[![GitHub Release](https://img.shields.io/github/v/release/Prince-nebhwani/WhatsApp-Auto-Responder?color=25D366&logo=github&style=for-the-badge)](https://github.com/Prince-nebhwani/WhatsApp-Auto-Responder/releases/latest)
[![Obtainium](https://img.shields.io/badge/Obtainium-Add%20App-007AFF?logo=android&logoColor=white&style=for-the-badge)](obtainium://app/https%3A%2F%2Fgithub.com%2FPrince-nebhwani%2FWhatsApp-Auto-Responder)
[![Website](https://img.shields.io/badge/Website-Live%20Demo-25D366?logo=googlechrome&logoColor=white&style=for-the-badge)](https://prince-nebhwani.github.io/WhatsApp-Auto-Responder/)
[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white&style=for-the-badge)](https://github.com/Prince-nebhwani/WhatsApp-Auto-Responder/releases)
[![AI Engine](https://img.shields.io/badge/AI%20Engine-Google%20Gemini-4285F4?logo=google&logoColor=white&style=for-the-badge)](https://ai.google.dev/)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white&style=for-the-badge)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

A modern, privacy-first Android application that automatically replies to WhatsApp messages using **Google Gemini AI**. Customize personality tones and automate your communication without sacrificing privacy or paying monthly subscription fees.

---

### 🌐 [**Visit Official Landing Page**](https://prince-nebhwani.github.io/WhatsApp-Auto-Responder/) &nbsp;|&nbsp; 📥 [**Download Latest APK (v1.0.0)**](https://github.com/Prince-nebhwani/WhatsApp-Auto-Responder/releases/download/v1.0.0/WhatsApp-Auto-Responder.apk) &nbsp;|&nbsp; 📲 [**Add to Obtainium**](obtainium://app/https%3A%2F%2Fgithub.com%2FPrince-nebhwani%2FWhatsApp-Auto-Responder)
*Direct download • Verified Build • Size: ~18.8 MB • Works on Android 8.0+*

---

## ✨ Features

- **🧠 Google Gemini AI Replies:** Unlike standard auto-responders that use rigid keyword matching or regex, this app analyzes conversational context and crafts human-like, helpful responses.
- **🔑 Gemini API:** AI features require `GEMINI_API_KEY` to be configured in the build environment.
- **🎭 Customizable Personality & Tones:** Choose how your auto-responder speaks or write your own custom prompt:
  - **Professional & Direct:** Polite, articulate, and business-ready.
  - **Friendly & Warm:** Enthusiastic, welcoming, and emoji-friendly.
  - **Chill & Casual:** Relaxed, lowercase, natural conversational flow.
  - **Witty & Sarcastic:** Fun, clever, and playful banter.
  - **Custom System Prompt:** Fully configure your own AI persona and instructions.
- **⚡ Native Notification Action Interception:** Uses Android's official `NotificationListenerService` and notification action replies (the same mechanism used by Wear OS smartwatches and Android Auto).
- **🧩 Persistent AI Automation Rules:** Draft Bengali or English instructions into reviewed rules for a selected sender, then keep each rule enabled until you edit, disable, or delete it. Rules support an AI reply, a fixed reply, or a manual cross-chat send review.
- **🎨 Modern Material 3 UI:** Clean, responsive user interface built 100% with Jetpack Compose.
- **🛡️ Local-first:** No developer-owned message relay, tracking, or external analytics. AI features send the prompt and relevant message text directly to Google Gemini.

---

## 🧩 AI Automation Rules (Preview)

Open **Automation Rules** from the WhatsApp settings area. Gemini interprets a Bengali or English instruction, but does not activate it directly: select the sender contact, review the interpreted action and contact mapping, then confirm. Enabled rules and their contact names/numbers are stored in the app's local preferences.

- Sender matching currently requires an exact, case-insensitive match with the notification's displayed name. The saved sender phone number is not available to the notification listener for matching, so identical names and renamed contacts can be ambiguous.
- Group notifications are skipped. While any rule is enabled, unmatched senders and group notifications do not fall back to the legacy general auto-reply.
- AI and fixed replies require a usable WhatsApp notification inline-reply action.
- An active AI-reply rule sends the matched incoming message and its instruction to Google's Gemini API; the generated reply is sent automatically through the notification action.
- Cross-chat rules only post a prefilled WhatsApp review notification. The app never taps Send; the user must review and send the message manually. The recipient must have an international `+` phone number saved.
- AI rule interpretation and AI replies require a build-time `GEMINI_API_KEY` environment variable. A key included in a distributed Android app can be extracted; use a restricted key and suitable quotas, or put Gemini behind a server you control for production distribution.

---

## 🥊 Why This App? (Comparison with Alternatives)

| Feature / Aspect | **This App (FOSS)** | Commercial Play Store Apps | Python / Web Scraper Bots |
| :--- | :--- | :--- | :--- |
| **Cost** | **100% Free forever** (BYOK Gemini API) | Paid subscription ($5–$15/mo) | Free, but requires 24/7 VPS hosting |
| **WhatsApp Ban Risk** | **0% Risk** (Uses official Android notification API) | 0% Risk (Uses same notification API) | **High Risk** (Scrapers get banned by WhatsApp) |
| **Privacy & Security** | Local rules/history; direct Gemini API calls; no trackers | Closed-source, ads, unknown data harvesting | Depends on self-hosted script |
| **Setup Difficulty** | Install a build with Gemini configured | Install app, pay paywall | Complex (Node/Python, Docker, headless Chrome) |
| **Smartwatch Emulation** | Handled natively via `RemoteInput` | Supported | Unsupported |

---

## 🛡️ Privacy & Transparency: Why You Can Trust This App

Security and transparency are essential when dealing with messaging applications. Here is an honest breakdown of how this application works:

### 1. Zero Middleman Servers
This app does **NOT** route your messages through any third-party server or developer-owned backend. When an auto-reply is generated, your device communicates **directly and securely** with Google's official Gemini API endpoints (`generativelanguage.googleapis.com`) using your personal API key.

### 2. Gemini API Key
Developers provide `GEMINI_API_KEY` as a build environment variable. It is embedded in the Android app and sent directly to Google's Gemini API over HTTPS when AI features are used. It is not encrypted in the APK; do not ship a personal key in a public build. Use a restricted key and quotas, or a server-side proxy for production distribution.

### 3. All Data Stored Locally
All reply logs, conversation history, and custom tone definitions are stored strictly inside a local SQLite database (powered by Android Jetpack Room) on your physical device:
- `personality_tones`
- `whatsapp_messages`

Automation rules and their contact names/numbers are stored separately in the app's local SharedPreferences. Incoming message text for AI replies is sent to Google Gemini as described above.

Uninstalling or clearing app data wipes everything instantly.

### 4. Transparent Permissions Breakdown

| Permission | Android ID | Why It Is Needed |
|---|---|---|
| **Notification Access** | `BIND_NOTIFICATION_LISTENER_SERVICE` | Required to detect incoming notifications from WhatsApp and send inline replies via the notification's `RemoteInput` action. Does not read or store notifications from other apps. |
| **Post Notifications** | `POST_NOTIFICATIONS` | (Android 13+) Required to display active background service status and notification alerts. |
| **Internet Access** | `INTERNET` & `ACCESS_NETWORK_STATE` | Required exclusively to send prompts to Google's official Gemini API and receive generated responses. |

---

## 🔒 Cryptographic Integrity & Checksums

To verify that your downloaded file has not been altered or tampered with, compare its cryptographic hashes against the official release:

- **Filename:** `WhatsApp-Auto-Responder.apk`
- **Package Name:** `com.aistudio.whatsappautoresponder.zkympl`
- **Version:** `1.0.0` — *Official Release*
- **File Size:** `19,699,079 bytes (18.79 MiB)`

| Hash Algorithm | Checksum |
|---|---|
| **SHA-256** | `255c574c682f407f9cf8fee56504f7ac2a578b40d1048008d3ad6a3f9667a443` |
| **SHA-1** | `ac5ab677409e9f64995ce7340460a92f78f90fb3` |
| **MD5** | `69168337f077148a04cd037a510b092a` |

### 🛡️ Security & Architecture
- **Prompt Injection Defense:** Hardened AI system instruction to explicitly reject adversarial override commands inside incoming messages.
- **Privacy First:** Zero hardcoded credentials or middleman servers. All chat logs and configuration remain on your local device.
- **ADB Backup Disabled:** `android:allowBackup="false"` prevents unauthorized data extractions via USB debugging.
- **Signed & Verified:** Signed with official APK Signature Scheme v2 & v3 with 4-byte zip alignment.

### How to Verify Checksum

#### On Linux / macOS:
```bash
sha256sum WhatsApp-Auto-Responder.apk
# Expected output: 39307f6230d2daa1f6935a8cadafdca8c47a4225dcab92dcf52017371fad73a9
```

#### On Windows (PowerShell):
```powershell
Get-FileHash .\WhatsApp-Auto-Responder.apk -Algorithm SHA256
```

---

## 🚀 Quick Setup Guide

### Step 1: Download & Install
1. Download [`WhatsApp-Auto-Responder.apk`](https://github.com/Prince-nebhwani/WhatsApp-Auto-Responder/releases/download/v1.0.0/WhatsApp-Auto-Responder.apk) on your Android device.
2. Tap the downloaded file $\rightarrow$ tap **Install**.
3. *(If updating from an earlier test build, uninstall the previous version first).*

### Step 2: Grant Notification Access (Android 13 / 14 / 15)
1. Open the app and tap **Enable Notification Access**.
2. **If Android says "Restricted setting":**
   * Go to phone **Settings $\rightarrow$ Apps $\rightarrow$ WhatsApp Auto-Responder**.
   * Tap the **three dots (⋮)** in the top-right corner.
   * Tap **Allow restricted settings** and enter your PIN/fingerprint.
   * Return to Notification Access and toggle it **ON**.

### Step 3: Configure Your Free Gemini API Key
1. For a developer-built APK, create a key in **[Google AI Studio](https://aistudio.google.com/app/apikey)** and set the `GEMINI_API_KEY` environment variable before building.
2. AI features in an already-installed APK work only if its build includes a configured key. The app currently does not offer in-app key entry.

### Step 4: Choose Your Tone & Turn On!
1. Select your preferred reply persona (e.g., *Professional*, *Friendly*, *Casual*, or create your own).
2. Toggle **Auto-Responder ON**.
3. You're all set! Incoming WhatsApp messages will now receive intelligent, automated replies.

---

## 🔋 Battery Optimization & Reliability Tips

Modern Android OEM skins (MIUI/HyperOS, ColorOS, OxygenOS, OneUI) aggressively kill background services. To ensure uninterrupted auto-replies:

1. **Disable Battery Optimization:** Go to `Settings` → `Apps` → `WhatsApp Auto-Responder` → `Battery` → Select **Unrestricted** / **Don't Optimize**.
2. **Enable Autostart (Xiaomi / Vivo / Oppo):** Enable **Autostart** in app permissions.
3. **Lock App in Recent Tasks:** Open Recent Apps, long press or swipe down on WhatsApp Auto-Responder, and tap the **Lock** icon.

---

## ❓ Frequently Asked Questions (FAQ)

### Can this get my WhatsApp account banned?
**No.** Unofficial WhatsApp bots that reverse-engineer the WhatsApp web protocol or modify the WhatsApp APK risk account bans. In contrast, this app **does not modify WhatsApp or connect to WhatsApp servers**. It operates entirely via Android's official system notification action API (`RemoteInput`), functioning identically to how Android Auto or a Galaxy Watch sends a quick reply.

### Does it work with WhatsApp Business?
**Yes.** In the Chatbot tab's settings, choose **Regular WhatsApp**, **WhatsApp Business**, or **Both**. Auto-replies use the inline reply action from the supported app's notification. Scheduled-message notifications use the selected app; with **Both**, Android opens an app chooser.

This app cannot select a particular number/account inside one WhatsApp installation. Auto-replies act on the account that generated the notification, while scheduled messages open WhatsApp with the message text ready to send. Confirm account behavior on your device if you use multiple accounts.

### What if I don't want to reply to every message?
You can pause the responder anytime from the main dashboard toggle or configure specific schedules.

---

## 👨‍💻 Author & Maintainer

**Prince Nebhwani**
- GitHub: [@Prince-nebhwani](https://github.com/Prince-nebhwani)
- Project: [WhatsApp-Auto-Responder](https://github.com/Prince-nebhwani/WhatsApp-Auto-Responder)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — free for personal and open-source use.
Copyright (c) 2026 Prince Nebhwani. All rights reserved.
