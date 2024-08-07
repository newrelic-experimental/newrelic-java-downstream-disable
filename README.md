<a href="https://opensource.newrelic.com/oss-category/#new-relic-experimental"><picture><source media="(prefers-color-scheme: dark)" srcset="https://github.com/newrelic/opensource-website/raw/main/src/images/categories/dark/Experimental.png"><source media="(prefers-color-scheme: light)" srcset="https://github.com/newrelic/opensource-website/raw/main/src/images/categories/Experimental.png"><img alt="New Relic Open Source experimental project banner." src="https://github.com/newrelic/opensource-website/raw/main/src/images/categories/Experimental.png"></picture></a>

![GitHub forks](https://img.shields.io/github/forks/newrelic-experimental/newrelic-java-downstream-disable?style=social)
![GitHub stars](https://img.shields.io/github/stars/newrelic-experimental/newrelic-java-downstream-disable?style=social)
![GitHub watchers](https://img.shields.io/github/watchers/newrelic-experimental/newrelic-java-downstream-disable?style=social)

![GitHub all releases](https://img.shields.io/github/downloads/newrelic-experimental/newrelic-java-downstream-disable/total)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/newrelic-experimental/newrelic-java-downstream-disable)
![GitHub last commit](https://img.shields.io/github/last-commit/newrelic-experimental/newrelic-java-downstream-disable)
![GitHub Release Date](https://img.shields.io/github/release-date/newrelic-experimental/newrelic-java-downstream-disable)


![GitHub issues](https://img.shields.io/github/issues/newrelic-experimental/newrelic-java-downstream-disable)
![GitHub issues closed](https://img.shields.io/github/issues-closed/newrelic-experimental/newrelic-java-downstream-disable)
![GitHub pull requests](https://img.shields.io/github/issues-pr/newrelic-experimental/newrelic-java-downstream-disable)
![GitHub pull requests closed](https://img.shields.io/github/issues-pr-closed/newrelic-experimental/newrelic-java-downstream-disable) 
    
# New Relic Java Agent Instrumentation for Downstream Trace Disabling

## Overview

The New Relic Java Agent Instrumentation for downstream trace disabling allows users to dynamically configure and disable specific method traces based on a JSON configuration file. This instrumentation reads the configuration file, processes the specified classes and methods, and applies the necessary transformations to disable tracing for the configured methods.

## Purpose   
The purpose of this extension is to allow you to disable downstream tracing of methods that may be impacting the amount of data collected and/or causing higher than normal CPU and memory usage.  Entering a method in the configuration will cause the Java Agent to track the configured method and turn off everything that is instrumented and called during the execution of that method.    
The typical scenerio would be a method that initiate a large number of very fast database calls, external calls or calls to a cache.    
Note that this turns of tracing of ever instrumented method not just the DB or external calls.   
   
## Key Components

1. **DisableConfigListener**: Listens for changes to the configuration file and processes the new configuration.
2. **DisablePreMain**: Initializes the agent, sets up the configuration listener, and schedules periodic checks for configuration updates.
3. **DisableClassMatcher**: Manages class matchers for the instrumentation.
4. **DisableMethodMatcher**: Manages method matchers for the instrumentation.
5. **DisableClassMethodMatcher**: Combines class and method matchers for the instrumentation.
6. **DisableTracerFactory**: Creates tracers with specific configurations to disable downstream tracing.
7. **TracerUtils**: Utility class for managing tracer configurations and generating method descriptors.

## How It Works

### JSON Configuration

The JSON configuration file (`downstream-disable.json`) specifies the classes and methods for which tracing should be disabled. The configuration file follows this structure:

```json
{
  "toDisable": [
    {
      "classname": "com.example.ExactClass",
      "classType": "exactclass",
      "methods": [
        {
          "methodName": "makeExternalCall",
          "returnType": "void",
          "args": []
        }
      ]
    },
    {
      "classname": "com.example.ExternalCallInterface",
      "classType": "interface",
      "methods": [
        {
          "methodName": "makeExternalCall",
          "returnType": "void",
          "args": []
        }
      ]
    }
  ]
}
```
---

**Please Note**:

- To remove existing configurations from `downstream-disable.json`, you must restart the application. However, adding new configurations does not require a restart.
- Ensure the method you configure for downstream disabling is not already being traced.
---

### Initialization and Configuration Processing

1. **Initialization**:
   - The `DisablePreMain` class initializes the agent and sets up the `DisableConfigListener`.
   - The `DisableConfigListener` reads the initial configuration from the JSON file and processes it.

2. **Configuration Processing**:
   - The `DisableConfigListener` processes the JSON configuration and updates the class and method matchers.
   - It clears existing matchers to ensure that removed configurations are reflected.
   - It creates and adds new matchers based on the updated configuration.

3. **Periodic Configuration Updates**:
   - The `DisablePreMain` class schedules the `DisableConfigListener` to run every minute.
   - The `DisableConfigListener` checks for changes to the configuration file and processes any updates.
   - It re-transforms the matching classes to apply the updated configuration.

### Class and Method Matchers

1. **DisableClassMatcher**:
   - Manages class matchers for the instrumentation.
   - Clears and updates matchers based on the configuration.

2. **DisableMethodMatcher**:
   - Manages method matchers for the instrumentation.
   - Clears and updates matchers based on the configuration.

3. **DisableClassMethodMatcher**:
   - Combines class and method matchers for the instrumentation.

### Tracer Factory

1. **DisableTracerFactory**:
   - Creates tracers with specific configurations to disable downstream tracing.
   - Uses `MetricNameFormat` to generate custom metric names for the tracers.

### Utility Class

1. **TracerUtils**:
   - Manages tracer configurations and generates method descriptors.
   - Provides utility methods for adding and retrieving tracer configurations.

## Example Workflow

1. **Initial Configuration**:
   - The agent reads the initial configuration from `downstream-disable.json`.
   - The `DisableConfigListener` processes the configuration and sets up the matchers.

2. **Configuration Update**:
   - The user updates the JSON configuration file to add or remove classes and methods.
   - The `DisableConfigListener` detects the changes and processes the updated configuration.
   - It clears existing matchers, updates them based on the new configuration, and re-transforms the matching classes.

3. **Tracing Behavior**:
   - The `DisableTracerFactory` creates tracers with configurations to disable downstream tracing for the specified methods.
   - The agent applies the transformations based on the matchers to disable tracing for the configured methods.

By following this workflow, the New Relic Java Agent Instrumentation for downstream trace disabling dynamically updates and applies the configuration to disable tracing for specified methods, ensuring that the tracing behavior is consistent with the provided JSON configuration.
 
## Installation
   
To install:

1. Download the latest release jar files.
2. In the New Relic Java directory (the one containing newrelic.jar), create a directory named extensions if it does not already exist.
3. Copy the downloaded jars into the extensions directory.
4. Create and update the `downstream-disable.json` file with the necessary configuration, and place it in the New Relic agent directory.
5. Restart the application.

## JSON Configuration

### How to Write `downstream-disable.json`

This section explains how to configure the custom tracing for your project. The configuration is a JSON object contained in a file named `downstream-disable.json`. This file should be placed in the New Relic Java Agent directory (the directory containing `newrelic.jar`).

The outer object is a `JSONObject` and contains one element: `toDisable`.

The `toDisable` element is a `JSONArray`, and each element of the array is a `JSONObject` containing configuration for each method to disable tracing.

### Method Tracing Object

The method tracing JSON object contains the following:

- **`classname`**: The fully qualified class name.
- **`classType`**: One of the following: `ExactClass`, `Interface`, `BaseClass`. If not set, 
- **`methods`**: JSON array of method configurations. Include each method that needs to have tracing disabled for the class.

### Method Configuration

The method configuration is a JSON object with the following:

- **`methodName`**: Name of the method.
- **`returnType`**: The class of the return object. Can be `void`, a primitive, or a fully qualified class name.
- **`args`**: A JSON array of strings representing the types (in order) of the parameters of the method.

### Example Configuration

```json
{
  "toDisable": [
    {
      "classname": "com.example.ExactClass",
      "classType": "ExactClass",
      "methods": [
        {
          "methodName": "makeExternalCall",
          "returnType": "void",
          "args": []
        }
      ]
    },
    {
      "classname": "com.example.ExternalCallInterface",
      "classType": "Interface",
      "methods": [
        {
          "methodName": "makeExternalCall",
          "returnType": "void",
          "args": []
        }
      ]
    },
    {
      "classname": "com.example.AbstractBaseClass",
      "classType": "BaseClass",
      "methods": [
        {
          "methodName": "makeExternalCall",
          "returnType": "void",
          "args": []
        }
      ]
    },
    {
      "classname": "zeyt.model.ObjectHome",
      "classType": "BaseClass",
      "methods": [
        {
          "methodName": "getObjectByIds",
          "args": [
            "zeyt.model.SaveContext",
            "java.util.Set"
          ],
          "returnType": "java.util.Set"
        },
        {
          "methodName": "getObjectByIds",
          "args": [
            "zeyt.model.SaveContext",
            "java.util.Set",
            "boolean"
          ],
          "returnType": "java.util.Set"
        },
        {
          "methodName": "getObjectByIds",
          "args": [
            "zeyt.model.SaveContext",
            "java.util.List"
          ],
          "returnType": "java.util.List"
        }
      ]
    },
    {
      "classname": "zeyt.model.KronosTestInterface",
      "classType": "Interface",
      "methods": [
        {
          "methodName": "testThreshold",
          "args": [
            "java.util.Collection"
          ],
          "returnType": "void"
        }
      ]
    },
    {
      "classname": "zeyt.model.KronosExactClass",
      "classType": "ExactClass",
      "methods": [
        {
          "methodName": "testExactClassThreshold",
          "args": [
            "java.util.Collection"
          ],
          "returnType": "void"
        }
      ]
    },
    {
      "classname": "zeyt.model.PositionTest",
      "classType": "ExactClass",
      "methods": [
        {
          "methodName": "doGet",
          "args": [
            "int",
            "java.util.List",
            "java.util.Set"
          ],
          "returnType": "void"
        }
      ]
    }
  ]
}
```



## Getting Started

Once installed and configured, the instrumentation will disable downstream tracing based on the provided configuration. You can monitor these changes in the Transactions and Distributed Traces sections of the New Relic platform.   

## Building

Building the extension requires that Gradle is installed.
To build the extension jars from source, follow these steps:

### Build all extension
To build  extension, do the following:
1. Set an environment variable *NEW_RELIC_EXTENSIONS_DIR* and set its value to the directory where you want the jar file built.
2. Run the command: gradlew clean install

## Support

New Relic has open-sourced this project. This project is provided AS-IS WITHOUT WARRANTY OR DEDICATED SUPPORT. Issues and contributions should be reported to the project here on GitHub.

We encourage you to bring your experiences and questions to the [Explorers Hub](https://discuss.newrelic.com) where our community members collaborate on solutions and new ideas.

## Contributing

We encourage your contributions to improve [Project Name]! Keep in mind when you submit your pull request, you'll need to sign the CLA via the click-through using CLA-Assistant. You only have to sign the CLA one time per project. If you have any questions, or to execute our corporate CLA, required if your contribution is on behalf of a company, please drop us an email at opensource@newrelic.com.

**A note about vulnerabilities**

As noted in our [security policy](../../security/policy), New Relic is committed to the privacy and security of our customers and their data. We believe that providing coordinated disclosure by security researchers and engaging with the security community are important means to achieve our security goals.

If you believe you have found a security vulnerability in this project or any of New Relic's products or websites, we welcome and greatly appreciate you reporting it to New Relic through [HackerOne](https://hackerone.com/newrelic).

## License

New Relic Java Agent Instrumentation for Downstream Trace Disabling is licensed under the [Apache 2.0](http://apache.org/licenses/LICENSE-2.0.txt) License.

>[If applicable: [Project Name] also uses source code from third-party libraries. You can find full details on which libraries are used and the terms under which they are licensed in the third-party notices document.]
