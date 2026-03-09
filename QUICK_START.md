# Guia Rápido - GAMA

## Para Usuários Finais

Se você recebeu o aplicativo GAMA já compilado:

1. **Extraia** a pasta `GAMA` para um local de sua preferência
2. **Execute** o arquivo `GAMA.exe` dentro da pasta
3. **Pronto!** O aplicativo já tem tudo incluído, incluindo JavaFX

**Nada mais precisa ser instalado!**

---

## Para Desenvolvedores

### Executar em Modo Desenvolvimento

A forma mais simples de executar o projeto durante o desenvolvimento:

```bash
mvn javafx:run
```

Isso compila e executa o projeto com todas as dependências JavaFX configuradas automaticamente.

### Criar Pacote para Distribuição

#### Opção A: Pacote Standalone (Recomendado)

**PowerShell:**
```powershell
.\build-package.ps1
```

**CMD:**
```cmd
build-package.bat
```

O pacote será criado em `dist\GAMA\` e pode ser distribuído copiando toda a pasta.

**Validar o pacote antes de distribuir:**
```powershell
.\validate-package.ps1
```

Este script verifica se o pacote tem todos os componentes necessários, incluindo os módulos JavaFX.

#### Opção B: Instalador MSI (Windows)

```powershell
.\build-installer.ps1
```

Cria um instalador `.msi` profissional com menu iniciar e atalhos.

**Requisito:** WiX Toolset 3.11+ deve estar instalado.

### Estrutura de Arquivos Após Build

```
dist/
  GAMA/
    GAMA.exe          ← Executável principal
    app/              ← JARs da aplicação
    runtime/          ← JRE + JavaFX embutidos
      bin/
      lib/
```

---

## Solução de Problemas

### "jpackage não encontrado"
- Certifique-se de usar JDK 16 ou superior
- Verifique se o diretório `bin` do JDK está no PATH

### "Maven não encontrado"
- Instale o Apache Maven 3.6+
- Adicione o diretório `bin` do Maven ao PATH

### Erro ao executar GAMA.exe
- Verifique se toda a pasta `GAMA` foi copiada (não apenas o .exe)
- O runtime do JavaFX está em `GAMA\runtime\lib`

---

## Desenvolvimento

### Estrutura do Projeto

- `src/main/java` - Código fonte Java
- `src/main/resources` - Recursos (FXML, imagens, configs)
- `src/test/java` - Testes unitários
- `target/` - Arquivos compilados (gerado pelo Maven)
- `dist/` - Pacotes distribuíveis (gerado por jpackage)

### Comandos Maven Úteis

```bash
# Compilar sem executar testes
mvn clean package -DskipTests

# Executar testes
mvn test

# Limpar build anterior
mvn clean

# Executar aplicação JavaFX
mvn javafx:run

# Instalar localmente
mvn install
```

### Configuração IDE

**IntelliJ IDEA:**
1. File → Open → Selecione a pasta do projeto
2. O IDEA detectará automaticamente o projeto Maven
3. Para executar: encontre `MainFX.java` → clique direito → Run

**Eclipse:**
1. File → Import → Maven → Existing Maven Projects
2. Selecione a pasta do projeto
3. Para executar: Run As → Java Application → MainFX

**VS Code:**
1. Instale as extensões "Extension Pack for Java" e "JavaFX Support"
2. Abra a pasta do projeto
3. Use Maven commands na barra lateral

---

## Recursos Adicionais

- **Documentação completa:** Veja [README.md](README.md)
- **Licença:** GNU AGPL v3.0 - Veja [LICENSE](LICENSE)
- **Configuração de classes:** Edite [classes.yaml](classes.yaml)

---

## Quick Start - GAMA (English)

### For End Users

If you received the compiled GAMA application:

1. **Extract** the `GAMA` folder to your preferred location
2. **Run** the `GAMA.exe` file inside the folder
3. **Done!** The application has everything included, including JavaFX

**Nothing else needs to be installed!**

### For Developers

#### Run in Development Mode

The simplest way to run the project during development:

```bash
mvn javafx:run
```

#### Create Distribution Package

**PowerShell:**
```powershell
.\build-package.ps1
```

**CMD:**
```cmd
build-package.bat
```

The package will be created in `dist\GAMA\` and can be distributed by copying the entire folder.

---

*Last updated: 2026-03-09*

