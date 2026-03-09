param(
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

    if (([string]::IsNullOrWhiteSpace($version)) -or ([string]::IsNullOrWhiteSpace($artifactId))) {
        throw "Could not determine project version or artifactId from pom.xml"
    }

    return @{
        Version    = $version
        ArtifactId = $artifactId
    }
}

# Build GAMA MSI Installer with JavaFX included
# Creates a Windows installer (.msi) that includes JavaFX runtime

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  GAMA MSI Installer Builder" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

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
    exit 1
}

Write-Host ""

# Step 1: Build the project
Write-Host "[1/4] Compiling the project with Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Error compiling the project." -ForegroundColor Red
    exit 1
}
Write-Host "OK: Project compiled successfully!" -ForegroundColor Green
Write-Host ""

# Step 2: Download JavaFX SDK
Write-Host "[2/4] Downloading JavaFX SDK..." -ForegroundColor Yellow
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

# Find JavaFX lib directory (contains jmods)
$javafxLibDir = Get-ChildItem -Path $javafxExtract -Filter "lib" -Recurse -Directory | Select-Object -First 1
if (-not $javafxLibDir) {
    Write-Host "ERROR: JavaFX lib directory not found." -ForegroundColor Red
    exit 1
}

# Resolve JavaFX bin directory (contains native DLLs)
$javafxBinDir = Join-Path $javafxLibDir.Parent.FullName "bin"

# Step 3: Create a custom runtime with jlink
Write-Host "[3/4] Creating custom runtime with jlink..." -ForegroundColor Yellow
$jlinkDir = Join-Path $tempDir "gama-runtime"
if (Test-Path $jlinkDir) { Remove-Item -Path $jlinkDir -Recurse -Force }
$jlinkArgs = @(
    "--module-path", "$($javafxLibDir.FullName);$($env:JAVA_HOME)\jmods",
    "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics,javafx.base",
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

# Step 4: Create MSI installer with jpackage
Write-Host "[4/4] Creating MSI installer with jpackage..." -ForegroundColor Yellow

# Ensure output directory exists
$OutputDir = ".\dist"
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

# Build a clean staging directory with ONLY the fat jar + native DLLs
$stagingDir = Join-Path $tempDir "staging"
if (Test-Path $stagingDir) { Remove-Item -Path $stagingDir -Recurse -Force }
New-Item -ItemType Directory -Path $stagingDir -Force | Out-Null

# Copy the fat jar
$jarFile = "target\$jarFileName"
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
$tempBuildPath = Join-Path $destPath "temp_build"

# Clean and create temp build directory
if (Test-Path $tempBuildPath) {
    Remove-Item -Path $tempBuildPath -Recurse -Force
}
New-Item -ItemType Directory -Path $tempBuildPath -Force | Out-Null

# Build jpackage command
$javaOptions = '--enable-preview -Dprism.order=d3d,es2,sw -Djava.library.path=$APPDIR'
$jpackageArgs = @(
    "--temp", $tempBuildPath,
    "--input", $inputPath,
    "--name", "GAMA",
    "--main-jar", $jarFileName,
    "--main-class", "com.digitallib.MainFX",
    "--type", "msi",
    "--dest", $destPath,
    "--app-version", $projectVersion,
    "--vendor", "Projeto Acervo Alcina Dantas",
    "--description", "GAMA Document Management System",
    "--java-options", $javaOptions,
    "--runtime-image", $jlinkDir,
    "--license-file", "LICENSE",
    "--copyright", "Copyright (c) 2024 GAMA",
    "--win-console",
    "--win-menu",
    "--win-shortcut",
    "--win-dir-chooser",
    "--win-upgrade-uuid", "c6d9a4b7-56f5-40d4-b351-6d7c4a32b149"
)

# Add icon if exists
if (Test-Path $iconPath) {
    $jpackageArgs += "--icon"
    $jpackageArgs += $iconPath
}

Write-Host "  Running jpackage (this may take a few minutes)..." -ForegroundColor Cyan
& jpackage @jpackageArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Error creating the MSI installer." -ForegroundColor Red
    Write-Host "  Make sure WiX Toolset is installed:" -ForegroundColor Yellow
    Write-Host "  https://wixtoolset.org/releases/" -ForegroundColor Yellow
    exit 1
}

Write-Host "OK: MSI installer created successfully!" -ForegroundColor Green
Write-Host ""

# Step 5: Summary and Cleanup
Write-Host "[5/5] Summary and Cleanup" -ForegroundColor Yellow
$msiFile = Get-ChildItem -Path $destPath -Filter "GAMA-*.msi" | Select-Object -First 1
if ($msiFile) {
    Write-Host "  Installer: $($msiFile.FullName)" -ForegroundColor White
    Write-Host "  Size: $([math]::Round($msiFile.Length / 1MB, 2)) MB" -ForegroundColor White
}

Write-Host "  Cleaning up temporary files..." -ForegroundColor Cyan
Remove-Item -Path $tempDir -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "OK: Cleanup complete." -ForegroundColor Green
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "OK: JavaFX is included in the installer!" -ForegroundColor Green
Write-Host "  The user does NOT need to install JavaFX separately." -ForegroundColor Green
Write-Host "  Distribute the .msi file to end users." -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
