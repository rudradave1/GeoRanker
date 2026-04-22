# GeoRanker 📍

GeoRanker is a map-first local ranking system built with **Kotlin Multiplatform**. It helps users discover and rank nearby places using a custom-built ranking engine that considers ratings, pricing, and proximity.

## 🚀 Key Features

- **Map-Centric Discovery**: Integrated Google Maps for a visual browsing experience.
- **Intelligent Ranking Engine**: Places are scored based on a weighted formula:
  - ⭐ **Rating (50%)**: Higher-rated places rank higher.
  - 💰 **Price Level (20%)**: Budget-friendly options are prioritized.
  - 📏 **Distance (30%)**: Closer places get a boost.
- **Dynamic Area Insights**: Automatically calculates and displays the average rating and price distribution for the selected category in the visible map area.
- **Explainable Insights**: Each recommendation comes with a human-readable reason (e.g., *"Highly rated (4.8★) and closer than 90% nearby cafes"*).
- **Offline-First Architecture**: Custom sync logic ensures data is available without an internet connection.
- **Background Synchronization**: Powered by **WorkManager** to keep local data fresh.

---

## 🏗️ Technical Architecture

The project follows a modular architecture using **Kotlin Multiplatform (KMP)**.

### Modules

1.  **`:composeApp` (Android Application)**
    - **UI**: Built with **Jetpack Compose Multiplatform**.
    - **Architecture**: MVVM using `androidx.lifecycle.ViewModel`.
    - **Map Integration**: Uses `google-maps-compose` for the map interface.
    - **Platform Bridges**: Uses `expect`/`actual` for platform-specific details (e.g., `Platform.kt`).

2.  **`:sync` (Multiplatform Library)**
    - **Database**: **SQLDelight** for type-safe local persistence.
    - **Domain Model**: Shared business objects like `Place`, `RankedPlace`, and `AreaInsight`.
    - **Sync Logic**: Custom `SyncManager` with `updatedAt` conflict resolution.
    - **Ranking Engine**: Pure Kotlin logic for location-aware scoring.
    - **Background Tasks**: Android-specific **WorkManager** integration for periodic syncing.

---

## 🛠️ Tech Stack & Dependencies

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin 2.0+ |
| **UI Framework** | Jetpack Compose Multiplatform |
| **State Management** | Kotlin Coroutines & Flow |
| **Persistence** | SQLDelight |
| **Dependency Injection** | Manual DI (Constructor Injection) |
| **Maps** | Google Maps SDK for Android |
| **Background Tasks** | Android WorkManager |
| **Build System** | Gradle Kotlin DSL |

---

## 📦 Data & Sync Library (`:sync`)

The `:sync` module handles the core "Offline Sync" feature:
- **Local Source of Truth**: Data is always served from the local database.
- **Interface Driven**: Mock data can be swapped for a real API by implementing `RemoteDataSource`.
- **Conflict Resolution**: Logic resides in `SyncWorker`, comparing `updatedAt` timestamps.

---

## 🚦 Getting Started

### Configuration
1. Create a `local.properties` file:
   ```properties
   MAPS_API_KEY=YOUR_API_KEY_HERE
   ```
2. The project uses **JDK 17+** and **Gradle 8.x**.

### Build & Run
- **Android**: `./gradlew :composeApp:installDebug`
- **JVM (Desktop)**: `./gradlew :composeApp:run`

---

## 🤝 Contributing

Contributions are welcome! Please open an issue first to discuss major changes.

---

## 📝 License

This project is licensed under the MIT License.
