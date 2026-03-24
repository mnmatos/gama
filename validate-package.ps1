# Validate GAMA Package - Verifies if the package is ready for distribution
# This script validates that the created package has all the necessary components.
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
        Write-Host "OK - $Description" -ForegroundColor Green
        $script:passedChecks++
        return $true
    } else {
        if ($IsWarning) {
            Write-Host "WARNING - $Description" -ForegroundColor Yellow
            $script:warnings += $Description
        } else {
            Write-Host "ERROR - $Description" -ForegroundColor Red
            $script:errorList += $Description
        }
        return $false
    }
}

# Check if the package exists
Write-Host "[1/6] Checking package structure..." -ForegroundColor Yellow
Test-Item-Exists -Path $PackagePath -Description "Package folder exists: $PackagePath"
Test-Item-Exists -Path "$PackagePath\GAMA.exe" -Description "Main executable exists"
Test-Item-Exists -Path "$PackagePath\app" -Description "app directory exists"
Test-Item-Exists -Path "$PackagePath\runtime" -Description "runtime directory exists"
Write-Host ""

# Verify application JAR
Write-Host "[2/6] Verifying application..." -ForegroundColor Yellow
$jarPath = Get-ChildItem -Path "$PackagePath\app" -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($jarPath) {
    $jarSize = [math]::Round($jarPath.Length / 1MB, 2)
    Write-Host "OK - JAR found: $($jarPath.Name) ($jarSize MB)" -ForegroundColor Green
    $passedChecks++
} else {
    Write-Host "ERROR - No JAR found in app/" -ForegroundColor Red
    $errorList += "Application JAR not found"
}
$totalChecks++
Write-Host ""

# Verify JavaFX modules in the bundled runtime
Write-Host "[3/6] Verifying JavaFX modules in runtime..." -ForegroundColor Yellow
$javaExe = "$PackagePath\runtime\bin\java.exe"
$totalChecks++
if (-not (Test-Path $javaExe)) {
    Write-Host "ERROR - java.exe not found in runtime\bin" -ForegroundColor Red
    $errorList += "java.exe not found in the bundled runtime"
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
        Write-Host "OK - All required JavaFX modules are present in the runtime" -ForegroundColor Green
        $passedChecks++
    } else {
        Write-Host "ERROR - Missing JavaFX modules in runtime:" -ForegroundColor Red
        foreach ($m in $missingModules) {
            Write-Host "       - $m" -ForegroundColor Red
        }
        $errorList += "Missing JavaFX modules: $($missingModules -join ', ')"
    }
}
Write-Host ""

# Verify JavaFX native libraries (in app\ or runtime\bin)
Write-Host "[4/6] Verifying native libraries..." -ForegroundColor Yellow
$nativeLibs = @("glass.dll", "javafx_font.dll", "prism_d3d.dll")
$nativeFound = 0
foreach ($lib in $nativeLibs) {
    $found = Get-ChildItem -Path $PackagePath -Filter $lib -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) { $nativeFound++ }
}
if ($nativeFound -gt 0) {
    Write-Host "OK - JavaFX native libraries found ($nativeFound/$($nativeLibs.Count))" -ForegroundColor Green
    $passedChecks++
} else {
    Write-Host "WARNING - JavaFX native libraries not found - verify the build included the DLLs" -ForegroundColor Yellow
    $warnings += "JavaFX native libraries not found"
}
$totalChecks++
Write-Host ""

# Verify package size
Write-Host "[5/6] Verifying package size..." -ForegroundColor Yellow
try {
    $size = (Get-ChildItem -Recurse -Path $PackagePath -ErrorAction Stop | Measure-Object -Property Length -Sum).Sum / 1MB
    $sizeRounded = [math]::Round($size, 2)
    if ($size -lt 50) {
        Write-Host "WARNING - Package is very small: $sizeRounded MB (expected: 100-250 MB)" -ForegroundColor Yellow
        $warnings += "Suspicious package size (too small)"
    } elseif ($size -gt 500) {
        Write-Host "WARNING - Package is very large: $sizeRounded MB (expected: 100-250 MB)" -ForegroundColor Yellow
        $warnings += "Package larger than expected"
    } else {
        Write-Host "OK - Package size: $sizeRounded MB" -ForegroundColor Green
        $passedChecks++
    }
} catch {
    Write-Host "WARNING - Could not calculate size" -ForegroundColor Yellow
    $warnings += "Size not verified"
}
$totalChecks++
Write-Host ""

# Check if the executable starts (quick test)
Write-Host "[6/6] Testing execution (quick test)..." -ForegroundColor Yellow
try {
    $process = Start-Process -FilePath "$PackagePath\GAMA.exe" -PassThru -WindowStyle Hidden
    Start-Sleep -Seconds 3
    if ($process.HasExited) {
        Write-Host "WARNING - Application closed quickly (could be an error or normal behavior)" -ForegroundColor Yellow
        $warnings += "Application exited quickly"
    } else {
        Write-Host "OK - Application started successfully" -ForegroundColor Green
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        $passedChecks++
    }
} catch {
    Write-Host "ERROR - Error trying to execute: $($_.Exception.Message)" -ForegroundColor Red
    $errorList += "Failed to execute GAMA.exe"
}
$totalChecks++
Write-Host ""

# Final Summary
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Validation Summary" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Checks: $passedChecks/$totalChecks passed" -ForegroundColor $(if ($passedChecks -eq $totalChecks) { "Green" } else { "Yellow" })
Write-Host ""

if ($errorList.Count -gt 0) {
    Write-Host "[ERRORS FOUND] ($($errorList.Count)):" -ForegroundColor Red
    foreach ($err in $errorList) {
        Write-Host "  - $err" -ForegroundColor Red
    }
    Write-Host ""
}

if ($warnings.Count -gt 0) {
    Write-Host "[WARNINGS] ($($warnings.Count)):" -ForegroundColor Yellow
    foreach ($warning in $warnings) {
        Write-Host "  - $warning" -ForegroundColor Yellow
    }
    Write-Host ""
}

if ($errorList.Count -eq 0 -and $warnings.Count -eq 0) {
    Write-Host "PACKAGE IS VALID AND READY FOR DISTRIBUTION!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Create ZIP: Compress-Archive -Path '$PackagePath' -DestinationPath 'GAMA-v1.2.2.zip'" -ForegroundColor White
    Write-Host "  2. Test on another computer" -ForegroundColor White
    Write-Host "  3. Distribute to end-users" -ForegroundColor White
    exit 0
} elseif ($errorList.Count -eq 0) {
    Write-Host "WARNING - PACKAGE HAS WARNINGS - Review before distributing" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "The package might work, but please check the warnings above." -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "ERROR - INVALID PACKAGE - Fix the errors before distributing" -ForegroundColor Red
    Write-Host ""
    Write-Host "Run the build again:" -ForegroundColor Yellow
    Write-Host "  .\build-package.ps1" -ForegroundColor White
    exit 1
}
