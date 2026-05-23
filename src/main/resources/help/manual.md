# Manual do Utilizador — GAMA Filologia
## 1. Gerenciamento de Projetos
Ao abrir o GAMA, a tela de gerenciamento de projetos é exibida:
![Gerenciamento de projetos](projetos.png)
- **Criar projeto** — clique em **Criar Projeto**, informe o nome e confirme.
- **Selecionar projeto** — dê duplo-clique na linha ou clique em **Selecionar Projeto**.
- **Importar projeto** — clique em **Importar Projeto** e selecione o arquivo `.zip`.
- **Exportar projeto** — clique em **Exportar Projeto** e escolha o destino.
- **Adicionar existente** — clique em **Adicionar Existente** e selecione a pasta.
---
## 2. Tela Principal — Lista de Documentos
![Lista de documentos](gerenciamento-de-dados-gama.png)
### 2.1 Adicionar Documentos
No menu **Arquivo** utilize as opções:
- **Adicionar monotestemunhal** — documento com um único testemunho.
- **Adicionar politestemunhal** — documento com múltiplos testemunhos.
### 2.2 Filtrar Documentos
Use os filtros no topo da tela:
- **Testemunho** — Inéditos, Éditos ou ambos.
- **Série** — filtra por classe/série do documento.
- **Código** — pesquisa parcial pelo código.
---
## 3. Adicionar / Editar Documento
Ao criar ou editar um documento, o formulário é exibido em abas:
![Formulário de documento](form.png)
### 3.1 Dados Gerais
Preencha os metadados principais — código, título, série e datas:
![Dados gerais](form1.png)
### 3.2 Localização e Arquivo
Informe onde o documento está fisicamente localizado:
![Localização](form2.png)
### 3.3 Informações Adicionais
Campos complementares de descrição e análise:
![Informações adicionais](form3.png)
### 3.4 Relações e Referências
Associe entidades e referências ao documento:
![Relações](form4.png)
### 3.5 Arquivos Anexos
Na aba de arquivos, clique duas vezes num arquivo anexo para abrir o diálogo de ações:
![Diálogo de arquivo](arquivo.png)
A partir deste diálogo é possível:
- **Abrir** o arquivo no programa padrão do sistema.
- **Transcrever** o conteúdo do arquivo com IA.
- **Analisar** o arquivo (PDF).
- **Remover** o arquivo do documento.
---
## 4. Documento Politestemunhal — Tradição
Para documentos com múltiplos testemunhos, a tela de tradição permite visualizar e gerir as diferentes versões:
![Tradição](tradicao.png)
---
## 5. Transcrição com IA
### 5.1 Configurar Serviço de Transcrição
1. No menu **Ferramentas**, clique em **Configurações de Transcrição…**.
2. Escolha o provedor (OpenAI, Anthropic ou AWS Bedrock).
3. Informe a chave de API e o modelo desejado.
4. Clique em **Salvar**.
![Configurações de transcrição](transcricao_config.png)
### 5.2 Transcrever uma Imagem
1. Abra um documento e clique em **Transcrever Imagem**.
2. Selecione o arquivo de imagem (`.jpg`, `.png`, `.tif`).
3. Aguarde o resultado e confirme ou edite a transcrição.
![Tela de transcrição](transcricao.png)
### 5.3 Comparar Transcrições
Use a tela de **Comparação de Transcrições** para visualizar lado a lado diferentes versões de um mesmo manuscrito.
---
## 6. Exportação
### 6.1 Exportar Inventário
1. No menu **Exportar**, clique em **Exportar Inventário**.
2. Escolha o local de destino.
3. O arquivo `.docx` será gerado com todos os documentos do projeto.
### 6.2 Exportar Ficha-Catálogo
1. Selecione um documento na lista.
2. No menu **Exportar**, clique em **Exportar Ficha-catálogo**.
3. O arquivo `.docx` será gerado com os dados detalhados do documento.
---
## 7. Entidades e Referências
O GAMA permite gerir entidades (autores, copistas, arquivos, etc.) e associá-las aos documentos.
1. Acesse o menu **Entidades** para adicionar ou editar.
2. Durante a edição de um documento, vincule entidades através das abas do formulário.
---
## 8. Análise de PDF
1. Clique duas vezes num arquivo PDF anexado ao documento.
2. No diálogo de arquivo, clique em **Analisar**.
3. O sistema extrai o texto e oferece opções de transcrição.
---
## 9. Atalhos de Teclado
- **F5** — Atualizar lista
- **Ctrl + P** — Abrir Gerenciador de Projetos
- **Ctrl + I** — Exportar Inventário
---
## 10. Solução de Problemas
- **Projeto não aparece na lista** — clique em **Adicionar Existente** e selecione a pasta.
- **Transcrição retorna erro** — verifique a chave de API nas Configurações.
- **Exportação falha** — verifique se a pasta de destino tem permissão de escrita.
- **Séries não aparecem nos filtros** — consulte o Guia de Instalação.
---
## 11. Suporte
Para dúvidas ou reportar problemas, consulte o `README.md` ou abra uma *issue* no repositório do projeto.