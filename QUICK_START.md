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

### Transcrever Imagens (Novo em v1.2.5)

1. Na lista de documentos, clique em **"Imagens / Transcrição"** na coluna de ações.
2. Selecione a imagem desejada no seletor.
3. Acesse **`Configurações > Configurações LLM…`** e escolha o motor:
   - **OCR (Tesseract)** — offline, sem chave de API. Informe o caminho do `tessdata` e o idioma (ex: `por`).
   - **Anthropic / OpenAI / AWS Bedrock / Ollama / LM Studio** — informe a chave de API ou URL.
4. Clique em **"Transcrever com LLM"** na barra de ferramentas da tela de transcrição.
5. Edite os blocos no painel à direita, depois clique em **"Salvar"**.

---

## Como Usar o GAMA

Ao executar a aplicação, a janela principal exibirá a lista de documentos cadastrados.

*   **Filtros**: Utilize os campos na parte superior para filtrar por código, série ou tipo de testemunho (édito/inédito).
*   **Adicionar Documento**:
    *   `Arquivo > Adicionar mono-testemunhal` — novo documento de fonte única.
    *   `Arquivo > Adicionar poli-testemunhal` — agrupa múltiplos testemunhos.
*   **Exportar Dados**: Menu `Exportar`:
    *   `Exportar Inventário` — lista completa em DOCX.
    *   `Exportar Ficha-catálogo` — fichas detalhadas em DOCX.
*   **Transcrever Imagens**: Na coluna "Ações", clique em **"Imagens / Transcrição"** para abrir o seletor de imagens. A **Tela de Transcrição** exibe:
    *   Fac-símile (com zoom in/out) à esquerda.
    *   **Editor de blocos** à direita: tipo (`HEADING`, `PARAGRAPH`, `LIST`, etc.), texto, confiança, dividir/mesclar.
    *   Barra de ferramentas: **Transcrever com LLM**, **Salvar**, **Exportar DOCX**, **Exportar TXT**.
*   **Configurar Motor de Transcrição**: `Configurações > Configurações LLM…`:
    *   **OCR local (Tesseract)** — offline, sem chave de API; configure `tessdata` e idioma (ex: `por`).
    *   **LLM em nuvem**: Anthropic, OpenAI, AWS Bedrock.
    *   **LLM local**: Ollama, LM Studio.
*   **Comparar Transcrições**: Para documentos poli-testemunhais, clique em **"Comparar Transcrições"** — painéis sincronizados com diff visual (amarelo = divergente, vermelho = ausente/extra).
*   **Editar/Excluir**: Botões na coluna "Ações" da lista.

---

## Usage (English)

When the application starts, the main window shows the list of registered documents.

*   **Filters**: Use the fields at the top to filter by code, series, or testimony type.
*   **Add Document**: `Arquivo > Adicionar mono-testemunhal` (single witness) or `Adicionar poli-testemunhal` (multi-witness).
*   **Export**: Menu `Exportar` → Inventory (full list DOCX) or Catalog Card (per-document DOCX).
*   **Transcribe Images**: Click **"Imagens / Transcrição"** in the Actions column → pick an image → **Transcription View**:
    *   Left pane: facsimile with zoom.
    *   Right pane: block editor (type, text, confidence badge, split/merge).
    *   Toolbar: **Transcribe with LLM**, **Save**, **Export DOCX**, **Export TXT**.
*   **Configure Transcription Engine**: `Configurações > Configurações LLM…` — choose **OCR (Tesseract)** for offline use or an LLM provider (Anthropic, OpenAI, AWS Bedrock, Ollama, LM Studio).
*   **Compare Transcriptions**: For multi-witness documents, click **"Comparar Transcrições"** → synchronized side-by-side diff view.
*   **Edit / Delete**: Action buttons in the document list.

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

*Last updated: 2026-05-22*

