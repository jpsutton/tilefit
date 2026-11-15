# Readable crash traces: keep file names and line numbers. Class and method names stay obfuscated;
# decode them with the release's mapping file (a release asset, see release.yml).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
