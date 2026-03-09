# Build GAMA MSI Installer with JavaFX included
# Creates a Windows installer (.msi) that includes JavaFX runtime

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  GAMA MSI Installer Builder" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Check if jpackage is available
try {
    $jpackageVersion = & jpackage --version 2>&1
    Write-Host "✓ jpackage encontrado: $jpackageVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Erro: jpackage não encontrado no PATH." -ForegroundColor Red
    Write-Host "  Certifique-se de usar JDK 16+ e que o diretório bin do JDK está no PATH." -ForegroundColor Yellow
    exit 1
}

# Check if Maven is available
try {
    $mvnVersion = & mvn --version 2>&1 | Select-Object -First 1
    Write-Host "✓ Maven encontrado: $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Erro: Maven não encontrado no PATH." -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 1: Build the project
Write-Host "[1/3] Compilando o projeto com Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Erro ao compilar o projeto." -ForegroundColor Red
    exit 1
}
Write-Host "✓ Projeto compilado com sucesso!" -ForegroundColor Green
Write-Host ""

# Step 2: Create MSI installer with jpackage
Write-Host "[2/3] Criando instalador MSI com jpackage..." -ForegroundColor Yellow
Write-Host "  Incluindo módulos JavaFX no runtime..." -ForegroundColor Cyan

# Ensure output directory exists
$OutputDir = ".\dist"
if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

# Get absolute paths
$inputPath = Join-Path $PSScriptRoot "target"
$iconPath = Join-Path $PSScriptRoot "book_ico.ico"
$destPath = Resolve-Path $OutputDir

# Build jpackage command
$jpackageArgs = @(
    "--input", $inputPath,
    "--name", "GAMA",
    "--main-jar", "digital-library-api-1.2.2-jar-with-dependencies.jar",
    "--main-class", "com.digitallib.MainFX",
    "--type", "msi",
    "--dest", $destPath,
    "--app-version", "1.2.2",
    "--vendor", "Projeto Acervo Alcina Dantas",
    "--description", "Sistema de Gerenciamento de Documentos GAMA",
    "--java-options", "--enable-preview",
    "--win-console",
    "--win-menu",
    "--win-shortcut",
    "--win-dir-chooser",
    "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics,javafx.base"
)

# Add icon if exists
if (Test-Path $iconPath) {
    $jpackageArgs += "--icon"
    $jpackageArgs += $iconPath
}

Write-Host "  Executando jpackage (isso pode levar alguns minutos)..." -ForegroundColor Cyan
& jpackage @jpackageArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Erro ao criar o instalador MSI." -ForegroundColor Red
    Write-Host "  Certifique-se de que WiX Toolset está instalado:" -ForegroundColor Yellow
    Write-Host "  https://wixtoolset.org/releases/" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Instalador MSI criado com sucesso!" -ForegroundColor Green
Write-Host ""

# Step 3: Summary
Write-Host "[3/3] Resumo" -ForegroundColor Yellow
$msiFile = Get-ChildItem -Path $destPath -Filter "GAMA-*.msi" | Select-Object -First 1
if ($msiFile) {
    Write-Host "  Instalador: $($msiFile.FullName)" -ForegroundColor White
    Write-Host "  Tamanho: $([math]::Round($msiFile.Length / 1MB, 2)) MB" -ForegroundColor White
}
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "✓ JavaFX está incluído no instalador!" -ForegroundColor Green
Write-Host "  O usuário NÃO precisa instalar JavaFX separadamente." -ForegroundColor Green
Write-Host "  Distribua o arquivo .msi para os usuários finais." -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan

