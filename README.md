# Sistema de Gerenciamento de Documentos GAMA

> 📖 **Quer começar rapidamente?** Veja o [Guia Rápido](QUICK_START.md)

## Licença

Este projeto é um software livre, licenciado sob a **GNU Affero General Public License v3.0**.

Consulte o arquivo `LICENSE` para obter detalhes.

## Descrição

Este projeto é um Sistema de Gerenciamento de dados para estudo filológico desenvolvido em Java para o projeto "Acervo Alcina Dantas (AAD): interação entre filologia, arquivística e TICs" de Pollianna dos Santos Ferreira Silva e Rosa Borges.

O principal objetivo do sistema é automatizar a organização e o gerenciamento dos dados relativos aos documentos do Acervo Alcina Dantas (AAD). Ele foi concebido a partir de uma perspectiva dialógica entre a Filologia e as Tecnologias da Informação e Comunicação (TICs), servindo como uma ferramenta para otimizar o acesso a informações essenciais para a elaboração de edições críticas e estudos filológicos.

O sistema fornece uma interface gráfica para que o(a) usuário(a) possa registrar, classificar e interagir com os documentos do acervo. Os dados são armazenados em um repositório local em formato JSON, com suporte para anexar arquivos de mídia como fac-símiles dos testemunhos (imagens, PDFs), vídeos e áudios.

## Funcionalidades

*   **Cadastro de Documentos**: Interface de formulário para registro detalhado de documentos, incluindo classificação por classes e subclasses, e informações para geração de referências (NBR 6023).
*   **Geração Automática de Código**: Criação automática de um código de identificação único para cada documento registrado.
*   **Indexação**: Permite a indexação de pessoas e instituições citadas nos documentos, facilitando a criação de links entre trabalhos relacionados.
*   **Gerenciamento de Documentos**: Exibe uma lista de todos os documentos, com funcionalidades para filtrar, visualizar e editar os registros.
*   **Suporte a Múltiplas Fontes**: Gerenciamento de documentos mono e poli-testemunhais.
*   **Exportação**: Gera relatórios em formato DOCX, incluindo um **Inventário** completo e uma **Ficha-Catálogo** para documentos específicos.
*   **Armazenamento Flexível**: Os dados são salvos em formato JSON, e os arquivos de mídia associados são mantidos em um diretório local.

## Começando

### Requisitos

- **Java JDK 17 ou superior** (recomendado JDK 17 com suporte a jpackage)
- **Maven 3.6+** para compilar o projeto
- **WiX Toolset 3.11+** (opcional, apenas para criar instaladores MSI)

### Opção 1: Executar com Maven (Desenvolvimento)

1.  Clone o repositório:
    ```bash
    git clone <url-do-repositorio>
    cd gama-filologia
    ```

2.  Execute diretamente com Maven (o Maven baixará o JavaFX automaticamente):
    ```bash
    mvn javafx:run
    ```

### Opção 2: Criar Pacote Standalone com JavaFX Embutido (Recomendado para Distribuição)

**Esta é a opção mais fácil para os usuários finais!** O JavaFX vem totalmente embutido no pacote, sem necessidade de instalação ou configuração adicional.

#### Windows PowerShell:

```powershell
# Cria um pacote standalone (pasta GAMA com executável)
.\build-package.ps1

# OU cria um instalador MSI
.\build-installer.ps1
```

#### Windows CMD:

```cmd
# Cria um pacote standalone (pasta GAMA com executável)
build-package.bat
```

