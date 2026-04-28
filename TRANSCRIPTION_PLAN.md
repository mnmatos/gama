# Implementation Plan: Image Transcription for GAMA

This plan adds AI-powered image transcription to the existing JavaFX desktop app. Users will be able to transcribe images attached to `Documento` objects using cloud (Anthropic, OpenAI, AWS Bedrock) or local (Ollama, LM Studio) LLMs, edit results in a block-based editor, compare transcriptions across witnesses linked by `MultiSourcedDocument`, and configure providers via a new settings screen.

---

## Resolved Design Decisions

1. **`arquivos` path format** — Entries are **filenames only**. The full disk path is resolved by calling `RepositoryManager.getPathFromCode(doc.getCodigo())`, which splits the code on `"."` and maps each segment to a subfolder (e.g. `ABC.001.02` → `…/repo/documents/ABC/001/02/`). The image byte path is then `getPathFromCode(codigo) + "/" + filename`.
2. **Transcription versioning** — Each new LLM run **overwrites** the previous `TranscriptionRecord`, but the record saves which `llmProvider` and `llmModel` were used last, so the user can always see what generated the current transcription.
3. **Block editor interactions** — The block editor supports **drag-to-reorder**, **split block** (cursor position splits a `TextBlock` into two), and **merge block** (adjacent blocks of compatible types merge into one). A custom `ListView`-based component handles all three gestures.

---

## Phase 1 — Data Model

**No breaking changes** — existing documents deserialize fine via `@JsonIgnoreProperties(ignoreUnknown=true)`.

### 1. `BlockType` enum — `com.digitallib.model`
Values: `HEADING`, `PARAGRAPH`, `TABLE`, `LIST`, `DATE`, `LABEL`, `FOOTER`, `OTHER`

### 2. `TextBlock` — `com.digitallib.model`
```
id            : String (UUID)
orderIndex    : int
blockType     : BlockType
originalText  : String  ← immutable, set once at transcription time
editedText    : String  ← nullable; null means "not yet manually edited"
confidence    : double  ← 0.0–1.0 from LLM response
boundingHint  : String  ← optional JSON string with rough image region hint
```
- `displayText()` convenience method: returns `editedText` if non-null, else `originalText`
- Jackson-annotated; Jackson already present in project

### 3. `TranscriptionRecord` — `com.digitallib.model`
```
imagePath     : String            ← filename only (e.g. "page1.jpg")
blocks        : List<TextBlock>
llmProvider   : String            ← last provider used (e.g. "anthropic")
llmModel      : String            ← last model used (e.g. "claude-3-5-sonnet-20241022")
status        : TranscriptionStatus (PENDING / PROCESSING / DONE / ERROR)
errorMessage  : String
createdAt     : LocalDateTime
updatedAt     : LocalDateTime
```

### 4. Extend `Documento`
Add field:
```java
@JsonProperty("transcriptions")
private Map<String, TranscriptionRecord> transcriptions = new LinkedHashMap<>();
```
Key = image filename. Existing serialized documents without this key deserialize with an empty map (default value).

### 5. `LlmSettings` — `com.digitallib.model`
```
provider          : String   ← "anthropic" | "openai" | "bedrock" | "ollama" | "lmstudio"
anthropicApiKey   : String
anthropicModel    : String   (default: "claude-3-5-sonnet-20241022")
openaiApiKey      : String
openaiModel       : String   (default: "gpt-4o")
bedrockRegion     : String   (default: "us-east-1")
bedrockModelId    : String   (default: "anthropic.claude-3-5-sonnet-20241022-v2:0")
ollamaBaseUrl     : String   (default: "http://localhost:11434")
ollamaModel       : String   (default: "llava")
lmStudioBaseUrl   : String   (default: "http://localhost:1234")
lmStudioModel     : String
```

### 6. `LlmSettingsManager` — `com.digitallib.manager`
- Loads/saves `llm-settings.json` from the project root directory (same folder as `config.properties`)
- On save: if an API key field arrives as `"***"` or blank, the stored key is preserved
- Singleton; loaded lazily on first access

---

## Phase 2 — LLM Adapter Layer

### 7. `LlmAdapter` interface — `com.digitallib.llm`
```java
List<TextBlock> transcribe(byte[] imageBytes, String mimeType) throws LlmException;
String testConnection() throws LlmException;
```

### 8. Implementations — `com.digitallib.llm`

| Class | Target |
|---|---|
| `AnthropicAdapter` | `https://api.anthropic.com/v1/messages` (raw `HttpClient`) |
| `OpenAiAdapter` | `https://api.openai.com/v1/chat/completions` |
| `BedrockAdapter` | AWS SDK `BedrockRuntimeClient.converseStream()` |
| `OpenAiCompatibleAdapter` | OpenAI-format API with configurable `baseUrl` — serves both Ollama and LM Studio |

