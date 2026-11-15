# Build environment for local builds without an Android SDK on the host:
#   scripts/docker-gradle.sh test lint assembleRelease
# JDK 21 and the SDK parts app/build.gradle.kts needs (compileSdk 37).
FROM eclipse-temurin:21-jdk

ARG CMDLINE_TOOLS=16111833
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin

RUN apt-get update \
 && apt-get install -y --no-install-recommends unzip git \
 && rm -rf /var/lib/apt/lists/* \
 && mkdir -p "$ANDROID_HOME/cmdline-tools" \
 && curl -fsSL -o /tmp/tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS}_latest.zip" \
 && unzip -q /tmp/tools.zip -d "$ANDROID_HOME/cmdline-tools" \
 && mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest" \
 && rm /tmp/tools.zip \
 && yes | sdkmanager --licenses >/dev/null \
 && sdkmanager --install "platforms;android-37" "build-tools;37.0.0" "platform-tools" >/dev/null \
 && chmod -R a+rwX "$ANDROID_HOME"

WORKDIR /src
