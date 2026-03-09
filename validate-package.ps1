# Validate GAMA Package - Verifica se o pacote está pronto para distribuição
# Este script valida que o pacote criado tem tudo necessário

param(
    [string]$PackagePath = ".\dist\GAMA"
)

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  GAMA Package Validator" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$errors = @()
$warnings = @()
$totalChecks = 0
$passedChecks = 0

function Test-Item-Exists {
    param([string]$Path, [string]$Description, [bool]$IsWarning = $false)

    $script:totalChecks++

    if (Test-Path $Path) {
        Write-Host "✓ $Description" -ForegroundColor Green
        $script:passedChecks++
        return $true
    } else {
        if ($IsWarning) {
            Write-Host "⚠ $Description" -ForegroundColor Yellow
            $script:warnings += $Description
        } else {
            Write-Host "✗ $Description" -ForegroundColor Red
            $script:errors += $Description
        }
        return $false
    }
}

# Verificar se o pacote existe
Write-Host "[1/6] Verificando estrutura do pacote..." -ForegroundColor Yellow
Test-Item-Exists -Path $PackagePath -Description "Pasta do pacote existe: $PackagePath"
Test-Item-Exists -Path "$PackagePath\GAMA.exe" -Description "Executável principal existe"
Test-Item-Exists -Path "$PackagePath\app" -Description "Diretório 'app' existe"
Test-Item-Exists -Path "$PackagePath\runtime" -Description "Diretório 'runtime' existe"
Write-Host ""

# Verificar JAR da aplicação
Write-Host "[2/6] Verificando aplicação..." -ForegroundColor Yellow
$jarPath = Get-ChildItem -Path "$PackagePath\app" -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($jarPath) {
    $jarSize = [math]::Round($jarPath.Length / 1MB, 2)
    Write-Host "✓ JAR encontrado: $($jarPath.Name) ($jarSize MB)" -ForegroundColor Green
    $passedChecks++
} else {
    Write-Host "✗ Nenhum JAR encontrado em app/" -ForegroundColor Red
    $errors += "JAR da aplicação não encontrado"
}
$totalChecks++
Write-Host ""

# Verificar módulos JavaFX
Write-Host "[3/6] Verificando módulos JavaFX..." -ForegroundColor Yellow
$javafxModules = @(
    @{Name="javafx.controls"; File="javafx.controls.jar"},
    @{Name="javafx.fxml"; File="javafx.fxml.jar"},
    @{Name="javafx.graphics"; File="javafx.graphics.jar"},
    @{Name="javafx.base"; File="javafx.base.jar"}
)

foreach ($module in $javafxModules) {
    $modulePath = "$PackagePath\runtime\lib\$($module.File)"
    Test-Item-Exists -Path $modulePath -Description "Módulo $($module.Name)"
}
Write-Host ""

# Verificar bibliotecas nativas JavaFX
Write-Host "[4/6] Verificando bibliotecas nativas..." -ForegroundColor Yellow
$nativeLibs = @("glass.dll", "javafx_font.dll", "prism_d3d.dll")
$nativeFound = 0
foreach ($lib in $nativeLibs) {
    $libPath = Get-ChildItem -Path "$PackagePath\runtime\bin" -Filter $lib -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($libPath) {
        $nativeFound++
    }
}
if ($nativeFound -gt 0) {
    Write-Host "✓ Bibliotecas nativas encontradas ($nativeFound/$($nativeLibs.Count))" -ForegroundColor Green
    $passedChecks++
} else {
    Write-Host "⚠ Bibliotecas nativas não encontradas (pode ser normal em alguns JDKs)" -ForegroundColor Yellow
    $warnings += "Bibliotecas nativas não verificadas"
}
$totalChecks++
Write-Host ""

# Verificar tamanho do pacote
Write-Host "[5/6] Verificando tamanho do pacote..." -ForegroundColor Yellow
try {
    $size = (Get-ChildItem -Recurse -Path $PackagePath -ErrorAction Stop | Measure-Object -Property Length -Sum).Sum / 1MB
    $sizeRounded = [math]::Round($size, 2)

    if ($size -lt 50) {
        Write-Host "⚠ Pacote muito pequeno: $sizeRounded MB (esperado: 100-250 MB)" -ForegroundColor Yellow
        $warnings += "Tamanho do pacote suspeito"
    } elseif ($size -gt 500) {
        Write-Host "⚠ Pacote muito grande: $sizeRounded MB (esperado: 100-250 MB)" -ForegroundColor Yellow
        $warnings += "Pacote maior que o esperado"
    } else {
        Write-Host "✓ Tamanho do pacote: $sizeRounded MB" -ForegroundColor Green
        $passedChecks++
    }
} catch {
    Write-Host "⚠ Não foi possível calcular o tamanho" -ForegroundColor Yellow
    $warnings += "Tamanho não verificado"
}
$totalChecks++
Write-Host ""

# Verificar se o executável inicia (teste rápido)
Write-Host "[6/6] Testando execução (teste rápido)..." -ForegroundColor Yellow
try {
    $process = Start-Process -FilePath "$PackagePath\GAMA.exe" -PassThru -WindowStyle Hidden
    Start-Sleep -Seconds 3

    if ($process.HasExited) {
        Write-Host "⚠ Aplicação fechou rapidamente (pode ser erro ou comportamento normal)" -ForegroundColor Yellow
        $warnings += "Aplicação fechou rapidamente"
    } else {
        Write-Host "✓ Aplicação iniciou com sucesso" -ForegroundColor Green
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        $passedChecks++
    }
} catch {
    Write-Host "✗ Erro ao tentar executar: $($_.Exception.Message)" -ForegroundColor Red
    $errors += "Falha ao executar GAMA.exe"
}
$totalChecks++
Write-Host ""

# Resumo final
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Resumo da Validação" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Verificações: $passedChecks/$totalChecks passaram" -ForegroundColor $(if ($passedChecks -eq $totalChecks) { "Green" } else { "Yellow" })
Write-Host ""

if ($errors.Count -gt 0) {
    Write-Host "❌ ERROS ENCONTRADOS ($($errors.Count)):" -ForegroundColor Red
    foreach ($error in $errors) {
        Write-Host "  - $error" -ForegroundColor Red
    }
    Write-Host ""
}

if ($warnings.Count -gt 0) {
    Write-Host "⚠ AVISOS ($($warnings.Count)):" -ForegroundColor Yellow
    foreach ($warning in $warnings) {
        Write-Host "  - $warning" -ForegroundColor Yellow
    }
    Write-Host ""
}

if ($errors.Count -eq 0 -and $warnings.Count -eq 0) {
    Write-Host "✓ PACOTE VÁLIDO E PRONTO PARA DISTRIBUIÇÃO!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Próximos passos:" -ForegroundColor Cyan
    Write-Host "  1. Criar ZIP: Compress-Archive -Path '$PackagePath' -DestinationPath 'GAMA-v1.2.2.zip'" -ForegroundColor White
    Write-Host "  2. Testar em outro computador" -ForegroundColor White
    Write-Host "  3. Distribuir para usuários finais" -ForegroundColor White
    exit 0
} elseif ($errors.Count -eq 0) {
    Write-Host "⚠ PACOTE COM AVISOS - Revisar antes de distribuir" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "O pacote pode funcionar, mas verifique os avisos acima." -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "❌ PACOTE INVÁLIDO - Corrija os erros antes de distribuir" -ForegroundColor Red
    Write-Host ""
    Write-Host "Execute novamente o build:" -ForegroundColor Yellow
    Write-Host "  .\build-package.ps1" -ForegroundColor White
    exit 1
}

