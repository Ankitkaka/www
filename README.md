<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/2b2f2e99-3098-4747-ad96-b1d7bd836a56

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

## 🚀 Automated GitHub Actions APK Build

This repository includes automated CI/CD via GitHub Actions (`.github/workflows/build-apk.yml`).

### Features:
- **Automatic Build on Push/PR**: Generates both **Debug APK** and **Signed Release APK** on every push or PR to `main` or `master`.
- **Downloadable Artifacts**: Directly download the compiled APKs from the **Actions** tab under **Artifacts** (`WishMagic-Admin-APKs`).
- **Automatic GitHub Releases**: Tagging a commit (e.g. `v1.0.0`) automatically publishes a GitHub Release with the signed APK attached.
- **Manual Trigger**: Build on demand from the **Actions** tab with choice of `both`, `release`, or `debug`.

### 🔑 Keystore Information

A persistent signing keystore is pre-configured for the project:
- **File**: `my-upload-key.jks`
- **Key Alias**: `upload`
- **Key Password**: `wishmagic123`
- **Store Password**: `wishmagic123`
- **Base64 Backup**: `my-upload-key.jks.base64`

#### Optional GitHub Secrets:
To override keystore parameters in GitHub Actions:
- `RELEASE_KEYSTORE_BASE64`: Base64 string of your `.jks` file (contents of `my-upload-key.jks.base64`)
- `STORE_PASSWORD`: Keystore store password
- `KEY_PASSWORD`: Key alias password

