# Sistema de Gerenciamento de Documentos GAMA

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

Para compilar e executar a aplicação, você precisará ter o Java (versão 15 ou superior) e o Maven instalados.

1.  Clone o repositório:
    ```bash
    git clone <url-do-repositorio>
    cd gama-filologia
    ```

2.  Compile o projeto com o Maven. Este comando irá baixar as dependências e criar o arquivo `.jar`.
    ```bash
    mvn clean install
    ```

3.  Execute a aplicação:
    ```bash
    java -jar target/digital-library-api-1.2.2.jar
    ```

Opcional: Criar uma imagem de aplicativo nativa (Windows) com jpackage

Se você deseja empacotar a aplicação como uma imagem de aplicativo nativa no Windows, pode usar a ferramenta `jpackage` que é fornecida com os JDKs recentes (JDK 14+; JDK 16+ recomendado). Execute isso a partir do diretório raiz do projeto após construir o jar. Certifique-se de que `jpackage` esteja no seu PATH (ou use o caminho completo para o executável `jpackage` no diretório `bin` do seu JDK) e que `book_ico.ico` esteja presente no diretório raiz do projeto.

Comando de exemplo (cria uma app-image):

```bash
jpackage --input target --name GAMA --main-jar digital-library-api-1.2.2-jar-with-dependencies.jar --main-class com.digitallib.main --type app-image --java-options '--enable-preview' --icon book_ico.ico --win-console false
```

Notas:
- `--input target` informa ao jpackage para procurar o jar no diretório `target` criado pelo Maven.
- `--name GAMA` é o nome da aplicação.
- `--main-jar` deve apontar para o jar montado (aquele criado pela construção do Maven, geralmente com dependências agrupadas).
- `--main-class` é a classe de ponto de entrada da aplicação.
- `--type app-image` produz uma imagem de aplicativo distribuível (as alternativas incluem `exe`, `msi`, etc.).
- `--java-options` passa flags da JVM para o runtime empacotado; aqui `--enable-preview` é preservado do runtime original.
- `--icon book_ico.ico` aponta para o arquivo de ícone (use um caminho absoluto se o jpackage não conseguir encontrá-lo).
- `--win-console false` desabilita a janela do console no Windows para aplicativos GUI.

Nota para Windows (cmd.exe):

No Windows `cmd.exe`, você deve usar aspas duplas para o valor de `--java-options` (aspas simples não são reconhecidas pelo cmd). Você também pode fornecer caminhos absolutos para `--icon` e `--input`/`--dest` para evitar problemas de caminho. Exemplo (cmd.exe):

```cmd
jpackage --input target --name GAMA --main-jar digital-library-api-1.2.2-jar-with-dependencies.jar --main-class com.digitallib.main --type app-image --java-options "--enable-preview" --icon "%CD%\book_ico.ico" --win-console false --dest "%CD%\out"
```

Você pode adicionar `--dest <diretorio-de-saida>` para controlar onde a imagem do aplicativo é gravada, e ajustar `--type` se você quiser um instalador em vez de uma imagem de aplicativo.

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

To build and run the application, you will need to have Java (version 15 or higher) and Maven installed.

1.  Clone the repository:
    ```bash
    git clone <repository-url>
    cd gama-filologia
    ```

2.  Build the project with Maven. This command will download the dependencies and create the `.jar` file.
    ```bash
    mvn clean install
    ```

3.  Run the application:
    ```bash
    java -jar target/digital-library-api-1.2.2.jar
    ```

Optional: Create a native application image (Windows) with jpackage