All adapters use the same transcription prompt (adapted from the brief):

> *"You are a philological transcription assistant. Transcribe all text in the image faithfully. Return a JSON array of objects with keys: blockType, text, confidence. blockType must be one of: heading, paragraph, table, list, date, label, footer, other. Preserve original spelling and punctuation. Do not summarize or paraphrase."*

### 9. `LlmAdapterFactory` — `com.digitallib.llm`
Static `getAdapter(LlmSettings)` selects the right implementation based on `provider`.

### 10. `ResponseParser` — `com.digitallib.llm`
- Strips markdown fences (` ```json ... ``` `)
- Parses JSON blocks array into `List<TextBlock>`
- On any parse failure: returns a single `PARAGRAPH` block with the raw response text and confidence `0.5`

---

## Phase 3 — Transcription Service

### 11. `TranscriptionService` — `com.digitallib.service`

```java
void transcribeAsync(
    Documento doc,
    String imageFilename,
    LlmSettings settings,
    Consumer<TranscriptionRecord> onDone,
    Consumer<Throwable> onError
)
```

- Resolves image path using `RepositoryManager.getPathFromCode(doc.getCodigo())` — which splits the code by `"."` and turns each segment into a subfolder (e.g. code `ABC.001.02` → `repo/documents/ABC/001/02/`). Full image path: `getPathFromCode(doc.getCodigo()) + "/" + imageFilename`
- Sets record status to `PROCESSING`, persists `Documento`
- Calls `LlmAdapterFactory.getAdapter(settings).transcribe(bytes, mimeType)` on an `ExecutorService` thread
- On success: sets status `DONE`, updates `llmProvider`/`llmModel`/`updatedAt`, persists `Documento`, calls `Platform.runLater(() -> onDone.accept(record))`
- On failure: sets status `ERROR`, persists, calls `Platform.runLater(() -> onError.accept(ex))`

---

## Phase 4 — Settings UI

### 12. `LlmSettingsController` + `LlmSettings.fxml`
- Package: `com.digitallib` / `src/main/resources/com/digitallib/LlmSettings.fxml`
- `TabPane` with tabs: Anthropic, OpenAI, AWS Bedrock, Ollama, LM Studio
- Each tab: API key `PasswordField`, model `ComboBox` (pre-filled with sensible defaults), plus extras:
  - Bedrock: region `TextField`
  - Ollama / LM Studio: base URL `TextField`
- **"Test Connection"** button: runs `adapter.testConnection()` on background thread, shows green ✓ or red ✗ label
- **"Save"** button: calls `LlmSettingsManager.save()`; masked keys are not overwritten
- A **provider selector** `ComboBox` at the top marks which provider is "active"

### 13. Entry point
Add **"Configurações LLM…"** menu item to the main menu bar in `ProjectManagerController`.

---

## Phase 5 — Block Editor Component

This is the most complex UI piece; it is shared between the Transcription View (editable) and Comparison View (read-only).

### 14. `BlockEditorController` + `BlockEditor.fxml` (reusable component)

Backed by an `ObservableList<TextBlock>`.

**Each block card** (custom `ListCell`) contains:
- `ComboBox<BlockType>` — change type
- `TextArea` — shows `displayText()`; writes back to `editedText` on change
- Confidence badge label (color-coded: green ≥ 0.8, yellow ≥ 0.5, red < 0.5)
- `⚠` icon if `confidence < 0.8` and no manual edit yet

**Drag-to-reorder**: standard JavaFX `ListView` drag-and-drop via `setOnDragDetected` / `setOnDragOver` / `setOnDragDropped` on cells; updates `orderIndex` of affected blocks after drop.

**Split block**: toolbar button (or keyboard shortcut `Ctrl+Enter`) splits the focused `TextBlock` at the cursor position into two blocks of the same type; the first block gets the text before the cursor as `editedText`, the second gets the text after.

**Merge block**: select two adjacent blocks → toolbar button "Merge" joins their `displayText()` with a newline as a new `editedText` on the first block; second block is removed from the list.

The component exposes a `readOnly` property; when `true`, the `TextArea` and action buttons are disabled (used in the comparison view).

---

## Phase 6 — Image Transcription View

### 15. `ImageTranscriptionController` + `ImageTranscription.fxml`

Layout (horizontal `SplitPane`):
- **Left pane**: `ScrollPane` containing `ImageView` (zoom in/out with `+`/`-` buttons or scroll + Ctrl)
- **Right pane**: embedded `BlockEditor` component (editable) + toolbar

Toolbar actions:
| Button | Action |
|---|---|
| Transcribe with LLM | Triggers `TranscriptionService.transcribeAsync()`; shows `ProgressIndicator`; disables button during processing |
| Save | Persists current block edits to `Documento` via `RepositoryManager` |
| Export DOCX | Calls `TranscriptionExporter.toDocx()` |
| Export TXT | Calls `TranscriptionExporter.toTxt()` |

