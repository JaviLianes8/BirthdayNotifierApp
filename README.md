# Birthday Notifier App

Birthday Notifier is an Android application written in Kotlin that reminds you of your friends' and family's birthdays and sends them a WhatsApp greeting automatically.

## Highlights

- **Firebase Auth & Firestore** – birthdays are stored per‑user in Firestore and synced across devices.
- **Daily background check** – an `AlarmManager` trigger runs every day and after reboots to look for birthdays.
- **WhatsApp integration** – notifications open WhatsApp with a prefilled message and allow snoozing for later.
- **Multi language & theme** – the UI supports several languages and light/dark/system themes.
- **Contact import** – quickly add entries from the device contacts database.

## Project structure

The code follows a simplified Clean Architecture with these layers:

- **domain** – use cases, models and repository interfaces.
- **data** – repository implementations.
- **framework** – platform specific helpers (Firestore, files, notifications, receivers).
- **presentation** – activities, adapters and UI helpers.

Kotlin coroutines are used for Firestore I/O and the UI mixes classic views with a small amount of Jetpack Compose.

## Build & run

Prerequisites:

1. Android Studio Bumblebee+ or command line tools.
2. A Firebase project with a valid `google-services.json` placed under `app/`.

Steps:

```bash
./gradlew assembleDebug
```

Install the resulting APK on a device, sign in with Google and set your notification time and language from **Settings**.

## Contributing

Pull requests and suggestions are welcome. Feel free to open an issue if you spot a bug or have an improvement in mind.

## License

This project is released under the MIT License. See [privacy.html](privacy.html) for the privacy policy.