If you want to package the application as a native app image on Windows, you can use the `jpackage` tool that ships with recent JDKs (JDK 14+; JDK 16+ recommended). Run this from the project root after building the jar. Make sure `jpackage` is on your PATH (or use the full path to the `jpackage` executable in your JDK's `bin` directory) and that `book_ico.ico` is present in the project root.

Example command (creates an app-image):

```bash
jpackage --input target --name GAMA --main-jar digital-library-api-1.2.2-jar-with-dependencies.jar --main-class com.digitallib.main --type app-image --java-options '--enable-preview' --icon book_ico.ico --win-console false
```

Notas:
- `--input target` informa ao jpackage para procurar o jar no diretório `target` criado pelo Maven.
- `--name GAMA` é o nome da aplicação.
- `--main-jar` deve apontar para o jar montado (aquele criado pela construção do Maven, geralmente com dependências agrupadas).
- `--main-class` é a classe de ponto de entrada da aplicação.
- `--type app-image` produz uma imagem de aplicativo distribuível (as alternativas incluem `exe`, `msi`, etc.).
- `--java-options` passa flags da JVM para o runtime empacotado; aqui `--enable-preview` é preservado do runtime original.
- `--icon book_ico.ico` aponta para o arquivo de ícone (use um caminho absoluto se o jpackage não conseguir encontrá-lo).
- `--win-console false` desabilita a janela do console no Windows para aplicativos GUI.

Nota para Windows (cmd.exe):

No Windows `cmd.exe`, você deve usar aspas duplas para o valor de `--java-options` (aspas simples não são reconhecidas pelo cmd). Você também pode fornecer caminhos absolutos para `--icon` e `--input`/`--dest` para evitar problemas de caminho. Exemplo (cmd.exe):

```cmd
jpackage --input target --name GAMA --main-jar digital-library-api-1.2.2-jar-with-dependencies.jar --main-class com.digitallib.main --type app-image --java-options "--enable-preview" --icon "%CD%\book_ico.ico" --win-console false --dest "%CD%\out"
```

Você pode adicionar `--dest <diretorio-de-saida>` para controlar onde a imagem do aplicativo é gravada, e ajustar `--type` se você quiser um instalador em vez de uma imagem de aplicativo.

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

## JavaFX runtime (required for the launcher)

This project UI was migrated to JavaFX. If you run the packaged `GAMA` launcher you may see the error (in Portuguese):

"Erro: os componentes de runtime do JavaFX não foram encontrados. Eles são obrigatórios para executar este aplicativo"

That means the JVM that launches the app could not find the JavaFX runtime modules. The launcher is configured to look for JavaFX jars under `GAMA/runtime/lib` and to pass them as a module path to the JVM. Follow these steps to provide the JavaFX runtime.

### 1) Download the JavaFX SDK
- Pick the JavaFX SDK version that matches the JDK you use to run the app (e.g. JDK 17 → JavaFX 17).
- Official sources: https://openjfx.io/ or GluonHQ (https://gluonhq.com/products/javafx/).
- Download and unzip the SDK for Windows x64 (if that's your platform).

### 2) Copy the JavaFX SDK `lib` contents to `GAMA/runtime/lib`
- Create the folder `GAMA/runtime/lib` (relative to the project root) and copy all jars from the SDK `lib` folder into it.

PowerShell example (adjust the sdk path):

```powershell
# Example: adjust paths to match your environment
$javafxSdkLib = 'C:\path\to\javafx-sdk-17.0.8\lib'
$dest = 'C:\Users\mnmat\IdeaProjects\gama-filologia\GAMA\runtime\lib'

# Create destination and copy files
New-Item -ItemType Directory -Force -Path $dest | Out-Null
Copy-Item -Path (Join-Path $javafxSdkLib '*') -Destination $dest -Recurse -Force
Write-Host "JavaFX jars copied to $dest"
```

The project launcher `GAMA/app/GAMA.cfg` was updated to include these VM options (it expects the runtime jars under `%APPDIR%\\runtime\\lib`):

``ini
java-options=--module-path=%APPDIR%\\runtime\\lib
java-options=--add-modules=javafx.controls,javafx.fxml,javafx.graphics
```

### 3) Run the packaged launcher
- After copying the JavaFX jars into `GAMA/runtime/lib`, start the `GAMA` launcher (the executable shipped in the `GAMA` folder). The launcher will pass `--module-path` and `--add-modules` to the JVM so JavaFX loads correctly.

### 4) Run from the IDE or directly with `java -jar`
If you prefer to run the jar directly (or from the IDE), pass the same JVM options. Example PowerShell:

```powershell
$javafxLib = 'C:\path\to\javafx-sdk-17.0.8\lib'
$jar = 'C:\Users\mnmat\IdeaProjects\gama-filologia\GAMA\app\digital-library-api-1.2.2-jar-with-dependencies.jar'

java --module-path "$javafxLib" --add-modules=javafx.controls,javafx.fxml,javafx.graphics -jar "$jar"
```

Or for the built target jar created by Maven:

```powershell
$jar = 'C:\Users\mnmat\IdeaProjects\gama-filologia\target\digital-library-api-1.2.2-jar-with-dependencies.jar'
java --module-path "$javafxLib" --add-modules=javafx.controls,javafx.fxml,javafx.graphics -jar "$jar"
```

### 5) Development with Maven
You can run the JavaFX app via the Maven plugin (which will configure the module path automatically):

```powershell
mvn javafx:run
```

Make sure Maven and your IDE use the same JDK version that matches the JavaFX SDK.

### Troubleshooting
- Version mismatch: Use JavaFX that matches the major JDK version (e.g. Java 17 ↔ JavaFX 17).
- Architecture mismatch: Download the JavaFX SDK for your OS/architecture (Windows x64, etc.).
- If the launcher still fails, verify the launcher actually reads `GAMA/app/GAMA.cfg`, and that the `GAMA/runtime/lib` folder contains the JavaFX jars.
- Native library errors: If there are missing DLL or native load errors, use the JavaFX SDK for your platform and ensure Visual C++ redistributable is installed if needed.

---

If you want, I can add a small PowerShell script to this repository that downloads a specified JavaFX SDK release, extracts it and copies the `lib` contents into `GAMA/runtime/lib`. Tell me which JavaFX version (e.g. `17.0.8` or `19.0.2`) you prefer and I'll add the script (I won't run it).