Status bar at bottom shows: last provider/model used, `updatedAt` timestamp, and current status.

### 16. `TranscriptionExporter` — `com.digitallib.exporter.docx`

Maps block types to Apache POI Word styles:

| BlockType | Word Style |
|---|---|
| `HEADING` | `Heading2` |
| `PARAGRAPH` | `Normal` |
| `LIST` | `ListBullet` |
| `TABLE` | POI `XWPFTable` |
| Other | `Normal` |

Blocks with `confidence < 0.8` and no manual edit append `[⚠ confiança: XX%]` after the text.

---

## Phase 7 — Integration Points

### 17. `DocumentListController` (or `DocumentCreatorController`)
- Add **"Imagens / Transcrição"** action in the document actions column
- Opens a small picker dialog listing `documento.getArquivos()` filtered to image extensions (`.jpg`, `.jpeg`, `.png`, `.tif`, `.tiff`, `.webp`)
- On selection: opens `ImageTranscription` view in a new `Stage`, passing `documento` and the selected filename via controller setter before `stage.show()`

### 18. `MultiSourceDocumentListController`
- Add **"Comparar Transcrições"** action
- Opens `TranscriptionComparison` view in a new `Stage`, passing the `MultiSourcedDocument`

---

## Phase 8 — Comparison View

### 19. `TranscriptionComparisonController` + `TranscriptionComparison.fxml`

- Accepts a `MultiSourcedDocument`; loads each referenced `Documento` from `RepositoryManager`
- Renders a horizontally scrollable `HBox` of panels — one per witness/document
- Each panel header shows the `Documento` code + title
- Each panel body is an embedded `BlockEditor` component in **read-only mode**
- A **"Sync Scroll"** toggle aligns scroll positions across all panels simultaneously

**Visual diff**:
- Blocks at the same `orderIndex` are compared across panels using `displayText()`
- Differing blocks get a **yellow** card background (same content structure, different text)
- Blocks that exist in some witnesses but not others get a **red** card background (missing/extra)

---

## Phase 9 — Maven Dependencies

Add to `pom.xml`:

```xml
<!-- AWS Bedrock -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>bedrockruntime</artifactId>
    <version>2.25.70</version>
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>auth</artifactId>
    <version>2.25.70</version>
</dependency>
```

**No extra HTTP client** — `java.net.http.HttpClient` (JDK 17 built-in) handles Anthropic, OpenAI, Ollama, and LM Studio.  
**Jackson and Apache POI** are already present in the project — no additions needed for JSON parsing or DOCX export.

---

## Phase 10 — Storage Strategy

`TranscriptionRecord` (with its `TextBlock` list) is stored **embedded inside the existing `Documento` JSON** under `transcriptions.<imageFilename>`:

```json
{
  "codigo": "DOC001",
  "titulo": "...",
  "transcriptions": {
    "page1.jpg": {
      "imagePath": "page1.jpg",
      "llmProvider": "anthropic",
      "llmModel": "claude-3-5-sonnet-20241022",
      "status": "DONE",
      "updatedAt": "2026-04-23T14:30:00",
      "blocks": [
        { "id": "uuid-1", "orderIndex": 0, "blockType": "HEADING", "originalText": "Título do Documento", "editedText": null, "confidence": 0.97 },
        { "id": "uuid-2", "orderIndex": 1, "blockType": "PARAGRAPH", "originalText": "Texto transcrito...", "editedText": "Texto corrigido...", "confidence": 0.72 }
      ]
    }
  }
}
```

No new files, no database — fully consistent with the flat-JSON repo structure in `repo/documents/<seg1>/<seg2>/.../<code>.json` (path built by splitting the document code on `"."` — e.g. code `ABC.001` → `repo/documents/ABC/001/ABC.001.json`).

---

## Phased Rollout Order

| # | Phase | Key Deliverable | Depends On |
|---|-------|----------------|-----------|
| 1 | Data Model (Steps 1–6) | New POJOs + `LlmSettingsManager`; existing data unaffected | — |
| 2 | LLM Adapters (Steps 7–10) | Independently testable with a `main()` driver; add Maven deps | Phase 1 |
| 3 | Transcription Service (Step 11) | Async wiring; path resolution from filename + doc code | Phase 2 |
| 4 | Settings UI (Steps 12–13) | Configure providers before first transcription | Phase 1 |
| 5 | Block Editor Component (Step 14) | Reusable drag/split/merge editor | Phase 1 |
| 6 | Image Transcription View (Steps 15–16) | Core single-document flow | Phases 3, 5 |
| 7 | Integration Points (Steps 17–18) | Expose new views from existing screens | Phase 6 |
| 8 | Comparison View (Step 19) | Multi-witness diff | Phases 5, 7 |
| 9 | Export polish | DOCX block mapping, TXT | Phase 6 |

