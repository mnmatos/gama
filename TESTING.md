# Testes / Testing

## `DocumentCreatorUITest`

**Localização / Location:** `src/test/java/com/digitallib/DocumentCreatorUITest.java`

---

### PT-BR

Este é um teste de integração de UI baseado em [TestFX](https://github.com/TestFX/TestFX) que exercita o fluxo completo de criação e edição de um documento através da interface gráfica (`DocumentCreator.fxml`).

#### O que é testado

O único caso de teste (`testCreateAndSaveDocument`) cobre as seguintes etapas de forma automatizada:

1. **Informações Gerais** – Preenchimento dos campos Título, Subtítulo, Instituição de Custódia, Ano e Local onde foi encontrado.
2. **Classe e Subclasse** – Seleção dos itens nas ComboBoxes de classificação.
3. **Código Manual** – Habilitação do checkbox de código manual e digitação de um código no formato `<PREFIXO>.CLASSE.SEQUÊNCIA` (ex.: `NC.TEST.001`).
4. **Aba "Descrição"** – Preenchimento dos campos de Descrição e Transcrição.
5. **Aba "ABNT"** – Preenchimento dos dados de Editora, Ano de Publicação, Lugar de Publicação e configuração dos Spinners de Edição, Páginas, Dia e Mês.
6. **Aba "ABNT p2"** – Preenchimento do Ano de Depósito e Tipo de Trabalho.
7. **Salvamento** – Clique no botão "Salvar" e verificação da criação do arquivo JSON no caminho esperado (`<projectPath>/documents/<PREFIXO>/<CLASSE>/<SEQUÊNCIA>/<código>.json`).
8. **Validação do conteúdo** – Leitura do JSON salvo e asserção de todos os campos preenchidos.
9. **Atualização** – Modificação do título e novo salvamento, com verificação de que o arquivo foi atualizado corretamente.

#### Configuração (`@BeforeEach` / `@AfterEach`)

- Um diretório temporário é criado antes de cada teste e definido na propriedade do sistema `selected.project.path`, que é o caminho base usado pelo `RepositoryManager`.
- Após cada teste o diretório temporário é deletado pelo `RobustFileDeleter`.

#### Inicialização da UI (`@Start`)

O método `start(Stage)` carrega `DocumentCreator.fxml` via `FXMLLoader`, envolve o `DialogPane` em um `VBox` junto com um `Button` de salvamento (id `#saveButton`) e exibe a cena numa janela 1000 × 800.

#### Como executar

```bash
mvn test -Dtest=DocumentCreatorUITest
```

Para executar todos os testes:

```bash
mvn test
```

> **Nota:** Os testes de UI requerem um ambiente gráfico (display) disponível. Em ambientes headless (ex.: CI sem servidor X), pode ser necessário configurar um display virtual (ex.: Xvfb no Linux).

#### Dependências de teste utilizadas

| Dependência | Função |
|-------------|--------|
| [TestFX](https://github.com/TestFX/TestFX) | Framework de automação de UI para JavaFX |
| [AssertJ](https://assertj.github.io/doc/) | Biblioteca de asserções fluentes |
| [JUnit 5](https://junit.org/junit5/) | Motor de execução dos testes |
| [Jackson](https://github.com/FasterXML/jackson) | Leitura do JSON salvo para validação |

---

### EN

This is a UI integration test based on [TestFX](https://github.com/TestFX/TestFX) that exercises the full create-and-edit document flow through the graphical interface (`DocumentCreator.fxml`).

#### What is tested

The single test case (`testCreateAndSaveDocument`) covers the following steps in an automated fashion:

1. **General Info** – Filling in the Title, Subtitle, Custodian Institution, Year and Place Found fields.
2. **Class & Subclass** – Selecting items from the classification ComboBoxes.
3. **Manual Code** – Enabling the manual code checkbox and typing a code in the format `<PREFIX>.CLASS.SEQUENCE` (e.g. `NC.TEST.001`).
4. **"Descrição" Tab** – Filling in the Description and Transcription fields.
5. **"ABNT" Tab** – Filling in Publisher, Publication Year, Place of Publication, and setting the Edition, Pages, Day and Month spinners.
6. **"ABNT p2" Tab** – Filling in the Deposit Year and Work Type fields.
7. **Save** – Clicking the "Save" button and verifying that the JSON file is created at the expected path (`<projectPath>/documents/<PREFIX>/<CLASS>/<SEQUENCE>/<code>.json`).
8. **Content Validation** – Reading the saved JSON file and asserting every filled field.
9. **Update** – Modifying the title and saving again, then verifying the file reflects the change.

#### Setup (`@BeforeEach` / `@AfterEach`)

- A temporary directory is created before each test and set in the `selected.project.path` system property, which is the base path used by `RepositoryManager`.
- After each test the temporary directory is deleted by `RobustFileDeleter`.

#### UI Initialisation (`@Start`)

The `start(Stage)` method loads `DocumentCreator.fxml` via `FXMLLoader`, wraps the `DialogPane` in a `VBox` alongside a save `Button` (id `#saveButton`), and displays the scene in a 1000 × 800 window.

#### How to run

```bash
mvn test -Dtest=DocumentCreatorUITest
```

To run all tests:

```bash
mvn test
```

> **Note:** UI tests require a graphical display. In headless environments (e.g. CI without an X server), a virtual display (e.g. Xvfb on Linux) may be required.

#### Test dependencies used

| Dependency | Purpose |
|------------|---------|
| [TestFX](https://github.com/TestFX/TestFX) | UI automation framework for JavaFX |
| [AssertJ](https://assertj.github.io/doc/) | Fluent assertion library |
| [JUnit 5](https://junit.org/junit5/) | Test execution engine |
| [Jackson](https://github.com/FasterXML/jackson) | Reading the saved JSON for content validation |

