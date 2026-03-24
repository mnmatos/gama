# Exemplos de Uso - Scripts de Build GAMA

## Cenário 1: Desenvolvimento Local (Testar rapidamente)

```powershell
# Execute diretamente com Maven (recomendado para desenvolvimento)
mvn javafx:run
```

**Quando usar:** Durante desenvolvimento, para testar alterações rapidamente.

---

## Cenário 2: Criar Pacote para Testes

```powershell
# PowerShell - Cria pacote standalone
.\build-package.ps1

# O pacote será criado em: dist\GAMA\
# Execute: dist\GAMA\GAMA.exe
```

```cmd
REM CMD - Alternativa
build-package.bat
```

**Quando usar:** Testar a aplicação empacotada antes de distribuir.

---

## Cenário 3: Distribuição para Usuários (Versão Portable)

```powershell
# 1. Criar o pacote
.\build-package.ps1

# 2. Comprimir para distribuição
Compress-Archive -Path dist\GAMA -DestinationPath GAMA-v1.2.2-Portable.zip

# 3. Distribuir o arquivo ZIP
# Usuários apenas extraem e executam GAMA.exe
```

**Quando usar:** Distribuir versão portable (sem instalador).

---

## Cenário 4: Criar Instalador Profissional (MSI)

```powershell
# Requer: WiX Toolset 3.11+ instalado
# Download: https://wixtoolset.org/releases/

.\build-installer.ps1

# Instalador criado em: dist\GAMA-1.2.2.msi
```

**Quando usar:** Distribuição corporativa ou publicação oficial.

---

## Cenário 5: Build com Customizações

### Criar EXE em vez de app-image

```powershell
.\build-package.ps1 -Type exe
```

### Especificar diretório de saída customizado

```powershell
.\build-package.ps1 -OutputDir "C:\Build\Release"
```

### Build completo com limpeza

```powershell
# Limpar builds anteriores
Remove-Item -Recurse -Force dist, target -ErrorAction SilentlyContinue

# Criar novo build
.\build-package.ps1
```

---

## Cenário 6: CI/CD (Build Automatizado)

```powershell
# Script para integração contínua
# Arquivo: .github/workflows/build.yml ou similar

# 1. Verificar pré-requisitos
if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    Write-Error "JDK 17+ com jpackage necessário"
    exit 1
}

# 2. Build
.\build-package.ps1 -OutputDir ".\artifacts"

# 3. Criar release artifact
$version = "1.2.2"
Compress-Archive -Path ".\artifacts\GAMA" -DestinationPath "GAMA-$version-win-x64.zip"

# 4. Upload para GitHub Releases, Artifactory, etc.
# ... seu código de upload ...
```

---

## Cenário 7: Build Multi-Versão

```powershell
# Criar várias versões em paralelo

# Versão portable (app-image)
.\build-package.ps1 -Type app-image -OutputDir ".\dist\portable"
Compress-Archive -Path ".\dist\portable\GAMA" -DestinationPath "GAMA-portable.zip"

# Instalador MSI
.\build-installer.ps1
Move-Item ".\dist\GAMA-*.msi" ".\dist\GAMA-installer.msi"

# Agora você tem:
# - dist\GAMA-portable.zip (versão portátil)
# - dist\GAMA-installer.msi (instalador)
```

---

## Cenário 8: Debug do Pacote

```powershell
# 1. Criar pacote com console habilitado (já é o padrão)
.\build-package.ps1

# 2. Execute e veja logs no console
.\dist\GAMA\GAMA.exe

# 3. Verifique logs da aplicação
Get-Content logs\sgbd.log -Tail 50

# 4. Verifique estrutura do pacote
tree /F .\dist\GAMA\runtime\lib | Select-String "javafx"
```

---

## Cenário 9: Atualizar Apenas a Aplicação (Sem Recompilar Runtime)

Se você já tem um pacote e quer apenas atualizar o JAR da aplicação:

```powershell
# 1. Compilar novo JAR
mvn clean package -DskipTests

# 2. Substituir JAR no pacote existente
Copy-Item "target\digital-library-api-1.2.2-jar-with-dependencies.jar" `
          "dist\GAMA\app\" -Force

# 3. Testar
.\dist\GAMA\GAMA.exe
```

**Vantagem:** Muito mais rápido que recriar todo o pacote (especialmente útil para hot-fixes).

---

## Cenário 10: Validar Pacote Antes de Distribuir

```powershell
# Usar o script de validação integrado (recomendado)
.\validate-package.ps1

# Ou especificar caminho customizado
.\validate-package.ps1 -PackagePath ".\dist\GAMA"
```

O script verifica automaticamente:
- ✓ Estrutura de diretórios
- ✓ Executável principal
- ✓ JAR da aplicação
- ✓ Módulos JavaFX (controls, fxml, graphics, base)
- ✓ Bibliotecas nativas
- ✓ Tamanho do pacote
- ✓ Teste de execução rápido

**Exemplo de saída:**
```
=====================================
  GAMA Package Validator
=====================================

[1/6] Verificando estrutura do pacote...
✓ Pasta do pacote existe: .\dist\GAMA
✓ Executável principal existe
✓ Diretório 'app' existe
✓ Diretório 'runtime' existe

[2/6] Verificando aplicação...
✓ JAR encontrado: digital-library-api-1.2.2-jar-with-dependencies.jar (5.23 MB)

[3/6] Verificando módulos JavaFX...
✓ Módulo javafx.controls
✓ Módulo javafx.fxml
✓ Módulo javafx.graphics
✓ Módulo javafx.base

[4/6] Verificando bibliotecas nativas...
✓ Bibliotecas nativas encontradas (3/3)

[5/6] Verificando tamanho do pacote...
✓ Tamanho do pacote: 156.42 MB

[6/6] Testando execução (teste rápido)...
✓ Aplicação iniciou com sucesso

=====================================
  Resumo da Validação
=====================================

Verificações: 12/12 passaram

✓ PACOTE VÁLIDO E PRONTO PARA DISTRIBUIÇÃO!
```

**Validação manual alternativa (caso queira fazer manualmente):**

```powershell
# Script de validação manual completo

Write-Host "Validando pacote GAMA..." -ForegroundColor Cyan

# 1. Verificar se o pacote existe
if (-not (Test-Path "dist\GAMA\GAMA.exe")) {
    Write-Error "Pacote não encontrado! Execute build-package.ps1 primeiro."
    exit 1
}

# 2. Verificar módulos JavaFX
$javafxModules = @(
    "dist\GAMA\runtime\lib\javafx.controls.jar",
    "dist\GAMA\runtime\lib\javafx.fxml.jar",
    "dist\GAMA\runtime\lib\javafx.graphics.jar",
    "dist\GAMA\runtime\lib\javafx.base.jar"
)

$missing = $javafxModules | Where-Object { -not (Test-Path $_) }
if ($missing) {
    Write-Error "Módulos JavaFX faltando: $($missing -join ', ')"
    exit 1
}

# 3. Verificar tamanho do pacote
$size = (Get-ChildItem -Recurse "dist\GAMA" | Measure-Object -Property Length -Sum).Sum / 1MB
Write-Host "Tamanho do pacote: $([math]::Round($size, 2)) MB" -ForegroundColor Green

# 4. Testar execução (apenas inicia e fecha)
Write-Host "Testando execução..." -ForegroundColor Yellow
$process = Start-Process -FilePath "dist\GAMA\GAMA.exe" -PassThru
Start-Sleep -Seconds 5
if ($process.HasExited) {
    Write-Error "Aplicação fechou inesperadamente!"
    exit 1
}
Stop-Process -Id $process.Id -Force
Write-Host "✓ Validação concluída com sucesso!" -ForegroundColor Green
```

---

## Cenário 11: Criar Documentação de Versão

```powershell
# Gerar README para distribuição

$version = "1.2.2"
$date = Get-Date -Format "yyyy-MM-dd"

@"
# GAMA - Sistema de Gerenciamento de Documentos
Versão: $version
Data: $date

## Requisitos
- Windows 10 ou superior (64-bit)
- Nenhum software adicional necessário

## Instalação (Versão Portable)
1. Extraia o arquivo ZIP
2. Execute GAMA.exe

## Instalação (Versão MSI)
1. Execute o instalador GAMA-$version.msi
2. Siga as instruções na tela
3. Acesse pelo Menu Iniciar

## Suporte
- Documentação: https://github.com/seu-usuario/gama-filologia
- Email: suporte@example.com

## Licença
GNU Affero General Public License v3.0
"@ | Out-File -FilePath "dist\LEIAME.txt" -Encoding UTF8

Write-Host "README criado em dist\LEIAME.txt"
```

---

## Cenário 12: Build para Diferentes Ambientes

```powershell
# Função para criar build com tag de ambiente

function Build-GAMA {
    param(
        [string]$Environment = "production"  # Options: development, staging, production
    )
    
    Write-Host "Building GAMA for: $Environment" -ForegroundColor Cyan
    
    # Configurar variáveis baseadas no ambiente
    $outputDir = ".\dist\$Environment"
    
    # Limpar diretório anterior
    if (Test-Path $outputDir) {
        Remove-Item -Recurse -Force $outputDir
    }
    
    # Build
    .\build-package.ps1 -OutputDir $outputDir
    
    # Adicionar marcador de ambiente
    $envFile = "$outputDir\GAMA\environment.txt"
    "Environment: $Environment`nBuild Date: $(Get-Date)" | Out-File $envFile
    
    Write-Host "✓ Build completo: $outputDir" -ForegroundColor Green
}

# Uso
Build-GAMA -Environment "production"
Build-GAMA -Environment "staging"
```

---

## Troubleshooting Comum

### Erro: "Access Denied" ao executar script

```powershell
# Permitir execução de scripts (uma vez, como administrador)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Erro: "Maven build failed"

```powershell
# Limpar cache do Maven e tentar novamente
mvn clean
Remove-Item -Recurse -Force ~/.m2/repository/org/example/digital-library-api
.\build-package.ps1
```

### Erro: "jpackage fails with exit code 1"

```powershell
# Verificar logs detalhados
.\build-package.ps1 -Verbose

# Ou executar jpackage manualmente com debug
jpackage --input target --name GAMA ... --verbose
```

---

## Recursos

- **Documentação completa:** [README.md](README.md)
- **Guia rápido:** [QUICK_START.md](QUICK_START.md)
- **Detalhes de distribuição:** [DISTRIBUTION.md](DISTRIBUTION.md)

---

*Última atualização: 2026-03-09*

