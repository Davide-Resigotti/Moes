# Moes

**Moes** is a native Android application built with **Kotlin** and **Jetpack Compose**, designed for tracking sports activities and managing training sessions.

### Key Features
*   **Real-time Tracking**: Monitors user location and performance metrics during activities using background services.
*   **Interactive Maps**: Integrated **Mapbox** SDK for detailed route visualization and navigation.
*   **Cloud Sync**: Powered by **Firebase** for secure authentication and real-time data synchronization.
*   **Modern UI**: Designed with Material Design 3 components for a seamless and responsive user experience.

### Authors
*   **Giorgio Bonzagni** - 914562
*   **Alessandro Martinelli** - 914362
*   **Davide Resigotti** - 914986

---

## Setup Instructions

To run this project locally, you need to configure your environment with the necessary API keys, as they are not included in the repository for security reasons.

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/Davide-Resigotti/Moes.git
    ```

2.  **Create `local.properties`**:
    Create a file named `local.properties` in the root directory of the project (if it doesn't already exist).

3.  **Add Configuration**:
    Add the following lines to `local.properties`, replacing `YOUR_SDK_PATH` and the tokens with your actual values:

    ```properties
    sdk.dir=/Users/YOUR_USER/Library/Android/sdk
    MAPBOX_DOWNLOADS_TOKEN=sk.YOUR_SECRET_TOKEN
    MAPBOX_PUBLIC_TOKEN=pk.YOUR_PUBLIC_TOKEN
    ```

    > **Note:** You can obtain Mapbox tokens from your [Mapbox Account Dashboard](https://account.mapbox.com/). The `MAPBOX_DOWNLOADS_TOKEN` requires the `DOWNLOADS:READ` scope.

4.  **Sync & Build**:
    Sync the project with Gradle files and build the application.