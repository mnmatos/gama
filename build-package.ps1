# Build GAMA Package with JavaFX included
# This script creates a standalone application with JavaFX bundled
# No additional JavaFX installation needed by end users!

param(
    [string]$Type = "app-image",  # Options: app-image, msi, exe
    [string]$OutputDir = ".\dist"
)

$ErrorActionPreference = "Stop"

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  GAMA Package Builder with JavaFX" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

# Check if jpackage is available
try {
    $jpackageVersion = & jpackage --version 2>&1
    Write-Host "? jpackage encontrado: $jpackageVersion" -ForegroundColor Green
} catch {
    Write-Host "? Erro: jpackage n?o encontrado no PATH." -ForegroundColor Red
    Write-Host "  Certifique-se de usar JDK 16+ e que o diret?rio bin do JDK est? no PATH." -ForegroundColor Yellow
    exit 1
}

# Check if Maven is available
try {
    $mvnVersion = & mvn --version 2>&1 | Select-Object -First 1
    Write-Host "? Maven encontrado: $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "? Erro: Maven n?o encontrado no PATH." -ForegroundColor Red
    Write-Host "  Instale o Maven e adicione-o ao PATH." -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Step 1: Build the project
Write-Host "[1/5] Compilando o projeto com Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "? Erro ao compilar o projeto." -ForegroundColor Red
    exit 1
}
Write-Host "? Projeto compilado com sucesso!" -ForegroundColor Green
Write-Host ""

# Step 2: Check if the JAR exists
$jarFile = "target\digital-library-api-1.2.2.jar"
if (-not (Test-Path $jarFile)) {
    Write-Host "? Erro: JAR n?o encontrado em $jarFile" -ForegroundColor Red
    exit 1
}
Write-Host "? JAR encontrado: $jarFile" -ForegroundColor Green
Write-Host ""

# Step 3: Download JavaFX SDK
Write-Host "[2/5] Baixando JavaFX SDK..." -ForegroundColor Yellow
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
        Write-Host "  Baixando de: $javafxUrl" -ForegroundColor Cyan
        Invoke-WebRequest -Uri $javafxUrl -OutFile $tempZip -UseBasicParsing
        Write-Host "? Download conclu?do" -ForegroundColor Green
    } catch {
        Write-Host "? Erro ao baixar JavaFX SDK: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "  JavaFX SDK encontrado no cache." -ForegroundColor Green
}

Write-Host "  Extraindo JavaFX SDK..." -ForegroundColor Cyan
Expand-Archive -Path $tempZip -DestinationPath $javafxExtract -Force
Write-Host "? JavaFX SDK extra?do" -ForegroundColor Green
Write-Host ""

# Find JavaFX lib directory
$javafxLibDir = Get-ChildItem -Path $javafxExtract -Filter "lib" -Recurse -Directory | Select-Object -First 1

if (-not $javafxLibDir) {
    Write-Host "? Erro: Diret?rio lib do JavaFX n?o encontrado" -ForegroundColor Red
    exit 1
}

# Step 4: Create package with jpackage (WITH --add-modules)
Write-Host "[3/5] Criando pacote com jpackage e módulos JavaFX..." -ForegroundColor Yellow

# Ensure output directory exists
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

# Get absolute paths
$inputPath = Join-Path $PSScriptRoot "target"
$iconPath = Join-Path $PSScriptRoot "book_ico.ico"
$destPath = Resolve-Path $OutputDir

# Clean destination directory
$gamaPackageDir = Join-Path $destPath "GAMA"
if (Test-Path $gamaPackageDir) {
    Write-Host "  Limpando diretório de destino: $gamaPackageDir" -ForegroundColor Cyan
    Remove-Item -Path $gamaPackageDir -Recurse -Force
}

# Build jpackage command WITH JavaFX modules
$jpackageArgs = @(
    "--input", $inputPath,
    "--name", "GAMA",
    "--main-jar", "digital-library-api-1.2.2.jar",
    "--main-class", "com.digitallib.MainFX",
    "--type", $Type,
    "--dest", $destPath,
    "--java-options", "--enable-preview -Dprism.order=sw",
    "--module-path", $javafxLibDir.FullName,
    "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.web"
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
} else {
    $jpackageArgs += "--win-console"
}

# Run jpackage
Write-Host "  Executando jpackage..." -ForegroundColor Cyan
& jpackage @jpackageArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host "? Erro ao criar o pacote." -ForegroundColor Red
    exit 1
}

Write-Host "? Pacote criado com sucesso!" -ForegroundColor Green
Write-Host ""

# Step 5 & 6 are no longer needed as jpackage handles everything.
Write-Host "[4/5] Limpando arquivos temporários..." -ForegroundColor Yellow
Remove-Item -Path $tempDir -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "? Limpeza concluída." -ForegroundColor Green
Write-Host ""

Write-Host "[5/5] Processo finalizado." -ForegroundColor Yellow
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "? Build Concluído com Sucesso!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Summary
Write-Host "Resumo:" -ForegroundColor Yellow
Write-Host "  Tipo de pacote: $Type" -ForegroundColor White
Write-Host "  Localização: $destPath" -ForegroundColor White

if ($Type -eq "app-image") {
    $exePath = Join-Path $gamaPackageDir "GAMA.exe"
    Write-Host "  Executável: $exePath" -ForegroundColor White
    Write-Host ""
    Write-Host "Para distribuir, copie toda a pasta 'GAMA' para o usuário final." -ForegroundColor Cyan
} else {
    Write-Host "  Instalador criado em: $destPath" -ForegroundColor White
    Write-Host ""
    Write-Host "Distribua o arquivo instalador (.msi ou .exe) para os usuários finais." -ForegroundColor Cyan
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Build Concluido - JavaFX Incluido!" -ForegroundColor Green
Write-Host "O usuario NAO precisa instalar JavaFX separadamente." -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan

