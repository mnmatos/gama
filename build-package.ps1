# Build GAMA Package with JavaFX included
# This script creates a standalone application with JavaFX bundled
# No additional JavaFX installation needed by end users!
param(
    [string]$Type = "app-image",
    [string]$OutputDir = ".\dist"
)
$ErrorActionPreference = "Stop"
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  GAMA Package Builder with JavaFX" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
# Check if jpackage is available
try {
    $jpackageVersion = & jpackage --version 2>&1
    Write-Host "[OK] jpackage encontrado: $jpackageVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERRO] jpackage nao encontrado no PATH." -ForegroundColor Red
    Write-Host "  Certifique-se de usar JDK 16+ e que o diretorio bin do JDK esta no PATH." -ForegroundColor Yellow
    exit 1
}
# Check if Maven is available
try {
    $mvnVersion = & mvn --version 2>&1 | Select-Object -First 1
    Write-Host "[OK] Maven encontrado: $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERRO] Maven nao encontrado no PATH." -ForegroundColor Red
    Write-Host "  Instale o Maven e adicione-o ao PATH." -ForegroundColor Yellow
    exit 1
}
Write-Host ""
# Step 1: Build the project
Write-Host "[1/5] Compilando o projeto com Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERRO] Erro ao compilar o projeto." -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Projeto compilado com sucesso!" -ForegroundColor Green
Write-Host ""
# Step 2: Check if the fat JAR exists
$jarFile = "target\digital-library-api-1.2.2-jar-with-dependencies.jar"
if (-not (Test-Path $jarFile)) {
    Write-Host "[ERRO] JAR nao encontrado em $jarFile" -ForegroundColor Red
    exit 1
}
Write-Host "[OK] JAR encontrado: $jarFile" -ForegroundColor Green
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
        Write-Host "[OK] Download concluido" -ForegroundColor Green
    } catch {
        Write-Host "[ERRO] Erro ao baixar JavaFX SDK: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "  JavaFX SDK encontrado no cache." -ForegroundColor Green
}
Write-Host "  Extraindo JavaFX SDK..." -ForegroundColor Cyan
Expand-Archive -Path $tempZip -DestinationPath $javafxExtract -Force
Write-Host "[OK] JavaFX SDK extraido" -ForegroundColor Green
Write-Host ""
# Find JavaFX lib directory
$javafxLibDir = Get-ChildItem -Path $javafxExtract -Filter "lib" -Recurse -Directory | Select-Object -First 1
if (-not $javafxLibDir) {
    Write-Host "[ERRO] Diretorio lib do JavaFX nao encontrado" -ForegroundColor Red
    exit 1
}

# Resolve JavaFX bin directory (contains native DLLs like prism_d3d.dll, glass.dll, etc.)
$javafxBinDir = Join-Path $javafxLibDir.Parent.FullName "bin"

# Step 3.5: Create a custom runtime with jlink
Write-Host "[3/5] Criando runtime customizado com jlink..." -ForegroundColor Yellow
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
    Write-Host "[ERRO] Erro ao criar o runtime com jlink." -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Runtime customizado criado em: $jlinkDir" -ForegroundColor Green
Write-Host ""
# Step 4: Create package with jpackage
Write-Host "[4/5] Criando pacote com jpackage e modulos JavaFX..." -ForegroundColor Yellow
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
    Write-Host "  Limpando diretorio de destino: $gamaPackageDir" -ForegroundColor Cyan
    Remove-Item -Path $gamaPackageDir -Recurse -Force
}
$jpackageArgs = @(
    "--input", $inputPath,
    "--name", "GAMA",
    "--main-jar", "digital-library-api-1.2.2-jar-with-dependencies.jar",
    "--main-class", "com.digitallib.MainFX",
    "--type", $Type,
    "--dest", $destPath,
    "--java-options", "--enable-preview -Dprism.order=d3d,es2,sw -Djava.library.path=`$APPDIR",
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
Write-Host "  Executando jpackage..." -ForegroundColor Cyan
& jpackage @jpackageArgs
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERRO] Erro ao criar o pacote." -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Pacote criado com sucesso!" -ForegroundColor Green
Write-Host ""
Write-Host "[4/5] Limpando arquivos temporarios..." -ForegroundColor Yellow
Remove-Item -Path $tempDir -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "[OK] Limpeza concluida." -ForegroundColor Green
Write-Host ""
Write-Host "[5/6] Processo finalizado." -ForegroundColor Yellow
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "[OK] Build Concluido com Sucesso!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
# Summary
Write-Host "Resumo:" -ForegroundColor Yellow
Write-Host "  Tipo de pacote: $Type" -ForegroundColor White
Write-Host "  Localizacao: $destPath" -ForegroundColor White
if ($Type -eq "app-image") {
    $exePath = Join-Path $gamaPackageDir "GAMA.exe"
    Write-Host "  Executavel: $exePath" -ForegroundColor White
    Write-Host ""
    Write-Host "Para distribuir, copie toda a pasta 'GAMA' para o usuario final." -ForegroundColor Cyan
} else {
    Write-Host "  Instalador criado em: $destPath" -ForegroundColor White
    Write-Host ""
    Write-Host "Distribua o arquivo instalador (.msi ou .exe) para os usuarios finais." -ForegroundColor Cyan
}
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Build Concluido - JavaFX Incluido!" -ForegroundColor Green
Write-Host "O usuario NAO precisa instalar JavaFX separadamente." -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan