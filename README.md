### Pre-requisite

You must have Java 8u25 installed and available in the PATH, already.

### Installing Gradle

```bash
sudo -i
cd /opt
wget https://services.gradle.org/distributions/gradle-2.2.1-bin.zip
unzip gradle-2.2.1-bin.zip
rm -f gradle-2.2.1-bin.zip
ln -s /opt/gradle-2.2.1/bin/gradle /usr/local/bin/gradle
gradle -v
```

### Using Gradle to install all other dependencies

```bash
gradle
```
