# Contributing to MockGPSPrivacy

Thank you for your interest in contributing! 🎉

## How to Contribute

### Reporting Bugs
1. Search the [existing issues](https://github.com/emanuele8888/MockGPSPrivacy/issues) to avoid duplicates.
2. Open a new issue using the **Bug Report** template.
3. Include Android version, device model, and steps to reproduce.

### Suggesting Features
1. Open a new issue using the **Feature Request** template.
2. Describe the use case clearly and why it benefits the project.

### Submitting Code

1. **Fork** the repository.
2. **Create a branch** with a descriptive name:
   ```
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/your-bug-description
   ```
3. **Follow the code style**: Kotlin, MVVM architecture, Jetpack Compose.
4. **Write clean, commented code** in English.
5. **Test your changes** on at least one physical device or emulator (API 26+).
6. **Open a Pull Request** with a clear description of what you changed and why.

## Code Style Guidelines

- Language: **Kotlin**
- Architecture: **MVVM**
- UI: **Jetpack Compose + Material 3**
- Comments and documentation: **English**
- No unused imports or dead code

## Development Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/YOUR_USERNAME/MockGPSPrivacy.git
   ```
2. Open in **Android Studio Hedgehog** or newer.
3. Sync Gradle and build the project.
4. Enable **Developer Options** on your test device and set this app as the mock location provider.

## License

By contributing, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).
