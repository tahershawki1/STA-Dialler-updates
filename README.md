# STA Dialer (Android)

A secure telephony dialer with high-contrast DTMF keypad, integrated signed APK update feed, and SIP/PBX management built with Kotlin and Jetpack Compose.

## Features

- **Interactive Tactical Keypad & DTMF Audio**:
  - Full dual-tone multi-frequency (DTMF) tone synthesis using `ToneGenerator`
  - Tactile haptic vibration response
  - Extension dialing and pause/wait DTMF characters (`*`, `#`, `,`, `;`)
  - Speed dial integration (slots 1–9) with fast-call long-press shortcuts
  - Dual routing: Secure SIP VoIP line vs. Cellular carrier dialer fallback

- **Signed APK Update Feed (STA-Dialler-updates)**:
  - Feed consumer for signed APK releases from `tahershawki1/STA-Dialler-updates`
  - Real-time version comparison and update notifications
  - Cryptographic verification: SHA-256 integrity checksum and RSA-4096 signing fingerprint validation
  - In-app download simulation with progress tracking and installation staging
  - Release channel switching (Enterprise / Tactical, Stable, Beta, Canary)
  - Complete changelog viewer and signed packages archive

- **Call Manager & History**:
  - Filterable call logs (All, Missed, Outgoing, Incoming, Blocked)
  - Call duration, carrier lines, and timestamps
  - One-tap quick callbacks and record management

- **Active Call Interface**:
  - Live audio waveform visualization
  - In-call DTMF transmitter pad
  - Mute, Speakerphone, Call Hold, and Secure Local Call Recording controls
  - Security indicator for TLS 1.3 / SRTP encryption

- **Directory & Speed Dial**:
  - Searchable phone directory with PBX extension mapping and department tags
  - Custom speed dial key assignments
  - Add, edit, and manage secure contacts

- **Telephony & PBX Settings**:
  - Configurable SIP server host, port, extension credentials, and TLS 1.3 toggles
  - DTMF audio and haptic feedback toggles
  - Client diagnostics and platform information

