# Tenjin SDK Demo App

This is a simple Android application that demonstrates the usage of the Tenjin SDK.

## Getting Started

To get started with this project, you'll need to clone the repository and open it in Android Studio.

### Configuration

Before you can build and run the app, you need to provide your Tenjin API key.

1.  Create a file named `local.properties` in the root of the project.
2.  Add your Tenjin API key to the `local.properties` file in the following format:

```
TENJIN_API_KEY="YOUR_TENJIN_API_KEY"
```

Replace `"YOUR_TENJIN_API_KEY"` with your actual Tenjin API key.

### Building

To build the project from the command line, you can use the following command:

```bash
./gradlew build
```

### Running the app

You can run the app from Android Studio or from the command line using the following command:

```bash
./gradlew installDebug
```