O script irá:
1. Compilar o projeto com Maven
2. Criar o pacote usando jpackage com JavaFX embutido
3. Gerar a aplicação em `dist\GAMA\`

**Vantagens:**
- ✅ JavaFX incluído no runtime - usuário não precisa instalar nada
- ✅ Aplicação standalone - basta copiar a pasta GAMA
- ✅ Funciona sem configuração adicional

### Opção 3: Compilar Manualmente

Se preferir executar os comandos manualmente:

1.  Compile o projeto:
    ```bash
    mvn clean package -DskipTests
    ```

2.  Crie o pacote com jpackage (inclui JavaFX):
    ```cmd
    jpackage --input target ^
      --name GAMA ^
      --main-jar digital-library-api-1.2.2-jar-with-dependencies.jar ^
      --main-class com.digitallib.MainFX ^
      --type app-image ^
      --dest dist ^
      --java-options "--enable-preview" ^
      --icon book_ico.ico ^
      --win-console ^
      --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base
    ```

**Nota importante:** O parâmetro `--add-modules` garante que os módulos JavaFX sejam incluídos no runtime empacotado.

## Uso

Ao executar a aplicação, a janela principal exibirá a lista de documentos cadastrados.

*   **Filtros**: Utilize os campos na parte superior da janela para filtrar a lista de documentos por código, série ou tipo de testemunho (édito/inédito).
*   **Adicionar Documento**:
    *   Clique no menu `Arquivo > Adicionar mono-testemunhal` para abrir o formulário de cadastro de um novo documento.
    *   Use `Arquivo > Adicionar poli-testemunhal` para agrupar múltiplos testemunhos sob um mesmo título.
*   **Exportar Dados**: No menu `Exportar`, você pode escolher entre:
    *   `Exportar Inventário`: Gera um documento Word com a lista de todos os documentos filtrados.
    *   `Exportar Ficha-catálogo`: Gera um documento Word com fichas detalhadas para os documentos selecionados.
*   **Editar/Excluir**: Na lista de documentos, a coluna "Ações" contém botões para editar ou excluir um registro.

## Dependências

*   [Jackson](https://github.com/FasterXML/jackson): Para manipulação de dados em formato JSON.
*   [Apache POI](https://poi.apache.org/): Para criação e manipulação de arquivos do Microsoft Office (DOCX).
*   [Log4j](https://logging.apache.org/log4j/2.x/): Para registro de logs da aplicação.
*   [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml/src/master/): Para manipulação de arquivos de configuração YAML.
*   [JUnit](https://junit.org/junit5/): Para a execução de testes unitários.

---

#  Philology Document Management System GAMA

> 📖 **Want to get started quickly?** See the [Quick Start Guide](QUICK_START.md)

## License

This project is free software, licensed under the **GNU Affero General Public License v3.0**.

See the `LICENSE` file for details.

## Description

Here is the translation:

This project is a Data Management System for philological study developed in Java for the project "Alcina Dantas Collection (AAD): interaction between philology, archival science, and ICTs" by Pollianna dos Santos Ferreira Silva and Rosa Borges.

The main objective of the system is to automate the organization and management of data related to the documents in the Alcina Dantas Collection (AAD). It was conceived from a dialogical perspective between Philology and Information and Communication Technologies (ICTs), serving as a tool to optimize access to essential information for the preparation of critical editions and philological studies.

The system provides a graphical interface for the user to register, classify, and interact with the collection's documents. The data is stored in a local repository in JSON format, with support for attaching media files such as facsimiles of the testimonies (images, PDFs), videos, and audio.

## Features

*   **Document Registration**: A form-based interface for detailed document registration, including classification by classes and subclasses, and information for generating references (according to the Brazilian standard NBR 6023).
*   **Automatic Code Generation**: Automatically creates a unique identification code for each registered document.
*   **Indexing**: Allows indexing of people and institutions mentioned in the documents, facilitating the creation of links between related works.
*   **Document Management**: Displays a list of all documents, with features to filter, view, and edit records.
*   **Multi-source Support**: Manages single and multi-testimonial documents.
*   **Exporting**: Generates reports in DOCX format, including a full **Inventory** and a **Catalog Card** for specific documents.
*   **Flexible Storage**: Data is saved in JSON format, and associated media files are kept in a local directory.

## Getting Started

### Requirements

- **Java JDK 17 or higher** (JDK 17 with jpackage support recommended)
- **Maven 3.6+** to build the project
- **WiX Toolset 3.11+** (optional, only needed to create MSI installers)

### Option 1: Run with Maven (Development)

1.  Clone the repository:
    ```bash
    git clone <repository-url>
    cd gama-filologia
    ```

2.  Run directly with Maven (Maven will download JavaFX automatically):
    ```bash
    mvn javafx:run
    ```

### Option 2: Create Standalone Package with Embedded JavaFX (Recommended for Distribution)

**This is the easiest option for end users!** JavaFX comes fully embedded in the package, no installation or additional configuration needed.

#### Windows PowerShell:

```powershell
# Create a standalone package (GAMA folder with executable)
.\build-package.ps1

# OR create an MSI installer
.\build-installer.ps1
```

#### Windows CMD:

```cmd
# Create a standalone package (GAMA folder with executable)
build-package.bat
```

The script will:
1. Compile the project with Maven
2. Create the package using jpackage with embedded JavaFX
3. Generate the application in `dist\GAMA\`

**Advantages:**
- ✅ JavaFX included in runtime - user doesn't need to install anything
- ✅ Standalone application - just copy the GAMA folder
- ✅ Works without additional configuration

### Option 3: Build Manually

If you prefer to run the commands manually:

1.  Build the project:
    ```bash
    mvn clean package -DskipTests
    ```

2.  Create the package with jpackage (includes JavaFX):
    ```cmd
    jpackage --input target ^
      --name GAMA ^
      --main-jar digital-library-api-1.2.2-jar-with-dependencies.jar ^
      --main-class com.digitallib.MainFX ^
      --type app-image ^
      --dest dist ^
      --java-options "--enable-preview" ^
      --icon book_ico.ico ^
      --win-console ^
      --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base
    ```

**Important note:** The `--add-modules` parameter ensures that JavaFX modules are included in the packaged runtime.

## Uso

Ao executar a aplicação, a janela principal exibirá a lista de documentos cadastrados.

*   **Filtros**: Utilize os campos na parte superior da janela para filtrar a lista de documentos por código, série ou tipo de testemunho (édito/inédito).
*   **Adicionar Documento**:
    *   Clique no menu `Arquivo > Adicionar mono-testemunhal` para abrir o formulário de cadastro de um novo documento.
    *   Use `Arquivo > Adicionar poli-testemunhal` para agrupar múltiplos testemunhos sob um mesmo título.
*   **Exportar Dados**: No menu `Exportar`, você pode escolher entre:
    *   `Exportar Inventário`: Gera um documento Word com a lista de todos os documentos filtrados.
    *   `Exportar Ficha-catálogo`: Gera um documento Word com fichas detalhadas para os documentos selecionados.
*   **Editar/Excluir**: Na lista de documentos, a coluna "Ações" contém botões para editar ou excluir um registro.

## Dependências

*   [Jackson](https://github.com/FasterXML/jackson): Para manipulação de dados em formato JSON.
*   [Apache POI](https://poi.apache.org/): Para criação e manipulação de arquivos do Microsoft Office (DOCX).
*   [Log4j](https://logging.apache.org/log4j/2.x/): Para registro de logs da aplicação.
*   [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml/src/master/): Para manipulação de arquivos de configuração YAML.
*   [JUnit](https://junit.org/junit5/): Para a execução de testes unitários.

## JavaFX Runtime

### Para Usuários Finais (Usando o Pacote Distribuído)

**Boa notícia!** Se você recebeu o pacote GAMA criado com os scripts `build-package.ps1` ou `build-package.bat`, **o JavaFX já está incluído** e você não precisa fazer nada. Basta executar `GAMA.exe`.

### Para Desenvolvedores (Executando via IDE ou JAR)

Se você está desenvolvendo e quer executar o projeto diretamente (sem usar o pacote):

#### Opção A: Usar Maven (Recomendado)
```bash
mvn javafx:run
```
O Maven configura automaticamente o JavaFX.

#### Opção B: Executar o JAR com JavaFX manualmente

1. Baixe o JavaFX SDK da sua versão de JDK: https://openjfx.io/
2. Execute com os parâmetros de módulo:

```powershell
$javafxLib = 'C:\caminho\para\javafx-sdk-17.0.8\lib'
$jar = '.\target\digital-library-api-1.2.2-jar-with-dependencies.jar'
java --module-path "$javafxLib" --add-modules=javafx.controls,javafx.fxml,javafx.graphics -jar "$jar"
```

### Solução de Problemas

- **Erro: "componentes de runtime do JavaFX não foram encontrados"**
  - Se usando o pacote distribuído: Verifique se a pasta `GAMA` está completa
  - Se desenvolvendo: Use `mvn javafx:run` ou adicione os parâmetros `--module-path` e `--add-modules`
  
- **Incompatibilidade de versão:** Use JavaFX que corresponda à versão do seu JDK (Java 17 → JavaFX 17)

