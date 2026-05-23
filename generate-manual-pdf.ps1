<#
.SYNOPSIS
    Gera o guia de instalacao do GAMA Filologia em PDF a partir de INSTALACAO.md.
.PARAMETER OutputFile
    Caminho do PDF de saida. Padrao: .\instalacao-gama.pdf
.PARAMETER SkipBuild
    Se informado, pula a etapa de compilacao Maven.
.EXAMPLE
    .\generate-manual-pdf.ps1
    .\generate-manual-pdf.ps1 -OutputFile ".\docs\instalacao.pdf"
    .\generate-manual-pdf.ps1 -SkipBuild
#>
param(
    [string]$OutputFile = '.\instalacao-gama.pdf',
    [switch]$SkipBuild
)
$ErrorActionPreference = 'Stop'
$ProjectRoot = $PSScriptRoot
Write-Host '=== Gerador de PDF - GAMA Filologia ===' -ForegroundColor Cyan
# --- Localizar Java 17+ ---
$javaExe = $null
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $javaExe = "$env:JAVA_HOME\bin\java.exe"
}
if (-not $javaExe) {
    $jdks = Get-ChildItem "$env:USERPROFILE\.jdks" -ErrorAction SilentlyContinue |
            Where-Object { $_.PSIsContainer } |
            Sort-Object Name -Descending
    foreach ($jdk in $jdks) {
        $candidate = Join-Path $jdk.FullName 'bin\java.exe'
        if (Test-Path $candidate) {
            $ver = & $candidate -version 2>&1 | Select-Object -First 1
            if ($ver -match '"(1[7-9]|[2-9]\d)\.' -or $ver -match '"2[1-9]\.') {
                $javaExe = $candidate
                break
            }
        }
    }
}
if (-not $javaExe) { $javaExe = 'java' }
Write-Host "      Usando Java: $javaExe" -ForegroundColor DarkGray
# --- Build ---
if (-not $SkipBuild) {
    Write-Host '[1/2] Compilando o projeto com Maven...' -ForegroundColor Yellow
    Push-Location $ProjectRoot
    try {
        & mvn package -q -DskipTests
        if ($LASTEXITCODE -ne 0) { throw "Falha na compilacao Maven (codigo $LASTEXITCODE)." }
    } finally {
        Pop-Location
    }
    Write-Host '      Compilacao concluida.' -ForegroundColor Green
} else {
    Write-Host '[1/2] Compilacao ignorada (-SkipBuild).' -ForegroundColor DarkGray
}
# --- Localizar JAR ---
$jar = Get-ChildItem -Path "$ProjectRoot\target" -Filter '*-jar-with-dependencies.jar' |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
if (-not $jar) {
    throw 'JAR nao encontrado em .\target\. Execute sem -SkipBuild primeiro.'
}
Write-Host "      Usando JAR: $($jar.Name)" -ForegroundColor DarkGray
# --- Gerar PDF ---
$InputMd   = Join-Path $ProjectRoot 'INSTALACAO.md'
$OutputAbs = [System.IO.Path]::GetFullPath($OutputFile)
Write-Host "[2/2] Gerando PDF: $OutputAbs" -ForegroundColor Yellow
Write-Host "      Fonte: $InputMd" -ForegroundColor DarkGray
& $javaExe -cp $jar.FullName com.digitallib.ManualPdfExporter $InputMd $OutputAbs
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao gerar o PDF (codigo $LASTEXITCODE)."
}
Write-Host "PDF gerado com sucesso: $OutputAbs" -ForegroundColor Green
if (Test-Path $OutputAbs) {
    $open = Read-Host 'Deseja abrir o PDF agora? (S/N)'
    if ($open -match '^[Ss]') {
        Start-Process $OutputAbs
    }
}