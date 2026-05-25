# Build GAMA Package with JavaFX included
# This script creates a standalone application with JavaFX bundled.
# No additional JavaFX installation is needed by end-users.

param(
    [string]$Type = "app-image",
    [string]$OutputDir = ".\dist"
)

# Function to get project details from pom.xml
function Get-ProjectDetails {
    param (
        [string]$PomPath = ".\pom.xml"
    )
    if (-not (Test-Path $PomPath)) {
        throw "pom.xml not found at $PomPath"
    }
    $pomContent = [xml](Get-Content $PomPath)

    $version = $pomContent.project.version
    if ([string]::IsNullOrWhiteSpace($version)) {
        $version = $pomContent.project.parent.version
    }

    $artifactId = $pomContent.project.artifactId
    if ([string]::IsNullOrWhiteSpace($artifactId)) {
        $artifactId = $pomContent.project.parent.artifactId
    }

    if ([string]::IsNullOrWhiteSpace($version) -or [string]::IsNullOrWhiteSpace($artifactId)) {
        throw "Could not determine project version or artifactId from pom.xml"
    }

    return @{
        Version    = $version
        ArtifactId = $artifactId
    }
}

$ErrorActionPreference = "Stop"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  GAMA Package Builder with JavaFX" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

# Get project details from pom.xml
$projectDetails = Get-ProjectDetails
$projectVersion = $projectDetails.Version
$projectArtifactId = $projectDetails.ArtifactId
$jarFileName = "${projectArtifactId}-${projectVersion}-jar-with-dependencies.jar"

# Check if jpackage is available
try {
    $jpackageVersion = & jpackage --version 2>&1
    Write-Host "OK: jpackage found: $jpackageVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: jpackage not found in PATH." -ForegroundColor Red
    Write-Host "  Make sure you are using JDK 16+ and the JDK bin directory is in the PATH." -ForegroundColor Yellow
    exit 1
}

# Check if Maven is available
try {
    $mvnVersion = & mvn --version 2>&1 | Select-Object -First 1
    Write-Host "OK: Maven found: $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Maven not found in PATH." -ForegroundColor Red
    Write-Host "  Please install Maven and add it to your PATH." -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# Step 1: Build the project
Write-Host "[1/5] Compiling the project with Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Error compiling the project." -ForegroundColor Red
    exit 1
}
Write-Host "OK: Project compiled successfully!" -ForegroundColor Green
Write-Host ""

# Step 2: Check if the fat JAR exists
$jarFile = "target\$jarFileName"
if (-not (Test-Path $jarFile)) {
    Write-Host "ERROR: JAR not found at $jarFile" -ForegroundColor Red
    exit 1
}
Write-Host "OK: Fat JAR found: $jarFile" -ForegroundColor Green
Write-Host ""

# Step 3: Download JavaFX SDK
Write-Host "[2/5] Downloading JavaFX SDK..." -ForegroundColor Yellow
$javafxVersion = "17.0.8"
$javafxUrl = "https://download2.gluonhq.com/openjfx/$javafxVersion/openjfx-${javafxVersion}_windows-x64_bin-sdk.zip"
$cacheDir = Join-Path $PSScriptRoot ".cache"
$tempDir = Join-Path $env:TEMP "gama-javafx-build"
$tempZip = Join-Path $cacheDir "javafx-sdk.zip"
$javafxExtract = Join-Path $tempDir "javafx-sdk"

# Create cache directory if it doesn't exist
if (-not (Test-Path $cacheDir)) {
    New-Item -ItemType Directory -Path $cacheDir -Force | Out-Null
}

# Create temp directory
if (Test-Path $tempDir) {
    Remove-Item -Path $tempDir -Recurse -Force
}
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

if (-not (Test-Path $tempZip)) {
    try {
        Write-Host "  Downloading from: $javafxUrl" -ForegroundColor Cyan
        Invoke-WebRequest -Uri $javafxUrl -OutFile $tempZip -UseBasicParsing
        Write-Host "OK: Download complete." -ForegroundColor Green
    } catch {
        Write-Host "ERROR: Error downloading JavaFX SDK: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "  JavaFX SDK found in cache." -ForegroundColor Green
}

Write-Host "  Extracting JavaFX SDK..." -ForegroundColor Cyan
Expand-Archive -Path $tempZip -DestinationPath $javafxExtract -Force
Write-Host "OK: JavaFX SDK extracted." -ForegroundColor Green
Write-Host ""

# Find JavaFX lib directory
$javafxLibDir = Get-ChildItem -Path $javafxExtract -Filter "lib" -Recurse -Directory | Select-Object -First 1
if (-not $javafxLibDir) {
    Write-Host "ERROR: JavaFX lib directory not found." -ForegroundColor Red
    exit 1
}

# Resolve JavaFX bin directory (contains native DLLs like prism_d3d.dll, glass.dll, etc.)
$javafxBinDir = Join-Path $javafxLibDir.Parent.FullName "bin"

# Step 3.5: Create a custom runtime with jlink
Write-Host "[3/5] Creating custom runtime with jlink..." -ForegroundColor Yellow
$jlinkDir = Join-Path $tempDir "gama-runtime"
if (Test-Path $jlinkDir) { Remove-Item -Path $jlinkDir -Recurse -Force }
$jlinkArgs = @(
    "--module-path", "$($javafxLibDir.FullName);$($env:JAVA_HOME)\jmods",
    "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.web",
    "--output", $jlinkDir,
    "--strip-debug",
    "--no-header-files",
    "--no-man-pages",
    "--compress=2"
)
& jlink @jlinkArgs
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Error creating runtime with jlink." -ForegroundColor Red
    exit 1
}
Write-Host "OK: Custom runtime created at: $jlinkDir" -ForegroundColor Green
Write-Host ""

