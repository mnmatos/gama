# Validate GAMA Package - Verifica se o pacote esta pronto para distribuicao
# Este script valida que o pacote criado tem tudo necessario
param(
    [string]$PackagePath = ".\dist\GAMA"
)
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  GAMA Package Validator" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
$errorList = @()
$warnings = @()
$totalChecks = 0
$passedChecks = 0
function Test-Item-Exists {
    param([string]$Path, [string]$Description, [bool]$IsWarning = $false)
    $script:totalChecks++
    if (Test-Path $Path) {
        Write-Host "[OK] $Description" -ForegroundColor Green
        $script:passedChecks++
        return $true
    } else {
        if ($IsWarning) {
            Write-Host "[AVISO] $Description" -ForegroundColor Yellow
            $script:warnings += $Description
        } else {
            Write-Host "[ERRO] $Description" -ForegroundColor Red
            $script:errorList += $Description
        }
        return $false
    }
}
# Verificar se o pacote existe
Write-Host "[1/6] Verificando estrutura do pacote..." -ForegroundColor Yellow
Test-Item-Exists -Path $PackagePath -Description "Pasta do pacote existe: $PackagePath"
Test-Item-Exists -Path "$PackagePath\GAMA.exe" -Description "Executavel principal existe"
Test-Item-Exists -Path "$PackagePath\app" -Description "Diretorio app existe"
Test-Item-Exists -Path "$PackagePath\runtime" -Description "Diretorio runtime existe"
Write-Host ""
# Verificar JAR da aplicacao
Write-Host "[2/6] Verificando aplicacao..." -ForegroundColor Yellow
$jarPath = Get-ChildItem -Path "$PackagePath\app" -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($jarPath) {
    $jarSize = [math]::Round($jarPath.Length / 1MB, 2)
    Write-Host "[OK] JAR encontrado: $($jarPath.Name) ($jarSize MB)" -ForegroundColor Green
    $passedChecks++
} else {
    Write-Host "[ERRO] Nenhum JAR encontrado em app/" -ForegroundColor Red
    $errorList += "JAR da aplicacao nao encontrado"
}
$totalChecks++
Write-Host ""
# Verificar modulos JavaFX no runtime bundled
Write-Host "[3/6] Verificando modulos JavaFX no runtime..." -ForegroundColor Yellow
$javaExe = "$PackagePath\runtime\bin\java.exe"
$totalChecks++
if (-not (Test-Path $javaExe)) {
    Write-Host "[ERRO] java.exe nao encontrado em runtime\bin" -ForegroundColor Red
    $errorList += "java.exe nao encontrado no runtime bundled"
} else {
    $moduleList = & $javaExe --list-modules 2>&1
    $requiredModules = @("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.base")
    $missingModules = @()
    foreach ($mod in $requiredModules) {
        if (-not ($moduleList | Select-String -Pattern "^$mod" -Quiet)) {
            $missingModules += $mod
        }
    }
    if ($missingModules.Count -eq 0) {
        Write-Host "[OK] Todos os modulos JavaFX estao presentes no runtime" -ForegroundColor Green
        $passedChecks++
    } else {
        Write-Host "[ERRO] Modulos JavaFX ausentes no runtime:" -ForegroundColor Red
        foreach ($m in $missingModules) {
            Write-Host "       - $m" -ForegroundColor Red
        }
        $errorList += "Modulos JavaFX ausentes: $($missingModules -join ', ')"
    }
}
Write-Host ""
# Verificar bibliotecas nativas JavaFX (app\ ou runtime\bin)
Write-Host "[4/6] Verificando bibliotecas nativas..." -ForegroundColor Yellow
$nativeLibs = @("glass.dll", "javafx_font.dll", "prism_d3d.dll")
$nativeFound = 0
foreach ($lib in $nativeLibs) {
    $found = Get-ChildItem -Path $PackagePath -Filter $lib -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) { $nativeFound++ }
}
if ($nativeFound -gt 0) {
    Write-Host "[OK] Bibliotecas nativas JavaFX encontradas ($nativeFound/$($nativeLibs.Count))" -ForegroundColor Green
    $passedChecks++
} else {
    Write-Host "[AVISO] Bibliotecas nativas JavaFX nao encontradas - verifique se o build incluiu os DLLs" -ForegroundColor Yellow
    $warnings += "Bibliotecas nativas JavaFX nao encontradas"
}
$totalChecks++
Write-Host ""
# Verificar tamanho do pacote
Write-Host "[5/6] Verificando tamanho do pacote..." -ForegroundColor Yellow
try {
    $size = (Get-ChildItem -Recurse -Path $PackagePath -ErrorAction Stop | Measure-Object -Property Length -Sum).Sum / 1MB
    $sizeRounded = [math]::Round($size, 2)
    if ($size -lt 50) {
        Write-Host "[AVISO] Pacote muito pequeno: $sizeRounded MB (esperado: 100-250 MB)" -ForegroundColor Yellow
        $warnings += "Tamanho do pacote suspeito"
    } elseif ($size -gt 500) {
        Write-Host "[AVISO] Pacote muito grande: $sizeRounded MB (esperado: 100-250 MB)" -ForegroundColor Yellow
        $warnings += "Pacote maior que o esperado"
    } else {
        Write-Host "[OK] Tamanho do pacote: $sizeRounded MB" -ForegroundColor Green
        $passedChecks++
    }
} catch {
    Write-Host "[AVISO] Nao foi possivel calcular o tamanho" -ForegroundColor Yellow
    $warnings += "Tamanho nao verificado"
}
$totalChecks++
Write-Host ""
# Verificar se o executavel inicia (teste rapido)
Write-Host "[6/6] Testando execucao (teste rapido)..." -ForegroundColor Yellow
try {
    $process = Start-Process -FilePath "$PackagePath\GAMA.exe" -PassThru -WindowStyle Hidden
    Start-Sleep -Seconds 3
    if ($process.HasExited) {
        Write-Host "[AVISO] Aplicacao fechou rapidamente (pode ser erro ou comportamento normal)" -ForegroundColor Yellow
        $warnings += "Aplicacao fechou rapidamente"
    } else {
        Write-Host "[OK] Aplicacao iniciou com sucesso" -ForegroundColor Green
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        $passedChecks++
    }
} catch {
    Write-Host "[ERRO] Erro ao tentar executar: $($_.Exception.Message)" -ForegroundColor Red
    $errorList += "Falha ao executar GAMA.exe"
}
$totalChecks++
Write-Host ""
# Resumo final
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Resumo da Validacao" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Verificacoes: $passedChecks/$totalChecks passaram" -ForegroundColor $(if ($passedChecks -eq $totalChecks) { "Green" } else { "Yellow" })
Write-Host ""
if ($errorList.Count -gt 0) {
    Write-Host "[ERROS ENCONTRADOS] ($($errorList.Count)):" -ForegroundColor Red
    foreach ($err in $errorList) {
        Write-Host "  - $err" -ForegroundColor Red
    }
    Write-Host ""
}
if ($warnings.Count -gt 0) {
    Write-Host "[AVISOS] ($($warnings.Count)):" -ForegroundColor Yellow
    foreach ($warning in $warnings) {
        Write-Host "  - $warning" -ForegroundColor Yellow
    }
    Write-Host ""
}
if ($errorList.Count -eq 0 -and $warnings.Count -eq 0) {
    Write-Host "[OK] PACOTE VALIDO E PRONTO PARA DISTRIBUICAO!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Proximos passos:" -ForegroundColor Cyan
    Write-Host "  1. Criar ZIP: Compress-Archive -Path '$PackagePath' -DestinationPath 'GAMA-v1.2.2.zip'" -ForegroundColor White
    Write-Host "  2. Testar em outro computador" -ForegroundColor White
    Write-Host "  3. Distribuir para usuarios finais" -ForegroundColor White
    exit 0
} elseif ($errorList.Count -eq 0) {
    Write-Host "[AVISO] PACOTE COM AVISOS - Revisar antes de distribuir" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "O pacote pode funcionar, mas verifique os avisos acima." -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "[ERRO] PACOTE INVALIDO - Corrija os erros antes de distribuir" -ForegroundColor Red
    Write-Host ""
    Write-Host "Execute novamente o build:" -ForegroundColor Yellow
    Write-Host "  .\build-package.ps1" -ForegroundColor White
    exit 1
}