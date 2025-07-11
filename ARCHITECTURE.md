# Monthly Goal Manager - Architecture Documentation

## 📁 Project Structure

This project follows a clean architecture pattern with clear separation of concerns. The codebase is organized into the following packages:

```
com/ablomm/monthlygoalmanager/
├── ui/                           # User Interface Layer
│   ├── screens/                  # Screen-level composables
│   │   ├── HomeScreen.kt        # Main goal management screen
│   │   └── SettingsScreen.kt    # App settings screen
│   ├── components/              # Reusable UI components
│   │   ├── UIHelperComponents.kt    # Common UI elements
│   │   ├── SettingsComponents.kt    # Settings-specific components
│   │   └── GoalListComponents.kt    # Goal list components
│   └── navigation/              # Navigation configuration
│       └── AppNavigation.kt     # Main navigation setup
├── data/                        # Data Layer
│   ├── database/               # Room database
│   │   └── AppDatabase.kt      # Database configuration
│   ├── repository/             # Repository pattern
│   │   └── GoalsRepository.kt  # Main data repository
│   └── preferences/            # App preferences
├── domain/                     # Domain Layer
│   ├── model/                 # Domain models (future)
│   └── enums/                 # Enumerations
│       └── SortMode.kt        # Goal sorting options
├── di/                        # Dependency Injection
│   └── AppModule.kt           # Hilt module configuration
├── utils/                     # Utility classes
└── MainActivity.kt            # Application entry point
```

## 🏗️ Architecture Principles

### 1. **Separation of Concerns**
- **UI Layer**: Handles presentation logic and user interactions
- **Data Layer**: Manages data sources and business logic
- **Domain Layer**: Contains business models and use cases
- **DI Layer**: Manages dependency injection

### 2. **Component Reusability**
- UI components are designed to be reusable across different screens
- Settings components can be easily extended for new preferences
- Helper components provide consistent UI patterns

### 3. **Type Safety**
- Proper package organization prevents accidental dependencies
- Clear import paths make code more maintainable
- Explicit exports through package indices

### 4. **Scalability**
- Easy to add new screens in `ui/screens/`
- New UI components go into appropriate `ui/components/` files
- Database entities and DAOs can be expanded in `data/database/`

## 🔄 Data Flow

```
UI Screens → ViewModels → Repository → Database/Preferences
     ↑                                        ↓
UI Components ← State Updates ← Flow/LiveData ←
```

## 📦 Package Responsibilities

### UI Layer (`ui/`)
- **Screens**: Full-screen composables that represent app screens
- **Components**: Reusable UI components for specific features
- **Navigation**: Routing and navigation configuration

### Data Layer (`data/`)
- **Database**: Room database configuration and DAOs
- **Repository**: Data access abstraction layer
- **Preferences**: App settings and user preferences

### Domain Layer (`domain/`)
- **Models**: Business logic models (future expansion)
- **Enums**: Type-safe enumerations for business logic

### DI Layer (`di/`)
- **Modules**: Hilt dependency injection configuration

## 🚀 Benefits of This Architecture

1. **Maintainability**: Clear file organization makes code easy to find and modify
2. **Testability**: Separated concerns enable easier unit testing
3. **Reusability**: UI components can be shared across screens
4. **Scalability**: Easy to add new features without breaking existing code
5. **Team Collaboration**: Developers can work on different layers independently

## 📋 File Naming Conventions

- Screens: `[Feature]Screen.kt` (e.g., `HomeScreen.kt`)
- Components: `[Feature]Components.kt` (e.g., `SettingsComponents.kt`)
- Repository: `[Domain]Repository.kt` (e.g., `GoalsRepository.kt`)
- Database: `[Entity]Dao.kt`, `AppDatabase.kt`
- Enums: `[Name].kt` (e.g., `SortMode.kt`)

## 🔧 Adding New Features

### New Screen
1. Create `ui/screens/[Feature]Screen.kt`
2. Add navigation route in `ui/navigation/AppNavigation.kt`
3. Create screen-specific components in `ui/components/[Feature]Components.kt`

### New UI Component
1. Add to appropriate file in `ui/components/`
2. Follow existing component patterns for consistency
3. Document component purpose and usage

### New Data Entity
1. Create entity in main package (temporary)
2. Add DAO methods for the entity
3. Update `AppDatabase.kt` and `GoalsRepository.kt`

This architecture provides a solid foundation for the Monthly Goal Manager app while maintaining flexibility for future enhancements.