# Step 4: Create package with jpackage
Write-Host "[4/5] Creating package with jpackage and JavaFX modules..." -ForegroundColor Yellow

# Ensure output directory exists
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

# Build a clean staging directory with ONLY the fat jar + native DLLs
$stagingDir = Join-Path $tempDir "staging"
if (Test-Path $stagingDir) { Remove-Item -Path $stagingDir -Recurse -Force }
New-Item -ItemType Directory -Path $stagingDir -Force | Out-Null

# Copy the fat jar
Copy-Item -Path (Join-Path $PSScriptRoot $jarFile) -Destination $stagingDir -Force

# Copy JavaFX native DLLs into staging
if (Test-Path $javafxBinDir) {
    Get-ChildItem -Path $javafxBinDir -Filter "*.dll" | ForEach-Object {
        Copy-Item -Path $_.FullName -Destination $stagingDir -Force
    }
}

# Get absolute paths
$inputPath = $stagingDir
$iconPath = Join-Path $PSScriptRoot "book_ico.ico"
$destPath = Resolve-Path $OutputDir

# Clean destination directory
$gamaPackageDir = Join-Path $destPath "GAMA"
if (Test-Path $gamaPackageDir) {
    Write-Host "  Cleaning destination directory: $gamaPackageDir" -ForegroundColor Cyan
    Remove-Item -Path $gamaPackageDir -Recurse -Force
}

# Note: The single quotes around the java-options value are important to prevent PowerShell from interpreting `$APPDIR`
$javaOptions = '--enable-preview -Dprism.order=d3d,es2,sw -Djava.library.path=$APPDIR'
$jpackageArgs = @(
    "--input", $inputPath,
    "--name", "GAMA",
    "--main-jar", $jarFileName,
    "--main-class", "com.digitallib.MainFX",
    "--type", $Type,
    "--dest", $destPath,
    "--app-version", $projectVersion,
    "--java-options", $javaOptions,
    "--runtime-image", $jlinkDir
)

# Add icon if exists
if (Test-Path $iconPath) {
    $jpackageArgs += "--icon"
    $jpackageArgs += $iconPath
}

# Add installer-specific options
if ($Type -eq "msi" -or $Type -eq "exe") {
    $jpackageArgs += "--win-menu"
    $jpackageArgs += "--win-shortcut"
}

# Run jpackage
Write-Host "  Running jpackage..." -ForegroundColor Cyan
& jpackage @jpackageArgs
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Error creating the package." -ForegroundColor Red
    exit 1
}
Write-Host "OK: Package created successfully!" -ForegroundColor Green
Write-Host ""

Write-Host "[4/5] Cleaning up temporary files..." -ForegroundColor Yellow
Remove-Item -Path $tempDir -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "OK: Cleanup complete." -ForegroundColor Green
Write-Host ""

Write-Host "[5/5] Process finished." -ForegroundColor Yellow
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "OK: Build Completed Successfully!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Summary
Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "  Package Type: $Type" -ForegroundColor White
Write-Host "  Location: $destPath" -ForegroundColor White
if ($Type -eq "app-image") {
    $exePath = Join-Path $gamaPackageDir "GAMA.exe"
    Write-Host "  Executable: $exePath" -ForegroundColor White
    Write-Host ""
    Write-Host "To distribute, zip the entire 'GAMA' folder and send it to the end-user." -ForegroundColor Cyan
} else {
    Write-Host "  Installer created at: $destPath" -ForegroundColor White
    Write-Host ""
    Write-Host "Distribute the installer file (.msi or .exe) to end-users." -ForegroundColor Cyan
}
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "OK: Build Complete - JavaFX Included!" -ForegroundColor Green
Write-Host "The user does NOT need to install JavaFX separately." -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
